//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidIntegerRangePropertyEditor;
import com.google.appinventor.client.explorer.score.ScoreProject;
import com.google.appinventor.client.utils.SubmitDialog;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ScorePanel extends Composite {
    private EditableProperty scoreProperty;

    ScorePanel() {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

        TextButton submitButton = new TextButton(MESSAGES.submitButton());
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                long currentProjectId = Ode.getInstance().getCurrentYoungAndroidProjectId();
                if (currentProjectId != 0) {
                    if (Ode.getInstance().isAdminMode()) {
                        new MarkScoreAction().execute();
                    } else {
                        new SubmitProjectAction().execute();
                    }
                }
            }
        });

        if (Ode.getInstance().isAdminMode()) {
            PropertyEditor scoreEditor = new YoungAndroidIntegerRangePropertyEditor(-1, 150);
            scoreProperty = new EditableProperty(new EditableProperties(true), "Score",
                    "0", "Score", scoreEditor, EditableProperty.TYPE_NORMAL);
            scoreEditor.setStyleName("ode-ScoreEditor");
            panel.add(scoreEditor);
            submitButton.setText(MESSAGES.scoreButton());
        }
        panel.add(submitButton);

        initWidget(panel);
    }

    public void setScore(int score) {
        scoreProperty.setValue(Integer.toString(score));
    }

    private class MarkScoreAction implements Command {

        @Override
        public void execute() {
            final ScoreProject scoreProject = OdeAdmin.getInstance().getScoreProjectManager()
                    .getScoreProject(OdeAdmin.getInstance().getCurrentYoungAndroidProjectId());
            final int score = Integer.parseInt(scoreProperty.getValue());
            OdeAdmin.getInstance().getAdminProjectService().updateProjectScore(
                    scoreProject.getProjectId(), score, new OdeAsyncCallback<Long>() {
                        @Override
                        public void onSuccess(Long result) {
                            Ode.CLog("Updated!");
                            scoreProject.setScore(score);
                            scoreProject.setScoredTime(result);
                            ScoreProjectListBox.getScoreProjectListBox()
                                    .getScoreProjectList().updateScoreProjectWidgets(scoreProject);
                        }
                    }
            );
        }
    }

    private class SubmitProjectAction implements Command {

        @Override
        public void execute() {
            Ode.getInstance().getAdminProjectService().getAllCourses(
                    new AsyncCallback<List<CourseInfo>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Window.alert("Get course message failed with error: " + caught.getLocalizedMessage());
                        }

                        @Override
                        public void onSuccess(List<CourseInfo> result) {
                            new SubmitDialog(result,
                                    Ode.getInstance().getCurrentYoungAndroidProjectId()
                            ).show();
                        }
                    }
            );
        }
    }
}
