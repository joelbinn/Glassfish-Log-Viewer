package se.joel.sailfinlogviewer.parser;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;


/**
 * Parse a stream with lines looking like this:
 *
 * [#|2008-10-15T13:29:04.541+0200|FINEST|sun-glassfish-comms-server1.0|javax.
 * enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-
 * clientsWorkerThread
 * -5060-6;ClassName=com.ericsson.ssa.sip.SipSessionDialogImpl
 * ;MethodName=doCleanup;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|The
 * sip session with ID=c867cdeaaf6ba5d749259b391b884a53ebd7##130.100.224.174 has
 * been removed from the active cache.|#]
 *
 * @author qbinjoe
 *
 */
public class LogParser {
    private LineNumberReader lnr;
    private String name;

    /**
     * @param lnr
     *            the line number reader to parse.
     */
    public LogParser(String name, LineNumberReader lnr) {
        assert lnr != null;
        this.name = name;
        this.lnr = lnr;
    }

    public Log parse() throws LogParserException {
        List<LogRecord> logRecords = new ArrayList<LogRecord>();

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            int lineNumber = 1;

            while ((line = lnr.readLine()) != null) {
                if (line.startsWith("[#")) {
                    sb.setLength(0);
                }

                sb.append(line);

                if (line.endsWith("#]")) {
                    LogRecord logRecord = new LogRecord(lineNumber, name, sb.toString());
                    logRecords.add(logRecord);
                    lineNumber++;
                } else {
                    sb.append('\n');
                }
            }
        } catch (IOException e) {
            throw new LogParserException("I/O exception when reading.", e);
        }

        return new Log(name, logRecords);
    }

    public static class LogParserException extends Exception {
        private static final long serialVersionUID = 1L;

        public LogParserException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
