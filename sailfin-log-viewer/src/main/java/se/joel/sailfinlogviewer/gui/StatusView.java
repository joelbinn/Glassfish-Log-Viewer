package se.joel.sailfinlogviewer.gui;

public interface StatusView {
    void setProgressPercentage(int percentage);
    void setStatusText(String statusText);
    void resetStatus();
}
