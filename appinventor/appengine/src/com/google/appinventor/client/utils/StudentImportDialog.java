package com.google.appinventor.client.utils;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.project.ClassInfo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class StudentImportDialog extends DialogBox {

    public interface StudentAction {
        void onStudentImported(List<ClassInfo> infos);
    }

    public StudentImportDialog(int courseId, StudentAction action) {
        setText("Import Students");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        center();

        initializeUi(courseId, action);
    }

    private void initializeUi(int courseId, StudentAction action) {
        VerticalPanel dialog = new VerticalPanel();
        FileUpload upload = new FileUpload();
        dialog.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

        dialog.add(new Label("Use .txt file, student accounts separated by 'Enter' key."));

        upload.setName(ServerLayout.UPLOAD_STUDENTS_FORM_ELEMENT);
        dialog.add(upload);

        HorizontalPanel buttonPanel = new HorizontalPanel();
        TextButton okButton = new TextButton("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                String filename = upload.getFilename();
                filename = filename.substring(Math.max(filename.lastIndexOf('/'),
                        filename.lastIndexOf('\\')) + 1);
                if (!filename.endsWith(".txt")) {
                    Window.alert("Please use '.txt' file!");
                    return;
                }
                String url = GWT.getModuleBaseURL() +
                        ServerLayout.UPLOAD_SERVLET + "/" +
                        ServerLayout.UPLOAD_STUDENTS + "/" +
                        filename;
                Uploader.getInstance().upload(upload, url,
                        new OdeAsyncCallback<UploadResponse>() {
                            @Override
                            public void onSuccess(UploadResponse uploadResponse) {
                                OdeAdmin.getInstance().getAdminProjectService().addStudents(
                                        courseId, uploadResponse.getInfo(),
                                        new OdeAsyncCallback<List<ClassInfo>>() {
                                            @Override
                                            public void onSuccess(List<ClassInfo> result) {
                                                if (result.size() == 0) {
                                                    Window.alert("No students added!");
                                                    return;
                                                }
                                                action.onStudentImported(result);
                                                StringBuilder builder = new StringBuilder("Added successfully with: ");
                                                for (ClassInfo info : result) {
                                                    builder.append(info.getUserName());
                                                    builder.append(", ");
                                                }
                                                Window.alert(builder.substring(0, builder.length() - 2));
                                            }
                                        }
                                );
                            }
                        });
                StudentImportDialog.this.hide(true);
            }
        });
        buttonPanel.add(okButton);

        TextButton cancelButton = new TextButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StudentImportDialog.this.hide(true);
            }
        });
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel);

        setWidget(dialog);
    }
}
