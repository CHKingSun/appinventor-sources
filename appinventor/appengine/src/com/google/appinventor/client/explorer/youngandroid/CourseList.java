package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.boxes.ClassListBox;
import com.google.appinventor.client.explorer.score.ClassManagerEventListener;
import com.google.appinventor.shared.rpc.project.ClassInfo;
import com.google.appinventor.shared.rpc.project.CourseInfo;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

import static com.google.appinventor.client.Ode.MESSAGES;

public class CourseList extends Composite implements ClassManagerEventListener {

    private final List<CourseInfo> infoList;
    private final Map<CourseInfo, CourseWidgets> courseWidgets;
    private final Set<CourseInfo> selectedInfos;

    private Grid table;

    public CourseList() {
        infoList = new ArrayList<>();
        courseWidgets = new HashMap<>();
        selectedInfos = new HashSet<>();

        table = new Grid(1, 2);
        table.addStyleName("ode-ProjectTable");
        table.setWidth("100%");
        table.setCellSpacing(0);

        table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");
        final Label nameHeaderLabel = new Label(MESSAGES.courseNameHeader());
        nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        table.setWidget(0, 1, nameHeaderLabel);

        ScrollPanel panel = new ScrollPanel();
        panel.setWidth("100%");
        panel.setHeight("540px");

        panel.add(table);
        initWidget(panel);

        OdeAdmin.getInstance().getClassManager().addClassManagerEventListener(this);
    }

    private class CourseWidgets {
        private final CheckBox checkBox;
        private final Label nameLabel;

        private CourseWidgets(CourseInfo info) {
            checkBox = new CheckBox();
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
                    int row = 1 + infoList.indexOf(info);
                    if (isChecked) {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                        selectedInfos.add(info);
                    } else {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                        selectedInfos.remove(info);
                    }
                    OdeAdmin.getInstance().getCourseToolbar().updateButtons();
                }
            });

            nameLabel = new Label(info.getCourseName());
            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    ClassListBox.getClassListBox().setCaption(info.getCourseName());
                    ClassListBox.getClassListBox().getClassList().changeCourse(info.getCourseId());
                }
            });
            nameLabel.addStyleName("ode-ProjectNameLabel");
        }
    }

    public void refreshTable() {
        table.resize(1 + infoList.size(), 2);
        int row = 1;
        for (CourseInfo info : infoList) {
            CourseWidgets cw = courseWidgets.get(info);
            if (selectedInfos.contains(info)) {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                cw.checkBox.setValue(true);
            } else {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                cw.checkBox.setValue(false);
            }

            table.setWidget(row, 0, cw.checkBox);
            table.setWidget(row, 1, cw.nameLabel);

            ++row;
        }

        OdeAdmin.getInstance().getCourseToolbar().updateButtons();
    }

    public int getNumSelectedCourseInfos() {
        return selectedInfos.size();
    }

    public List<CourseInfo> getSelectedCourseInfos() {
        return new ArrayList<>(selectedInfos);
    }

    public void selectAllCourseInfos() {
        for (CourseInfo info : infoList) {
            CourseWidgets widgets = courseWidgets.get(info);
            if (!widgets.checkBox.getValue()) {
                widgets.checkBox.setValue(true, true);
            }
        }
    }

    public void deselectAllCourseInfos() {
        for (CourseInfo info : infoList) {
            CourseWidgets widgets = courseWidgets.get(info);
            if (widgets.checkBox.getValue()) {
                widgets.checkBox.setValue(false, true);
            }
        }
    }


    @Override
    public void onCourseAdded(CourseInfo courseInfo) {
        if (!infoList.contains(courseInfo)) {
            infoList.add(courseInfo);
            courseWidgets.put(courseInfo, new CourseWidgets(courseInfo));
        }
        refreshTable();
    }

    @Override
    public void onClassAdded(ClassInfo classInfo) {

    }

    @Override
    public void onCourseRemoved(CourseInfo courseInfo) {
        infoList.remove(courseInfo);
        courseWidgets.remove(courseInfo);
        refreshTable();
    }

    @Override
    public void onClassRemoved(ClassInfo classInfo) {

    }

    @Override
    public void onClassesLoaded() {

    }
}
