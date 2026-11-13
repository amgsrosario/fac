#!/usr/bin/env python3
"""M4-04 FT identity validation: fresh synthetic PostgreSQL, Git identity per selected class.
No snapshots, retained resources, external credentials or environment dumps.
Requires local Docker postgres:16.3/node:20-alpine and the existing offline Maven.
"""
from pathlib import Path
import hashlib
import json
import os
import secrets
import shutil
import subprocess as sp
import time
import sys
import xml.etree.ElementTree as ET

os.umask(0o077)
ROOT = Path(__file__).resolve().parents[3]
CLASSES = ['FtIdentityJourneyTests']
CASE_ID = 'M404-01'
SOURCE = ROOT/'src/test/java/com/ar2lda/fac/fiscal/catalog/FtIdentityJourneyTests.java'
STAMP = time.strftime('%Y%m%d-%H%M%S',time.gmtime())+'-'+secrets.token_hex(3)
OUT = ROOT/'target/m404-qr-evidence'/STAMP
PRIVATE = ROOT/'target/m404-qr-private'/STAMP

def call(args):
    if OUT.exists():
        with (OUT/'commands.jsonl').open('a') as command_log:
            command_log.write(json.dumps(args)+'\n')
    return sp.check_output(args,cwd=ROOT,stderr=sp.PIPE,text=True).strip()

def git_identity():
    return dict(head=call(['git','rev-parse','HEAD']),tree=call(['git','rev-parse','HEAD^{tree}']),
                branch=call(['git','branch','--show-current']),
                status=call(['git','status','--porcelain=v1','--untracked-files=all']),
                test_sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest())

def record(name,value):
    (OUT/name).write_text(json.dumps(value,indent=2)+'\n')

before=git_identity()
assert before['branch']=='codex/m4-04-ft-identity' and not before['status'], 'Require clean M4-04 worktree'
OUT.mkdir(parents=True,exist_ok=False)
PRIVATE.mkdir(parents=True,exist_ok=False)
record('git-before.json',before)
record('selection.json',dict(case_id=CASE_ID,expected_executed_tests=1,previous_cases_not_repeated=True))
assert json.loads((ROOT/'src/test/resources/m404/expected.json').read_text())['G']=='GFT M404A/1'
shutil.copy2(ROOT/'src/test/resources/m404/expected.json',OUT/'expected-before-execution.json')
name='tuuli-m404-'+secrets.token_hex(3)
network=name+'-isolated'
proxy=name+'-proxy'
proxy_network=proxy+'-net'
db='tuuli_m404_control_test'
secret=PRIVATE/'postgres.env'
password=secrets.token_urlsafe(48)
secret.write_text('POSTGRES_PASSWORD='+password+'\nPOSTGRES_DB='+db+'\n')
container_ids={}
networks=[]
try:
    for image in ['postgres@sha256:d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609','node@sha256:fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293']:
        call(['docker','image','inspect',image]) # No implicit dependency download.
    call(['docker','network','create','--internal','--label','ar2.mission=M404-ft-identity',network])
    networks.append(network)
    cid=call(['docker','run','--pull=never','-d','--name',name,'--label','ar2.mission=M404-ft-identity',
              '--label','ar2.disposable=true','--network',network,
              '--tmpfs','/var/lib/postgresql/data:rw,nosuid,size=768m',
              '--env-file',str(secret),'postgres@sha256:d0f363f8366fbc3f52d172c6e76bc27151c3d643b870e1062b4e8bfe65baf609'])
    container_ids[name]=cid
    for attempt in range(90):
        try:
            cluster=call(['docker','exec',name,'psql','-XqAt','-v','ON_ERROR_STOP=1','-U','postgres',
                          '-d',db,'-c','select system_identifier::text from pg_control_system()'])
            break
        except sp.CalledProcessError:
            time.sleep(.5)
    else:
        raise RuntimeError('Synthetic PostgreSQL not ready')
    assert cluster not in ['7689035507522658342','7701042457621884972']
    info=json.loads(call(['docker','inspect',name]))[0]
    assert not info['Mounts'] and not info['NetworkSettings']['Ports'].get('5432/tcp')
    ip=info['NetworkSettings']['Networks'][network]['IPAddress']
    call(['docker','network','create','--label','ar2.mission=M404-ft-identity',proxy_network])
    networks.append(proxy_network)
    code="require('net').createServer(c=>{let r=require('net').connect(5432,process.env.TARGET);c.pipe(r);r.pipe(c);r.on('error',()=>c.destroy());c.on('error',()=>r.destroy())}).listen(5432,'0.0.0.0')"
    pid=call(['docker','run','--pull=never','-d','--name',proxy,'--label','ar2.mission=M404-ft-identity',
              '--label','ar2.disposable=true','--network',proxy_network,'-p','127.0.0.1::5432',
              '--read-only','--cap-drop','ALL','--security-opt','no-new-privileges',
              '-e','TARGET='+ip,'node@sha256:fb4cd12c85ee03686f6af5362a0b0d56d50c58a04632e6c0fb8363f609372293','node','-e',code])
    container_ids[proxy]=pid
    call(['docker','network','connect',network,proxy])
    info=json.loads(call(['docker','inspect',proxy]))[0]
    ports=info['NetworkSettings']['Ports']['5432/tcp']
    assert all(p['HostIp']=='127.0.0.1' for p in ports)
    record('isolation.json',dict(database=db,cluster=cluster,containers=container_ids,networks=networks,
                                tmpfs=True,no_retained_mounts=True,loopback_only=True,no_snapshot=True))
    record('versions.json',dict(python=sys.version,docker=call(['docker','version','--format','{{.Client.Version}} {{.Server.Version}}']),context=call(['docker','context','show']),maven=call(['mvn','-version']),postgres=call(['docker','exec',name,'psql','-U','postgres','-d',db,'-Atc','select version()'])))
    env=os.environ.copy()
    env.update(FAC_TEST_DATASOURCE_USERNAME='postgres',FAC_TEST_DATASOURCE_PASSWORD=password,
               FAC_JWT_SECRET=secrets.token_urlsafe(64),M404_DISPOSABLE='true',M404_EXPECTED_CLUSTER=cluster,M404_EVIDENCE=str(OUT/'persistent'))
    summary=[]
    for index,test in enumerate(CLASSES):
        target='tuuli_m404_'+str(index)+'_test'
        call(['docker','exec',name,'createdb','-U','postgres',target])
        env['FAC_TEST_DATASOURCE_URL']='jdbc:postgresql://127.0.0.1:'+ports[0]['HostPort']+'/'+target
        cmd=['/mnt/c/Tools/apache-maven-3.9.16/bin/mvn','-o','-B','-Dspring.profiles.active=test','-Dtest='+test,'test']
        stage=OUT/(str(index)+'-'+test.split('#')[0]);stage.mkdir()
        (stage/'git-before.json').write_text(json.dumps(git_identity(),indent=2))
        (stage/'command.json').write_text(json.dumps(cmd))
        with (stage/'test.log').open('w') as log:
            result=sp.run(cmd,cwd=ROOT,env=env,stdout=log,stderr=sp.STDOUT)
        after=git_identity();(stage/'git-after.json').write_text(json.dumps(after,indent=2))
        assert after==before, 'Git state changed during execution'
        counts=dict(tests=0,failures=0,errors=0,skipped=0)
        for report in (ROOT/'target/surefire-reports').glob('*'+test.split('#')[0]+'*'):
            shutil.copy2(report,stage/report.name)
            if report.suffix=='.xml':
                xml=ET.parse(report).getroot();counts={key:int(xml.get(key)) for key in counts}
        summary.append(dict(test=test,exit_code=result.returncode,**counts))
        record('result.json',summary)
        print(json.dumps(summary[-1]),flush=True)
        assert result.returncode==0, 'Test failed: inspect private log'
        assert counts['tests']==1 and counts['skipped']==0, 'M4-04 must execute exactly its one directed case'

    record('flyway.json',dict(rows=call(['docker','exec',name,'psql','-XqAt','-U','postgres','-d',target,'-c',"select json_agg(t) from (select version,description,success from flyway_schema_history order by installed_rank) t"])))
