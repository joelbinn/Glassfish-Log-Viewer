package se.joel.sailfinlogviewer.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.InvalidPropertiesFormatException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import se.joel.sailfinlogviewer.parser.Log;
import se.joel.sailfinlogviewer.parser.LogParser;
import se.joel.sailfinlogviewer.parser.Log.FilterException;
import se.joel.sailfinlogviewer.parser.LogParser.LogParserException;
import se.joel.sailfinlogviewer.util.ThreadUtil;
import se.joel.sailfinlogviewer.util.ToolState;


public class LogViewerMainPanel extends JPanel implements FilterHandler, SearchHandler, TagHandler, StatusView, SearchResultStatusView {
    private static final String SEARCH_HISTORY = "SearchHistory";
    private static final String FILTER_HISTORY = "FilterHistory";
    private static final String NONE = "<None>";
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger("GUI");
    private LogPanel logPanel;
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JPanel searchAndFilterPanel;
    private JComboBox filterCombo;
    private Vector<String> filterHistory = new Vector<String>();
    private JComboBox searchCombo;
    private JProgressBar progressBar;
    private Vector<String> searchHistory = new Vector<String>();
    private ToolState toolState;
    private JRadioButton forwardSearchRadioButton;

    public LogViewerMainPanel(ToolState toolState) {
        this.toolState = toolState;
        setLayout(new BorderLayout());
        add(getSearchAndFilterPanel(), BorderLayout.NORTH);
        add(getLogPanel(), BorderLayout.CENTER);
        add(getStatusPanel(), BorderLayout.SOUTH);
        restoreToolState();
        new PopupMenu(null, this, this, this, this);
    }

    @Override
    public void setFilter() {
        String selectedItem = ((String) filterCombo.getSelectedItem()).trim();

        try {
            logPanel.setFilter((selectedItem == NONE) ? null : selectedItem);
            filterCombo.addItem(selectedItem);
            filterCombo.setBackground(Color.WHITE);
        } catch (FilterException e1) {
            filterCombo.setBackground(Color.RED);
        }
    }

    @Override
    public void jumpToNextMatching() {
        Object item = searchCombo.getSelectedItem();
        final String selectedItem;

        if (item == null) {
            selectedItem = NONE;
        } else {
            selectedItem = ((String) item).trim();
        }

        try {
            logPanel.jumpToNextMatching((selectedItem == NONE) ? null : selectedItem, forwardSearchRadioButton.isSelected(), this);
            searchCombo.addItem(selectedItem);
            searchCombo.setBackground(Color.WHITE);
        } catch (FilterException e1) {
            markSearchError("Error in expression");
        }
    }

    private void markSearchError(String msg) {
        searchCombo.setBackground(Color.RED);
        setStatusText(msg);
        ThreadUtil.doLater(5000, new Runnable(){
            @Override
            public void run() {
                resetStatus();
            }
        });
    }

    @Override
    public void toggleRecordTag() {
        logPanel.toggleRecordTag();
    }

    @Override
    public void clearRecordTags() {
        logPanel.clearRecordTags();
    }

    @Override
    public void gotoNextTag() {
        logPanel.gotoNextTag();
    }

    @Override
    public void gotoPrevTag() {
        logPanel.gotoPrevTag();
    }

    @Override
    public void toggleShowOnlyTaggedRecords() throws FilterException {
        logPanel.toggleShowOnlyTaggedRecords();
    }

    private void restoreToolState() {
        try {
            addUnique(toolState.getList(FILTER_HISTORY), filterHistory);
            addUnique(toolState.getList(SEARCH_HISTORY), searchHistory);
        } catch (InvalidPropertiesFormatException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to read history", e);
        } catch (IOException e) {
            // Not so important, ignore it
            logger.log(Level.WARNING, "Failed to read history", e);
        }
    }

    private void addUnique(Collection<?> list, Vector<String> vector) {
        for (Object listItem : list) {
            if (!vector.contains(listItem)) {
                vector.add(listItem + "");
            }
        }
    }

