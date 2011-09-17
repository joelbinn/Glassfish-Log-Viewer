/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * Copyright (c) Ericsson AB, 2004-2008. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package se.joel.sailfinlogviewer.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import se.joel.sailfinlogviewer.parser.LogRecord;


public class LogRecordList extends JList {
    private static final long serialVersionUID = 1L;
    private JScrollPane listScrollPane;

    public LogRecordList() {
        super(new LogRecordListModel());
        listScrollPane = new JScrollPane(this);
        listScrollPane.setPreferredSize(new Dimension(400, 200));
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setLayoutOrientation(JList.VERTICAL);
        setCellRenderer(new LogRecordListCellRenderer());
    }

    /**
     * @return the listScrollPane
     */
    public JScrollPane getScrollPane() {
        return listScrollPane;
    }

    public void addLogRecord(LogRecord logRecord) {
        ((LogRecordListModel) getModel()).addElement(logRecord);
    }

    public LogRecord getLogRecordAt(int index) {
        if ((index >= 0) && (index < ((LogRecordListModel) getModel()).getSize())) {
            return (LogRecord) ((LogRecordListModel) getModel()).getElementAt(index);
        } else {
            return null;
        }
    }

    public void clear() {
        ((LogRecordListModel) getModel()).clear();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });

        LogRecordList list = new LogRecordList();
        list.addLogRecord(new LogRecord(1, "instance1", "[#|2008-10-15T13:29:04.541+0200|FINER|sun-glassfish-comms-server1.0|javax.enterprise.system.container.ssr|_ThreadID=39;_ThreadName=SipContainer-clientsWorkerThread-5060-6;ClassName=SipTransactionPersistentManager;MethodName=removeSipSession;_RequestID=e4486be0-9e75-43ed-a44f-73a856e27a8e;|RETURN|#]\n"));
        f.getContentPane().add(list);
        f.pack();
        f.setVisible(true);
    }

    public Iterable<LogRecord> getLogRecords() {
        Enumeration<?> elements = ((LogRecordListModel) getModel()).elements();
        List<LogRecord> records = new ArrayList<LogRecord>();

        while (elements.hasMoreElements()) {
            records.add((LogRecord) elements.nextElement());
        }

        return records;
    }

    private static class LogRecordListCellRenderer implements ListCellRenderer {
        private static final Color LIGHT_BLUE = new Color(186, 229, 255);

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            LogRecord lr = (LogRecord) value;
            JLabel cell = new JLabel(lr.getIndex() + ": " + lr.getTime() + ": " + lr.getMessage());
            Font font = new Font("Sans-serif", Font.PLAIN, 12);
            cell.setFont(font);
            cell.setOpaque(true);

            if (isSelected) {
                cell.setBackground(Color.LIGHT_GRAY);
            } else if ((index % 2) == 0) {
                cell.setBackground(LIGHT_BLUE);
            } else {
                cell.setBackground(Color.WHITE);
            }

            return cell;
        }
    }

    private static class LogRecordListModel extends DefaultListModel {
        private static final long serialVersionUID = 1L;
    }
}