finally:
    # Remove only resources created here, guarded by exact IDs and labels.
    for resource,cid in reversed(list(container_ids.items())):
        info=json.loads(call(['docker','inspect',resource]))[0]
        assert info['Id']==cid and info['Config']['Labels'].get('ar2.mission')=='M404-ft-identity'
        call(['docker','rm','-f',resource])
    for net in reversed(networks):
        info=json.loads(call(['docker','network','inspect',net]))[0]
        assert info['Labels'].get('ar2.mission')=='M404-ft-identity' and not info.get('Containers')
        call(['docker','network','rm',net])
    secret.unlink(missing_ok=True)
    PRIVATE.rmdir()
    record('disposal.json',dict(containers=call(['docker','ps','-aq','--filter','label=ar2.mission=M404-ft-identity']),networks=call(['docker','network','ls','-q','--filter','label=ar2.mission=M404-ft-identity']),ephemeral_secret_removed=not secret.exists()))
    # Do not archive a leaked secret even if a dependency were to log it.
    assert all(password.encode() not in p.read_bytes() and ('operator_password' not in globals() or operator_password.encode() not in p.read_bytes()) for p in OUT.rglob('*') if p.is_file())
    record('git-after.json',git_identity())
    shutil.copy2(SOURCE,OUT/'FtIdentityJourneyTests.java')
    shutil.copy2(Path(__file__),OUT/'run_validation.py')
    shutil.copy2(ROOT/'src/test/resources/m404/expected.json',OUT/'expected.json')
    files=[dict(name=str(p.relative_to(OUT)),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest())
           for p in sorted(OUT.rglob('*')) if p.is_file()]
    record('MANIFEST_SHA256.json',dict(files=files))
print(json.dumps(dict(evidence=str(OUT),manifest_sha256=hashlib.sha256((OUT/'MANIFEST_SHA256.json').read_bytes()).hexdigest(),result='PASS')))
