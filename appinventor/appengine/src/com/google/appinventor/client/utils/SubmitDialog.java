package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.commands.ChainableCommand;
import com.google.appinventor.client.explorer.commands.SaveAllEditorsCommand;
import com.google.appinventor.client.tracking.Tracking;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.List;

public class SubmitDialog extends DialogBox {
    private final long projectId;

    public SubmitDialog(List<CourseInfo> courseInfos, long projectId) {
        setText("Submit account");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        center();

        this.projectId = projectId;

        initializeUi(courseInfos);
    }

    private void initializeUi(List<CourseInfo> courseInfos) {
        VerticalPanel panel = new VerticalPanel();

        for (CourseInfo courseInfo : courseInfos) {
            panel.add(new CourseButton(courseInfo));
        }

        TextButton cancelButton = new TextButton("Close");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                SubmitDialog.this.hide(true);
            }
        });
        panel.add(cancelButton);

        setWidget(panel);
    }

    private class CourseButton extends TextButton {
        CourseButton(final CourseInfo info) {
            super(info.getAdminName() + ": " + info.getCourseName());

            addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    new ConfirmDialog(info).show();
                }
            });
        }
    }

    private class ConfirmDialog extends DialogBox {
        CourseInfo info;

        ConfirmDialog(final CourseInfo info) {
            setText("Confirm");
            setStylePrimaryName("ode-DialogBox");
            setGlassEnabled(true);
            setAnimationEnabled(true);
            center();

            this.info = info;

            initializeUi();
        }

        private void initializeUi() {
            HorizontalPanel panel = new HorizontalPanel();

            TextButton okButton = new TextButton("OK");
            okButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    // TODO how to deal with repeat submit
                    ProjectRootNode projectRootNode = Ode.getInstance().getCurrentYoungAndroidProjectRootNode();
                    if (projectRootNode != null) {
                        ChainableCommand cmd = new SaveAllEditorsCommand(null);
                        cmd.startExecuteChain(Tracking.PROJECT_ACTION_SAVE_YA, projectRootNode);
                    }
                    Ode.getInstance().getAdminProjectService().submitProject(
                            info, projectId, new AsyncCallback<Boolean>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    Window.alert("Submit failed with error: " + caught.getLocalizedMessage());
                                    ConfirmDialog.this.hide(true);
                                }

                                @Override
                                public void onSuccess(Boolean result) {
                                    if (result) {
                                        Window.alert("Submit success!");
                                    } else {
                                        Window.alert("Submit failed!");
                                    }
                                    ConfirmDialog.this.hide(true);
                                }
                            }
                    );
                }
            });
            panel.add(okButton);

            TextButton cancelButton = new TextButton("Cancel");
            cancelButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ConfirmDialog.this.hide(true);
                }
            });
            panel.add(cancelButton);

            setWidget(panel);
        }

        @Override
        public void show() {
            super.show();
            SubmitDialog.this.hide();
        }
    }
}

