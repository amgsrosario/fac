#!/usr/bin/env python3
"""M05 synthetic installation/recovery only. Does not accept retained DB/resources.
Requires cached postgres:16.3, node:20-alpine, eclipse-temurin:21-jre and offline Maven.
Only exact container IDs and labels created by this process are removed.
"""
from pathlib import Path
import os,sys,json,hashlib,secrets,time,subprocess as sp,pty,select,signal,re,urllib.request,urllib.error,datetime
os.umask(0o077)
ROOT=Path(__file__).resolve().parents[3]
STAMP=time.strftime('%Y%m%d-%H%M%S',time.gmtime())+'-'+secrets.token_hex(3)
OUT=ROOT/'target/m05-evidence'/STAMP
PRIVATE=ROOT/'target/m05-private'/STAMP
LABEL='M05'
containers={};networks=[];secret_values=[];checks=[]

def run(args,input=None,check=True):
    r=sp.run(args,input=input,stdout=sp.PIPE,stderr=sp.PIPE,cwd=ROOT)
    if check and r.returncode:
        error=(r.stderr+r.stdout).decode(errors='replace')
        for value in secret_values:error=error.replace(value,'[REDACTED]')
        if OUT.exists():write('command-error.json',dict(command=args[0],exit_code=r.returncode,detail=error))
        raise RuntimeError('Command failed: '+args[0]+'; see filtered command-error.json')
    return r

def call(args):return run(args).stdout.decode().strip()
def write(name,value):
    p=OUT/name;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(value,indent=2)+'\n')
