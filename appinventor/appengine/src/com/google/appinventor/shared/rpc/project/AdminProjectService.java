package com.google.appinventor.shared.rpc.project;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

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
     * Upload a project to remote main server
     * @param projectId project ID to upload
     * @param host host of the server
     * @param port port of the server
     * @param cookie cookie to login
     * @return status of upload
     */
    int uploadProject(long projectId, String host, int port, String cookie);
}
