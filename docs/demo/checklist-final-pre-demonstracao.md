# Checklist final pre-demonstracao

## Na vespera

- [ ] Energia e carregador confirmados.
- [ ] Rede local e alternativa sem rede preparadas.
- [ ] Browser atualizado, zoom testado e cache limpa.
- [ ] Porta 8088 livre ou alternativa definida em `.env.demo`.
- [ ] Docker Desktop ativo.
- [ ] `.env.demo` preenchido localmente, sem marcadores do exemplo.
- [ ] `SPRING_PROFILES_ACTIVE=demo` e `FAC_DEMO_DATABASE=fac_demo`.
- [ ] `mvn -q test` executado com sucesso.
- [ ] Frontend compilado com `npm run build`.
- [ ] Reset demo feito quando necessario.
- [ ] `FAC_DEMO_CHECK OK`.
- [ ] `FAC_COMMERCIAL_DEMO_CHECK OK`.
- [ ] Backup recente criado e verificado.
- [ ] PDF multipagina, QR Code e ATCUD confirmados.
- [ ] Importacao, exportacao, permissoes e auditoria revistos.
- [ ] Logs sem passwords.
- [ ] IP local anotado se a apresentacao for em rede local.
- [ ] Roteiro completo e curto disponiveis.

## Cinco minutos antes da reuniao

```powershell
.\scripts\demo\prepare-commercial-demo.ps1 -SkipBackup
```

Confirmar:

- [ ] `FAC_DEMO_CHECK OK`
- [ ] `FAC_COMMERCIAL_DEMO_CHECK OK`
- [ ] `FAC_COMMERCIAL_DEMO_READY`
- [ ] Abrir `http://127.0.0.1:8088`
- [ ] Login admin, PDF, extrato e auditoria prontos em separadores discretos

## Com backup de referencia

```powershell
.\scripts\demo\prepare-commercial-demo.ps1
```

## Com reset controlado

```powershell
.\scripts\demo\prepare-commercial-demo.ps1 -Reset
```
