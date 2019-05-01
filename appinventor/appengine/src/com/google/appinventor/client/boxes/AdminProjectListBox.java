//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.AdminProjectList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for project list.
 *
 */
public final class AdminProjectListBox extends Box {

    // Singleton project explorer box instance (only one project explorer allowed)
    private static final AdminProjectListBox INSTANCE = new AdminProjectListBox();

    // Project list for young android
    private final AdminProjectList plist;

    /**
     * Returns the singleton projects list box.
     *
     * @return  project list box
     */
    public static AdminProjectListBox getAdminProjectListBox() {
        return INSTANCE;
    }

    /**
     * Creates new project list box.
     */
    private AdminProjectListBox() {
        super(MESSAGES.projectListBoxCaption(),
                300,    // height
                false,  // minimizable
                false); // removable

        plist = new AdminProjectList();
        setContent(plist);
    }

    /**
     * Returns project list associated with projects explorer box.
     *
     * @return  project list
     */
    public AdminProjectList getAdminProjectList() {
        return plist;
    }
}
