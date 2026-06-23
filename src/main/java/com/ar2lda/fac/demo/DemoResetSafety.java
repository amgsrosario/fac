package com.ar2lda.fac.demo;

public final class DemoResetSafety {
    private DemoResetSafety() {}
    public static void validate(String database, String expected, boolean authorized) {
        if (!authorized || !"fac_demo".equals(database) || !database.equals(expected))
            throw new IllegalStateException("Seed demo recusado: exige perfil demo, FAC_DEMO_RESET_AUTHORIZED=true e base fac_demo");
    }
}
