//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.score.ScoreProject;
import com.google.appinventor.client.explorer.score.ScoreProjectManagerEventListener;
import com.google.appinventor.client.utils.UploadToServerDialog;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ScoreProjectToolbar extends VerticalPanel implements ScoreProjectManagerEventListener {
    private static final String WIDGET_NAME_COURSE = "Course";
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_UPLOAD_TO_SERVER = "Upload To Server";

    private Toolbar toolbar;

    /**
     * Initializes and assembles all commands into buttons in the toolbar.
     */
    public ScoreProjectToolbar() {
        super();
        setStylePrimaryName("ya-ProjectToolbar");
        setWidth("100%");

        toolbar = new Toolbar();
        toolbar.addDropDownButton(WIDGET_NAME_COURSE, MESSAGES.courseButton(),
                new ArrayList<DropDownButton.DropDownItem>());
        toolbar.addButton(new Toolbar.ToolbarItem(WIDGET_NAME_SELECT_ALL, MESSAGES.selectAllButton(),
                new SelectAllAction()));
        toolbar.addButton(new Toolbar.ToolbarItem(WIDGET_NAME_DESELECT_ALL, MESSAGES.deselectAllButton(),
                new DeselectAllAction()));
        toolbar.addButton(new Toolbar.ToolbarItem(WIDGET_NAME_UPLOAD_TO_SERVER, MESSAGES.uploadToServerButton(),
                new UploadToServerAction()));

        add(toolbar);

        CheckBox scoreCheckBox = new CheckBox(MESSAGES.hideScoreLabel());
        scoreCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideScoreColumn(event.getValue());
            }
        });
        scoreCheckBox.setStyleName("ode-ProjectNameLabel");
        add(scoreCheckBox);

        CheckBox submitterCheckBox = new CheckBox(MESSAGES.hideSubmitterLabel());
        submitterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideSubmitterColumn(event.getValue());
            }
        });
        submitterCheckBox.setStyleName("ode-ProjectNameLabel");
        add(submitterCheckBox);

        CheckBox submitTimeCheckBox = new CheckBox(MESSAGES.hideSubmitTimeLabel());
        submitTimeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideSubmitTimeColumn(event.getValue());
            }
        });
        submitTimeCheckBox.setStyleName("ode-ProjectNameLabel");
        add(submitTimeCheckBox);

        CheckBox scoreTimeCheckBox = new CheckBox(MESSAGES.hideScoredTimeLabel());
        scoreTimeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideScoredTimeColumn(event.getValue());
            }
        });
        scoreTimeCheckBox.setStyleName("ode-ProjectNameLabel");
        add(scoreTimeCheckBox);

        updateButtons();

        OdeAdmin.getInstance().getScoreProjectManager().addScoreProjectManagerEventListener(this);
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

    private static class UploadToServerAction implements Command {

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

    private static class SwitchCourseAction implements Command {
        private int courseId;

        SwitchCourseAction(int courseId) {
            this.courseId = courseId;
        }

        @Override
        public void execute() {
            ScoreProjectListBox.getScoreProjectListBox().getScoreProjectList().changeCourse(courseId);
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
        toolbar.setButtonEnabled(WIDGET_NAME_UPLOAD_TO_SERVER, numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(),
                numSelectedProjects > 0);
        Ode.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
                numSelectedProjects > 0);
    }

    @Override
    public void onScoreProjectAdded(ScoreProject scoreProject) {

    }

    @Override
    public void onScoreProjectsLoaded() {
        List<DropDownButton.DropDownItem> items = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : OdeAdmin.getInstance().
                getScoreProjectManager().getAllCoursesInfos().entrySet()) {
            toolbar.addDropDownButtonItem(WIDGET_NAME_COURSE,
                    new DropDownButton.DropDownItem(entry.getValue(), entry.getValue(),
                    new SwitchCourseAction(entry.getKey())));
        }
    }

}
