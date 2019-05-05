//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.score.ScoreProject;
import com.google.appinventor.client.utils.UploadToServerDialog;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.user.client.Command;

import java.util.ArrayList;
import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ScoreProjectToolbar extends Toolbar {
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_UPLOAD_TO_SERVER = "Upload To Server";

    /**
     * Initializes and assembles all commands into buttons in the toolbar.
     */
    public ScoreProjectToolbar() {
        super();
        getWidget().setStylePrimaryName("ya-ProjectToolbar");

        addButton(new ToolbarItem(WIDGET_NAME_SELECT_ALL, MESSAGES.selectAllButton(),
                new SelectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DESELECT_ALL, MESSAGES.deselectAllButton(),
                new DeselectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_UPLOAD_TO_SERVER, MESSAGES.uploadToServerButton(),
                new UploadToServerAction()));

        updateButtons();
    }

    private static class SelectAllAction implements Command {

        @Override
        public void execute() {
            ScoreProjectListBox.getScoreProjectListBox().getScoreProjectList().selectAllProjects();
        }
    }

    private static class DeselectAllAction implements Command {

        @Override
        public void execute() {
            ScoreProjectListBox.getScoreProjectListBox().getScoreProjectList().deselectAllProjects();
        }
    }

    private class UploadToServerAction implements Command {

        @Override
        public void execute() {
            List<ScoreProject> scoreProjects = ScoreProjectListBox.getScoreProjectListBox()
                    .getScoreProjectList().getSelectedProjects();
            List<Project> projects = new ArrayList<>(scoreProjects.size());
            for (ScoreProject scoreProject : scoreProjects) {
                projects.add(scoreProject.getProject());
            }
            new UploadToServerDialog(projects).show();
        }
    }

    /**
     * Enables and/or disables buttons based on how many projects exist
     * (in the case of "Download All Projects") or are selected (in the case
     * of "Delete" and "Download Source").
     */
    public void updateButtons() {
        ScoreProjectList projectList = ScoreProjectListBox.getScoreProjectListBox().getScoreProjectList();
        int numSelectedProjects = projectList.getNumSelectedProjects();
        setButtonEnabled(WIDGET_NAME_UPLOAD_TO_SERVER, numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
                numSelectedProjects > 0);
    }

}
