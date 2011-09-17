package se.joel.sailfinlogviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import se.joel.sailfinlogviewer.parser.Log;
import se.joel.sailfinlogviewer.parser.LogRecord;
import se.joel.sailfinlogviewer.parser.Log.FilterException;
import se.joel.sailfinlogviewer.util.ToolState;


public class LogRecordTable extends JTable implements TagHandler {
    private static final String TAGGED_RECORDS = "TaggedRecords";
    private static final long serialVersionUID = 1L;
    private static final int INDEX_COLUMN = 0;
    private static final int INSTANCE_COLUMN = 1;
    private static final int THREAD_ID_COLUMN = 2;
    private static final int LOG_RECORD_COLUMN = 3;
    private static final Color LIGHT_BLUE = new Color(186, 229, 255);
    private static final Color SAND = new Color(255, 237, 165);
    private static final Color GREY_SAND = new Color(216, 204, 144);
    private static final String[] COLUMN_IDENTIFIERS = new String[] { "Rec", "Instance", "Thread", "Time: Message" };
    private static Logger logger = Logger.getLogger("GUI");
    private JScrollPane listScrollPane;
    private List<Integer> taggedRecords = new ArrayList<Integer>();
    private int currentTagIndex = -1;
    private ToolState toolState;
    private Log log;
    private String filter;
    private boolean showOnlyTaggedRecords;
    private Map<Integer, Color> threadColorMap = new HashMap<Integer, Color>();
    private int lowestThreadId = Integer.MAX_VALUE;
    private int highestThreadId = Integer.MIN_VALUE;
    private StatusView statusView;

    public LogRecordTable(ToolState toolState, StatusView statusView) {
        super(new LogRecordTableModel());
        this.statusView = statusView;
        this.toolState = toolState;
        listScrollPane = new JScrollPane(this);
        listScrollPane.setPreferredSize(new Dimension(400, 200));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDefaultRenderer(Object.class, new LogRecordTableCellRenderer());
        setAutoscrolls(true);
        // setAutoCreateRowSorter(true);
        setColumnProperties();
    }

    private void setColumnProperties() {
        getColumnModel().getColumn(INDEX_COLUMN).setMinWidth(50);
        getColumnModel().getColumn(INDEX_COLUMN).setPreferredWidth(50);
        getColumnModel().getColumn(INDEX_COLUMN).setMaxWidth(100);
        getColumnModel().getColumn(INSTANCE_COLUMN).setMinWidth(70);
        getColumnModel().getColumn(INSTANCE_COLUMN).setPreferredWidth(70);
        getColumnModel().getColumn(INSTANCE_COLUMN).setMaxWidth(120);
        getColumnModel().getColumn(THREAD_ID_COLUMN).setMinWidth(50);
        getColumnModel().getColumn(THREAD_ID_COLUMN).setPreferredWidth(100);
        getColumnModel().getColumn(THREAD_ID_COLUMN).setMaxWidth(300);
        getColumnModel().getColumn(LOG_RECORD_COLUMN).setPreferredWidth(600);
        int threadIdwidth = new ThreadIdComponent(null, highestThreadId, lowestThreadId).getActualWidth() + 2;
        getColumnModel().getColumn(THREAD_ID_COLUMN).setPreferredWidth(threadIdwidth);
        getColumnModel().getColumn(THREAD_ID_COLUMN).setMaxWidth(threadIdwidth);
        
        setIntercellSpacing(new Dimension(2, 2));
    }

    /**
     * @return the listScrollPane
     */
    public JScrollPane getScrollPane() {
        return listScrollPane;
    }

    public void setLog(Log log, String filter) {
        clear();
        if (log == null) {
            return;
        }

        this.log = log;
        this.filter = filter;
        restoreTaggedRows();

        addLogRecordsToTable(log, filter);
    }

