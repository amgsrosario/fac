#!/usr/bin/env python3
"""Run selected Maven test classes against a fresh, disposable PostgreSQL cluster."""
import argparse
import hashlib
import importlib.metadata
import json
import os
from pathlib import Path
import re
import secrets
import shlex
import shutil
import signal
import subprocess
import sys
import tempfile
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
POSTGRES = "postgres@sha256:d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609"
PROXY = "node@sha256:fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293"
FORBIDDEN = {"7689035507522658342", "7701042457621884972"}
PORT = "5432/tcp"
ALLOWED_ENV = ("PATH", "HOME", "USER", "JAVA_HOME", "LANG", "LC_ALL", "TZ",
               "PYTHONPATH", "PYTHONOPTIMIZE", "SAFT_XSD_PATH", "WSL_INTEROP")


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def call(command):
    result = subprocess.run(command, cwd=ROOT, text=True, capture_output=True)
    require(result.returncode == 0, f"{Path(command[0]).name} {command[1]} exited {result.returncode}")
    return result.stdout.strip()


def git_identity():
    return dict(head=call(["git", "rev-parse", "HEAD"]), tree=call(["git", "rev-parse", "HEAD^{tree}"]),
                branch=call(["git", "branch", "--show-current"]),
                status=call(["git", "status", "--porcelain=v1", "--untracked-files=all"]))


def source(test, ambiguities=None):
    matches = sorted((ROOT / "src/test/java").rglob(test + ".java"))
    if len(matches) > 1 and ambiguities is not None:
        ambiguities[test] = [
            dict(path=path.relative_to(ROOT).as_posix(), sha256=sha(path)) for path in matches
        ]
    require(len(matches) == 1, f"Class {test}: {len(matches)} matching sources")
    relative = matches[0].relative_to(ROOT).as_posix()
    subprocess.run(["git", "ls-files", "--error-unmatch", "--", relative], cwd=ROOT,
                   stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, check=True)
    package = re.search(r"(?m)^\s*package\s+([\w.]+)\s*;", matches[0].read_text())
    qualified = f"{package[1]}.{test}" if package else test
    return dict(path=relative, sha256=sha(matches[0]), qualified_class=qualified)


def check_cluster(cluster):
    require(re.fullmatch(r"[0-9]{19,20}", cluster) and cluster not in FORBIDDEN,
            "PostgreSQL cluster identifier invalid or prohibited")


class Docker:
    def __init__(self, command, label, run_id):
        self.command = command
        self.labels = {"ar2.mission": label, "ar2.disposable": "true", "ar2.evidence-run": run_id}
        self.resources = []

    def run(self, *args):
        return call([*self.command, *args])

    def inspect(self, kind, resource):
        return json.loads(self.run(kind, "inspect", resource))[0]

    def create(self, kind, *args):
        labels = [item for key, value in self.labels.items() for item in ("--label", f"{key}={value}")]
        command = ["network", "create"] if kind == "network" else ["run", "-d", "--pull=never"]
        resource = self.run(*command, *labels, *args)
        self.resources.append((kind, resource))
        return resource

    def dispose(self):
        errors = []
        for kind, resource in reversed(self.resources):
            try:
                info = self.inspect(kind, resource)
                labels = info["Config"]["Labels"] if kind == "container" else info["Labels"]
                require(info["Id"] == resource and all(labels.get(k) == v for k, v in self.labels.items()),
                        "Refusing cleanup of resource with mismatched identity")
                command = ("container", "rm", "-f", "-v") if kind == "container" else ("network", "rm")
                self.run(*command, resource)
            except Exception as exc:
                errors.append(str(exc))
        remaining = {}
        for kind in ("container", "network"):
            try:
                remaining[kind] = self.run(kind, "ls", "-aq" if kind == "container" else "-q", "--filter",
                                           "label=ar2.evidence-run=" + self.labels["ar2.evidence-run"])
            except Exception as exc:
                errors.append(str(exc))
        return dict(owned=self.resources, remaining=remaining, errors=errors)


