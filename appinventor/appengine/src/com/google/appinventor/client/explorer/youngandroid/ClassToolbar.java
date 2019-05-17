package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.boxes.ClassListBox;
import com.google.appinventor.client.widgets.Toolbar;
import com.google.gwt.user.client.Command;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ClassToolbar extends Toolbar {
    private static final String WIDGET_NAME_SELECT_ALL = "Select All";
    private static final String WIDGET_NAME_DESELECT_ALL = "Deselect All";
    private static final String WIDGET_NAME_ADD = "Add Student";
    private static final String WIDGET_NAME_IMPORT = "Import Students";
    private static final String WIDGET_NAME_DELETE = "Delete Student";

    public ClassToolbar() {
        super();
        getWidget().setStylePrimaryName("ya-ProjectToolbar");

        addButton(new ToolbarItem(WIDGET_NAME_SELECT_ALL, MESSAGES.selectAllButton(),
                new SelectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DESELECT_ALL, MESSAGES.deselectAllButton(),
                new DeselectAllAction()));
        addButton(new ToolbarItem(WIDGET_NAME_ADD, MESSAGES.addStudentButton(),
                new AddStudentAction()));
        addButton(new ToolbarItem(WIDGET_NAME_IMPORT, MESSAGES.importStudentButton(),
                new ImportStudentsAction()));
        addButton(new ToolbarItem(WIDGET_NAME_DELETE, MESSAGES.deleteStudentButton(),
                new DeleteStudentAction()));

        setButtonEnabled(WIDGET_NAME_DELETE, false);
    }

    private static class SelectAllAction implements Command {

        @Override
        public void execute() {
            ClassListBox.getClassListBox().getClassList().selectAllClassInfos();
        }
    }

    private static class DeselectAllAction implements Command {

        @Override
        public void execute() {
            ClassListBox.getClassListBox().getClassList().deselectAllClassInfos();
        }
    }

    private static class AddStudentAction implements Command {

        @Override
        public void execute() {
            // TODO dialog
        }
    }

    private static class ImportStudentsAction implements Command {

        @Override
        public void execute() {
            // TODO dialog
        }
    }

    private static class DeleteStudentAction implements Command {

        @Override
        public void execute() {
            // TODO dialog
        }
    }

    public void updateButtons() {
        int numSelectedStudents = ClassListBox.getClassListBox()
                .getClassList().getNumSelectedClassInfos();
        setButtonEnabled(WIDGET_NAME_DELETE, numSelectedStudents > 0);
    }
}
