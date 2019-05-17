package com.google.appinventor.client.boxes;

import com.google.appinventor.client.explorer.youngandroid.ClassList;
import com.google.appinventor.client.widgets.boxes.Box;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ClassListBox extends Box {

    // Singleton project explorer box instance (only one project explorer allowed)
    private static final ClassListBox INSTANCE = new ClassListBox();

    // Project list for young android
    private final ClassList plist;

    public static ClassListBox getClassListBox() {
        return INSTANCE;
    }

    private ClassListBox() {
        super(MESSAGES.classListBoxCaption(),
                300, false, false);

        plist = new ClassList();
        setContent(plist);
    }

    public ClassList getClassList() {
        return plist;
    }
}
