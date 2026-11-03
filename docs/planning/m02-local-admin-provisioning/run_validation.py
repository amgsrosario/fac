#!/usr/bin/env python3
"""M02 only: fresh synthetic PostgreSQL; capture Git identity around each class.
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
CLASSES = ['FirstAdministratorAuthorizationTests','FirstAdministratorProvisioningTests','FirstAdministratorLoginTests',
           'UtilizadorControllerTests','SecurityIntegrationTests','PermissionMatrixIntegrationTests','SecuritySettingsTests',
           'DocumentTypeImmutabilityTests','FinancialDocumentClassificationTests','FiscalMigrationCompatibilityTests']
if len(sys.argv)>1:
    selected=sys.argv[1:]
    if len(set(selected))!=len(selected) or not set(selected)<=set(CLASSES):
        raise ValueError('Only explicit M02 validation classes are allowed')
    CLASSES=selected
SOURCE = ROOT/'src/main/java/com/ar2lda/fac/installation/FirstAdministratorProvisioner.java'
STAMP = time.strftime('%Y%m%d-%H%M%S',time.gmtime())+'-'+secrets.token_hex(3)
OUT = ROOT/'target/m02-evidence'/STAMP
PRIVATE = ROOT/'target/m02-private'/STAMP

def call(args):
    return sp.check_output(args,cwd=ROOT,stderr=sp.PIPE,text=True).strip()

def git_identity():
    return dict(head=call(['git','rev-parse','HEAD']),tree=call(['git','rev-parse','HEAD^{tree}']),
                branch=call(['git','branch','--show-current']),
                status=call(['git','status','--porcelain=v1','--untracked-files=all']),
                test_sha256=hashlib.sha256(SOURCE.read_bytes()).hexdigest())

def record(name,value):
    (OUT/name).write_text(json.dumps(value,indent=2)+'\n')

before=git_identity()
assert before['branch']=='codex/m02-local-admin-provisioning' and not before['status'], 'Require clean M02 worktree'
OUT.mkdir(parents=True,exist_ok=False)
PRIVATE.mkdir(parents=True,exist_ok=False)
record('git-before.json',before)
name='tuuli-m02-'+secrets.token_hex(3)
network=name+'-isolated'
proxy=name+'-proxy'
proxy_network=proxy+'-net'
db='tuuli_m02_control_test'
secret=PRIVATE/'postgres.env'
password=secrets.token_urlsafe(48)
secret.write_text('POSTGRES_PASSWORD='+password+'\nPOSTGRES_DB='+db+'\n')
container_ids={}
networks=[]
try:
    for image in ['postgres:16.3','node:20-alpine']:
        call(['docker','image','inspect',image]) # No implicit dependency download.
    call(['docker','network','create','--internal','--label','ar2.mission=M02',network])
    networks.append(network)
    cid=call(['docker','run','--pull=never','-d','--name',name,'--label','ar2.mission=M02',
              '--label','ar2.disposable=true','--network',network,
              '--tmpfs','/var/lib/postgresql/data:rw,nosuid,size=768m',
              '--env-file',str(secret),'postgres:16.3'])
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
    call(['docker','network','create','--label','ar2.mission=M02',proxy_network])
    networks.append(proxy_network)
    code="require('net').createServer(c=>{let r=require('net').connect(5432,process.env.TARGET);c.pipe(r);r.pipe(c);r.on('error',()=>c.destroy());c.on('error',()=>r.destroy())}).listen(5432,'0.0.0.0')"
    pid=call(['docker','run','--pull=never','-d','--name',proxy,'--label','ar2.mission=M02',
              '--label','ar2.disposable=true','--network',proxy_network,'-p','127.0.0.1::5432',
              '--read-only','--cap-drop','ALL','--security-opt','no-new-privileges',
              '-e','TARGET='+ip,'node:20-alpine','node','-e',code])
    container_ids[proxy]=pid
    call(['docker','network','connect',network,proxy])
    info=json.loads(call(['docker','inspect',proxy]))[0]
    ports=info['NetworkSettings']['Ports']['5432/tcp']
    assert all(p['HostIp']=='127.0.0.1' for p in ports)
    record('isolation.json',dict(database=db,cluster=cluster,containers=container_ids,networks=networks,
                                tmpfs=True,no_retained_mounts=True,loopback_only=True,no_snapshot=True))
    # Restricted synthetic SQL operator; never a commercial identity or a role on a retained cluster.
    operator='m02_operator_'+secrets.token_hex(3)
    operator_password=secrets.token_urlsafe(48)
    grant="CREATE ROLE "+operator+" LOGIN PASSWORD '"+operator_password+"' NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION NOBYPASSRLS;"
    sp.run(['docker','exec','-i',name,'psql','-Xq','-v','ON_ERROR_STOP=1','-U','postgres','-d',db],
           input=grant.encode(),stdout=sp.DEVNULL,stderr=sp.PIPE,check=True)
    env=os.environ.copy()
    env.update(FAC_TEST_DATASOURCE_USERNAME='postgres',FAC_TEST_DATASOURCE_PASSWORD=password,
               FAC_JWT_SECRET=secrets.token_urlsafe(64),M02_DISPOSABLE='true',M02_OPERATOR=operator,
               M02_OPERATOR_PASSWORD=operator_password)
    summary=[]
    for index,test in enumerate(CLASSES):
        target='tuuli_m02_'+str(index)+'_test'
        call(['docker','exec',name,'createdb','-U','postgres',target])
        env['FAC_TEST_DATASOURCE_URL']='jdbc:postgresql://127.0.0.1:'+ports[0]['HostPort']+'/'+target
        cmd=['/mnt/c/Tools/apache-maven-3.9.16/bin/mvn','-o','-B','-Dspring.profiles.active=test','-Dtest='+test,'test']
        stage=OUT/test;stage.mkdir()
        (stage/'git-before.json').write_text(json.dumps(git_identity(),indent=2))
        (stage/'command.json').write_text(json.dumps(cmd))
        with (stage/'test.log').open('w') as log:
            result=sp.run(cmd,cwd=ROOT,env=env,stdout=log,stderr=sp.STDOUT)
        after=git_identity();(stage/'git-after.json').write_text(json.dumps(after,indent=2))
        assert after==before, 'Git state changed during execution'
        counts=dict(tests=0,failures=0,errors=0,skipped=0)
        for report in (ROOT/'target/surefire-reports').glob('*'+test+'*'):
            shutil.copy2(report,stage/report.name)
            if report.suffix=='.xml':
                xml=ET.parse(report).getroot();counts={key:int(xml.get(key)) for key in counts}
        summary.append(dict(test=test,exit_code=result.returncode,**counts))
        record('result.json',summary)
        print(json.dumps(summary[-1]),flush=True)
        assert result.returncode==0, 'Test failed: inspect private log'
    cmd=['/mnt/c/Tools/apache-maven-3.9.16/bin/mvn','-o','-B','-DskipTests','package']
    record('package-git-before.json',git_identity())
    with (OUT/'package.log').open('w') as log:
        packaged=sp.run(cmd,cwd=ROOT,env=env,stdout=log,stderr=sp.STDOUT)
    record('package-git-after.json',git_identity())
    assert git_identity()==before and packaged.returncode==0
    # Smoke the actual executable JAR entry point without opening Spring or a datasource.
    cmd=['java','-Dloader.main=com.ar2lda.fac.installation.FirstAdministratorCli','-cp',str(ROOT/'target/fac-0.0.1-SNAPSHOT.jar'),
         'org.springframework.boot.loader.launch.PropertiesLauncher','--help']
    smoke=sp.run(cmd,cwd=ROOT,stdout=sp.PIPE,stderr=sp.STDOUT)
    (OUT/'cli-jar-smoke.log').write_bytes(smoke.stdout)
    assert smoke.returncode==0 and b'Uso local:' in smoke.stdout and b'Started' not in smoke.stdout

finally:
    # Remove only resources created here, guarded by exact IDs and labels.
    for resource,cid in reversed(list(container_ids.items())):
        info=json.loads(call(['docker','inspect',resource]))[0]
        assert info['Id']==cid and info['Config']['Labels'].get('ar2.mission')=='M02'
        call(['docker','rm','-f',resource])
    for net in reversed(networks):
        info=json.loads(call(['docker','network','inspect',net]))[0]
        assert info['Labels'].get('ar2.mission')=='M02' and not info.get('Containers')
        call(['docker','network','rm',net])
    secret.unlink(missing_ok=True)
    PRIVATE.rmdir()
    record('disposal.json',dict(only_M02_resources_removed=True,ephemeral_secret_removed=True))
    # Do not archive a leaked secret even if a dependency were to log it.
    assert all(password.encode() not in p.read_bytes() and ('operator_password' not in globals() or operator_password.encode() not in p.read_bytes()) for p in OUT.rglob('*') if p.is_file())
    record('git-after.json',git_identity())
    shutil.copy2(SOURCE,OUT/'FirstAdministratorProvisioner.java')
    shutil.copy2(Path(__file__),OUT/'run_validation.py')
    files=[dict(name=str(p.relative_to(OUT)),bytes=p.stat().st_size,sha256=hashlib.sha256(p.read_bytes()).hexdigest())
           for p in sorted(OUT.rglob('*')) if p.is_file()]
    record('MANIFEST_SHA256.json',dict(files=files))
print(json.dumps(dict(evidence=str(OUT),manifest_sha256=hashlib.sha256((OUT/'MANIFEST_SHA256.json').read_bytes()).hexdigest(),result='PASS')))
