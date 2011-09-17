package se.joel.sailfinlogviewer.parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import se.joel.sailfinlogviewer.gui.StatusView;


public class Log {
    private List<LogRecord> logRecords = new ArrayList<LogRecord>();
    private String name;

    /**
     *
     * @param logRecords
     */
    public Log(String name, List<LogRecord> logRecords) {
        this.name = name;
        this.logRecords = logRecords;
    }

    /**
     * Gets the indices of log records matching the regular expression.
     * @param regex
     * @param logRecords
     * @param statusView 
     *
     * @return the logRecords
     * @throws FilterException
     */
    public static List<Integer> findMatchingIndices(String regex, Collection<LogRecord> logRecords, StatusView statusView) throws FilterException {
        List<Integer> matchingIndices = new ArrayList<Integer>();

        try {
            if ((regex != null) && (regex.trim().length() > 0)) {
                Pattern pattern = setupPattern(regex);

                int n = 0;
                for (LogRecord logRecord : logRecords) {
                    if (pattern.matcher(logRecord.getRawLine().replace('\n', ' ')).matches()) {
                        matchingIndices.add(logRecord.getIndex());
                    }
                    n++;
                    statusView.setProgressPercentage((int) (100.0*n/logRecords.size()));
                }

                return matchingIndices;
            }

            return matchingIndices;
        } catch (PatternSyntaxException e) {
            throw new FilterException("Could not compile regular expression", e);
        }
    }

    public List<LogRecord> getLogRecords() {
        return logRecords;
    }

    protected void addLogRecords(Collection<LogRecord> logRecords) {
        this.logRecords.addAll(logRecords);
    }

    /**
     * Gets the parsed records.
     * @param regex
     * @param statusView 
     *
     * @return the logRecords
     * @throws FilterException
     */
    public Collection<LogRecord> getLogRecords(String regex, StatusView statusView) throws FilterException {
        try {
            if ((regex != null) && (regex.trim().length() > 0)) {
                Pattern pattern = setupPattern(regex);
                List<LogRecord> filteredRecords = new ArrayList<LogRecord>();

                int n = 0;
                for (LogRecord logRecord : logRecords) {
                    if (pattern.matcher(logRecord.getRawLine().replace('\n', ' ')).matches()) {
                        filteredRecords.add(logRecord);
                    }
                    n++;
                    statusView.setProgressPercentage((int) (100.0*n/logRecords.size()));
                }

                return filteredRecords;
            } else {
                return Collections.unmodifiableCollection(logRecords);
            }
        } catch (PatternSyntaxException e) {
            throw new FilterException("Could not compile regular expression", e);
        }
    }

    private static Pattern setupPattern(String regex) {
        String prefix = "";
        String suffix = "";
        if (!(regex.startsWith("^") || regex.startsWith(".*"))) prefix  = ".*"; 
        if (!(regex.endsWith("$") || regex.endsWith(".*"))) suffix  = ".*"; 
        Pattern pattern = Pattern.compile(prefix + regex + suffix);
        return pattern;
    }

    public static class FilterException extends Exception {
        private static final long serialVersionUID = 1L;

        public FilterException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public String getName() {
        return name;
    }

    public int size() {
        return logRecords.size();
    }
}
