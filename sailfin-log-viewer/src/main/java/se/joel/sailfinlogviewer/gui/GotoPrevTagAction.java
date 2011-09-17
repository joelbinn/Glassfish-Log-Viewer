package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class GotoPrevTagAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private TagHandler tagHandler;

    public GotoPrevTagAction(TagHandler tagHandler) {
        super("Goto previous tag");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        this.tagHandler = tagHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        tagHandler.gotoPrevTag();
    }
}
