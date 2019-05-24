package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.rpc.project.*;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import org.freedom.analysis.SimilarityAnalysis;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService.*;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class AdminProjectServiceImpl extends OdeRemoteServiceServlet implements AdminProjectService {
    private static final Logger LOG = Logger.getLogger(ProjectServiceImpl.class.getName());

    private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

    private final transient FileExporter fileExporter = new FileExporterImpl();

    @Override
    public String testLogin(String host, int port, String userName, String passwd) {
        host = host.trim();
        if (host.startsWith("http://")) {
            host = host.substring(7);
        } else if (host.startsWith("https://")) {
            host = host.substring(8);
        }
        CookieStore cookiestore = new BasicCookieStore();
        try(CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookiestore).build()){
            HttpHost httpHost = new HttpHost(host, port);
            HttpPost postReq = new HttpPost("/login");

            List<BasicNameValuePair> params = new LinkedList<>();
            params.add(new BasicNameValuePair("email", userName));
            params.add(new BasicNameValuePair("password", passwd));
            postReq.setEntity(new UrlEncodedFormEntity(params));

            client.execute(httpHost, postReq);
            for(Cookie c : cookiestore.getCookies()) {
                if(c.getName().equals("AppInventor")){
                    return c.getValue();
                }
            }
            return "";
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Network error.", e);
            return "error";
        }
    }

    @Override
    public int uploadProject(long projectId, String host, int port, String cookie) {
        try {
            ProjectSourceZip zipFile = fileExporter.exportProjectSourceZip(userInfoProvider.getUserId(),
                    projectId, false, false,
                    null, false, false, false, false);
            RawFile srcFile = zipFile.getRawFile();
            byte[] content = srcFile.getContent();

            host = host.trim();
            if (host.startsWith("http://")) {
                host = host.substring(7);
            } else if (host.startsWith("https://")) {
                host = host.substring(8);
            }
            CloseableHttpClient client = HttpClientBuilder.create().build();
            HttpHost httpHost = new HttpHost(host, port);
            HttpPost postReq = new HttpPost("/ode/upload/project/" + storageIo.getProjectName(
                    userInfoProvider.getUserId(), projectId
            ));
            postReq.setHeader("Cookie", "AppInventor=" + cookie);
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("uploadProjectArchive", content).build();
            postReq.setEntity(entity);

            HttpResponse postResp = client.execute(httpHost, postReq);
            int statusCode = postResp.getStatusLine().getStatusCode();
            client.close();
            return statusCode;
        }catch(Exception e){
            LOG.log(Level.SEVERE, "Network error", e);
            return -1;
        }
    }

    @Override
    public List<CourseInfo> getAllCourses() {
        return storageIo.getAllCourses(userInfoProvider.getUserId());
    }

    @Override
    public boolean submitProject(CourseInfo info, long projectId) {
        long newProjectId = copyProjectToAdmin(userInfoProvider.getUserId(), projectId, info.getAdminId());
        return storageIo.addScore(info, userInfoProvider.getUserId(), newProjectId);
    }

    public long copyProjectToAdmin(String userId, long oldProjectId, String adminId) {
        String oldProjectSettings = storageIo.loadProjectSettings(userId, oldProjectId);
        String oldProjectHistory = storageIo.getProjectHistory(userId, oldProjectId);
        Settings oldSettings = new Settings(new ServerJsonParser(), oldProjectSettings);
        String icon = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_ICON);
        String vcode = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_CODE);
        String vname = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_VERSION_NAME);
        String useslocation = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_USES_LOCATION);
        String aname = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_APP_NAME);
        String sizing = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_SIZING);
        String showListsAsJson = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_SHOW_LISTS_AS_JSON);
        String tutorialURL = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_TUTORIAL_URL);
        String actionBar = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_ACTIONBAR);
        String theme = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_THEME);
        String primaryColor = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR);
        String primaryColorDark = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_PRIMARY_COLOR_DARK);
        String accentColor = oldSettings.getSetting(
                SettingsConstants.PROJECT_YOUNG_ANDROID_SETTINGS,
                SettingsConstants.YOUNG_ANDROID_SETTINGS_ACCENT_COLOR);

        String newName = storageIo.getProjectName(userId, oldProjectId);
        Project newProject = new Project(newName);
        newProject.setProjectType(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE);
        newProject.setProjectHistory(oldProjectHistory);

        // Get the old project's source files and add them to new project, modifying where necessary.
        for (String oldSourceFileName : storageIo.getProjectSourceFiles(userId, oldProjectId)) {
            // String newSourceFileName = oldSourceFileName;

            String newContents = null;
            if (oldSourceFileName.equals(PROJECT_PROPERTIES_FILE_NAME)) {
                // This is the project properties file. The name of the file doesn't contain the old
                // project name.
                // newSourceFileName = oldSourceFileName;
                // For the contents of the project properties file, generate the file with the new project
                // name and qualified name.
                String qualifiedFormName = StringUtils.getQualifiedFormName(
                        storageIo.getUser(adminId).getUserEmail(), newName);
                newContents = getProjectPropertiesFileContents(newName, qualifiedFormName, icon, vcode,
                        vname, useslocation, aname, sizing, showListsAsJson, tutorialURL, actionBar,
                        theme, primaryColor, primaryColorDark, accentColor);
            }
            // else {
            //     // This is some file other than the project properties file.
            //     // oldSourceFileName may contain the old project name as a path segment, surrounded by /.
            //     // Replace the old name with the new name.
            //     newSourceFileName = StringUtils.replaceLastOccurrence(oldSourceFileName,
            //             "/" + oldName + "/", "/" + newName + "/");
            // }

            if (newContents != null) {
                // We've determined (above) that the contents of the file must change for the new project.
                // Use newContents when adding the file to the new project.
                // newProject.addTextFile(new TextFile(newSourceFileName, newContents));
                newProject.addTextFile(new TextFile(oldSourceFileName, newContents));
            } else {
                // If we get here, we know that the contents of the file can just be copied from the old
                // project. Since it might be a binary file, we copy it as a raw file (that works for both
                // text and binary files).
                // byte[] contents = storageIo.downloadRawFile(userId, oldProjectId, oldSourceFileName);
                // newProject.addRawFile(new RawFile(newSourceFileName, contents));
                byte[] contents = storageIo.downloadRawFile(userId, oldProjectId, oldSourceFileName);
                newProject.addRawFile(new RawFile(oldSourceFileName, contents));
            }
        }

        // Create the new project for adminId and return the new project's id.
        return storageIo.createProject(adminId, newProject, getProjectSettings(icon, vcode, vname,
                useslocation, aname, sizing, showListsAsJson, tutorialURL, actionBar, theme, primaryColor,
                primaryColorDark, accentColor));
    }

    @Override
    public List<ScoreInfo> getAllScoreInfos() {
        if (!userInfoProvider.getIsAdmin()) return null;
        return storageIo.getAllScoreInfos(userInfoProvider.getUserId());
    }

    @Override
    public long updateProjectScore(long projectId, int score) {
        return storageIo.updateProjectScore(userInfoProvider.getUserId(), projectId, score);
    }

    @Override
    public Map<Long, Float> similarity(List<Long> projectsId, long targetProjectId) {
        Map<Long, String> dirsMap = new HashMap<>();
        String storageRoot = Flag.createFlag("storage.root", "").get();
        for (long projectId : projectsId) {
            dirsMap.put(projectId, Paths.get(storageRoot, userInfoProvider.getUserId(),
                            Long.toString(projectId), "src").normalize().toString());
        }
        dirsMap.put(targetProjectId,
                Paths.get(storageRoot, userInfoProvider.getUserId(),
                        Long.toString(targetProjectId), "src").normalize().toString());
        Map<Long, Float> res = SimilarityAnalysis.getProjectsSimilarity(dirsMap, targetProjectId);
        if (projectsId.contains(targetProjectId)) res.put(targetProjectId, 100.f);
        storageIo.updateProjectsSimilarity(res, userInfoProvider.getUserId());
        return res;
    }

    @Override
    public List<CourseInfo> getAllAdminCourses() {
        return storageIo.getAllAdminCourses(userInfoProvider.getUserId());
    }

    @Override
    public List<ClassInfo> getClassInfos(int courseId) {
        return storageIo.getClassInfos(courseId);
    }

    @Override
    public Map<Integer, List<ClassInfo>> getAllClassInfos(List<Integer> courseIds) {
        Map<Integer, List<ClassInfo>> res = new HashMap<>(courseIds.size());

        for (int courseId : courseIds) {
            res.put(courseId, storageIo.getClassInfos(courseId));
        }

        return res;
    }

    @Override
    public CourseInfo createCourse(String courseName) {
        int courseId = storageIo.addCourse(courseName, userInfoProvider.getUserId());
        if (courseId < 0) return null;
        return new CourseInfo(courseId, courseName, userInfoProvider.getUserId(), userInfoProvider.getUserEmail());
    }

    @Override
    public boolean deleteCourse(CourseInfo info) {
       return storageIo.deleteCourse(info);
    }

    @Override
    public ClassInfo addStudent(int courseId, String userName) {
        String userId = storageIo.getUserIdByEmail(userName);
        if (userId != null && storageIo.addStudent(courseId, userId)) {
            return new ClassInfo(courseId, userId, userName);
        }
        return null;
    }

    @Override
    public List<ClassInfo> addStudents(int courseId, String filename) {
        List<ClassInfo> res = new ArrayList<>();

        try {
            Scanner scanner = new Scanner(storageIo.openTempFile(filename));
            while (scanner.hasNextLine()) {
                String userName = scanner.nextLine().trim();
                String userId = storageIo.getUserIdByEmail(userName);
                if (userId != null && storageIo.addStudent(courseId, userId)) {
                    res.add(new ClassInfo(courseId, userId, userName));
                }
            }
            scanner.close();
            storageIo.deleteTempFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public boolean deleteStudent(int courseId, String userId) {
        return storageIo.deleteStudent(courseId, userId);
    }
}
