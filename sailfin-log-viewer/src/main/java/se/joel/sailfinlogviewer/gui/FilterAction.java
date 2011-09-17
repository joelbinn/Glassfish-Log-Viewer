package se.joel.sailfinlogviewer.gui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class FilterAction extends AbstractAction {
    private static final long serialVersionUID = 1L;
    private FilterHandler filterHandler;

    public FilterAction(FilterHandler filterHandler) {
        super("Filter");
        this.filterHandler = filterHandler;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        filterHandler.setFilter();
    }
}
