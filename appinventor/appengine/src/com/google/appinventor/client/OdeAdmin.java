package com.google.appinventor.client;

import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.AdminProjectToolbar;
import com.google.appinventor.client.explorer.youngandroid.ProjectList;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OdeAdmin extends Ode {

    //Just something changed in Admin page
    private AdminProjectToolbar projectToolbar;

    private HorizontalPanel adminPanel;

    public void onModuleLoad() {
        onAdminModuleLoad(this);
    }

    @Override
    protected void initializeUi() {
        super.initializeUi();

        ProjectListBox.getProjectListBox().getProjectList().setDateCreatedVisible(false);
        ProjectListBox.getProjectListBox().getProjectList().setDateModifiedVisible(false);
        PaletteBox.getPaletteBox().setHide(true);

        adminPanel = new HorizontalPanel();

        // Projects tab
        VerticalPanel pVertPanel = new VerticalPanel();
        pVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
        projectToolbar = new AdminProjectToolbar();
        pVertPanel.add(projectToolbar);
        pVertPanel.add(ProjectListBox.getProjectListBox());
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
    public void openYoungAndroidProjectInDesigner(Project project) {
        super.openYoungAndroidProjectInDesigner(project);
    }

    @Override
    public void updateProjectToolbarButtons() {
        projectToolbar.updateButtons();
    }
}
