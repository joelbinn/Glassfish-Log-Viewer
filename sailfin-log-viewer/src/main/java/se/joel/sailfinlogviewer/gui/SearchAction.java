package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;


public class SearchAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private SearchHandler searchHandler;

    public SearchAction(SearchHandler searchHandler) {
        super("Search again");
        this.searchHandler = searchHandler;
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        searchHandler.jumpToNextMatching();
    }
}
