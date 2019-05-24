package com.google.appinventor.client.utils;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CourseCreateDialog extends DialogBox {

    public interface CourseAction {
        void onCourseCreated(CourseInfo courseInfo);
    }

    public CourseCreateDialog(CourseAction action) {
        setText("Create Course");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        center();

        initializeUi(action);
    }

    private void initializeUi(CourseAction action) {
        VerticalPanel dialog = new VerticalPanel();
        EditableProperties properties = new EditableProperties(true);
        dialog.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

        HorizontalPanel namePanel = new HorizontalPanel();
        namePanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        namePanel.add(new Label("Course Name:   "));
        TextPropertyEditor editor = new TextPropertyEditor();
        properties.addProperty("CourseName", "",
                "CourseName", editor, EditableProperty.TYPE_NORMAL);
        namePanel.add(editor);
        namePanel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(namePanel);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        TextButton okButton = new TextButton("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String courseName = properties.getExistingProperty("CourseName").getValue().trim();
                if (courseName.equals("")) {
                    Window.alert("Course name cannot be empty!");
                    return;
                }
                OdeAdmin.getInstance().getAdminProjectService().createCourse(courseName,
                        new OdeAsyncCallback<CourseInfo>() {
                            @Override
                            public void onSuccess(CourseInfo result) {
                                if (result == null) {
                                    Window.alert("Created course failed!");
                                    return;
                                }
                                action.onCourseCreated(result);
                            }
                        });
                CourseCreateDialog.this.hide(true);
            }
        });
        buttonPanel.add(okButton);

        TextButton cancelButton = new TextButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                CourseCreateDialog.this.hide(true);
            }
        });
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel);

        setWidget(dialog);
    }
}
