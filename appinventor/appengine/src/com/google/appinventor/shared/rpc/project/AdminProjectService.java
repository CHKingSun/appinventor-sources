package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;
import java.util.Map;

/**
 * Interface for the service providing admin project information.
 *
 */
@RemoteServiceRelativePath(ServerLayout.ADMIN_PROJECT_SERVICE)
public interface AdminProjectService extends RemoteService {

    /**
     * Test login for upload projects to main server.
     * @param host host of the server
     * @param port port of the server
     * @param userName user name to login
     * @param passwd password to login
     * @return a cookie for login successfully or nothing
     */
    String testLogin(String host, int port, String userName, String passwd);

    /**
     * Upload a project to remote main server.
     * @param projectId project ID to upload
     * @param host host of the server
     * @param port port of the server
     * @param cookie cookie to login
     * @return status of upload
     */
    int uploadProject(long projectId, String host, int port, String cookie);

    /**
     * Get all the courses info of the user.
     * @return all the courses info of the user
     */
    List<CourseInfo> getAllCourses();

    /**
     * Submit the project to the course's admin.
     * @param info the info of the course
     * @param projectId the project ID to submit
     * @return whether submitted successfully
     */
    boolean submitProject(CourseInfo info, long projectId);

    /**
     * Get all the score info of the admin.
     * @return all the score info of the admin
     */
    List<ScoreInfo> getAllScoreInfos();

    /**
     * Update the score of the project.
     * @param projectId the project ID
     * @param score the new score
     * @return 0 if update failed, scored time if update success
     */
    long updateProjectScore(long projectId, int score);

    /**
     * Similarity analysis
     * @param projectsId the score projects ID to analyse
     * @param targetProjectId the target project ID to analyse
     * @return the similarities between target project and score project.
     */
    Map<Long, Float> similarity(List<Long> projectsId, long targetProjectId);

    /**
     * Get all the courses the admin created.
     * @return all the courses the admin created.
     */
    List<CourseInfo> getAllAdminCourses();

    List<ClassInfo> getClassInfos(int courseId);

    Map<Integer, List<ClassInfo>> getAllClassInfos(List<Integer> courseIds);

    CourseInfo createCourse(String courseName);

    boolean deleteCourse(CourseInfo info);

    ClassInfo addStudent(int courseId, String userName);

    List<ClassInfo> addStudents(int courseId, String filename);

    boolean deleteStudent(int courseId, String userId);
}
