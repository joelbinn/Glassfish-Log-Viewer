package se.joel.sailfinlogviewer.gui;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import se.joel.sailfinlogviewer.parser.CompositeLog;
import se.joel.sailfinlogviewer.parser.Log;
import se.joel.sailfinlogviewer.parser.LogParser;
import se.joel.sailfinlogviewer.parser.LogParser.LogParserException;
import se.joel.sailfinlogviewer.util.ToolState;


public class LogViewerMainFrame extends JFrame implements LoadHandler{
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger("GUI");
    private LogViewerMainPanel mainView;
    private ToolState toolState;
    private StatusView statusView;
    private File loadedFile;

    public LogViewerMainFrame(ToolState toolState) throws HeadlessException {
        this.toolState = toolState;
        mainView = new LogViewerMainPanel(toolState);
        getContentPane().add(mainView, BorderLayout.CENTER);
        statusView = mainView;

        JMenuBar menuBar = createMenuBar();
        getContentPane().add(menuBar, BorderLayout.NORTH);

        addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        LogViewerMainFrame.this.toolState.savePosAndSize(LogViewerMainFrame.class.toString(), LogViewerMainFrame.this);
                    } catch (FileNotFoundException e1) {
                        // Not so important, ignore it
                        logger.log(Level.WARNING, "Failed to save position and size", e1);
                    } catch (IOException e1) {
                        // Not so important, ignore it
                        logger.log(Level.WARNING, "Failed to save position and size", e1);
                    }
                    System.exit(0);
                }
            });
        if (!toolState.restorePosAndSize(this.getClass().toString(), this)) {
            pack();
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem openFileItem = new JMenuItem("Open file...");
        fileMenu.add(openFileItem);
        openFileItem.addActionListener(new OpenFileAction());
        
        JMenuItem reloadFileItem = new JMenuItem(new ReloadAction(this));
        fileMenu.add(reloadFileItem);

        JMenu toolMenu = new JMenu("Tools");
        menuBar.add(toolMenu);

        JMenuItem searchAgainFileItem = new JMenuItem(new SearchAction(mainView));
        toolMenu.add(searchAgainFileItem);

        JMenuItem toggleTagItem = new JMenuItem(new ToggleTagAction(mainView));
        toolMenu.add(toggleTagItem);

        JMenuItem toggleShowOnlyTags = new JMenuItem(new ToggleShowOnlyTagsAction(mainView));
        toolMenu.add(toggleShowOnlyTags);

        JMenuItem nextTagItem = new JMenuItem(new GotoNextTagAction(mainView));
        toolMenu.add(nextTagItem);

        JMenuItem prevTagItem = new JMenuItem(new GotoPrevTagAction(mainView));
        toolMenu.add(prevTagItem);

        JMenuItem clearTagsItem = new JMenuItem(new ClearTagsAction(mainView));
        toolMenu.add(clearTagsItem);
        return menuBar;
    }
    
    private final class OpenFileAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            File lastSelectedFile = toolState.getLastSelectedFile();

            if (lastSelectedFile != null) {
                fc.setSelectedFile(lastSelectedFile);
            }

            int returnVal = fc.showOpenDialog(LogViewerMainFrame.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                loadedFile = fc.getSelectedFile();

                loadFile();
            }
        }
    }

    private class ProgressReportingReader extends Reader {

        private int nRead;
        private Reader wrappedReader;
        private double size;

        public ProgressReportingReader(Reader wrappedReader, long size) {
            this.wrappedReader = wrappedReader;
            this.size = size;
        }

        @Override
        public int read() throws IOException {
            nRead++;
            updateProgress();
            return wrappedReader.read();
        }

        private void updateProgress() {
            statusView.setProgressPercentage((int)Math.ceil(100*nRead/size));
        }

        @Override
        public int read(char[] cbuf, int offset, int length) throws IOException {
            int n = wrappedReader.read(cbuf, offset, length);
            if (n >= 0) {
                nRead += n;
            } 
            updateProgress();
            return n;
        }

        @Override
        public void close() throws IOException {
            wrappedReader.close();
        }
    }

    @Override
    public void reloadLastFile() {
        if (loadedFile != null) {
            loadFile();
        } else {
            File lastSelectedFile = toolState.getLastSelectedFile();
            if (lastSelectedFile != null) {
                loadedFile = lastSelectedFile;
                loadFile();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFile() {
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                LineNumberReader reader = null;
                try {
                    List<File> logFiles = new ArrayList<File>();
                    if (loadedFile.isDirectory()) {
                        getLogFiles(loadedFile, logFiles);
                    } else {
                        logFiles.add(loadedFile);
                    }

                    CompositeLog compositeLog = new CompositeLog();
                    for (File logFile : logFiles) {
                        String instanceName = logFile.getParentFile().getParentFile().getName();
                        reader = new LineNumberReader(new ProgressReportingReader(new FileReader(logFile), logFile.length()));
                        LogParser logParser = new LogParser(instanceName, reader);
                        statusView.setStatusText("Parsing "+logFile.getAbsolutePath());
                        mainView.resetView();
                        Log log = logParser.parse();
                        compositeLog.addLog(log);
                    }
                    statusView.resetStatus();
                    mainView.setLog(compositeLog);
                    toolState.setLastSelectedFile(loadedFile);
                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog(LogViewerMainFrame.this, "Could not open file: " + loadedFile + ": " + e1);
                } catch (LogParserException e2) {
                    JOptionPane.showMessageDialog(LogViewerMainFrame.this, "Could not parse file: " + loadedFile + ": " + e2);
                } catch (IOException e3) {
                    JOptionPane.showMessageDialog(LogViewerMainFrame.this, "Could not parse file: " + loadedFile + ": " + e3);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e1) {
                            // Ignore
                        }
                    }
                }
                return null;
            }

            private void getLogFiles(File file, List<File> logFiles) {
                for (File childFile : file.listFiles()) {
                    if (childFile.isDirectory()) {
                        getLogFiles(childFile, logFiles);
                    } else if (childFile.isFile() && childFile.getName().startsWith("server.log")) {
                        logFiles.add(childFile);
                    }
                }
            }
        }.execute();
    }
}
