//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.score;

import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.OdeAsyncCallback;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.ScoreInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class manages score infos.
 */
public final class ScoreProjectManager {

    // Map to get all the score infos by project ID.
    // Project ID is unique for one admin.
    private final Map<Long, ScoreProject> scoreProjectsMap;

    private final Map<Integer, String> coursesMap;

    // List of listeners for any project manager events.
    private final List<ScoreProjectManagerEventListener> scoreProjectManagerEventListeners;

    /**
     * Creates a new projects manager.
     */
    public ScoreProjectManager() {
        scoreProjectsMap = new HashMap<>();
        coursesMap = new HashMap<>();
        scoreProjectManagerEventListeners = new ArrayList<>();
        OdeAdmin.getInstance().getAdminProjectService().getAllScoreInfos(
                new OdeAsyncCallback<List<ScoreInfo>>() {
                    @Override
                    public void onSuccess(List<ScoreInfo> result) {
                        List<Project> projects = new ArrayList<>(result.size());

                        for (ScoreInfo info : result) {
                            projects.add(addScoreProject(info).getProject());
                        }

                        OdeAdmin.getInstance().getProjectManager().reloadProjects(projects);
                        fireScoreProjectsLoaded();
                    }
                }
        );
    }

    public Map<Integer, String> getAllCoursesInfos() {
        return new HashMap<>(coursesMap);
    }

    public List<ScoreProject> getAllScoreProjects() {
        return new ArrayList<>(scoreProjectsMap.values());
    }

    public ScoreProject getScoreProject(long projectId) {
        return scoreProjectsMap.get(projectId);
    }

    public ScoreProject addScoreProject(ScoreInfo scoreInfo) {
        ScoreProject project = new ScoreProject(scoreInfo);
        scoreProjectsMap.put(project.getProjectId(), project);
        coursesMap.put(scoreInfo.getCourseId(), scoreInfo.getCourseName());
        fireScoreProjectAdded(project);
        return project;
    }

    /**
     * Adds a {@link ScoreProjectManagerEventListener} to the listener list.
     *
     * @param listener  the {@code ScoreProjectManagerEventListener} to be added
     */
    public void addScoreProjectManagerEventListener(ScoreProjectManagerEventListener listener) {
        scoreProjectManagerEventListeners.add(listener);
    }

    /**
     * Removes a {@link ScoreProjectManagerEventListener} from the listener list.
     *
     * @param listener  the {@code ScoreProjectManagerEventListener} to be removed
     */
    public void removeProjectManagerEventListener(ScoreProjectManagerEventListener listener) {
        scoreProjectManagerEventListeners.remove(listener);
    }

    private List<ScoreProjectManagerEventListener> copyProjectManagerEventListeners() {
        return new ArrayList<>(scoreProjectManagerEventListeners);
    }

    /*
     * Triggers a 'score project added' event to be sent to the listener on the listener list.
     */
    private void fireScoreProjectAdded(ScoreProject scoreProject) {
        for (ScoreProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
            listener.onScoreProjectAdded(scoreProject);
        }
    }

    /*
     * Triggers a 'score projects loaded' event to be sent to the listener on the listener list.
     */
    private void fireScoreProjectsLoaded() {
        for (ScoreProjectManagerEventListener listener : copyProjectManagerEventListeners()) {
            listener.onScoreProjectsLoaded();
        }
    }
}
