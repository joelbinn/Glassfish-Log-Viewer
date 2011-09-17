package se.joel.sailfinlogviewer;

import se.joel.sailfinlogviewer.gui.LogViewerMainFrame;
import se.joel.sailfinlogviewer.util.ToolState;


public class LogViewerApp {
    public static void main(String[] args) {
        ToolState toolState = new ToolState();
        LogViewerMainFrame mainFrame = new LogViewerMainFrame(toolState);
        mainFrame.setVisible(true);
    }
}
