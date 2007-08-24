package liquibase.migrator.sybase;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

import java.net.InetAddress;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class SybaseSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public SybaseSampleChangeLogRunnerTest() throws Exception {
        super("sybase", "com.sybase.jdbc3.jdbc.SybDriver", "jdbc:sybase:Tds:"+ InetAddress.getLocalHost().getHostName()+":5000/liquibase");
    }
}
