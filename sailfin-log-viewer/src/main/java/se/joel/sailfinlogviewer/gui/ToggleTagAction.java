package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class ToggleTagAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private TagHandler tagHandler;

    public ToggleTagAction(TagHandler tagHandler) {
        super("Toggle tag on record");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        this.tagHandler = tagHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tagHandler.toggleRecordTag();
    }
}
