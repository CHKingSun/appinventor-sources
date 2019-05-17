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
import com.google.appinventor.client.utils.ProjectSearchDialog;
import com.google.appinventor.client.utils.UploadToServerDialog;
import com.google.appinventor.client.widgets.DropDownButton;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project toolbar houses command buttons in the Young Android Project tab.
 *
 */
public class ScoreProjectToolbar extends VerticalPanel implements ScoreProjectManagerEventListener {
    private static final String WIDGET_NAME_COURSE = "Course";
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_SIMILARITY_ANALYSE = "Similarity Analyse";
    private static final String WIDGET_NAME_UPLOAD_TO_SERVER = "Upload To Server";

    private Toolbar toolbar;
    private DateBox fromDateBox;
    private DateBox toDateBox;

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
        toolbar.addButton(new Toolbar.ToolbarItem(WIDGET_NAME_SIMILARITY_ANALYSE, MESSAGES.similarityAnalyseButton(),
                new SimilarityAnalyseAction()));
        toolbar.addButton(new Toolbar.ToolbarItem(WIDGET_NAME_UPLOAD_TO_SERVER, MESSAGES.uploadToServerButton(),
                new UploadToServerAction()));

        add(toolbar);

        HorizontalPanel panel = new HorizontalPanel();
        CheckBox submitterCheckBox = new CheckBox(MESSAGES.hideSubmitterLabel());
        submitterCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideSubmitterColumn(event.getValue());
            }
        });
        submitterCheckBox.setStyleName("ode-ProjectNameLabel");
        panel.add(submitterCheckBox);

        CheckBox submitTimeCheckBox = new CheckBox(MESSAGES.hideSubmitTimeLabel());
        submitTimeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideSubmitTimeColumn(event.getValue());
            }
        });
        submitTimeCheckBox.setStyleName("ode-ProjectNameLabel");
        panel.add(submitTimeCheckBox);
        add(panel);

        panel = new HorizontalPanel();
        CheckBox scoreCheckBox = new CheckBox(MESSAGES.hideSimilarityLabel());
        scoreCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideSimilarityColumn(event.getValue());
            }
        });
        scoreCheckBox.setStyleName("ode-ProjectNameLabel");
        panel.add(scoreCheckBox);

        CheckBox scoreTimeCheckBox = new CheckBox(MESSAGES.hideScoredTimeLabel());
        scoreTimeCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                ScoreProjectListBox.getScoreProjectListBox()
                        .getScoreProjectList().setHideScoredTimeColumn(event.getValue());
            }
        });
        scoreTimeCheckBox.setStyleName("ode-ProjectNameLabel");
        panel.add(scoreTimeCheckBox);
        add(panel);

        panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
        panel.add(new Label("From: "));
        fromDateBox = new DateBox(new DatePicker(), null,
                new DateBox.DefaultFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM)));
        fromDateBox.setStyleName("ode-DateBox");
        panel.add(fromDateBox);
        panel.add(new Label(" - To: "));
        toDateBox = new DateBox(new DatePicker(), null,
                new DateBox.DefaultFormat(DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM)));
        toDateBox.setStyleName("ode-DateBox");
        panel.add(toDateBox);
        TextButton okButton = new TextButton(MESSAGES.okButton());
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new FilterTimeAction().execute();
            }
        });
        panel.add(okButton);
        add(panel);

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

    private static class SimilarityAnalyseAction implements Command {

        @Override
        public void execute() {
            List<ScoreProject> projects = ScoreProjectListBox.getScoreProjectListBox()
                    .getScoreProjectList().getSelectedProjects();
            if (projects.size() == 0) {
                Window.alert("No projects are selected!");
                return;
            }
            List<Long> projectsId = new ArrayList<>(projects.size());
            for (ScoreProject project : projects) {
                projectsId.add(project.getProjectId());
            }
            new ProjectSearchDialog(new ProjectSearchDialog.SearchAction() {
                @Override
                public void onProjectSelected(long projectId) {
                    OdeAdmin.getInstance().getAdminProjectService().similarity(
                            projectsId, projectId,
                            new AsyncCallback<Map<Long, Float>>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Similarity analyse failed with error: " + caught.getLocalizedMessage());
                                }

                                @Override
                                public void onSuccess(Map<Long, Float> result) {
                                    for (ScoreProject project : projects) {
                                        project.setSimilarity(result.get(project.getProjectId()));
                                    }
                                    ScoreProjectListBox.getScoreProjectListBox()
                                            .getScoreProjectList().updateSimilarityWidgets(projects);
                                }
                            }
                    );
                }
            }).show();
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

    private class SwitchCourseAction implements Command {
        private int courseId;
        private String courseName;

        SwitchCourseAction(int courseId, String courseName) {
            this.courseId = courseId;
            this.courseName = courseName;
        }

        @Override
        public void execute() {
            toolbar.setDropDownButtonCaption(WIDGET_NAME_COURSE, courseName);
            ScoreProjectListBox.getScoreProjectListBox().getScoreProjectList().changeCourse(courseId);
        }
    }

    private class FilterTimeAction implements Command {

        @Override
        public void execute() {
            if (fromDateBox.getValue() == null) {
                Window.alert("Please set from time first!");
                return;
            }
            if (toDateBox.getValue() == null) {
                Window.alert("Please set to time first!");
                return;
            }
            final long fromTime = fromDateBox.getValue().getTime();
            final long toTime = toDateBox.getValue().getTime() + 86400000L;
            OdeAdmin.CLog(new Date(fromTime));
            OdeAdmin.CLog(new Date(toTime));
            if (toTime <= fromTime) {
                Window.alert("FromDate is lager than toDate");
                return;
            }
            ScoreProjectListBox.getScoreProjectListBox()
                    .getScoreProjectList().filterTime(new Predicate<ScoreProject>() {
                @Override
                public boolean test(ScoreProject project) {
                    OdeAdmin.CLog(new Date(project.getSubmitTime()));
                    return fromTime <= project.getSubmitTime()
                            && project.getSubmitTime() <= toTime;
                }
            });
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
        OdeAdmin.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.deleteProjectMenuItem(),
                numSelectedProjects > 0);
        OdeAdmin.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportProjectMenuItem(),
                numSelectedProjects > 0);
        OdeAdmin.getInstance().getTopToolbar().fileDropDown.setItemEnabled(MESSAGES.exportAllProjectsMenuItem(),
                numSelectedProjects > 0);
    }

    @Override
    public void onScoreProjectAdded(ScoreProject scoreProject) {

    }

    @Override
    public void onScoreProjectsLoaded() {
        int courseId = -1;
        String courseName = null;
        for (Map.Entry<Integer, String> entry : OdeAdmin.getInstance().
                getScoreProjectManager().getAllCoursesInfos().entrySet()) {
            courseId = entry.getKey();
            courseName = entry.getValue();
            toolbar.addDropDownButtonItem(WIDGET_NAME_COURSE,
                    new DropDownButton.DropDownItem(courseName, courseName,
                    new SwitchCourseAction(courseId, courseName)));
        }
        if (courseId != -1) {
            new SwitchCourseAction(courseId, courseName).execute();
        }
    }

}
