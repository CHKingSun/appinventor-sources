package com.google.appinventor.client.utils;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.appinventor.shared.rpc.project.ClassInfo;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class StudentAddDialog extends DialogBox {

    public interface StudentAction {
        void onStudentAdded(ClassInfo info);
    }

    public StudentAddDialog(int courseId, StudentAction action) {
        setText("Add Student");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        center();

        initializeUi(courseId, action);
    }

    private void initializeUi(int courseId, StudentAction action) {
        VerticalPanel dialog = new VerticalPanel();
        EditableProperties properties = new EditableProperties(true);
        dialog.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

        HorizontalPanel namePanel = new HorizontalPanel();
        namePanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        namePanel.add(new Label("Student Account:   "));
        TextPropertyEditor editor = new TextPropertyEditor();
        properties.addProperty("Student", "",
                "Student", editor, EditableProperty.TYPE_NORMAL);
        namePanel.add(editor);
        namePanel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(namePanel);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        TextButton okButton = new TextButton("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String student = properties.getExistingProperty("Student").getValue().trim();
                if (student.equals("")) {
                    Window.alert("Student account cannot be empty!");
                    return;
                }
                OdeAdmin.getInstance().getAdminProjectService().addStudent(
                        courseId, student, new OdeAsyncCallback<ClassInfo>() {
                            @Override
                            public void onSuccess(ClassInfo result) {
                                if (result == null) {
                                    Window.alert("Add student failed!");
                                    return;
                                }
                                action.onStudentAdded(result);
                                Window.alert("Added successfully");
                            }
                        });
                StudentAddDialog.this.hide(true);
            }
        });
        buttonPanel.add(okButton);

        TextButton cancelButton = new TextButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StudentAddDialog.this.hide(true);
            }
        });
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel);

        setWidget(dialog);
    }
}
