//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Interface for the service providing admin project information. All declarations
 * in this interface are mirrored in {@link AdminProjectService}. For further
 * information see {@link AdminProjectService}.
 *
 */
public interface AdminProjectServiceAsync {

    /**
     * @see AdminProjectService#testLogin(String, int, String, String)
     */
    void testLogin(String host, int port, String userName, String passwd,
                   AsyncCallback<String> callback);
    /**
     * @see AdminProjectService#uploadProject(long, String, int, String)
     */
    void uploadProject(long projectId, String host, int port, String cookie,
                       AsyncCallback<Integer> callback);

    /**
     * @see AdminProjectService#getAllCourses()
     */
    void getAllCourses(AsyncCallback<List<CourseInfo>> callback);

    /**
     * @see AdminProjectService#submitProject(CourseInfo, long)
     */
    void submitProject(CourseInfo info, long projectId, AsyncCallback<Boolean> callback);

    /**
     * @see AdminProjectService#getAllScoreInfos()
     */
    void getAllScoreInfos(AsyncCallback<List<ScoreInfo>> callback);

    /**
     * @see AdminProjectService#updateProjectScore(long, int)
     */
    void updateProjectScore(long projectId, int score, AsyncCallback<Long> callback);
}
