package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import se.joel.sailfinlogviewer.parser.Log.FilterException;


public class ToggleShowOnlyTagsAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger("GUI");
    private TagHandler tagHandler;

    public ToggleShowOnlyTagsAction(TagHandler tagHandler) {
        super("Toggle show only tagged records");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_DOWN_MASK));
        this.tagHandler = tagHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            tagHandler.toggleShowOnlyTaggedRecords();
        } catch (FilterException e1) {
            // Ignore
            logger.log(Level.WARNING, "Could nat set filter", e1);
        }
    }
}
