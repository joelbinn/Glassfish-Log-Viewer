package se.joel.sailfinlogviewer.util;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.SwingUtilities;

public class ThreadUtil {

    public static void doLater(long delay, final Runnable runnable) {
        Timer t = new Timer();
        t.schedule(new TimerTask(){
            @Override
            public void run() {
                SwingUtilities.invokeLater(runnable);
            }
        }, delay);
    }
}
