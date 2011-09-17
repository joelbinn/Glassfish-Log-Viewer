package se.joel.sailfinlogviewer.parser;

import org.junit.Test;

import se.joel.sailfinlogviewer.gui.StatusView;
import se.joel.sailfinlogviewer.parser.Log.FilterException;
import se.joel.sailfinlogviewer.parser.LogParser.LogParserException;

import java.io.LineNumberReader;
import java.io.StringReader;


public class LogParserTest {
    private static final String TEST_LOG = "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipApplicationSession;SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|ENTRY SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0|#]\n" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|#]\n" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|\n" + "#]\n";

    @Test
    public void testGetLogRecords() throws LogParserException, FilterException {
        StringReader sr = new StringReader(TEST_LOG);
        LineNumberReader lnr = new LineNumberReader(sr);
        LogParser lp = new LogParser("loggen", lnr);
        Log log = lp.parse();
        int n = 1;

        StatusView statusView = new StatusView() {
                @Override
                public void resetStatus() {
                }

                @Override
                public void setProgressPercentage(int percentage) {
                }

                @Override
                public void setStatusText(String statusText) {
                }
            };
        for (LogRecord logRecord : log.getLogRecords(null, statusView)) {
            System.out.println("Log record " + n);
            System.out.println("  compact format: " + logRecord);

            int fn = 1;

            for (String field : logRecord.getFields()) {
                System.out.println("  field#" + fn + ": " + field);
                fn++;
            }

            n++;
        }

        n = 1;

        for (LogRecord logRecord : log.getLogRecords(".*replication.*", statusView)) {
            System.out.println("Log record " + n);
            System.out.println("  compact format: " + logRecord);

            int fn = 1;

            for (String field : logRecord.getFields()) {
                System.out.println("  field#" + fn + ": " + field);
                fn++;
            }

            n++;
        }
    }
}
