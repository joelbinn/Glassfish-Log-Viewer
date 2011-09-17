package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class ReloadAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private LoadHandler loadHandler;

    public ReloadAction(LoadHandler loadHandler) {
        super("Reload file");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        this.loadHandler = loadHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        loadHandler.reloadLastFile();
    }
}