def digest(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def identity():return dict(head=call(['git','rev-parse','HEAD']),tree=call(['git','rev-parse','HEAD^{tree}']),branch=call(['git','branch','--show-current']),status=call(['git','status','--porcelain=v1','--untracked-files=all']))
def check(name,condition):
    checks.append(dict(name=name,passed=bool(condition)));write('checks.json',checks)
    if not condition:raise AssertionError(name)
def password():
    s='Aa7!'+secrets.token_urlsafe(30);secret_values.append(s);return s
def envfile(name,values):
    p=PRIVATE/name;p.write_text(''.join(k+'='+v+'\n' for k,v in values.items()));return p

def docker_new(name,args):
    cid=call(['docker','run','--pull=never','-d','--name',name,'--label','ar2.mission='+LABEL,'--label','ar2.disposable=true',*args]);containers[name]=cid;return name

def remove(name):
    if name not in containers:return
    x=json.loads(call(['docker','inspect',name]))[0]
    assert x['Id']==containers[name] and x['Config']['Labels'].get('ar2.mission')==LABEL
    call(['docker','rm','-f',name]);del containers[name]

def sql(pg,db,text):return run(['docker','exec','-i',pg,'psql','-XqAt','-v','ON_ERROR_STOP=1','-U','postgres','-d',db],text.encode()).stdout.decode().strip()
def pg_cluster(pg):return sql(pg,'postgres','select system_identifier::text from pg_control_system()')

def make_pg(letter):
    name='tuuli-m05-'+STAMP[-6:]+'-'+letter
    n=name+'-internal';call(['docker','network','create','--internal','--label','ar2.mission='+LABEL,n]);networks.append(n)
    e=envfile(letter+'-postgres.env',{'POSTGRES_PASSWORD':password()})
    docker_new(name,['--network',n,'--tmpfs','/var/lib/postgresql/data:rw,nosuid,size=768m','--env-file',str(e),'postgres:16.3'])
    for _ in range(100):
        r=run(['docker','exec',name,'pg_isready','-h','127.0.0.1','-U','postgres'],check=False)
        if r.returncode==0:break
        time.sleep(.3)
    cluster=pg_cluster(name);assert cluster not in ['7689035507522658342','7701042457621884972']
    meta=json.loads(call(['docker','inspect',name]))[0];assert not meta['Mounts'] and not meta['NetworkSettings']['Ports'].get('5432/tcp')
    # This disposable cluster alone: no external TCP database access.
    hba="local all postgres trust\nhost all all 127.0.0.1/32 scram-sha-256\nhost all all ::1/128 scram-sha-256\nhost all all 0.0.0.0/0 reject\nhost all all ::/0 reject\n"
    run(['docker','exec','-i',name,'sh','-c','cat > "$PGDATA/pg_hba.conf"'],hba.encode());sql(name,'postgres','select pg_reload_conf()')
    db='tuuli_m05_'+letter+'_test';owner_pw=password();app_pw=password();op_pw=password()
    sql(name,'postgres',"create role m05_owner login password '"+owner_pw+"'; create role m05_app login password '"+app_pw+"'; create role m05_operator login password '"+op_pw+"'; create database "+db+' owner m05_owner;')
    sql(name,db,'revoke create on schema public from public; grant execute on function pg_catalog.pg_control_system() to m05_owner;')
    p=dict(pg=name,db=db,network=n,cluster=cluster,owner_pw=owner_pw,app_pw=app_pw,op_pw=op_pw)
    ip=meta['NetworkSettings']['Networks'][n]['IPAddress'];net=name+'-proxy-net';call(['docker','network','create','--label','ar2.mission='+LABEL,net]);networks.append(net)
    code="require('net').createServer(c=>{let r=require('net').connect(18080,process.env.TARGET);c.pipe(r);r.pipe(c);r.on('error',()=>c.destroy());c.on('error',()=>r.destroy())}).listen(18080,'0.0.0.0')"
    proxy=name+'-proxy';docker_new(proxy,['--network',net,'-p','127.0.0.1::18080','--read-only','--cap-drop','ALL','--security-opt','no-new-privileges','-e','TARGET='+ip,'node:20-alpine','node','-e',code]);call(['docker','network','connect',n,proxy])
    ports=json.loads(call(['docker','inspect',proxy]))[0]['NetworkSettings']['Ports']['18080/tcp'];assert all(x['HostIp']=='127.0.0.1' for x in ports)
    p['http']='http://127.0.0.1:'+ports[0]['HostPort']
    write(letter+'/isolation.json',dict(database=db,cluster=cluster,postgres_id=containers[name],network=n,proxy_id=containers[proxy],http=p['http'],tmpfs=True,retained_mounts=False,db_port_published=False,hba_sha256=hashlib.sha256(hba.encode()).hexdigest()))
    return p

def collect_log(name,path):
    r=run(['docker','logs',name],check=False);data=r.stdout+r.stderr
    for s in secret_values:data=data.replace(s.encode(),b'[REDACTED]')
    f=OUT/path;f.parent.mkdir(parents=True,exist_ok=True);f.write_bytes(data)

def start_app(p,phase,migrate=False):
    name=p['pg']+'-'+phase
    env={'SPRING_PROFILES_ACTIVE':'prod','SERVER_PORT':'18080','SERVER_ADDRESS':'0.0.0.0','SPRING_JPA_SHOW_SQL':'false','SPRING_MAIN_BANNER_MODE':'off',
         'DATASOURCE_URL':'jdbc:postgresql://127.0.0.1:5432/'+p['db'],'DATASOURCE_USERNAME':'m05_owner' if migrate else 'm05_app',
         'DATASOURCE_PASSWORD':p['owner_pw'] if migrate else p['app_pw'],'SPRING_FLYWAY_ENABLED':'true' if migrate else 'false',
         'FAC_SECURITY_ENABLED':'true','FAC_JWT_SECRET':password(),'FAC_DEMO_SEED_ON_STARTUP':'false','MANAGEMENT_ENDPOINT_HEALTH_SHOW_DETAILS':'never','MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE':'health,info'}
    docker_new(name,['--network','container:'+p['pg'],'--user','10001:10001','--read-only','--tmpfs','/tmp:rw,nosuid,size=128m',
                     '--cap-drop','ALL','--security-opt','no-new-privileges','--memory','768m','--env-file',str(envfile(name+'.env',env)),
                     '--mount','type=bind,src='+str(JAR)+',dst=/app/fac.jar,readonly','--entrypoint','java','eclipse-temurin:21-jre','-Xmx512m','-jar','/app/fac.jar'])
    for _ in range(180):
        try:
            with urllib.request.urlopen(p['http']+'/actuator/health',timeout=1) as r:
                if r.status==200 and json.load(r)['status']=='UP':break
        except Exception:pass
        meta=json.loads(call(['docker','inspect',name]))[0]
        if not meta['State']['Running']:
            collect_log(name,p['db']+'/'+phase+'.log');raise RuntimeError('Application failed to start: '+phase)
        time.sleep(.5)
    else:
        collect_log(name,p['db']+'/'+phase+'.log');raise RuntimeError('Application readiness timeout')
    meta=json.loads(call(['docker','inspect',name]))[0]
    check(phase+' nonroot/read-only',meta['Config']['User']=='10001:10001' and meta['HostConfig']['ReadonlyRootfs'])
    return name

def grants(p):
    sql(p['pg'],p['db'],"grant connect on database "+p['db']+" to m05_app,m05_operator; grant usage on schema public to m05_app,m05_operator; grant select,insert,update,delete on all tables in schema public to m05_app; grant usage,select on all sequences in schema public to m05_app; revoke all on public.local_admin_provisioning,public.flyway_schema_history from m05_app; grant select on public.flyway_schema_history to m05_app; grant execute on function public.local_admin_identity() to m05_operator; grant execute on function public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) to m05_operator;")
    checks_sql="select json_build_object('owner_superuser',(select rolsuper from pg_roles where rolname='m05_owner'),'operator_superuser',(select rolsuper from pg_roles where rolname='m05_operator'),'runtime_schema_create',has_schema_privilege('m05_app','public','CREATE'),'runtime_receipt_insert',has_table_privilege('m05_app','local_admin_provisioning','INSERT'),'function_owner',(select pg_get_userbyid(proowner) from pg_proc where oid='public.local_admin_identity()'::regprocedure))"
    x=json.loads(sql(p['pg'],p['db'],checks_sql));check('separated database roles',x==dict(owner_superuser=False,operator_superuser=False,runtime_schema_create=False,runtime_receipt_insert=False,function_owner='m05_owner'));write(p['db']+'/roles.json',x)

def cli(p,admin_password,expected):
    name=p['pg']+'-cli';docker_new(name,['--network','container:'+p['pg'],'--read-only','--tmpfs','/run/m05:rw,nosuid,size=1m,mode=0700','--tmpfs','/tmp:rw,nosuid,size=32m','--cap-drop','ALL','--security-opt','no-new-privileges','--memory','384m','--mount','type=bind,src='+str(JAR)+',dst=/app/fac.jar,readonly','--entrypoint','sleep','eclipse-temurin:21-jre','600'])
    install=sql(p['pg'],p['db'],'select installation_id from local_admin_provisioning')
    values=dict(jdbcUrl='jdbc:postgresql://127.0.0.1:5432/'+p['db'],operator='m05_operator',database=p['db'],cluster=p['cluster'],installation=install,reference='M05-SYNTHETIC-6629520296',actor='M05 synthetic operator',expiresAt=(datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(minutes=15)).isoformat().replace('+00:00','Z'))
    content=''.join(k+'='+v+'\n' for k,v in values.items())
    run(['docker','exec','-i',name,'sh','-c','umask 077; cat > /run/m05/authorization.properties'],content.encode())
    args=['docker','exec','-it',name,'java','-Dloader.main=com.ar2lda.fac.installation.FirstAdministratorCli','-cp','/app/fac.jar','org.springframework.boot.loader.launch.PropertiesLauncher','--authorization','/run/m05/authorization.properties']
    prompts=[('> ','PROVISIONAR '+install),('Password do operador tecnico local: ',p['op_pw']),('Codigo do primeiro administrador: ','M05ADMIN'),('Nome: ','Synthetic M05 admin'),('Email: ','m05admin@example.test'),('Password do administrador: ',admin_password),('Repetir password: ',admin_password)]
    pid,fd=pty.fork()
    if pid==0:os.execvp(args[0],args)
    data=b'';index=0;cursor=0;status=None;deadline=time.monotonic()+70
    try:
        while time.monotonic()<deadline:
            ready,_,_=select.select([fd],[],[],.2)
            if ready:
                try:chunk=os.read(fd,4096)
                except OSError:chunk=b''
                if not chunk:break
                data+=chunk
                if index<len(prompts) and prompts[index][0].encode() in data[cursor:]:
                    time.sleep(.1);os.write(fd,(prompts[index][1]+'\n').encode());index+=1;cursor=len(data)
            done,state=os.waitpid(pid,os.WNOHANG)
            if done:status=os.waitstatus_to_exitcode(state);break
        if status is None:
            done,state=os.waitpid(pid,os.WNOHANG)
            if not done:os.kill(pid,signal.SIGTERM);_,state=os.waitpid(pid,0)
            status=os.waitstatus_to_exitcode(state)
    finally:os.close(fd)
    # No terminal transcript stored. Only outcomes and no-echo assertion.
    no_echo=all(s.encode() not in data for s in [p['op_pw'],admin_password])
    record=dict(exit_code=status,prompts_reached=index,expected_exit=expected,passwords_not_echoed=no_echo,spring_started=b'Spring Boot' in data,success_message=b'Provisionamento concluido' in data,refusal_message=b'Provisionamento recusado' in data)
    write(p['db']+'/cli.json',record)
    remove(name)
    check('CLI outcome and terminal/password boundary',status==expected and index==7 and no_echo and not record['spring_started'] and record['success_message']==(expected==0))
    sql(p['pg'],p['db'],"revoke execute on function public.local_admin_identity() from m05_operator; revoke execute on function public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) from m05_operator; alter role m05_operator nologin password null;")

def request(p,method,path,expected,token=None,body=None):
    raw=None if body is None else json.dumps(body).encode();headers={'Content-Type':'application/json'}
    if token:headers['Authorization']='Bearer '+token
    req=urllib.request.Request(p['http']+path,data=raw,headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=15) as r:code=r.status;data=r.read()
    except urllib.error.HTTPError as r:code=r.code;data=r.read()
    check(method+' '+path+' => '+str(expected),code==expected)
    try:return json.loads(data)
    except Exception:return data

def login(p,code,pwd):
    x=request(p,'POST','/auth/login',200,body={'username':code,'password':pwd});secret_values.append(x['token']);return x

def journey(p,pwd):
    request(p,'GET','/clientes',401);admin=login(p,'M05ADMIN',pwd);check('first-admin profile and permissions',admin['papel']=='ADMINISTRADOR' and 'CONFIGURACAO_GERIR' in admin['permissoes']);a=admin['token']
    reader_pwd=password();request(p,'POST','/utilizadores',201,a,dict(codigo='M05READ',nome='Synthetic reader',email='m05read@example.test',password=reader_pwd,papel='CONSULTA',inativo=False));reader=login(p,'M05READ',reader_pwd)['token'];request(p,'GET','/auditoria',403,reader)
    # Synthetic references only; no fiscal revision or AT validation code.
    sql(p['pg'],p['db'],"insert into codpostal(id,nome) values('1000-010','Synthetic');insert into transporte(id,nome) values('M5T','Synthetic');insert into armazem(id,nome,morada,id_codpostal,localidade,id_pais) values('M5A','Synthetic','Rua Sintetica','1000-010','Synthetic','PT');")
    family=int(sql(p['pg'],p['db'],"insert into familia(descricao) values('M05 synthetic') returning id"))
    company=dict(nome='Synthetic M05 company',nif='509999999',morada='Rua Sintetica',codPostalId='1000-010',localidade='Synthetic',paisId='PT',capitalSocial=0,matriculaRegistoComercial='SYNTHETIC',cae='62010',descricaoCae='Synthetic',email='company@example.test')
    request(p,'POST','/empresa',201,a,company);request(p,'PUT','/empresa',403,reader,company)
    customer=request(p,'POST','/clientes',201,a,dict(nome='Synthetic M05 customer',morada='Rua Sintetica',localidade='Synthetic',nif='509123456',email='customer@example.test',retencao=False,inativo=False,codPostalId='1000-010',paisId='PT',moedaId='EUR',rivaId='CON',transporteId='M5T'))['id']
    article=dict(codigo='M5ART',descricao='Synthetic M05 article',tipoArtigo='SERVICO',unidade='UN',familiaId=family,ivaCompraId='NORMAL',ivaVendaId='NORMAL',pvp=10,inativo=False,retencao=False)
    request(p,'POST','/artigos',201,a,article);request(p,'GET','/artigos/M5ART',200,reader)
    typ=dict(id='M5N',descricao='Synthetic nonfiscal draft',areaGestao=1,entidade=1,sinalContabilistico=1,liquidacaoImediata=False)
    request(p,'POST','/tipos-documento',201,a,typ);request(p,'POST','/series',201,a,dict(serie='M05TEMP',tipoDocumentoId='M5N',nome='Synthetic NO AT code'))
    line=dict(artigoId='M5ART',quantidade=1,precoUnitario=10,tipoDesconto='VALOR',desconto=0,tipoTaxaIvaId='NORMAL')
    draft=request(p,'POST','/documentos-comerciais',201,a,dict(documento=dict(tipoDocumentoId='M5N',serie='M05TEMP',dataEmissao='2026-11-04',clienteId=customer,armazemCargaId='M5A',moedaId='EUR',rivaId='CON',pPagamentoId='P30',transporteId='M5T'),linha=line));id=draft['id']
    x=request(p,'GET','/documentos-comerciais/'+str(id),200,reader);check('draft persisted total',x['estado']=='RASCUNHO' and x['valorTotal']==12.3)
    diag=request(p,'GET','/documentos-comerciais/'+str(id)+'/diagnostico',200,a);write('journey-diagnostic.json',diag)
    refusal=request(p,'POST','/documentos-comerciais/'+str(id)+'/emitir',400,a,{'emissorId':'M05ADMIN'});check('emission blocked by missing AT code', 'AT' in refusal.get('message',''))
    ft=request(p,'POST','/tipos-documento',409,a,dict(id='M5F',descricao='Synthetic negative FT',codigoFiscal='FT',areaGestao=2,entidade=1,sinalContabilistico=1,liquidacaoImediata=False));write('runtime-gates.json',dict(no_at_emission_status=400,no_at_message=refusal.get('message'),governed_ft_status=409,governed_ft_message=ft.get('message'),signature_409='reused M03 evidence; not reached in this runtime because legitimate AT series missing; no synthetic AT code fabricated'))
    csv=request(p,'GET','/exportacoes/clientes?formato=csv',200,reader);check('nonfiscal export',isinstance(csv,bytes) and b'Synthetic M05' in csv)
    audit=request(p,'GET','/auditoria?entidadeTipo=DOCUMENTO_COMERCIAL&entidadeId='+str(id),200,a);check('audit journey visible',audit['totalElements']>=1)
    return id

def financial_state(p):
    tables=['pendente','documento_financeiro','financial_position','financial_operation','financial_document_origin','financial_receipt','fiscal_catalog_revision']
    values={t:int(sql(p['pg'],p['db'],'select count(*) from public.'+t)) for t in tables}
    values['issued']=int(sql(p['pg'],p['db'],"select count(*) from documento_comercial where estado <> 'RASCUNHO' or numero_documento is not null or atcud is not null or qr_payload is not null"))
    values['numbered_series']=int(sql(p['pg'],p['db'],'select count(*) from serie where numerador<>0 or codigo_at is not null'))
    check('financial and fiscal gates unchanged',all(v==0 for v in values.values()));return values

def controls(p):
    tables=sql(p['pg'],p['db'],"select tablename from pg_tables where schemaname='public' order by tablename").splitlines();result={}
    for t in tables:
        assert re.fullmatch('[a-zA-Z0-9_]+',t)
        result[t]=json.loads(sql(p['pg'],p['db'],f'''select json_build_object('count',count(*),'digest',md5(coalesce(string_agg(md5(row_to_json(t)::text),'' order by row_to_json(t)::text),''))) from public."{t}" t'''))
    seqs=sql(p['pg'],p['db'],"select sequencename from pg_sequences where schemaname='public' order by sequencename").splitlines();result['_sequences']={s:sql(p['pg'],p['db'],'select last_value::text||\',\'||is_called::text from public."'+s+'"') for s in seqs}
    return result

def integrity(p):
    constraints=json.loads(sql(p['pg'],p['db'],"select coalesce(json_agg(json_build_object('name',c.conname,'src',c.conrelid::regclass::text,'dst',c.confrelid::regclass::text,'valid',c.convalidated,'a',(select json_agg(a.attname order by k.n) from unnest(c.conkey) with ordinality k(id,n) join pg_attribute a on a.attrelid=c.conrelid and a.attnum=k.id),'b',(select json_agg(a.attname order by k.n) from unnest(c.confkey) with ordinality k(id,n) join pg_attribute a on a.attrelid=c.confrelid and a.attnum=k.id))),'[]'::json) from pg_constraint c join pg_namespace n on n.oid=c.connamespace where n.nspname='public' and c.contype='f'"))
    def qi(s):return '.'.join('"'+x+'"' for x in s.split('.'))
    for c in constraints:
        assert c['valid'];cond=' and '.join('s."'+a+'" is not null' for a in c['a']);join=' and '.join('d."'+b+'"=s."'+a+'"' for a,b in zip(c['a'],c['b']))
        assert sql(p['pg'],p['db'],'select count(*) from '+qi(c['src'])+' s where '+cond+' and not exists(select 1 from '+qi(c['dst'])+' d where '+join+')')=='0'
    write(p['db']+'/integrity.json',dict(foreign_keys=len(constraints),orphans=0,not_validated=0))

before=identity();assert before['branch']=='codex/m05-isolated-pilot' and not before['status']
OUT.mkdir(parents=True);PRIVATE.mkdir(parents=True);write('git-before.json',before)
JAR=ROOT/'target/fac-0.0.1-SNAPSHOT.jar'
try:
    images={}
    for image in ['postgres:16.3','node:20-alpine','eclipse-temurin:21-jre']:
        x=json.loads(call(['docker','image','inspect',image]))[0];images[image]=dict(id=x['Id'],digests=x.get('RepoDigests'))
    write('images.json',images)
    if len(sys.argv)==3 and sys.argv[1]=='--reuse-package':
        previous=(ROOT/'target/m05-evidence'/sys.argv[2]).resolve()
        assert previous.parent==(ROOT/'target/m05-evidence').resolve()
        prior=json.loads((previous/'jar.json').read_text())
        assert prior['sha256']==digest(JAR) and not prior['git']['status']
        assert not call(['git','diff',prior['git']['head'],'HEAD','--','src','frontend','pom.xml'])
        write('package-reused.json',dict(original=str(previous),original_manifest_sha256=digest(previous/'MANIFEST_SHA256.json'),original_jar=prior,production_code_unchanged=True))
    else:
        assert len(sys.argv)==1
        with (OUT/'package.log').open('wb') as f:
            r=sp.run(['/mnt/c/Tools/apache-maven-3.9.16/bin/mvn','-o','-B','-DskipTests','package'],cwd=ROOT,stdout=f,stderr=sp.STDOUT)
        check('package',r.returncode==0)
    check('Git unchanged after package/reuse',identity()==before);write('jar.json',dict(bytes=JAR.stat().st_size,sha256=digest(JAR),git=identity(),reused_package=len(sys.argv)>1))
    a=make_pg('a');boot=start_app(a,'migration',True);collect_log(boot,'migration-a.log');remove(boot)
    check('initial installation without users',sql(a['pg'],a['db'],'select count(*) from utilizador')=='0')
    grants(a);admin_password=password();cli(a,admin_password,0)
    receipt=sql(a['pg'],a['db'],"select json_build_object('consumed',consumed_at is not null,'administrator',administrator_code,'event',audit_event_id,'owner',(select pg_get_userbyid(proowner) from pg_proc where oid='public.local_admin_identity()'::regprocedure)) from local_admin_provisioning")
    write('bootstrap-receipt.json',json.loads(receipt));check('first-admin receipt consumed',json.loads(receipt)['consumed'] is True)
    app=start_app(a,'runtime');doc=journey(a,admin_password);write('financial-state-a.json',financial_state(a));collect_log(app,'runtime-a.log');remove(app)
    integrity(a);source=controls(a);write('controls-a.json',source)
    dump=OUT/'synthetic.backup'
    with dump.open('wb') as f:
        r=sp.run(['docker','exec',a['pg'],'pg_dump','-Fc','--no-password','-U','postgres','-d',a['db']],cwd=ROOT,stdout=f,stderr=sp.PIPE)
    check('synthetic backup',r.returncode==0 and dump.stat().st_size>0);write('backup.json',dict(bytes=dump.stat().st_size,sha256=digest(dump),database=a['db'],cluster=a['cluster'],global_roles_or_plain_secrets_included=False))
    b=make_pg('b');check('second cluster identity differs',a['cluster']!=b['cluster'])
    r=run(['docker','exec','-i',b['pg'],'pg_restore','--exit-on-error','--single-transaction','--no-password','-U','postgres','-d',b['db']],dump.read_bytes());(OUT/'restore.log').write_bytes(r.stdout+r.stderr)
    restored=controls(b);write('controls-b-restored.json',restored);check('all tables and sequences equal after restore',source==restored);integrity(b)
    boot=start_app(b,'restore-validate',True);collect_log(boot,'restore-validate.log');remove(boot);check('no migrations or data rewritten on restore boot',controls(b)==source)
    grants(b);cli(b,admin_password,1);check('restored receipt prevents new first admin',sql(b['pg'],b['db'],'select count(*) from utilizador')=='2')
    app=start_app(b,'runtime');x=login(b,'M05ADMIN',admin_password);check('restored login profile',x['papel']=='ADMINISTRADOR');r=request(b,'GET','/documentos-comerciais/'+str(doc),200,x['token']);check('restored draft unchanged',r['estado']=='RASCUNHO' and r['valorTotal']==12.3);write('financial-state-b.json',financial_state(b));collect_log(app,'runtime-b.log');remove(app)
    check('Git unchanged throughout pilot',identity()==before);write('result.json',dict(result='PASS',checks=len(checks),passed=sum(c['passed'] for c in checks),scope='nonproductive isolated pilot; runtime signature409 reused; AT missing returns400; catalogue gate409 observed',git=identity()))
    print('M05_PILOT_PASS',str(OUT),flush=True)
finally:
    for name in list(containers)[::-1]:
        if 'runtime' in name or 'migration' in name or 'validate' in name:collect_log(name,'cleanup-'+name+'.log')
        remove(name)
    for net in networks[::-1]:
        x=json.loads(call(['docker','network','inspect',net]))[0];assert x['Labels'].get('ar2.mission')==LABEL and not x.get('Containers');call(['docker','network','rm',net])
    for f in PRIVATE.iterdir():f.unlink()
    PRIVATE.rmdir();write('disposal.json',dict(only_own_resources_removed=True,secrets_removed=True))
    write('git-after.json',identity())
    (OUT/'run_pilot.py').write_bytes(Path(__file__).read_bytes())
    for f in OUT.rglob('*'):
        if f.is_file() and f.name!='synthetic.backup':assert all(s.encode() not in f.read_bytes() for s in secret_values),'Do not archive exposed secrets'
    files=[dict(name=str(f.relative_to(OUT)),bytes=f.stat().st_size,sha256=digest(f)) for f in sorted(OUT.rglob('*')) if f.is_file()]
    write('MANIFEST_SHA256.json',dict(files=files))
