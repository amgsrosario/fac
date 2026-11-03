package com.ar2lda.fac.installation;

import java.io.Console;
import java.nio.file.Path;
import java.sql.*;
import java.util.Arrays;

/** Explicit local CLI. Never starts Spring, a web server, migrations or a demo runner. */
public final class FirstAdministratorCli {
    private FirstAdministratorCli() { }
    @FunctionalInterface interface TerminalProbe { boolean isTerminal() throws ReflectiveOperationException; }
    /** Java 21 has no isTerminal API; newer runtimes must positively prove a terminal. */
    static boolean terminalAllowed(int runtime,boolean consolePresent,TerminalProbe probe) {
        if(!consolePresent || runtime<21)return false;
        if(runtime==21)return true;
        try {return probe.isTerminal();}
        catch(ReflectiveOperationException | RuntimeException unavailable) {return false;}
    }
    private static boolean localTerminal(Console console) {
        return terminalAllowed(Runtime.version().feature(),console!=null,
            ()->(boolean)Console.class.getMethod("isTerminal").invoke(console));
    }
    public static void main(String[] args) {
        if(args.length==1 && args[0].equals("--help")) {
            System.out.println("Uso local: --authorization /caminho/absoluto/autorizacao.properties (0600; consola obrigatoria)");return;
        }
        try {
            if(args.length!=2 || !args[0].equals("--authorization")) throw new IllegalArgumentException();
            Console console=System.console();
            if(!localTerminal(console)) {
                System.err.println("Consola terminal local obrigatoria; entrada redireccionada recusada.");
                System.exit(3);return;
            }
            var auth=FirstAdministratorAuthorization.load(Path.of(args[1]));
            console.printf("Instalacao %s; base %s; cluster %s; referencia %s%n",auth.installation(),auth.database(),auth.cluster(),auth.reference());
            String confirm=console.readLine("Confirmar: PROVISIONAR %s%n> ",auth.installation());
            if(!("PROVISIONAR "+auth.installation()).equals(confirm)) throw new IllegalArgumentException();
            char[] databasePassword=console.readPassword("Password do operador tecnico local: ");
            try(var db=DriverManager.getConnection(auth.jdbcUrl(),auth.operator(),new String(databasePassword))) {
                String code=console.readLine("Codigo do primeiro administrador: ");
                String name=console.readLine("Nome: ");String email=console.readLine("Email: ");
                char[] password=console.readPassword("Password do administrador: ");
                char[] repeated=console.readPassword("Repetir password: ");
                try {
                    if(!Arrays.equals(password,repeated)) throw new IllegalArgumentException();
                    long event=new FirstAdministratorProvisioner().provision(db,auth,
                        new FirstAdministratorProvisioner.Administrator(code,name,email),password);
                    console.printf("Provisionamento concluido; evento de auditoria %d. Retirar grants/credencial tecnica.%n",event);
                } finally {if(password!=null)Arrays.fill(password,'\0');if(repeated!=null)Arrays.fill(repeated,'\0');}
            } finally {if(databasePassword!=null)Arrays.fill(databasePassword,'\0');}
        } catch(Exception error) {
            // Never print a JDBC/validation exception which may include supplied values.
            System.err.println("Provisionamento recusado ou resultado nao confirmado. Consultar comprovativo antes de repetir; nao criar outro administrador.");
            System.exit(1);
        }
    }
}