def start_database(docker, name, database, password, tmp):
    for image in (POSTGRES, PROXY):
        docker.run("image", "inspect", image)
    internal = docker.create("network", "--internal", name + "-db")
    require(docker.inspect("network", internal)["Internal"], "PostgreSQL network is not internal")
    secret_file = tmp / "postgres.env"
    secret_file.write_text(f"POSTGRES_PASSWORD={password}\nPOSTGRES_DB={database}\n")
    secret_path = str(secret_file)
    if any(part.lower().endswith("docker.exe") for part in docker.command):
        secret_path = call(["wslpath", "-w", secret_path])
    postgres = docker.create("container", "--name", name + "-db", "--network", internal,
                             "--tmpfs", "/var/lib/postgresql/data:rw,nosuid,size=768m",
                             "--env-file", secret_path, POSTGRES)
    secret_file.unlink()
    info = docker.inspect("container", postgres)
    require(not info["Mounts"] and not info["NetworkSettings"]["Ports"].get(PORT)
            and "/var/lib/postgresql/data" in info["HostConfig"]["Tmpfs"]
            and len(info["NetworkSettings"]["Networks"]) == 1, "Unsafe PostgreSQL mounts, ports or network")
    for _ in range(90):
        try:
            cluster = docker.run("exec", postgres, "psql", "-XqAt", "-U", "postgres",
                                 "-d", database, "-c", "select system_identifier::text from pg_control_system()")
        except RuntimeError:
            time.sleep(.5)
        else:
            break
    else:
        raise RuntimeError("Disposable PostgreSQL did not become ready")
    check_cluster(cluster)
    bridge = docker.create("network", name + "-proxy")
    ip = next(iter(info["NetworkSettings"]["Networks"].values()))["IPAddress"]
    code = ("require('net').createServer(c=>{let r=require('net').connect(5432,process.env.TARGET);"
            "c.pipe(r);r.pipe(c);r.on('error',()=>c.destroy());c.on('error',()=>r.destroy())"
            "}).listen(5432,'0.0.0.0')")
    proxy = docker.create("container", "--name", name + "-proxy", "--network", bridge,
                          "-p", "127.0.0.1::5432", "--read-only", "--cap-drop", "ALL",
                          "--security-opt", "no-new-privileges", "-e", "TARGET=" + ip, PROXY, "node", "-e", code)
    docker.run("network", "connect", internal, proxy)
    proxy_info = docker.inspect("container", proxy)
    ports = proxy_info["NetworkSettings"]["Ports"]
    bindings = ports.get(PORT)
    require(not proxy_info["Mounts"] and bindings and len(bindings) == 1 and bindings[0]["HostIp"] == "127.0.0.1"
            and not any(value for key, value in ports.items() if key != PORT), "Unsafe proxy mounts or ports")
    return postgres, cluster, bindings[0]


def run_class(options, docker, postgres, cluster, binding, test, index, expected, out, password, jwt):
    selected = expected["sources"][test]
    stage = out / f"{index}-{test}"
    stage.mkdir()
    shutil.copy2(ROOT / selected["path"], stage / f"{test}.java")
    require(sha(stage / f"{test}.java") == selected["sha256"], f"Source copy mismatch for {test}")
    before = git_identity()
    save(stage / "git-before.json", dict(**before, source=selected))
    require(before == expected["git"], f"Git identity changed before {test}")
    database = f"{options.db_prefix}{index}_test"
    docker.run("exec", postgres, "createdb", "-U", "postgres", database)
    (stage / "persistent").mkdir()
    command = [options.maven, "-o", "-B", "-Dspring.profiles.active=test",
               f"-Dtest={selected['qualified_class']}", "test"]
    # Remove the exact default report before execution to exclude stale proof.
    old_report = ROOT / "target/surefire-reports" / f"TEST-{selected['qualified_class']}.xml"
    old_report.unlink(missing_ok=True)
    env = {key: os.environ[key] for key in ALLOWED_ENV if key in os.environ}
    env.update(FAC_TEST_DATASOURCE_USERNAME="postgres", FAC_TEST_DATASOURCE_PASSWORD=password,
               FAC_TEST_DATASOURCE_URL=f"jdbc:postgresql://127.0.0.1:{binding['HostPort']}/{database}",
               FAC_JWT_SECRET=jwt, M03_DISPOSABLE="true", M03_EXPECTED_CLUSTER=cluster,
               M402_EVIDENCE=str(stage / "persistent"))
    save(stage / "command.json", command)
    with (stage / "test.log").open("w") as log:
        process = subprocess.Popen(command, cwd=ROOT, env=env, stdout=log,
                                   stderr=subprocess.STDOUT, start_new_session=True)
        try:
            status = process.wait()
        except BaseException:
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
            process.wait()
            raise
    after = git_identity()
    save(stage / "git-after.json", dict(**after, source=source(test)))
    report = stage / old_report.name
    if old_report.is_file():
        shutil.copy2(old_report, report)
    results = dict(test=test, exit_code=status, source=selected)
    if report.is_file():
        root = ET.parse(report).getroot()
        results.update({key: int(root.attrib[key]) for key in ("tests", "failures", "errors", "skipped")})
    save(stage / "result.json", results)
    require(after == expected["git"] and source(test) == selected, f"Git or source changed during {test}")
    require(status == 0, f"{test} exited {status}")
    require(results.get("tests", 0) > results.get("skipped", 0)
            and not results["failures"] and not results["errors"], f"{test} has no successful Surefire proof")


