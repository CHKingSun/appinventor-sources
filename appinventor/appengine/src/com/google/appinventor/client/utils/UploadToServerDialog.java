//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.*;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;

import java.util.List;

public class UploadToServerDialog extends DialogBox {

    private Label resultLabel;

    private EditableProperty serverProperty;
    private EditableProperty portProperty;
    private EditableProperty userNameProperty;
    private EditableProperty passwdProperty;

    private List<Project> projects;

    public UploadToServerDialog(List<Project> projects) {
        setText("Upload To Server");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        setHeight("120px");
        setWidth("360px");
        center();

        this.projects = projects;

        initializeUi();
    }

    private void initializeUi() {
        VerticalPanel dialog = new VerticalPanel();
        EditableProperties properties = new EditableProperties(true);
        dialog.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        HorizontalPanel panel;
        PropertyEditor editor;

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.add(new Label("Server:   "));
        editor = new TextPropertyEditor();
        properties.addProperty("Server", "app.gzjkw.net",
                "Server", editor, EditableProperty.TYPE_NORMAL);
        serverProperty = properties.getExistingProperty("Server");
        panel.add(editor);
        panel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(panel);

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.add(new Label("Port:     "));
        editor = new NonNegativeIntegerPropertyEditor();
        properties.addProperty("Port", "80",
                "Port", editor, EditableProperty.TYPE_NORMAL);
        portProperty = properties.getExistingProperty("Port");
        panel.add(editor);
        panel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(panel);

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.add(new Label("UserName: "));
        editor = new TextPropertyEditor();
        properties.addProperty("UserName", "",
                "UserName", editor, EditableProperty.TYPE_NORMAL);
        userNameProperty = properties.getExistingProperty("UserName");
        panel.add(editor);
        panel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(panel);

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        panel.add(new Label("Password: "));
        editor = new TextPropertyEditor();
        editor.setStyleName("ode-PasswordEditor");
        properties.addProperty("Password", "",
                "Password", editor, EditableProperty.TYPE_NORMAL);
        passwdProperty = properties.getExistingProperty("Password");
        panel.add(editor);
        panel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(panel);

        panel = new HorizontalPanel();
        panel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
        TextButton testLoginButton = new TextButton("Test Login");
        testLoginButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new TestLoginAction(false).execute();
            }
        });
        panel.add(testLoginButton);
        TextButton okButton = new TextButton("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new TestLoginAction(true).execute();
            }
        });
        panel.add(okButton);
        TextButton cancelButton = new TextButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                UploadToServerDialog.this.hide(true);
            }
        });
        panel.add(cancelButton);

        dialog.add(panel);
        resultLabel = new Label("");
        dialog.add(resultLabel);

        setWidget(dialog);
    }

    private class TestLoginAction implements Command {
        private boolean upload;

        TestLoginAction(boolean upload) {
            this.upload = upload;
        }

        @Override
        public void execute() {
            resultLabel.setText("Logging...");
            Ode.getInstance().getAdminProjectService().testLogin(
                    serverProperty.getValue(), Integer.parseInt(portProperty.getValue()),
                    userNameProperty.getValue(), passwdProperty.getValue(),
                    new AsyncCallback<String>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            resultLabel.setText("Error: " + caught.getLocalizedMessage());
                        }

                        @Override
                        public void onSuccess(String result) {
                            if (result.equals("")) {
                                resultLabel.setText("Login failed! Check out your account and password.");
                            } else if (result.equals("error")) {
                                resultLabel.setText("Login failed! Check out your server and port.");
                            }else {
                                resultLabel.setText("Login success!");
                                if(upload) {
                                    new UploadToServerAction(result).execute();
                                }
                            }
                        }
                    }
            );
        }
    }

    private class UploadToServerAction implements Command {
        int totalNum;
        int executeNum;
        int successNum;

        String cookie;

        UploadToServerAction(String cookie) {
            this.cookie = cookie;
            totalNum = projects.size();
            executeNum = 1;
            successNum = 0;
        }

        @Override
        public void execute() {
            resultLabel.setText("Uploading No." + executeNum + "\tSuccess: " + successNum);
            for (Project project : projects) {
                Ode.getInstance().getAdminProjectService().uploadProject(
                        project.getProjectId(), serverProperty.getValue(),
                        Integer.parseInt(portProperty.getValue()), cookie,
                        new AsyncCallback<Integer>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                updateResultLabel();
                            }

                            @Override
                            public void onSuccess(Integer result) {
                                if (result == 200) {
                                    ++successNum;
                                }
                                updateResultLabel();
                            }
                        }
                );
            }
        }

        private void updateResultLabel() {
            if (totalNum == executeNum) {
                if (successNum == 0) {
                    resultLabel.setText("No projects uploaded! Test Login for more information.");
                } else {
                    resultLabel.setText("Upload finished!\tAll: " + executeNum + "\tSuccess: " + successNum);
                }
            } else {
                resultLabel.setText("Uploading No." + executeNum + "\tSuccess: " + successNum);
            }
            ++executeNum;
        }
    }
}
