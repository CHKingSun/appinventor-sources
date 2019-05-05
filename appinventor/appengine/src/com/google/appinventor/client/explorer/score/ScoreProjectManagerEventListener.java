//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.score;

/**
 * Listener interface for receiving project manager events.
 *
 * <p>Classes interested in processing project manager events must implement
 * this interface, and instances of that class must be registered with the
 * {@link ScoreProjectManager} instance using its
 * {@link ScoreProjectManager#addScoreProjectManagerEventListener(ScoreProjectManagerEventListener)}
 * method. When a project is added to the project manager, the listeners'
 * {@link #onScoreProjectAdded(ScoreProject)} methods will be invoked.
 *
 */
public interface ScoreProjectManagerEventListener {

    /**
     * Invoked after a project was added to the ProjectManager
     *
     * @param scoreProject  score project added
     */
    void onScoreProjectAdded(ScoreProject scoreProject);


    /**
     * Invoked after all projects have been loaded by ProjectManager
     *
     */
    void onScoreProjectsLoaded();

}
