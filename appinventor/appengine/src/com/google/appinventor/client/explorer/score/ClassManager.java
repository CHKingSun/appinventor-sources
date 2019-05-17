package com.google.appinventor.client.explorer.score;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.shared.rpc.project.ClassInfo;
import com.google.appinventor.shared.rpc.project.CourseInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassManager {

    private final Map<Integer, CourseInfo> coursesMap;

    private final Map<Integer, List<ClassInfo>> classesMap;

    private final List<ClassManagerEventListener> listensers;

    public ClassManager() {
        coursesMap = new HashMap<>();
        classesMap = new HashMap<>();
        listensers = new ArrayList<>();

        OdeAdmin.getInstance().getAdminProjectService().getAllAdminCourses(
                new OdeAsyncCallback<List<CourseInfo>>() {
                    @Override
                    public void onSuccess(List<CourseInfo> result) {
                        for (CourseInfo info : result) {
                            addCourse(info);
                        }
                        OdeAdmin.getInstance().getAdminProjectService()
                                .getAllClassInfos(new ArrayList<>(coursesMap.keySet()),
                                        new OdeAsyncCallback<Map<Integer, List<ClassInfo>>>() {
                                            @Override
                                            public void onSuccess(Map<Integer, List<ClassInfo>> result) {
                                                for(Map.Entry<Integer, List<ClassInfo>> entry : result.entrySet()) {
                                                    classesMap.put(entry.getKey(), entry.getValue());
                                                    for (ClassInfo info : entry.getValue()) {
                                                        fireClassAdded(info);
                                                    }
                                                }
                                                fireClassesLoaded();
                                            }
                                        });
                    }
                }
        );
    }

    public void addCourse(CourseInfo info) {
        coursesMap.put(info.getCourseId(), info);
        fireCourseAdded(info);
    }

    public void deleteCourse(CourseInfo info) {
        coursesMap.remove(info.getCourseId());
        fireCourseRemoved(info);
    }

    public void addClass(ClassInfo info) {
        if (!classesMap.containsKey(info.getCourseId())) return;
        classesMap.get(info.getCourseId()).add(info);
        fireClassAdded(info);
    }

    public void removeClass(ClassInfo info) {
        if (!classesMap.containsKey(info.getCourseId())) return;
        classesMap.get(info.getCourseId()).remove(info);
        fireClassRemoved(info);
    }

    public List<CourseInfo> getAllCourseInfos() {
        return new ArrayList<>(coursesMap.values());
    }

    public List<ClassInfo> getClassInfosByCourseId(int courseId) {
        return classesMap.getOrDefault(courseId, null);
    }

    /**
     * Adds a {@link ClassManagerEventListener} to the listener list.
     *
     * @param listener  the {@code ClassManagerEventListener} to be added
     */
    public void addClassManagerEventListener(ClassManagerEventListener listener) {
        listensers.add(listener);
    }

    /**
     * Removes a {@link ClassManagerEventListener} from the listener list.
     *
     * @param listener  the {@code ClassManagerEventListener} to be removed
     */
    public void removeProjectManagerEventListener(ClassManagerEventListener listener) {
        listensers.remove(listener);
    }

    private List<ClassManagerEventListener> copyClassManagerEventListeners() {
        return new ArrayList<>(listensers);
    }

    private void fireCourseAdded(CourseInfo courseInfo) {
        for (ClassManagerEventListener listener : copyClassManagerEventListeners()) {
            listener.onCourseAdded(courseInfo);
        }
    }

    private void fireClassAdded(ClassInfo classInfo) {
        for (ClassManagerEventListener listener : copyClassManagerEventListeners()) {
            listener.onClassAdded(classInfo);
        }
    }

    private void fireCourseRemoved(CourseInfo courseInfo) {
        for (ClassManagerEventListener listener : copyClassManagerEventListeners()) {
            listener.onCourseRemoved(courseInfo);
        }
    }

    private void fireClassRemoved(ClassInfo classInfo) {
        for (ClassManagerEventListener listener : copyClassManagerEventListeners()) {
            listener.onClassRemoved(classInfo);
        }
    }

    private void fireClassesLoaded() {
        for (ClassManagerEventListener listener : copyClassManagerEventListeners()) {
            listener.onClassesLoaded();
        }
    }
}
