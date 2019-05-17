package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAdmin;
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

public class ClassList extends Composite implements ClassManagerEventListener {

    private final Map<Integer, List<ClassInfo>> classesMap;
    private final Map<ClassInfo, ClassWidgets> classWidgets;
    private final Set<ClassInfo> selectedInfos;
    private int currentCourse;

    private final Grid table;

    public ClassList() {
        classesMap = new HashMap<>();
        classWidgets = new HashMap<>();
        currentCourse = -1;
        classesMap.put(currentCourse, new ArrayList<ClassInfo>());
        selectedInfos = new HashSet<>();

        table = new Grid(1, 2);
        table.addStyleName("ode-ProjectTable");
        table.setWidth("100%");
        table.setCellSpacing(0);

        table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");
        final Label nameHeaderLabel = new Label(MESSAGES.studentNameHeader());
        nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        table.setWidget(0, 1, nameHeaderLabel);

        ScrollPanel panel = new ScrollPanel();
        panel.setWidth("100%");
        panel.setHeight("540px");

        panel.add(table);
        initWidget(panel);

        OdeAdmin.getInstance().getClassManager().addClassManagerEventListener(this);
    }

    private class ClassWidgets {
        private final CheckBox checkBox;
        private final Label nameLabel;

        private ClassWidgets(ClassInfo info) {
            checkBox = new CheckBox();
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
                    int row = 1 + classesMap.get(currentCourse).indexOf(info);
                    if (isChecked) {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                        selectedInfos.add(info);
                    } else {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                        selectedInfos.remove(info);
                    }
                    OdeAdmin.getInstance().getClassToolbar().updateButtons();
                }
            });

            nameLabel = new Label(info.getUserName());
            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    checkBox.setValue(!checkBox.getValue(), true);
                }
            });
            nameLabel.addStyleName("ode-ProjectNameLabel");
        }
    }

    public void refreshTable() {
        table.resize(1 + classesMap.get(currentCourse).size(), 2);
        int row = 1;
        for (ClassInfo info : classesMap.get(currentCourse)) {
            ClassWidgets cw = classWidgets.get(info);
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

        OdeAdmin.getInstance().getClassToolbar().updateButtons();
    }

    public void changeCourse(int courseId) {
        if (currentCourse == courseId || !classesMap.containsKey(courseId)) return;
        deselectAllClassInfos();
        currentCourse = courseId;
        refreshTable();
    }

    public int getNumSelectedClassInfos() {
        return selectedInfos.size();
    }

    public List<ClassInfo> getSelectedClassInfos() {
        return new ArrayList<>(selectedInfos);
    }

    public void selectAllClassInfos() {
        for (ClassInfo info : classesMap.get(currentCourse)) {
            ClassWidgets widgets = classWidgets.get(info);
            if (!widgets.checkBox.getValue()) {
                widgets.checkBox.setValue(true, true);
            }
        }
    }

    public void deselectAllClassInfos() {
        for (ClassInfo info : classesMap.get(currentCourse)) {
            ClassWidgets widgets = classWidgets.get(info);
            if (widgets.checkBox.getValue()) {
                widgets.checkBox.setValue(false, true);
            }
        }
    }

    @Override
    public void onCourseAdded(CourseInfo courseInfo) {
        if (!classesMap.containsKey(courseInfo.getCourseId())) {
            classesMap.put(courseInfo.getCourseId(), new ArrayList<ClassInfo>());
        }
    }

    @Override
    public void onClassAdded(ClassInfo classInfo) {
        if (!classesMap.containsKey(classInfo.getCourseId())) {
            classesMap.put(classInfo.getCourseId(), new ArrayList<ClassInfo>());
        }
        classesMap.get(classInfo.getCourseId()).add(classInfo);
        classWidgets.put(classInfo, new ClassWidgets(classInfo));
        if (currentCourse == classInfo.getCourseId()) {
            refreshTable();
        }
    }

    @Override
    public void onCourseRemoved(CourseInfo courseInfo) {
        if (!classesMap.containsKey(courseInfo.getCourseId())) return;
        if (currentCourse == courseInfo.getCourseId()) {
            deselectAllClassInfos();
        }
        for (ClassInfo info : classesMap.get(courseInfo.getCourseId())) {
            classWidgets.remove(info);
        }
        classesMap.remove(courseInfo.getCourseId());
        if (currentCourse == courseInfo.getCourseId()) {
            changeCourse(classesMap.keySet().iterator().next());
        }
    }

    @Override
    public void onClassRemoved(ClassInfo classInfo) {
        classesMap.get(classInfo.getCourseId()).remove(classInfo);
        classWidgets.remove(classInfo);
        if (classInfo.getCourseId() == currentCourse) {
            selectedInfos.remove(classInfo);
            refreshTable();
        }
    }

    @Override
    public void onClassesLoaded() {

    }
}
