//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.boxes;

import static com.google.appinventor.client.Ode.MESSAGES;

import com.google.appinventor.client.explorer.youngandroid.ScoreProjectList;
import com.google.appinventor.client.widgets.boxes.Box;


/**
 * Box implementation for project list.
 *
 */
public final class ScoreProjectListBox extends Box {

    // Singleton project explorer box instance (only one project explorer allowed)
    private static final ScoreProjectListBox INSTANCE = new ScoreProjectListBox();

    // Project list for young android
    private final ScoreProjectList plist;

    /**
     * Returns the singleton projects list box.
     *
     * @return  project list box
     */
    public static ScoreProjectListBox getAdminProjectListBox() {
        return INSTANCE;
    }

    /**
     * Creates new project list box.
     */
    private ScoreProjectListBox() {
        super(MESSAGES.projectListBoxCaption(),
                300,    // height
                false,  // minimizable
                false); // removable

        plist = new ScoreProjectList();
        setContent(plist);
    }

    /**
     * Returns project list associated with projects explorer box.
     *
     * @return  project list
     */
    public ScoreProjectList getAdminProjectList() {
        return plist;
    }
}