    private void saveToolState(ContentType contentType, Object anObject) {
        try {
            switch (contentType) {
            case FILTER:
                toolState.saveList(FILTER_HISTORY, filterHistory);

                break;

            case SEARCH:
                toolState.saveList(SEARCH_HISTORY, searchHistory);

                break;

            default:
                break;
            }
        } catch (FileNotFoundException e) {
            // Not so important, ignore it
            e.printStackTrace();
        } catch (IOException e) {
            // Not so important, ignore it
            e.printStackTrace();
        }
    }

    private LogPanel getLogPanel() {
        if (logPanel == null) {
            logPanel = new LogPanel(toolState, this);
        }

        return logPanel;
    }

    private JPanel getStatusPanel() {
        if (statusPanel == null) {
            statusPanel = new JPanel();
            ((FlowLayout) statusPanel.getLayout()).setAlignment(FlowLayout.LEFT);
            progressBar = new JProgressBar();
            progressBar.setMaximum(100);
            statusPanel.add(progressBar);
            statusLabel = new JLabel("--");
            statusPanel.add(statusLabel);
        }

        return statusPanel;
    }

    private JPanel getSearchAndFilterPanel() {
        if (searchAndFilterPanel == null) {
            searchAndFilterPanel = new JPanel();
            searchAndFilterPanel.setBorder(BorderFactory.createEtchedBorder());
            searchAndFilterPanel.setLayout(new GridBagLayout());

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 2, 2, 2);

            JLabel filterLabel = new JLabel("Filter:");
            filterHistory.add(NONE);
            filterCombo = new JComboBox(new FilterComboBoxModel(filterHistory, ContentType.FILTER));
            filterCombo.setEditable(true);
            filterCombo.setOpaque(true);
            filterCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox) e.getSource();
                        String selectedItem = ((String) cb.getSelectedItem()).trim();

