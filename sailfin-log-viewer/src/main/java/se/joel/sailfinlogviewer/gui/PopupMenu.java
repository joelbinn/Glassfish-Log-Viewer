package se.joel.sailfinlogviewer.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;

public class PopupMenu extends JPopupMenu {
    private static final long serialVersionUID = 1L;
    private ToggleTagAction toggleTagAction;
    private FilterAction filterAction;
    private SearchAction searchAction;
    private GotoNextTagAction nextTagAction;
    private GotoPrevTagAction prevTagAction;
    private ClearTagsAction clearTagsAction;

    public PopupMenu(String label, Component parent, TagHandler tagHandler, FilterHandler filterHandler, SearchHandler searchHandler) {
        super(label);
        filterAction = new FilterAction(filterHandler);
        add(filterAction);
        searchAction = new SearchAction(searchHandler);
        add(searchAction);
        toggleTagAction = new ToggleTagAction(tagHandler);
        add(toggleTagAction);
        nextTagAction = new GotoNextTagAction(tagHandler);
        add(nextTagAction);
        prevTagAction = new GotoPrevTagAction(tagHandler);
        add(prevTagAction);
        clearTagsAction = new ClearTagsAction(tagHandler);
        add(clearTagsAction);
        MouseListener popupListener = new PopupListener(this);
        parent.addMouseListener(popupListener);
    }

    private static class PopupListener extends MouseAdapter {
        private JPopupMenu popup;

        public PopupListener(JPopupMenu popup) {
            this.popup = popup;
            
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