    private void addLogRecordsToTable(final Log log, final String filter) {
        new SwingWorker<Vector<Vector<Object>>, Object>(){
            @Override
            protected Vector<Vector<Object>> doInBackground() throws Exception {
                statusView.setStatusText("Updating...");
                int n = 0;
                Collection<LogRecord> logRecords = log.getLogRecords(filter, statusView);
                Vector<Vector<Object>> table = new Vector<Vector<Object>>();
                for (final LogRecord logRecord : logRecords) {
                    // addLogRecord(logRecord);
                    updateThreadInfo(logRecord);
                    table.add(new Vector<Object>(Arrays.asList(new Object[] { logRecord.getIndex(), logRecord, logRecord, logRecord })));
                    statusView.setProgressPercentage((int) (100.0*n/logRecords.size()));
                    n++;
                }
                return table;
            }

            @Override
            protected void done() {
                try {
                    Vector<Vector<Object>> tableData = get();
                    LogRecordTableModel logRecordTableModel = (LogRecordTableModel) getModel();
                    logRecordTableModel.setDataVector(tableData, new Vector<String>(Arrays.asList(COLUMN_IDENTIFIERS)));
                    setColumnProperties();
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Error", e);
                } catch (ExecutionException e) {
                    logger.log(Level.WARNING, "Error", e);
                } finally {
                    SwingUtilities.invokeLater(new Runnable(){
                        @Override
                        public void run() {
                            statusView.resetStatus();
                        }
                    });
                }
            }
        }.execute();
    }

    private void clearTable(final LogRecordTableModel logRecordTableModel) {
        logRecordTableModel.setDataVector(new Object[][]{}, COLUMN_IDENTIFIERS);
    }
    
    public void setFilter(String filter) {
        clear();
        if (log == null) {
            return;
        }
        this.filter = filter;

        addLogRecordsToTable(log, filter);
    }
    

    @Override
    public void toggleShowOnlyTaggedRecords() throws FilterException {
        clear();
        if (log == null) {
            return;
        }
        showOnlyTaggedRecords = !showOnlyTaggedRecords;
        
        new SwingWorker<Iterable<LogRecord>, Object>(){
            @Override
            protected Iterable<LogRecord> doInBackground() throws Exception {
                statusView.setStatusText("Filtering...");
                return log.getLogRecords(filter, statusView);
            }

            @Override
            protected void done() {
                try {
                    for (LogRecord logRecord : get()) {
                        if (!showOnlyTaggedRecords || taggedRecords.contains(logRecord.getIndex())) addLogRecord(logRecord);
                    }
                } catch (InterruptedException e) {
                    logger.log(Level.WARNING, "Error", e);
                } catch (ExecutionException e) {
                    logger.log(Level.WARNING, "Error", e);
                } finally {
                    statusView.resetStatus();
                }
            }
        }.execute();
    }
    
    private void addLogRecord(LogRecord logRecord) {
        updateThreadInfo(logRecord);
        LogRecordTableModel logRecordTableModel = (LogRecordTableModel) getModel();
        logRecordTableModel.addRow(new Object[] { logRecord.getIndex(), logRecord, logRecord, logRecord });
    }

    private void updateThreadInfo(final LogRecord logRecord) {
        int threadId = Integer.parseInt(logRecord.getPositionInfo().getThreadId());
        highestThreadId = Math.max(threadId, highestThreadId);
        lowestThreadId = Math.min(threadId, lowestThreadId);
        threadColorMap.put(threadId, new Color(threadId * 107 % 255, threadId * 67 % 255, threadId * 37 % 255));
    }

    public LogRecord getLogRecordAt(int index) {
        if ((index >= 0) && (index < getLogRecordTableModel().getRowCount())) {
            return (LogRecord) getLogRecordTableModel().getValueAt(index, LOG_RECORD_COLUMN);
        }
        return null;
    }

    private LogRecordTableModel getLogRecordTableModel() {
        return ((LogRecordTableModel) getModel());
    }

