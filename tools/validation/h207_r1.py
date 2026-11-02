#!/usr/bin/env python3
"""Audited single invocation on a clean immutable commit; no Docker or production DB access."""
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import urllib.parse
import xml.etree.ElementTree as ET
import zipfile

ROOT = Path(__file__).resolve().parents[2]
CLASSES = [
    'TrialCanonicalTests', 'TrialPersistenceTests', 'FiscalQrServiceTests',
    'FiscalQrIdentityTests', 'DemoSignatureSourceTests', 'GovernedFtPersistenceTests',
    'DocumentoComercialControllerTests', 'DocumentoComercialEmissaoIntegridadeTests',
    'PermissionMatrixIntegrationTests', 'DocumentoComercialPdfServiceTests',
]

def git(*args):
    return subprocess.check_output(['git', *args], cwd=ROOT, text=True).strip()

def state():
    return {'head': git('rev-parse', 'HEAD'), 'tree': git('rev-parse', 'HEAD^{tree}'),
            'status': git('status', '--porcelain=v1', '--untracked-files=all')}

def write(path, value):
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n')

def run(args, output):
    with output.open('wb') as log:
        return subprocess.run(args, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT).returncode

if __name__ == '__main__':
    if len(sys.argv) != 2:
        raise SystemExit('Usage: python3 tools/validation/h207_r1.py /tmp/new-evidence-directory')
    destination = Path(sys.argv[1]).resolve()
    if destination.is_relative_to(ROOT):
        raise SystemExit('Evidence must be outside the worktree, preserved across clean')
    url = urllib.parse.urlparse(os.environ.get('FAC_TEST_DATASOURCE_URL', '').removeprefix('jdbc:'))
    if url.scheme != 'postgresql' or url.hostname != '127.0.0.1' or not url.path.endswith('_test'):
        raise SystemExit('Explicit localhost disposable test datasource required')
    before = state()
    if before['status']:
        raise SystemExit('Clean worktree required')
    destination.mkdir(parents=True, exist_ok=False)
    write(destination / 'before.json', before)
    commands = [['mvn', '-o', 'clean', 'test', '-Dtest=' + ','.join(CLASSES)],
                ['mvn', '-o', '-DskipTests', 'package']]
    write(destination / 'commands.json', commands)
    result = {'tests_exit': None, 'package_exit': None, 'artifact_check': False}
    try:
        result['tests_exit'] = run(commands[0], destination / 'tests.log')
        reports = ROOT / 'target/surefire-reports'
        if reports.exists():
            shutil.copytree(reports, destination / 'surefire-reports')
        rows = []
        for path in sorted((destination / 'surefire-reports').glob('TEST-*.xml')):
            suite = ET.parse(path).getroot()
            rows.append({'class': suite.attrib['name'], **{
                key: int(suite.attrib.get(key, 0)) for key in ['tests', 'failures', 'errors', 'skipped']}})
        result['suites'] = rows
        assert result['tests_exit'] == 0, 'Single test invocation failed; original log/XML preserved'
        assert {r['class'].split('.')[-1] for r in rows} == set(CLASSES), 'Unexpected suite set'
        assert sum(r['tests'] for r in rows) == 194, 'Expected exactly 194 cases'
        assert all(r['failures'] == r['errors'] == r['skipped'] == 0 for r in rows), 'Failed or skipped cases'
        result['package_exit'] = run(commands[1], destination / 'package.log')
        assert result['package_exit'] == 0, 'Package failed'
        jar = ROOT / 'target/fac-0.0.1-SNAPSHOT.jar'
        with zipfile.ZipFile(jar) as archive:
            forbidden = [name for name in archive.namelist()
                         if 'signingtrial' in name or 'signature-trial' in name or 'h207fixture' in name]
            references = [name for name in archive.namelist()
                          if name.startswith('BOOT-INF/classes/') and name.endswith('.class')
                          and (b'fiscal/signingtrial' in archive.read(name)
                               or b'fiscal.signingtrial' in archive.read(name))]
        write(destination / 'artifact.json', {'jar_sha256': hashlib.sha256(jar.read_bytes()).hexdigest(),
                                             'forbidden_entries': forbidden, 'trial_references': references})
        assert not forbidden and not references, 'Experimental content/dependency in production JAR'
        result['artifact_check'] = True
    finally:
        after = state()
        write(destination / 'after.json', after)
        result['unchanged_clean_commit'] = before == after and not after['status']
        write(destination / 'result.json', result)
        manifest = [{'path': str(p.relative_to(destination)), 'bytes': p.stat().st_size,
                     'sha256': hashlib.sha256(p.read_bytes()).hexdigest()}
                    for p in sorted(destination.rglob('*')) if p.is_file()]
        write(destination / 'MANIFEST.json', manifest)
    assert result['unchanged_clean_commit'], 'Commit/tree/status changed during validation'
    print(json.dumps(result, indent=2))
