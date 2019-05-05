package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.explorer.score.ScoreProject;
import com.google.appinventor.client.explorer.score.ScoreProjectManager;
import com.google.appinventor.client.explorer.score.ScoreProjectManagerEventListener;
import com.google.appinventor.client.explorer.youngandroid.ScoreProjectToolbar;
import com.google.appinventor.client.wizards.TemplateUploadWizard;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OdeAdmin extends Ode {

    //Just something changed in Admin page
    private ScoreProjectManager scoreProjectManager;

    private ScoreProjectToolbar scoreProjectToolbar;

    /**
     * Returns global instance of OdeAdmin.
     *
     * @return  global OdeAdmin instance
     */
    public static OdeAdmin getInstance() {
        return (OdeAdmin)Ode.getInstance();
    }

    public ScoreProjectManager getScoreProjectManager() { return scoreProjectManager; }

    public ScoreProjectToolbar getScoreProjectToolbar() {
        return scoreProjectToolbar;
    }

    public void onModuleLoad() {
        onAdminModuleLoad(this);
    }

    protected void loadAdminModule() {
        scoreProjectManager = new ScoreProjectManager();
    }


    @Override
    protected void initializeUi() {
        super.initializeUi();

        PaletteBox.getPaletteBox().setHide(true);

        HorizontalPanel adminPanel = new HorizontalPanel();

        // Projects tab
        VerticalPanel pVertPanel = new VerticalPanel();
        pVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
        scoreProjectToolbar = new ScoreProjectToolbar();
        pVertPanel.add(scoreProjectToolbar);
        pVertPanel.add(ScoreProjectListBox.getScoreProjectListBox());
        adminPanel.add(pVertPanel);
        adminPanel.setCellWidth(pVertPanel, "36%");

        VerticalPanel spacePanel = new VerticalPanel();
        spacePanel.setSpacing(0);
        adminPanel.add(spacePanel);
        adminPanel.setCellWidth(spacePanel, "1%");

        // Design tab
        VerticalPanel dVertPanel = new VerticalPanel();
        dVertPanel.setWidth("100%");
        dVertPanel.setSpacing(0);
        dVertPanel.add(Ode.getInstance().getDesignToolbar());
        dVertPanel.add(Ode.getInstance().getWorkColumns());
        adminPanel.add(dVertPanel);
        adminPanel.setCellWidth(dVertPanel, "63%");

        setProjectView(adminPanel);
        switchToProjectsView();
    }
}
