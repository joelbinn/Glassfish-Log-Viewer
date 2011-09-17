package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class GotoNextTagAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private TagHandler tagHandler;

    public GotoNextTagAction(TagHandler tagHandler) {
        super("Goto next tag");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        this.tagHandler = tagHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tagHandler.gotoNextTag();
    }
}