def save(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mission", required=True)
    parser.add_argument("--branch", required=True)
    parser.add_argument("--docker-label", required=True)
    parser.add_argument("--db-prefix", required=True)
    parser.add_argument("--docker-command", default="docker")
    parser.add_argument("--maven", default="mvn")
    parser.add_argument("--docker-wrapper-source", type=str)
    parser.add_argument("classes", nargs="+")
    options = parser.parse_args()
    os.umask(0o077)
    stamp = time.strftime("%Y%m%d-%H%M%S", time.gmtime()) + "-" + secrets.token_hex(6)
    out = ROOT / "target/evidence-runner" / stamp
    out.mkdir(parents=True)
    password, jwt = secrets.token_urlsafe(48), secrets.token_urlsafe(64)
    errors = []
    docker = None
    expected = dict(sources={}, ambiguous_sources={})
    states = dict.fromkeys(options.classes, "not_executed")
    completed = set()
    result = "FAIL"
    active = None
    try:
        require(all(re.fullmatch(r"[A-Za-z0-9_.-]{1,64}", s) for s in (options.mission, options.docker_label)),
                "Invalid mission or Docker label")
        require(re.fullmatch(r"tuuli_[a-z0-9_]{1,40}_", options.db_prefix), "Invalid disposable database prefix")
        require(len(set(options.classes)) == len(options.classes)
                and all(re.fullmatch(r"[A-Za-z_$][A-Za-z0-9_$]*", s) for s in options.classes),
                "Classes must be distinct simple Java class names")
        expected["git"] = git_identity()
        save(out / "git-before.json", expected["git"])
        require(expected["git"]["branch"] == options.branch, "Wrong branch before Docker")
        require(not expected["git"]["status"], "Dirty worktree before Docker")
        for test in options.classes:
            expected["sources"][test] = source(test, expected["ambiguous_sources"])
        docker_command = shlex.split(options.docker_command)
        if options.docker_wrapper_source:
            wrapper = Path(options.docker_wrapper_source).resolve(strict=True)
            require(any(Path(part).resolve() == wrapper for part in docker_command),
                    "Declared Docker wrapper not in Docker command")
            digest = sha(wrapper)
            archived = out / "docker-wrapper-source"
            shutil.copy2(wrapper, archived)
            require(sha(archived) == digest, "Docker wrapper source copy mismatch")
            expected["docker_wrapper"] = dict(path=str(wrapper), sha256=digest, archived=archived.name)
        docker = Docker(docker_command, options.docker_label, stamp)
        with tempfile.TemporaryDirectory(prefix="tuuli-evidence-") as tmp:
            postgres, cluster, binding = start_database(docker, "tuuli-evidence-" + stamp,
                                                        options.db_prefix + "control_test", password, Path(tmp))
        save(out / "isolation.json", dict(cluster=cluster, binding=binding, database_prefix=options.db_prefix,
                                         resources=docker.resources, images=[POSTGRES, PROXY],
                                         postgres=docker.run("exec", postgres, "postgres", "--version"),
                                         maven=call([options.maven, "-version"])))
        for index, test in enumerate(options.classes):
            active = test
            try:
                run_class(options, docker, postgres, cluster, binding, test, index, expected, out, password, jwt)
            except Exception as exc:
                states[test] = "failed"
                errors.append(str(exc))
            else:
                states[test] = "completed"
                completed.add(test)
            active = None
    except BaseException as exc:
        if active is not None:
            states[active] = "failed"
        errors.append(f"{type(exc).__name__}: {exc}".rstrip(": "))
    finally:
        if docker:
            try:
                disposal = docker.dispose()
                save(out / "disposal.json", disposal)
                if disposal["errors"] or any(disposal["remaining"].values()):
                    errors.append("Disposable resources could not be verified as removed")
            except BaseException as exc:
                errors.append(f"Cleanup: {type(exc).__name__}: {exc}")
        try:
            after = git_identity()
            save(out / "git-after.json", after)
            require(after == expected.get("git"), "Git identity changed or could not be established")
        except BaseException as exc:
            errors.append(f"Final Git check: {type(exc).__name__}: {exc}")
        shutil.copy2(__file__, out / "run_validation.py")
        for path in list(out.rglob("*")):
            if path.is_symlink():
                path.unlink()
                errors.append("Evidence symlink removed")
            elif path.is_file() and any(value.encode() in path.read_bytes() for value in (password, jwt)):
                path.unlink()
                errors.append("Evidence containing an ephemeral secret was discarded")
        for index, error in enumerate(errors):
            errors[index] = error.replace(password, "[REDACTED]").replace(jwt, "[REDACTED]")
        runtime = dict(executable=sys.executable, version=sys.version, optimize=sys.flags.optimize,
                       libraries={d.metadata["Name"]: d.version for d in importlib.metadata.distributions()
                                  if d.metadata.get("Name", "").lower() in {"xmlschema", "elementpath"}})
        if not errors and completed == set(options.classes):
            result = "PASS"
        files = [dict(path=p.relative_to(out).as_posix(), bytes=p.stat().st_size, sha256=sha(p))
                 for p in sorted(out.rglob("*")) if p.is_file()]
        manifest = out / "MANIFEST_SHA256.json"
        save(manifest, dict(result=result, errors=errors, arguments=vars(options), runtime=runtime,
                            classes=[dict(name=name, state=states[name]) for name in options.classes],
                            **expected, files=files))
        digest = sha(manifest)
        manifest.with_suffix(".json.sha256").write_text(f"{digest}  {manifest.name}\n")
        print(json.dumps(dict(result=result, errors=errors, manifest=str(manifest), sha256=digest)))
    return 0 if result == "PASS" else 1


if __name__ == "__main__":
    sys.exit(main())
