package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.explorer.youngandroid.ScoreProjectToolbar;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OdeAdmin extends Ode {

    //Just something changed in Admin page
    private ScoreProjectToolbar projectToolbar;

    /**
     * Returns global instance of OdeAdmin.
     *
     * @return  global OdeAdmin instance
     */
    public static OdeAdmin getInstance() {
        return (OdeAdmin)Ode.getInstance();
    }

    public void onModuleLoad() {
        onAdminModuleLoad(this);
    }

    @Override
    protected void initializeUi() {
        super.initializeUi();

        PaletteBox.getPaletteBox().setHide(true);
        ScoreProjectListBox.getAdminProjectListBox().getAdminProjectList().setScoreHeaderVisible(false);

        HorizontalPanel adminPanel = new HorizontalPanel();

        // Projects tab
        VerticalPanel pVertPanel = new VerticalPanel();
        pVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
        projectToolbar = new ScoreProjectToolbar();
        pVertPanel.add(projectToolbar);
        pVertPanel.add(ScoreProjectListBox.getAdminProjectListBox());
        adminPanel.add(pVertPanel);
        adminPanel.setCellWidth(pVertPanel, "24%");

        // Design tab
        VerticalPanel dVertPanel = new VerticalPanel();
        dVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
//        dVertPanel.setHeight("100%");
        dVertPanel.add(Ode.getInstance().getDesignToolbar());
        dVertPanel.add(Ode.getInstance().getWorkColumns());
        adminPanel.add(dVertPanel);
        adminPanel.setCellWidth(dVertPanel, "76%");

        setProjectView(adminPanel);
        switchToProjectsView();
    }

    @Override
    public void updateProjectToolbarButtons() {
        projectToolbar.updateButtons();
    }
}
