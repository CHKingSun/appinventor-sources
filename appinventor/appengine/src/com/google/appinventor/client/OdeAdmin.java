package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.youngandroid.AdminProjectToolbar;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OdeAdmin extends Ode {

    //Just something changed in Admin page
    private AdminProjectToolbar projectToolbar;

    public void onModuleLoad() {
        onAdminModuleLoad(this);
    }

    @Override
    protected void initializeUi() {
        super.initializeUi();

        ProjectListBox.getProjectListBox().getProjectList().setDateCreatedVisible(false);
        ProjectListBox.getProjectListBox().getProjectList().setDateModifiedVisible(false);

        // Projects tab
        VerticalPanel pVertPanel = new VerticalPanel();
        pVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
        HorizontalPanel projectListPanel = new HorizontalPanel();
        projectListPanel.setWidth("100%");
        projectToolbar = new AdminProjectToolbar();
        projectListPanel.add(ProjectListBox.getProjectListBox());
        pVertPanel.add(projectToolbar);
        pVertPanel.add(projectListPanel);
        setProjectView(pVertPanel);

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
