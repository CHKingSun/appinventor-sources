package com.google.appinventor.client.boxes;

import com.google.appinventor.client.explorer.youngandroid.CourseList;
import com.google.appinventor.client.widgets.boxes.Box;

import static com.google.appinventor.client.Ode.MESSAGES;

public class CourseListBox extends Box {

    // Singleton project explorer box instance (only one project explorer allowed)
    private static final CourseListBox INSTANCE = new CourseListBox();

    // Project list for young android
    private final CourseList plist;

    public static CourseListBox getCourseListBox() {
        return INSTANCE;
    }

    private CourseListBox() {
        super(MESSAGES.courseListBoxCaption(),
                300, false, false);

        plist = new CourseList();
        setContent(plist);
    }

    public CourseList getCourseList() {
        return plist;
    }
}
