///usr/bin/env jbang "$0" "$@" ; exit $?
////REPOS mavencentral,nexus=https://nexus.icm.med.uni-greifswald.de/repository/maven-group
//DEPS info.picocli:picocli:4.6.3
//DEPS org.emau.icmvc.ganimed.ttp:gpas-commons:2024.2.0-SNAPSHOT
//DEPS org.apache.logging.log4j:log4j-api:2.23.1
//DEPS org.apache.logging.log4j:log4j-core:2.23.1
//DEPS org.apache.cxf:cxf-rt-frontend-jaxws:4.0.4
//DEPS org.apache.cxf:cxf-rt-transports-http-jetty:4.0.4
//FILES ../../gpas-ejb/src/test/resources/log4j2.properties
//SOURCES ../../gpas-ejb/src/test/java/org/emau/icmvc/ganimed/ttp/psn/test/GPASSoapClientDemo.java

import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;

import org.emau.icmvc.ganimed.ttp.psn.test.GPASSoapClientDemo;

@Command(
        name = "gpas_soap_client_demo",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "\nDemonstrates using a SOAP client for gPAS and measures its performance.\n")
class gpas_soap_client_demo implements Callable<Integer> {

    @Option(names = {"-d", "--domain"},
            description = "The demo domain (defaults to " + GPASSoapClientDemo.DEFAULT_DOMAIN_NAME + ")",
            defaultValue = GPASSoapClientDemo.DEFAULT_DOMAIN_NAME)
    private String domain;

    @Option(names = {"-e", "--empty", "--empty-domain"},
            description = "Forces emptying the domain")
    private boolean empty;

    @Option(names = {"-c", "--bc", "--batch-count"},
            description = "The number of batches to test (defaults to '" + GPASSoapClientDemo.DEFAULT_BATCH_COUNT + "')",
            defaultValue = GPASSoapClientDemo.DEFAULT_BATCH_COUNT + "")
    private int batchCount;

    @Option(names = {"-s", "--bs", "--batch-size"},
            description = "The size of batches to test (defaults to '" + GPASSoapClientDemo.DEFAULT_BATCH_SIZE + "')" ,
            defaultValue = GPASSoapClientDemo.DEFAULT_BATCH_SIZE + "")
    private int batchSize;

    @Option(names = {"-v", "--sv", "--start-value"},
            description = "The start value (defaults to '" + GPASSoapClientDemo.DEFAULT_START_VALUE + "')" ,
            defaultValue = GPASSoapClientDemo.DEFAULT_START_VALUE + "")
    private int startValue;

    @Option(names = {"-i", "--increment", "--increment-start-value"},
            description = "Increment the start value by batch size after ever batch")
    private boolean incrementStartValue;

    @Option(names = {"-m", "--mpsn", "--multi-psn-domain"},
            description = "Create the test domain as MPSN-domain, or assert that it is a MPSN-domain, if it already exists")
    private boolean multiPsnDomain;

    public static void main(String... args) {
        System.exit(new CommandLine(new gpas_soap_client_demo()).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        GPASSoapClientDemo.timing(domain, empty, batchCount, batchSize, startValue, incrementStartValue, multiPsnDomain);
        return 0;
    }
}
