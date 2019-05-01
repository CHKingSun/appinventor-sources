//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.AdminProjectListBox;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.user.client.Command;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class AdminProjectToolbar extends Toolbar {
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_IMPORT_TO_ME = "Import to me";

    /**
     * Initializes and assembles all commands into buttons in the toolbar.
     */
    public AdminProjectToolbar() {
        super();
        getWidget().setStylePrimaryName("ya-ProjectToolbar");

        addButton(new ToolbarItem(WIDGET_NAME_SELECT_ALL, MESSAGES.selectAllButton(),
                new SelectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DESELECT_ALL, MESSAGES.deselectAllButton(),
                new DeselectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_IMPORT_TO_ME, MESSAGES.importToMeButton(),
                new ImportToMeAction()));

        updateButtons();
    }

    private static class SelectAllAction implements Command {

        @Override
        public void execute() {
            AdminProjectListBox.getAdminProjectListBox().getAdminProjectList().setlectAllProjects();
        }
    }

    private static class DeselectAllAction implements Command {

        @Override
        public void execute() {
            AdminProjectListBox.getAdminProjectListBox().getAdminProjectList().DesetlectAllProjects();
        }
    }

    private static class ImportToMeAction implements Command {

        @Override
        public void execute() {

        }
    }

    /**
     * Enables and/or disables buttons based on how many projects exist
     * (in the case of "Download All Projects") or are selected (in the case
     * of "Delete" and "Download Source").
     */
    public void updateButtons() {
        AdminProjectList projectList = AdminProjectListBox.getAdminProjectListBox().getAdminProjectList();
        int numSelectedProjects = projectList.getNumSelectedProjects();
        setButtonEnabled(WIDGET_NAME_IMPORT_TO_ME, numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
                numSelectedProjects > 0);
    }

}
