# Procedimento futuro — não executar numa instalação real nesta missão

1. Obter autorização administrativa, parar a aplicação, identificar base/cluster/UUID e confirmar elegibilidade/ausência de utilizadores. Aplicar migrations por procedimento autónomo autorizado e backup/teste prévios; esta missão só ensaia V29 em descartáveis.
2. DBA cria role técnica temporária **sem privilégios administrativos/ownership** e restringe acesso no HBA à instalação local. Exemplo de grants, usando identidade legítima escolhida pelo DBA, não um utilizador universal:

```sql
GRANT CONNECT ON DATABASE base_autorizada TO operador_autorizado;
GRANT USAGE ON SCHEMA public TO operador_autorizado;
GRANT EXECUTE ON FUNCTION public.local_admin_identity() TO operador_autorizado;
GRANT EXECUTE ON FUNCTION public.provision_first_administrator(text,text,uuid,text,text,text,timestamptz,text,text,text,text) TO operador_autorizado;
```

Nenhuma password neste exemplo. Funções pertencem ao dono/migrador controlado, com search_path fixo; não conceder ownership, CREATE no schema, DML de tabelas, memberships privilegiadas ou EXECUTE a PUBLIC. Se o modelo actual de instalação não oferecer essa separação de roles, resolver o deployment autorizado antes de utilizar a ferramenta; não executar como postgres/superuser.

3. Obter metadados por `SELECT * FROM public.local_admin_identity()` usando a role restrita; DBA confirma a origem independentemente. Criar ficheiro privado 0600 em directório protegido com os oito campos descritos em DESIGN.md e data de expiração explícita. Não copiar identidades/credenciais demo nem exemplos como valores comerciais. Dados do administrador precisam de autorização legítima.
4. Na consola local, usar o artefacto aprovado:

```sh
java -Dloader.main=com.ar2lda.fac.installation.FirstAdministratorCli \
  -cp fac-0.0.1-SNAPSHOT.jar \
  org.springframework.boot.loader.launch.PropertiesLauncher \
  --authorization /caminho/protegido/autorizacao.properties
```

Confirmar UUID, fornecer passwords apenas nas perguntas sem eco, repetir a password do administrador. Sem consola a operação é recusada. Não redireccionar stdout/stdin para arquivo de segredos, gravar terminal, passar passwords por argv ou ligar este comando a um endpoint.

5. Confirmar auditoria e comprovativo pelo DBA; em resultado incerto consultar antes de repetir. Revogar os dois grants, desactivar/remover a role técnica conforme a política operacional e retirar o ficheiro. Não apagar/repor o comprovativo; não alterar hash via SQL como recuperação implícita. Arrancar a aplicação pelo procedimento autorizado e testar login/permissões sem divulgar tokens. Não confundir provisionamento com autorização de emissão/activação.

Recuperação de administrador existente, alteração de identidade ou reabertura do consumo exige mandato próprio. Não existe reset/force automático. Esta missão não executa nenhum destes passos numa base conservada/comercial.