    public void clear() {
        lowestThreadId = Integer.MAX_VALUE;
        highestThreadId = Integer.MIN_VALUE;
        threadColorMap.clear();
        LogRecordTableModel logRecordTableModel = (LogRecordTableModel) getModel();
        clearTable(logRecordTableModel);
    }

    private void restoreTaggedRows() {
        try {
            for (String string : toolState.getList(getTaggedRecordsPropertyName())) {
                taggedRecords.add(Integer.parseInt(string));
            }
        } catch (NumberFormatException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to restore row tag", e);
        } catch (InvalidPropertiesFormatException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to restore row tag", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to restore row tag", e);
        }
    }

    public void toggleRecordTag() {
        LogRecord logRecord = getLogRecordAt(getSelectedRow());
        if (logRecord == null) return;

        int index = logRecord.getIndex();

        if (!taggedRecords.contains(index)) {
            taggedRecords.add(index);
        } else {
            taggedRecords.remove(new Integer(index));
        }

        Collections.sort(taggedRecords);
        requestFocusInWindow();
        repaint();
        
        try {
            toolState.saveList(getTaggedRecordsPropertyName(), taggedRecords);
        } catch (FileNotFoundException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to save row tag", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save row tag", e);
        }
    }

    private String getTaggedRecordsPropertyName() {
        return log.getName()+"."+TAGGED_RECORDS;
    }

    public void clearRecordTags() {
        taggedRecords.clear();
        requestFocusInWindow();
        repaint();

        try {
            toolState.saveList(getTaggedRecordsPropertyName(), taggedRecords);
        } catch (FileNotFoundException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to save row tag", e);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to save row tag", e);
        }
    }

    public void gotoPrevTag() {
        if (taggedRecords.size() == 0) return;
        currentTagIndex--;

        if (currentTagIndex < 0) {
            currentTagIndex = taggedRecords.size() - 1;
        }

        int row = getRowForRecordIndex(taggedRecords.get(currentTagIndex));
        
        if (row >= 0) {
            changeSelection(row, 0, false, false);
            requestFocusInWindow();
        }
    }

    public void gotoNextTag() {
        if (taggedRecords.size() == 0) return;
        
        currentTagIndex++;

        if (currentTagIndex >= taggedRecords.size()) {
            currentTagIndex = 0;
        }

        int row = getRowForRecordIndex(taggedRecords.get(currentTagIndex));
        
        if (row >= 0) {
            changeSelection(row, 0, false, false);
            requestFocusInWindow();
        }
    }

    public int getRowForRecordIndex(int index) {
        int row = -1;
        for (int i = 0; i < getLogRecordTableModel().getRowCount(); i++) {
            if (getLogRecordAt(i).getIndex() == index) {
                row = i;
                break;
            }
        }
        return row;
    }

    public Collection<LogRecord> getLogRecords() {
        List<LogRecord> records = new ArrayList<LogRecord>();
        LogRecordTableModel logRecordTableModel = (LogRecordTableModel) getModel();

        for (int i = 0; i < logRecordTableModel.getRowCount(); i++) {
            records.add((LogRecord) logRecordTableModel.getValueAt(i, LOG_RECORD_COLUMN));
        }

        return Collections.unmodifiableCollection(records);
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        LogRecordTable list = new LogRecordTable(new ToolState(), new StatusView() {
            @Override
            public void resetStatus() {
                // Nada
            }

            @Override
            public void setProgressPercentage(int percentage) {
                // Nada
            }

            @Override
            public void setStatusText(String statusText) {
                // Nada
            }
        });
        list.addLogRecord(new LogRecord(1, "instance1", "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipSession;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|RETURN|#]\n"));
        f.getContentPane().add(list);
        f.pack();
        f.setVisible(true);
    }

    private class LogRecordTableCellRenderer implements TableCellRenderer {
        private void setBackgroundColor(int logRecordIndex, int row, boolean isSelected, JComponent cell, boolean isException) {
            if (isException) {
                cell.setBackground(Color.RED);
            } else if (taggedRecords.contains(logRecordIndex) && isSelected) {
                cell.setBackground(GREY_SAND);
            } else if (isSelected) {
                cell.setBackground(Color.LIGHT_GRAY);
            } else if (taggedRecords.contains(logRecordIndex)) {
                cell.setBackground(SAND);
            } else if ((row % 2) == 0) {
                cell.setBackground(LIGHT_BLUE);
            } else {
                cell.setBackground(Color.WHITE);
            }
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == INDEX_COLUMN) {
                JLabel cell = new JLabel("" + value);
                Font font = new Font("Sans-serif", Font.PLAIN, 12);
                cell.setFont(font);
                cell.setOpaque(true);
                setBackgroundColor((Integer) value, row, isSelected, cell, exceptionMessage(row));
                return cell;
            } else if (column == INSTANCE_COLUMN) {
                JLabel cell = new JLabel(((LogRecord) value).getInstanceName());
                Font font = new Font("Sans-serif", Font.PLAIN, 12);
                cell.setFont(font);
                cell.setOpaque(true);
                setBackgroundColor(((LogRecord) value).getIndex(), row, isSelected, cell, exceptionMessage(row));

                return cell;
            } else if (column == THREAD_ID_COLUMN) {
                int tid = Integer.parseInt(((LogRecord) value).getPositionInfo().getThreadId());
                int lrIndex = ((LogRecord) value).getIndex();
                ThreadIdComponent tidLabel = new ThreadIdComponent(threadColorMap.get(tid), tid, lowestThreadId);
                setBackgroundColor(lrIndex, row, isSelected, tidLabel, exceptionMessage(row));

                return tidLabel;
            } else if (column == LOG_RECORD_COLUMN) {
                LogRecord lr = (LogRecord) value;
                JLabel cell = new JLabel("<html>" + lr.getTime() + ": <b>" + lr.getMessage() + "</b></html>");
                Font font = new Font("Sans-serif", Font.PLAIN, 12);
                cell.setFont(font);
                cell.setOpaque(true);
                setBackgroundColor(((LogRecord) value).getIndex(), row, isSelected, cell, exceptionMessage(row));

                return cell;
            } else {
                return new JLabel("Unexpected object: " + value);
            }
        }

        private boolean exceptionMessage(int row) {
            return log.getLogRecords().get(row).getMessage().contains("\tat ");
        }
    }

    private static class LogRecordTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 1L;

        public LogRecordTableModel() {
            setColumnCount(3);
            setColumnIdentifiers(COLUMN_IDENTIFIERS);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private static class ThreadIdComponent extends JComponent {
        private static final long serialVersionUID = 1L;
        private int threadId;
        private Color threadColor;
        private int strWidth;
        private int threadLineOffset;
        private static final int THREAD_LINE_WIDTH = 2;
        private static final int X_PADDING = 2;
        private static final int SPACE = 4;

        public ThreadIdComponent(Color threadColor, int threadId, int lowestTid) {
            this.threadColor = threadColor;
            this.threadId = threadId;
            Font font = new Font("Sans-serif", Font.PLAIN, 12);
            setFont(font);
            FontMetrics fontMetrics = getFontMetrics(font);
            strWidth = fontMetrics.stringWidth(""+threadId);
            threadLineOffset = (threadId-lowestTid)*2;
        }

        
        public int getActualWidth() {
            return X_PADDING + strWidth + SPACE + threadLineOffset + THREAD_LINE_WIDTH  + X_PADDING;
        }


        @Override
        public void paint(Graphics g) {
            Color orgColor = g.getColor();
            g.setColor(getBackground());
            g.fillRect(0,0, getWidth(), getHeight());
            g.setColor(Color.BLACK);
            String tidStr = threadId+"";
            g.drawString(tidStr, X_PADDING, getHeight()-2);
            g.setColor(threadColor);
            g.fillRect(X_PADDING + strWidth + SPACE + threadLineOffset, 0, THREAD_LINE_WIDTH, getHeight());
            g.setColor(orgColor);
        }
    }
}
