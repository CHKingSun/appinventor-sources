package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.boxes.CourseListBox;
import com.google.appinventor.client.utils.CourseCreateDialog;
import com.google.appinventor.client.utils.DeleteConfirmDialog;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.gwt.user.client.Command;

import java.util.List;

import static com.google.appinventor.client.Ode.MESSAGES;

public class CourseToolbar extends Toolbar {
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_CREATE = "Create Course";
    private static final String WIDGET_NAME_DELETE = "Delete Course";

    public CourseToolbar() {
        super();
        getWidget().setStylePrimaryName("ya-ProjectToolbar");

        addButton(new ToolbarItem(WIDGET_NAME_SELECT_ALL, MESSAGES.selectAllButton(),
                new SelectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DESELECT_ALL, MESSAGES.deselectAllButton(),
                new DeselectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_CREATE, MESSAGES.createCourseButton(),
                new CreateCourseAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DELETE, MESSAGES.deleteCourseButton(),
                new DeleteCourseAction()));

        setButtonEnabled(WIDGET_NAME_DELETE, false);
    }

    private static class SelectAllAction implements Command {

        @Override
        public void execute() {
            CourseListBox.getCourseListBox().getCourseList().selectAllCourseInfos();
        }
    }

    private static class DeselectAllAction implements Command {

        @Override
        public void execute() {
            CourseListBox.getCourseListBox().getCourseList().deselectAllCourseInfos();
        }
    }

    private static class CreateCourseAction implements Command {

        @Override
        public void execute() {
            new CourseCreateDialog(new CourseCreateDialog.CourseAction() {
                @Override
                public void onCourseCreated(CourseInfo courseInfo) {
                    OdeAdmin.getInstance().getClassManager().addCourse(courseInfo);
                }
            }).show();
        }
    }

    private static class DeleteCourseAction implements Command {

        @Override
        public void execute() {
            List<CourseInfo> infos = CourseListBox.getCourseListBox().getCourseList().getSelectedCourseInfos();
            StringBuilder names = new StringBuilder();
            for (CourseInfo info : infos) {
                names.append(info.getCourseName());
                names.append(", ");
            }
            new DeleteConfirmDialog("Delete Course",
                    "Sure to delete " + names.substring(0, names.length() - 2),
                    new Command() {
                        @Override
                        public void execute() {
                            for (CourseInfo info : infos) {
                                OdeAdmin.getInstance().getAdminProjectService().deleteCourse(info,
                                        new OdeAsyncCallback<Boolean>() {
                                            @Override
                                            public void onSuccess(Boolean result) {
                                                if (result) {
                                                    OdeAdmin.getInstance().getClassManager().removeCourse(info);
                                                }
                                            }
                                        });
                            }
                        }
                    }
            ).show();
        }
    }

    public void updateButtons() {
        int numSelectedCourses = CourseListBox.getCourseListBox()
                .getCourseList().getNumSelectedCourseInfos();
        setButtonEnabled(WIDGET_NAME_DELETE, numSelectedCourses > 0);
    }
}
