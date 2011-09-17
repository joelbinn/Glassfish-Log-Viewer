package se.joel.sailfinlogviewer.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class CompositeLog extends Log {
    public CompositeLog() {
        super("Composite log", new ArrayList<LogRecord>());
    }

    public void addLog(Log log) {
        super.addLogRecords(log.getLogRecords());
        Collections.sort(getLogRecords(), new Comparator<LogRecord>() {
            @Override
            public int compare(LogRecord o1, LogRecord o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                try {
                    Date d1 = simpleDateFormat.parse(o1.getTime());
                    Date d2 = simpleDateFormat.parse(o2.getTime());
                    if (d1.before(d2))
                        return -1;
                    if (d2.before(d1))
                        return 1;
                    return 0;
                } catch (ParseException e) {
                    return 0;
                }
            }

        });
    }
}
