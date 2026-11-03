package com.ar2lda.fac.installation;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/** Policies for post-21 runtimes are simulated; actual subprocess test uses installed Java. */
class FirstAdministratorTerminalTests {
    @Test void noConsoleIsAlwaysRefused(){for(int v:new int[]{21,22,25})assertThat(FirstAdministratorCli.terminalAllowed(v,false,()->true)).isFalse();}
    @Test void java21RetainsConsoleCompatibility(){assertThat(FirstAdministratorCli.terminalAllowed(21,true,()->{throw new AssertionError("No 22 API on Java 21");})).isTrue();}
    @Test void redirectedConsoleOnJava22IsRefused(){assertThat(FirstAdministratorCli.terminalAllowed(22,true,()->false)).isFalse();}
    @Test void redirectedConsoleOnLaterRuntimeIsRefused(){assertThat(FirstAdministratorCli.terminalAllowed(25,true,()->false)).isFalse();}
    @Test void newerTerminalIsAcceptedOnlyWhenConfirmed(){assertThat(FirstAdministratorCli.terminalAllowed(22,true,()->true)).isTrue();}
    @Test void missingOrInaccessibleTerminalApiFailsClosed(){
        assertThat(FirstAdministratorCli.terminalAllowed(22,true,()->{throw new NoSuchMethodException();})).isFalse();
        assertThat(FirstAdministratorCli.terminalAllowed(25,true,()->{throw new IllegalAccessException();})).isFalse();
    }
    @Test void brokenProbeAndUnsupportedRuntimeFailClosed(){
        assertThat(FirstAdministratorCli.terminalAllowed(25,true,()->{throw new IllegalStateException();})).isFalse();
        assertThat(FirstAdministratorCli.terminalAllowed(20,true,()->true)).isFalse();
    }
}
