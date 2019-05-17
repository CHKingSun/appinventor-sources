package com.google.appinventor.client.explorer.score;

import com.google.appinventor.shared.rpc.project.ClassInfo;
import com.google.appinventor.shared.rpc.project.CourseInfo;

public interface ClassManagerEventListener {
    /**
     * Invoked after a project was added to the ClassManager
     *
     * @param courseInfo  score project added
     */
    void onCourseAdded(CourseInfo courseInfo);

    void onClassAdded(ClassInfo classInfo);

    void onCourseRemoved(CourseInfo courseInfo);

    void onClassRemoved(ClassInfo classInfo);


    /**
     * Invoked after all class info have been loaded by ClassManager
     *
     */
    void onClassesLoaded();
}
