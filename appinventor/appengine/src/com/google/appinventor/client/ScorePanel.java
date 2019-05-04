//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client;

import com.google.appinventor.client.editor.youngandroid.properties.YoungAndroidIntegerRangePropertyEditor;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.PropertyEditor;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ScorePanel extends Composite {
    private Label userNameLabel;
    private EditableProperty scoreProperty;

    ScorePanel() {
        this("", 0);
    }

    ScorePanel(String userName, int score) {
        HorizontalPanel panel = new HorizontalPanel();
        panel.setSpacing(4);
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);

        TextButton submitButton = new TextButton(MESSAGES.submitButton());
        submitButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Ode.getInstance().isAdminMode()) {
                    new MarkScoreAction().execute();
                } else {
                    new SubmitProjectAction().execute();
                }
            }
        });

        if (Ode.getInstance().isAdminMode()) {
            userNameLabel = new Label(userName);
            panel.add(userNameLabel);
            PropertyEditor scoreEditor = new YoungAndroidIntegerRangePropertyEditor(-1, 150);
            scoreProperty = new EditableProperty(new EditableProperties(true), "Score",
                    Integer.toString(score), "Score", scoreEditor, EditableProperty.TYPE_NORMAL);
            scoreEditor.setStyleName("ode-ScoreEditor");
            panel.add(scoreEditor);
            submitButton.setText(MESSAGES.scoreButton());
        }
        panel.add(submitButton);

        initWidget(panel);
    }

    public void setUserName(String userName) {
        userNameLabel.setText(userName);
    }

    public void setScore(int score) {
        scoreProperty.setValue(Integer.toString(score));
    }

    private class MarkScoreAction implements Command {

        @Override
        public void execute() {
            setScore(100);
        }
    }

    private class SubmitProjectAction implements Command {

        @Override
        public void execute() {

        }
    }

}
