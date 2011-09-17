package se.joel.sailfinlogviewer.gui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import se.joel.sailfinlogviewer.parser.LogRecord;
import se.joel.sailfinlogviewer.parser.PositionInfo;


public class LogRecordPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Font font = new Font("Sans-serif", Font.PLAIN, 12);
    private LogRecord selectedLogRecord;
    private JTextArea messageTextArea;
    private JLabel timeLabel;
    private JTextField timeValueLabel;
    private JLabel serverTypeLabel;
    private JTextField serverTypeValueLabel;
    private JLabel logDomainLabel;
    private JTextField logDomainValueLabel;
    private JLabel logLevelLabel;
    private JTextField logLevelValueLabel;
    private JLabel threadLabel;
    private JTextField threadValueLabel;
    private JLabel methodLabel;
    private JTextField methodValueLabel;
    private JLabel paramLabel;
    private JTextArea paramValueTextArea;
    private JLabel rawLineLabel;
    private JTextArea rawLineValueTextArea;
    private JLabel requestIdLabel;
    private JTextField requestIdValueLabel;
    private JCheckBox wrapCheckBox;

    public LogRecordPanel() {
        setLayout(new GridBagLayout());
        init();
    }

    private void init() {
        setBorder(BorderFactory.createEtchedBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        timeLabel = new JLabel("Time:");
        timeLabel.setFont(font);
        add(timeLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        timeValueLabel = new JTextField("-");
        timeValueLabel.setEditable(false);
        timeValueLabel.setFont(font);
        add(timeValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 1;
        serverTypeLabel = new JLabel("Server type:");
        serverTypeLabel.setFont(font);
        add(serverTypeLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        serverTypeValueLabel = new JTextField("-");
        serverTypeValueLabel.setEditable(false);
        serverTypeValueLabel.setFont(font);
        add(serverTypeValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 2;
        logDomainLabel = new JLabel("Log domain:");
        logDomainLabel.setFont(font);
        add(logDomainLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        logDomainValueLabel = new JTextField("-");
        logDomainValueLabel.setEditable(false);
        logDomainValueLabel.setFont(font);
        add(logDomainValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 3;
        logLevelLabel = new JLabel("Log level:");
        logLevelLabel.setFont(font);
        add(logLevelLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        logLevelValueLabel = new JTextField("-");
        logLevelValueLabel.setEditable(false);
        logLevelValueLabel.setFont(font);
        add(logLevelValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 4;
        threadLabel = new JLabel("Thread:");
        threadLabel.setFont(font);
        add(threadLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        threadValueLabel = new JTextField("-");
        threadValueLabel.setEditable(false);
        threadValueLabel.setFont(font);
        add(threadValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 5;
        methodLabel = new JLabel("Method:");
        methodLabel.setFont(font);
        add(methodLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        methodValueLabel = new JTextField("-");
        methodValueLabel.setEditable(false);
        methodValueLabel.setFont(font);
        add(methodValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 6;
        paramLabel = new JLabel("Parameters:");
        paramLabel.setFont(font);
        add(paramLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        paramValueTextArea = new JTextArea("-");
        paramValueTextArea.setRows(3);
        paramValueTextArea.setEditable(false);
        paramValueTextArea.setFont(font);
        JScrollPane paramValueScrollPane = new JScrollPane(paramValueTextArea);
        paramValueScrollPane.setMinimumSize(new Dimension(100, 40));
        add(paramValueScrollPane, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 7;
        requestIdLabel = new JLabel("Request-ID:");
        requestIdLabel.setFont(font);
        add(requestIdLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        requestIdValueLabel = new JTextField("-");
        requestIdValueLabel.setEditable(false);
        requestIdValueLabel.setFont(font);
        add(requestIdValueLabel, gbc);

        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 8;
        rawLineLabel = new JLabel("Raw log line:");
        rawLineLabel.setFont(font);
        add(rawLineLabel, gbc);

        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 1;
        rawLineValueTextArea = new JTextArea("-");
        rawLineValueTextArea.setRows(3);
        rawLineValueTextArea.setEditable(false);
        rawLineValueTextArea.setFont(font);
        JScrollPane rawLineValueScrollPane = new JScrollPane(rawLineValueTextArea);
        rawLineValueScrollPane.setMinimumSize(new Dimension(100, 40));
        add(rawLineValueScrollPane, gbc);
        
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 9;
        wrapCheckBox = new JCheckBox("Wrap lines", false);
        wrapCheckBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    messageTextArea.setLineWrap(wrapCheckBox.isSelected());
                }
            });
        add(wrapCheckBox, gbc);

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.gridheight = GridBagConstraints.REMAINDER;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        messageTextArea = new JTextArea();
        messageTextArea.setRows(2);
        messageTextArea.setEditable(false);
        messageTextArea.setWrapStyleWord(true);

        JScrollPane messageScrollPane = new JScrollPane(messageTextArea);
        messageScrollPane.setMinimumSize(new Dimension(100, 40));
        add(messageScrollPane, gbc);
    }

    public void setLogRecord(LogRecord logRecord) {
        selectedLogRecord = logRecord;
        update();
    }

    public void reset() {
        selectedLogRecord = null;
    }

    private void update() {
        if (selectedLogRecord != null) {
            timeValueLabel.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getTime());
            serverTypeValueLabel.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getServerType());
            logDomainValueLabel.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getLogDomain());
            logLevelValueLabel.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getLogLevel());
            rawLineValueTextArea.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getRawLine());
            rawLineValueTextArea.setCaretPosition(0);
            messageTextArea.setText((selectedLogRecord == null) ? "-" : selectedLogRecord.getMessage());
            messageTextArea.setCaretPosition(0);
            updatePosInfo(selectedLogRecord.getPositionInfo());
        } else {
            timeValueLabel.setText("-");
            serverTypeValueLabel.setText("-");
            logDomainValueLabel.setText("-");
            logLevelValueLabel.setText("-");
            messageTextArea.setText("-");
            paramValueTextArea.setText("-");
            paramValueTextArea.setCaretPosition(0);
            rawLineValueTextArea.setText("-");
            rawLineValueTextArea.setCaretPosition(0);
            updatePosInfo(null);
        }
    }

    private void updatePosInfo(PositionInfo posInfo) {
        if (posInfo != null) {
            threadValueLabel.setText(posInfo.getThreadName() + " (ID=" + posInfo.getThreadId() + ")");
            methodValueLabel.setText(posInfo.getClassName() + "." + posInfo.getMethodName() + "()");
            paramValueTextArea.setText(posInfo.getMiscData());
            paramValueTextArea.setCaretPosition(0);
            requestIdValueLabel.setText(posInfo.getRequestId());
        } else {
            threadValueLabel.setText("-");
            methodValueLabel.setText("-");
            paramValueTextArea.setText("-");
            paramValueTextArea.setCaretPosition(0);
            requestIdValueLabel.setText("-");
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        f.getContentPane().add(new LogRecordPanel());
        f.pack();
        f.setVisible(true);
    }
}