                        try {
                            logPanel.setFilter((selectedItem == NONE) ? null : selectedItem);
                            filterCombo.addItem(selectedItem);
                            filterCombo.setBackground(Color.WHITE);
                        } catch (FilterException e1) {
                            filterCombo.setBackground(Color.RED);
                        }
                    }
                });
            searchAndFilterPanel.add(filterLabel, gbc);
            gbc.gridx = 1;
            gbc.gridwidth = 1;
            gbc.weightx = 1;
            searchAndFilterPanel.add(filterCombo, gbc);

            JLabel searchLabel = new JLabel("Search:");
            searchCombo = new JComboBox(new FilterComboBoxModel(searchHistory, ContentType.SEARCH));
            searchCombo.setEditable(true);
            searchCombo.setOpaque(true);
            gbc.weightx = 0;
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            searchAndFilterPanel.add(searchLabel, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.gridwidth = 1;
            searchAndFilterPanel.add(searchCombo, gbc);

            forwardSearchRadioButton = new JRadioButton("Forward");
            forwardSearchRadioButton.setSelected(true);

            JRadioButton backwardSearchRadioButton = new JRadioButton("Backward");
            ButtonGroup buttonGroup = new ButtonGroup();
            buttonGroup.add(forwardSearchRadioButton);
            buttonGroup.add(backwardSearchRadioButton);
            gbc.weightx = 0;
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;

            JPanel checkBoxPanel = new JPanel();
            checkBoxPanel.add(forwardSearchRadioButton);
            checkBoxPanel.add(backwardSearchRadioButton);
            searchAndFilterPanel.add(checkBoxPanel, gbc);

            searchCombo.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        JComboBox cb = (JComboBox) e.getSource();
                        final String selectedItem = ((String) cb.getSelectedItem()).trim();

                        try {
                            logPanel.jumpToNextMatching((selectedItem == NONE) ? null : selectedItem, forwardSearchRadioButton.isSelected(), LogViewerMainPanel.this);
                            searchCombo.addItem(selectedItem);
                            searchCombo.setBackground(Color.WHITE);
                        } catch (FilterException e1) {
                            markSearchError("Error in expression");                            
                        }
                    }
                });
        }

        return searchAndFilterPanel;
    }

    public void setLog(Log log) {
        try {
            String selectedItem = ((String) filterCombo.getSelectedItem()).trim();
            logPanel.setLogData(log, (selectedItem == NONE) ? null : selectedItem);
            filterCombo.setBackground(Color.WHITE);
        } catch (FilterException e) {
            filterCombo.setBackground(Color.RED);

            try {
                logPanel.setLogData(log, null);
            } catch (FilterException e1) {
                // Can not happen now!
            }
        }
    }

    @Override
    public void setProgressPercentage(final int percentage) {
        if (EventQueue.isDispatchThread()) {
            progressBar.setValue(percentage);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setProgressPercentage(percentage);
                    }
                });
        }
    }

    @Override
    public void setStatusText(final String statusText) {
        if (EventQueue.isDispatchThread()) {
            statusLabel.setText(statusText);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        setStatusText(statusText);
                    }
                });
        }
    }

    @Override
    public void resetStatus() {
        if (EventQueue.isDispatchThread()) {
            progressBar.setValue(0);
            statusLabel.setText("--");
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        resetStatus();
                    }
                });
        }
    }

    public void resetView() {
        logPanel.resetView();
    }

    @Override
    public void setFound(boolean found) {
        if (!found) {
            markSearchError("Not found");
        } else {
            searchCombo.setBackground(Color.WHITE);
            resetStatus();
        }
    }

    public static void main(String[] args) throws LogParserException {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        String testLog = "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipSession;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|RETURN|#]\n" + "[#|2008-10-15T13:29:04.541+0200|FINEST|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=com.ericsson.ssa.sip.SipSessionDialogImpl;MethodName=doCleanup;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|The sip session with ID=c867cdeaaf6ba5d749259b391b884a53ebd7##130.100.224.174 has been removed from the active cache.|#]\n" + "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipApplicationSession;SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|ENTRY SipApplicationSession with id SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout:7,43,TMb4_10c867b08d7ddf862a414b984db7fe9db8e6ba/s4_10_subscriptionTimeout:version:0|#]" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|#]\n" + "[#|2008-10-15T13:29:04.542+0200|FINE|sun-glassfish-comms-server1.0|javax.enterprise.system.container.sip|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=org.jvnet.glassfish.comms.replication.sessmgmt.SipApplicationSessionStoreImpl;MethodName=remove;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|SipApplicationSessionStoreImpl>>remove: replicator: _mode = sip\n" + "_appid = SIP:cluster1:com.sun.appserv:server:/s4_10_subscriptionTimeout\n" + "_idleTimeoutInSeconds = 0|\n" + "#]\n";

        LogViewerMainPanel mainPanel = new LogViewerMainPanel(new ToolState());
        mainPanel.setLog(new LogParser("Kalle", new LineNumberReader(new StringReader(testLog))).parse());
        f.getContentPane().add(mainPanel);
        f.pack();
        f.setVisible(true);
    }

    private class FilterComboBoxModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = 1L;
        private Vector<?> v;
        private ContentType contentType;

        public FilterComboBoxModel(Vector<?> v, ContentType contentType) {
            super(v);
            this.contentType = contentType;
            this.v = v;
        }

        @Override
        public void addElement(Object anObject) {
            if (!v.contains(anObject) && (((String) anObject).length() != 0)) {
                super.addElement(anObject);
                saveToolState(contentType, v);
            }
        }

        @Override
        public void insertElementAt(Object anObject, int index) {
            if (!v.contains(anObject) && (((String) anObject).length() != 0)) {
                super.insertElementAt(anObject, index);
                saveToolState(contentType, v);
            }
        }
    }
    public enum ContentType {FILTER, SEARCH;
    }
}
