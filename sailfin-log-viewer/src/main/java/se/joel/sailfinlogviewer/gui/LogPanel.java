package se.joel.sailfinlogviewer.gui;

import se.joel.sailfinlogviewer.parser.Log;
import se.joel.sailfinlogviewer.parser.Log.FilterException;
import se.joel.sailfinlogviewer.parser.LogParser;
import se.joel.sailfinlogviewer.parser.LogParser.LogParserException;
import se.joel.sailfinlogviewer.parser.LogRecord;
import se.joel.sailfinlogviewer.util.ToolState;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.LineNumberReader;
import java.io.StringReader;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class LogPanel extends JPanel implements TagHandler {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger("GUI");
    private JSplitPane splitPane;
    private LogRecordTable logRecordTable;
    private LogRecordPanel logRecordPanel;
    private int numberOfRecords;
    private StatusView statusView;

    public LogPanel(ToolState toolState, StatusView statusView) {
        this.statusView = statusView;
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        logRecordTable = new LogRecordTable(toolState, statusView);
        logRecordPanel = new LogRecordPanel();
        splitPane.add(logRecordTable.getScrollPane());
        splitPane.add(logRecordPanel);
        setPreferredSize(new Dimension(1000, 800));
        splitPane.setDividerLocation(400);

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        add(splitPane, gbc);

        logRecordTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting()) {
                        return;
                    }

                    ListSelectionModel lsm = (ListSelectionModel) e.getSource();

                    if (lsm.isSelectionEmpty()) {
                        logRecordPanel.reset();
                    }

                    int selectionIndex = lsm.getAnchorSelectionIndex();
                    LogRecord lr = logRecordTable.getLogRecordAt(selectionIndex);
                    logRecordPanel.setLogRecord(lr);
                }
            });
    }

    public void jumpToNextMatching(final String regexp, final boolean forward, final SearchResultStatusView searchResultStatusView) throws FilterException {
        new SwingWorker<List<Integer>, Object>() {

                @Override
                protected List<Integer> doInBackground() throws Exception {
                    statusView.setStatusText("Searching...");
                    return Log.findMatchingIndices(regexp, logRecordTable.getLogRecords(), statusView);
                }

                @Override
                protected void done() {
                    List<Integer> matchingIndices;

                    try {
                        matchingIndices = get();
                        if (matchingIndices == null) return;
                        
                        LogRecord selectedRecord = logRecordTable.getLogRecordAt(logRecordTable.getSelectedRow());
                        int indexOfSelected = (selectedRecord != null) ? selectedRecord.getIndex() : 0;

                        if (forward) {
                            // Search forward
                            // 1. Try from selected line in list
                            for (Integer index : matchingIndices) {
                                if (indexOfSelected < index) {
                                    logRecordTable.changeSelection(logRecordTable.getRowForRecordIndex(index), 0, false, false);
                                    logRecordTable.requestFocusInWindow();
                                    searchResultStatusView.setFound(true);
                                    return;
                                }
                            }

                            // 2. OK couldn't find it after the selected line try from the
                            // beginning
                            for (Integer index : matchingIndices) {
                                if (0 < index) {
                                    logRecordTable.changeSelection(logRecordTable.getRowForRecordIndex(index), 0, false, false);
                                    logRecordTable.requestFocusInWindow();
                                    searchResultStatusView.setFound(true);
                                    return;
                                }
                            }
                        } else {
                            // Search backward
                            // 1. Try from selected line in list
                            for (ListIterator<Integer> li = matchingIndices.listIterator(matchingIndices.size());
                                    li.hasPrevious();) {
                                int index = li.previous();

                                if (indexOfSelected > index) {
                                    logRecordTable.changeSelection(logRecordTable.getRowForRecordIndex(index), 0, false, false);
                                    logRecordTable.requestFocusInWindow();
                                    searchResultStatusView.setFound(true);
                                    return;
                                }
                            }

                            // 2. OK couldn't find it after the selected line try from the end
                            for (ListIterator<Integer> li = matchingIndices.listIterator(matchingIndices.size());
                                    li.hasPrevious();) {
                                int index = li.previous();

                                if (numberOfRecords > index) {
                                    logRecordTable.changeSelection(logRecordTable.getRowForRecordIndex(index), 0, false, false);
                                    logRecordTable.requestFocusInWindow();
                                    searchResultStatusView.setFound(true);
                                    return;
                                }
                            }
                        }
                        searchResultStatusView.setFound(false);
                    } catch (InterruptedException e) {
                        logger.log(Level.WARNING, "Error", e);
                        Throwable rootCause = e;
                        while (rootCause.getCause() != null) {
                            rootCause = rootCause.getCause();
                        }
                        statusView.setStatusText(rootCause.getMessage());
                    } catch (ExecutionException e) {
                        logger.log(Level.WARNING, "Error", e);
                        Throwable rootCause = e;
                        while (rootCause.getCause() != null) {
                            rootCause = rootCause.getCause();
                        }
                        statusView.setStatusText(rootCause.getMessage());
                    } 
                }
            }.execute();
    }

    public void setLogData(Log log, String filter) throws FilterException {
        updateList(log, filter);
        logRecordTable.changeSelection(0, 0, true, false);
    }

    private void updateList(final Log log, final String filter) throws FilterException {
        numberOfRecords = log.size();
        logRecordTable.setLog(log, filter);
    }

    public void setFilter(String filter) throws FilterException {
        logRecordTable.setFilter(filter);
    }

    public int getSelectedRow() {
        return logRecordTable.getSelectedRow();
    }

    public void toggleRecordTag() {
        logRecordTable.toggleRecordTag();
    }

    public void clearRecordTags() {
        logRecordTable.clearRecordTags();
    }

    public void gotoPrevTag() {
        logRecordTable.gotoPrevTag();
    }

    public void gotoNextTag() {
        logRecordTable.gotoNextTag();
    }


    @Override
    public void toggleShowOnlyTaggedRecords() throws FilterException {
        logRecordTable.toggleShowOnlyTaggedRecords();
    }

    public void resetView() {
        logRecordTable.clear();
    }

    public static void main(String[] args) throws LogParserException, FilterException {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        String testLog = "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipSession;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|RETURN|#]\n" + "[#|2008-10-15T13:29:04.541+0200|FINEST|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=com.ericsson.ssa.sip.SipSessionDialogImpl;MethodName=doCleanup;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|The sip session with ID=c867cdeaaf6ba5d749259b391b884a53ebd7##130.100.224.174 has been removed from the active cache.|#]\n" + "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipApplicationSession;SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|ENTRY SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0|#]" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|#]\n" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|\n" + "#]\n";

        LogPanel mainPanel = new LogPanel(new ToolState(), new StatusView() {
                @Override
                public void resetStatus() {
                }

                @Override
                public void setProgressPercentage(int percentage) {
                }

                @Override
                public void setStatusText(String statusText) {
                }
            });
        mainPanel.setLogData(new LogParser("loggen", new LineNumberReader(new StringReader(testLog))).parse(), null);
        f.getContentPane().add(mainPanel);
        f.pack();
        f.setVisible(true);
    }
}
