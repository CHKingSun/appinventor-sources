package com.google.appinventor.server.storage;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.project.*;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SQLStorageIo implements StorageIo {
    private static final Flag<Boolean> requireTos = Flag.createFlag("require.tos", false);
    private static final Flag<String> storageRoot = Flag.createFlag("storage.root", "");
    private static final Flag<String> dbAddress = Flag.createFlag("db.address", "");
    private static final Flag<String> dbUsername = Flag.createFlag("db.username", "");
    private static final Flag<String> dbPassword = Flag.createFlag("db.password", "");
    private static DataSource ds = null;

    public SQLStorageIo() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + dbAddress.get());
        config.setUsername(dbUsername.get());
        config.setPassword(dbPassword.get());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("useLocalTransactionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        ds = new HikariDataSource(config);

        // createTables();
    }

    private void createTables() {
        try (Connection conn = getConnection()) {
            beginTransaction(conn);

            Statement statement = conn.createStatement();
            // users
            statement.executeUpdate("create table users(userId varchar(64) primary key, email varchar(255), name varchar(255), "
                    + "visited timestamp, settings text, tosAccepted tinyint(1), isAdmin tinyint(1), "
                    + "sessionId varchar(255), password varchar(255))");
            statement.executeUpdate("create index index_email on users(email)");

            // nonce
            statement.executeUpdate("create table nonces(nonceValue varchar(255) primary key, userId varchar(64), "
                    + "projectId int, time timestamp)");
            statement.executeUpdate("create index index_ndate on nonces(time)");

            // pwdata
            statement.executeUpdate("create table pwdata(userId varchar(64), email varchar(255), time timestamp)");
            statement.executeUpdate("create index index_pdate on pwdata(time)");

            // rendezvous
            statement.executeUpdate("create table rendezvous(rkey varchar(64) primary key, ipaddr varchar(64), time timestamp)");

            // groups
            statement.executeUpdate("create table groups(groupId int primary key autoincrement, name varchar(255))");
            statement.executeUpdate("create table gusers(groupId int, userId varchar(64), primary key(groupId, userId))");

            // backpack
            statement.executeUpdate("create table backpack(backpackId varchar(255) primary key, content text)");

            // buildStatus
            statement.executeUpdate("create table build_status(userId varchar(64), projectId int, progress tinyint, primary key(userId, projectId))");

            statement.close();
            conn.commit();
            endTransaction(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() throws Exception {
        return ds.getConnection();
    }

    private void beginTransaction(Connection conn) {
        try {
            conn.setAutoCommit(false);
        } catch (Exception e) {
        }
    }

    private void endTransaction(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (Exception e) {
        }
    }

    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public User getUser(String userId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select * from users where userId=?");
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String r_userId = result.getString("userId");
                String r_email = result.getString("email");
                String r_name = result.getString("name");
                boolean r_tosAccepted = result.getBoolean("tosAccepted") || (!requireTos.get());
                boolean r_isAdmin = result.getBoolean("isAdmin");
                String r_sessionId = result.getString("sessionId");
                String r_password = result.getString("password");
                result.close();
                statement.close();

                User user = new User(r_userId, r_email, r_name, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                        r_tosAccepted, r_isAdmin, User.USER, r_sessionId);
                user.setPassword(r_password);
                return user;
            }
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return createUser(userId, null);
    }

    @Override
    public User getUser(String userId, String email) {
        User user = getUser(userId);
        if (!user.getUserEmail().equals(email))
            setUserEmail(userId, email);
        return user;
    }

    @Override
    public User getUserFromEmail(String email) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select * from users where email=?");
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String r_userId = result.getString("userId");
                String r_email = result.getString("email");
                String r_name = result.getString("name");
                boolean r_tosAccepted = result.getBoolean("tosAccepted") || (!requireTos.get());
                boolean r_isAdmin = result.getBoolean("isAdmin");
                String r_sessionId = result.getString("sessionId");
                String r_password = result.getString("password");
                result.close();
                statement.close();

                User user = new User(r_userId, r_email, r_name, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                        r_tosAccepted, r_isAdmin, User.USER, r_sessionId);
                user.setPassword(r_password);
                return user;
            }
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return createUser(null, email);
    }

    private User createUser(String userId, String email) {
        if (userId == null)
            userId = UUID.randomUUID().toString();
        User user = new User(userId, email, null, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                false, false, User.USER, "");

        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("insert into users values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, userId);
            statement.setString(2, email);
            statement.setString(3, null);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.setString(5, "{}");
            statement.setBoolean(6, false);
            statement.setBoolean(7, false);
            statement.setString(8, "");
            statement.setString(9, "");
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Path userDir = Paths.get(storageRoot.get(), userId);
            Files.createDirectory(userDir);
            String data = "{\"nextProjectId\": 1}";
            Files.write(userDir.resolve("projects.json"), data.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return user;
    }

    @Override
    public void setUserEmail(String userId, String email) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set email=? where userId=?");
            statement.setString(1, email);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setTosAccepted(String userId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set tosAccepted=? where userId=?");
            statement.setBoolean(1, true);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setUserSessionId(String userId, String sessionId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set sessionId=? where userId=?");
            statement.setString(1, sessionId);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setUserPassword(String userId, String password) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set password=? where userId=?");
            statement.setString(1, password);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String loadSettings(String userId) {
        String r_settings = "";
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select settings from users where userId=?");
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_settings = result.getString("settings");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_settings;
    }

    @Override
    public void setUserName(String userId, String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set name=? where userId=?");
            statement.setString(1, name);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUserName(String userId) {
        String r_name = "user";
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select name from users where userId=?");
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_name = result.getString("name");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_name;
    }

    @Override
    public String getUserLink(String userId) {
        return "";
    }

    @Override
    public void setUserLink(String userId, String link) {

    }

    @Override
    public int getUserEmailFrequency(String userId) {
        return User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY;
    }

    @Override
    public void setUserEmailFrequency(String userId, int emailFrequency) {

    }

    @Override
    public void storeSettings(String userId, String settings) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update users set settings=? where userId=?");
            statement.setString(1, settings);
            statement.setString(2, userId);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long createProject(String userId, Project project, String projectSettings) {
        long r_projectId = 0;
        try {
            Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            r_projectId = json.getInt("nextProjectId");

            JSONObject newProject = new JSONObject();
            newProject.put("name", project.getProjectName());
            newProject.put("settings", projectSettings);
            newProject.put("created", System.currentTimeMillis());
            newProject.put("modified", System.currentTimeMillis());
            newProject.put("history", project.getProjectHistory());

            json.put(String.valueOf(r_projectId), newProject);
            json.increment("nextProjectId");
            Files.write(projectsJSON, json.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (r_projectId != 0) {
            Path projectDir = Paths.get(storageRoot.get(), userId, String.valueOf(r_projectId));
            for (TextFile textFile : project.getSourceFiles()) {
                Path path = projectDir.resolve(textFile.getFileName());
                try {
                    Files.createDirectories(path.getParent());
                    Files.write(path, textFile.getContent().getBytes("UTF-8"));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            for (RawFile rawFile : project.getRawSourceFiles()) {
                Path path = projectDir.resolve(rawFile.getFileName());
                try {
                    Files.createDirectories(path.getParent());
                    Files.write(path, rawFile.getContent());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return r_projectId;
    }

    @Override
    public void deleteProject(String userId, long projectId) {
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            json.remove(String.valueOf(projectId));
            Files.write(projectsJSON, json.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Path projectDir = Paths.get(storageRoot.get(), userId, String.valueOf(projectId));
        try {
            Files.walkFileTree(projectDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getProjects(String userId) {
        List<Long> projects = new LinkedList<>();
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            for (Object obj : json.keySet()) {
                String key = (String) obj;
                if (!key.equals("nextProjectId"))
                    projects.add(Long.parseLong(key));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return projects;
    }

    @Override
    public void setProjectGalleryId(String userId, long projectId, long galleryId) {

    }

    @Override
    public void setProjectAttributionId(String userId, long projectId, long attributionId) {

    }

    @Override
    public String loadProjectSettings(String userId, long projectId) {
        String r_settings = "";
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            r_settings = project.getString("settings");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_settings;
    }

    @Override
    public void storeProjectSettings(String userId, long projectId, String settings) {
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            project.put("settings", settings);
            Files.write(projectsJSON, json.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProjectType(String userId, long projectId) {
        return "YoungAndroid";
    }

    @Override
    public UserProject getUserProject(String userId, long projectId) {
        UserProject userProject = null;
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            String r_name = project.getString("name");
            long r_created = project.getLong("created");
            long r_modified = project.getLong("modified");
            userProject = new UserProject(projectId, r_name, "YoungAndroid", r_created, r_modified, 0, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return userProject;
    }

    @Override
    public List<UserProject> getUserProjects(String userId, List<Long> projectIds) {
        List<UserProject> userProjects = new LinkedList<>();
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            for (Object obj : json.keySet()) {
                String key = (String) obj;
                if (key.equals("nextProjectId"))
                    continue;

                JSONObject project = json.getJSONObject(key);
                long r_projectId = Long.parseLong(key);
                String r_name = project.getString("name");
                long r_created = project.getLong("created");
                long r_modified = project.getLong("modified");
                userProjects.add(new UserProject(r_projectId, r_name, "YoungAndroid", r_created, r_modified, 0, 0));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return userProjects;
    }

    @Override
    public String getProjectName(String userId, long projectId) {
        String r_name = null;
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            r_name = project.getString("name");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_name;
    }

    @Override
    public long getProjectDateModified(String userId, long projectId) {
        long r_time = 0;
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            r_time = project.getLong("modified");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_time;
    }

    private void setProjectDateModified(String userId, long projectId, long dateModified) {
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            project.put("modified", dateModified);
            Files.write(projectsJSON, json.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getProjectHistory(String userId, long projectId) {
        String r_history = null;
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            r_history = project.getString("history");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_history;
    }

    @Override
    public long getProjectDateCreated(String userId, long projectId) {
        long r_time = 0;
        Path projectsJSON = Paths.get(storageRoot.get(), userId, "projects.json");
        try {
            String data = new String(Files.readAllBytes(projectsJSON), "UTF-8");
            JSONObject json = new JSONObject(data);
            JSONObject project = json.getJSONObject(String.valueOf(projectId));
            r_time = project.getLong("created");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_time;
    }

    @Override
    public void addFilesToUser(String userId, String... fileIds) {

    }

    @Override
    public List<String> getUserFiles(String userId) {
        final List<String> files = new LinkedList<>();
        final Path userDir = Paths.get(storageRoot.get(), userId);
        try {
            Files.walkFileTree(userDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String str = userDir.relativize(file).toString();
                    files.add(str.replace('\\', '/'));
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    @Override
    public void uploadUserFile(String userId, String fileId, String content, String encoding) {
        try {
            uploadRawUserFile(userId, fileId, content.getBytes(encoding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadRawUserFile(String userId, String fileName, byte[] content) {
        Path path = Paths.get(storageRoot.get(), userId, fileName);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String downloadUserFile(String userId, String fileId, String encoding) {
        String content = null;
        try {
            content = new String(downloadRawUserFile(userId, fileId), encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    @Override
    public byte[] downloadRawUserFile(String userId, String fileName) {
        byte content[] = null;
        Path path = Paths.get(storageRoot.get(), userId, fileName);
        if (Files.notExists(path))
            return null;

        try {
            content = Files.readAllBytes(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    @Override
    public void deleteUserFile(String userId, String fileId) {
        Path path = Paths.get(storageRoot.get(), userId, fileId);
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getMaxJobSizeBytes() {
        return 5 * 1024 * 1024;
    }

    @Override
    public void addSourceFilesToProject(String userId, long projectId, boolean changeModDate, String... fileIds) {

    }

    @Override
    public void addOutputFilesToProject(String userId, long projectId, String... fileIds) {

    }

    @Override
    public void removeSourceFilesFromProject(String userId, long projectId, boolean changeModDate, String... fileIds) {
        Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId));
        try {
            boolean anyFilesDeleted = false;
            for (String file : fileIds)
                anyFilesDeleted |= Files.deleteIfExists(path.resolve(file));
            if (anyFilesDeleted && changeModDate)
                setProjectDateModified(userId, projectId, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeOutputFilesFromProject(String userId, long projectId, String... fileIds) {
        Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId));
        try {
            for (String file : fileIds)
                Files.deleteIfExists(path.resolve(file));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getProjectSourceFiles(String userId, long projectId) {
        final List<String> files = new LinkedList<>();
        final Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId));
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (!(fileName.endsWith(".apk") || fileName.endsWith(".out"))) {
                        String str = path.relativize(file).toString();
                        files.add(str.replace('\\', '/'));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    @Override
    public List<String> getProjectOutputFiles(String userId, long projectId) {
        final List<String> files = new LinkedList<>();
        final Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId));
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String fileName = file.getFileName().toString();
                    if (fileName.endsWith(".apk") || fileName.endsWith(".out")) {
                        String str = path.relativize(file).toString();
                        files.add(str.replace('\\', '/'));
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return files;
    }

    @Override
    public long getProjectGalleryId(String userId, long projectId) {
        return 0;
    }

    @Override
    public long getProjectAttributionId(long projectId) {
        return 0;
    }

    @Override
    public long uploadFile(long projectId, String fileId, String userId, String content, String encoding) throws BlocksTruncatedException {
        try {
            return uploadRawFile(projectId, fileId, userId, false, content.getBytes(encoding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long uploadFileForce(long projectId, String fileId, String userId, String content, String encoding) {
        try {
            return uploadRawFile(projectId, fileId, userId, true, content.getBytes(encoding));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long uploadRawFile(long projectId, String fileId, String userId, boolean force, byte[] content) throws BlocksTruncatedException {
        Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId), fileId);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content);
            setProjectDateModified(userId, projectId, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis();
    }

    @Override
    public long uploadRawFileForce(long projectId, String fileId, String userId, byte[] content) {
        try {
            return uploadRawFile(projectId, fileId, userId, true, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long deleteFile(String userId, long projectId, String fileId) {
        Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId), fileId);
        try {
            if (Files.deleteIfExists(path))
                setProjectDateModified(userId, projectId, System.currentTimeMillis());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return System.currentTimeMillis();
    }

    @Override
    public String downloadFile(String userId, long projectId, String fileId, String encoding) {
        String content = null;
        try {
            content = new String(downloadRawFile(userId, projectId, fileId), encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    @Override
    public void recordCorruption(String userId, long projectId, String fileId, String message) {

    }

    @Override
    public byte[] downloadRawFile(String userId, long projectId, String fileId) {
        byte content[] = null;
        Path path = Paths.get(storageRoot.get(), userId, String.valueOf(projectId), fileId);
        if (Files.notExists(path))
            return null;

        try {
            content = Files.readAllBytes(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return content;
    }

    @Override
    public String uploadTempFile(byte[] content) throws IOException {
        String fileName = "__TEMP__" + UUID.randomUUID().toString();
        Path tempFile = Paths.get(storageRoot.get(), "temp", fileName);
        if(Files.notExists(tempFile.getParent()))
            Files.createDirectories(tempFile.getParent());
        Files.write(tempFile, content);
        return fileName;
    }

    @Override
    public InputStream openTempFile(String fileName) throws IOException {
        Path path = Paths.get(storageRoot.get(), "temp", fileName);
        return Files.newInputStream(path);
    }

    @Override
    public void deleteTempFile(String fileName) throws IOException {
        Path path = Paths.get(storageRoot.get(), "temp", fileName);
        Files.deleteIfExists(path);
    }

    @Override
    public Motd getCurrentMotd() {
        return new Motd(1, "", "");
    }

    @Override
    public ProjectSourceZip exportProjectSourceZip(String userId, long projectId, boolean includeProjectHistory, boolean includeAndroidKeystore, @Nullable String zipName, boolean includeYail, boolean includeScreenShots, boolean forGallery, boolean fatalError) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int fileCount = 0;
        ZipOutputStream out = new ZipOutputStream(buf);

        List<String> sources = getProjectSourceFiles(userId, projectId);
        for (String srcFile : sources) {
            if (srcFile.endsWith(".yail") && (!includeYail))
                continue;
            if (srcFile.startsWith("screenshot") && (!includeScreenShots))
                continue;
            byte data[] = downloadRawFile(userId, projectId, srcFile);
            out.putNextEntry(new ZipEntry(srcFile));
            out.write(data, 0, data.length);
            out.closeEntry();
            fileCount++;
        }

        if (includeProjectHistory) {
            String history = getProjectHistory(userId, projectId);
            if (history != null) {
                byte data[] = history.getBytes("UTF-8");
                out.putNextEntry(new ZipEntry("youngandroidproject/remix_history"));
                out.write(data, 0, data.length);
                out.closeEntry();
                fileCount++;
            }
        }

        if (includeAndroidKeystore) {
            byte data[] = downloadRawUserFile(userId, "android.keystore");
            if (data != null) {
                out.putNextEntry(new ZipEntry("android.keystore"));
                out.write(data, 0, data.length);
                out.closeEntry();
                fileCount++;
            }
        }

        out.close();
        String projectName = getProjectName(userId, projectId);
        if (zipName == null)
            zipName = projectName + ".aia";
        ProjectSourceZip projectSourceZip = new ProjectSourceZip(zipName, buf.toByteArray(), fileCount);
        projectSourceZip.setMetadata(projectName);
        return projectSourceZip;
    }

    @Override
    public boolean userExists(String uid) {
        boolean r_exists = false;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select userId from users where userId=?");
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_exists = uid.equals(result.getString("userId"));
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_exists;
    }

    @Override
    public String findUserByEmail(String email) throws NoSuchElementException {
        String r_userId = null;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select userId from users where email=?");
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_userId = result.getString("userId");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (r_userId != null)
            return r_userId;
        else
            throw new NoSuchElementException();
    }

    @Override
    public String findIpAddressByKey(String key) {
        String r_ipaddr = null;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select ipaddr from rendezvous where rkey=?");
            statement.setString(1, key);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_ipaddr = result.getString("ipaddr");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_ipaddr;
    }

    @Override
    public void storeIpAddressByKey(String key, String ipAddress) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("replace into rendezvous values (?, ?, ?)");
            statement.setString(1, key);
            statement.setString(2, ipAddress);
            statement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean checkWhiteList(String email) {
        return true;
    }

    @Override
    public void storeFeedback(String notes, String foundIn, String faultData, String comments, String datestamp, String email, String projectId) {

    }

    @Override
    public Nonce getNoncebyValue(String nonceValue) {
        Nonce nonce = null;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select * from nonces where nonceValue=?");
            statement.setString(1, nonceValue);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                String r_nonceValue = result.getString("nonceValue");
                String r_userId = result.getString("userId");
                long r_projectId = result.getLong("projectId");
                Timestamp r_timestamp = result.getTimestamp("time");
                nonce = new Nonce(r_nonceValue, r_userId, r_projectId, new Date(r_timestamp.getTime()));
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return nonce;
    }

    @Override
    public void storeNonce(String nonceValue, String userId, long projectId) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("insert into nonces values (?, ?, ?, ?)");
            statement.setString(1, nonceValue);
            statement.setString(2, userId);
            statement.setLong(3, projectId);
            statement.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cleanupNonces() {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("delete from nonces where time<?");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis() - 3 * 60 * 60 * 1000L));
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void checkUpgrade(String userId) {

    }

    @Override
    public void doUpgrade(String userId) {

    }

    @Override
    public SplashConfig getSplashConfig() {
        return new SplashConfig(0, 640, 100, "Welcome to MIT App Inventor");
    }

    @Override
    public StoredData.PWData createPWData(String email) {
        StoredData.PWData pwData = new StoredData.PWData();
        pwData.id = UUID.randomUUID().toString();
        pwData.email = email;
        pwData.timestamp = new Timestamp(System.currentTimeMillis());

        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("insert into pwdata values (?, ?, ?)");
            statement.setString(1, pwData.id);
            statement.setString(2, pwData.email);
            statement.setTimestamp(3, (Timestamp) pwData.timestamp);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return pwData;
    }

    @Override
    public StoredData.PWData findPWData(String uid) {
        StoredData.PWData pwData = null;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select * from pwdata where userId=?");
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                pwData = new StoredData.PWData();
                pwData.id = result.getString("userId");
                pwData.email = result.getString("email");
                pwData.timestamp = result.getTimestamp("time");
            }
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return pwData;
    }

    @Override
    public void cleanuppwdata() {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("delete from pwdata where time<?");
            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis() - 60 * 60 * 1000L));
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<AdminUser> searchUsers(String partialEmail) {
        return new ArrayList<>();
    }

    @Override
    public void storeUser(AdminUser user) throws AdminInterfaceException {

    }

    @Override
    public List<String> listUsers() {
        List<String> users = new LinkedList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select userId from users");
            ResultSet result = statement.executeQuery();
            while (result.next())
                users.add(result.getString("userId"));
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public long getUserLastVisited(String uid) {
        long r_visited = System.currentTimeMillis();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select visited from users where userId=?");
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_visited = result.getTimestamp("visited").getTime();
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_visited;
    }

    @Override
    public void removeUsers(List<String> users) {
        Connection conn = null;
        try {
            conn = getConnection();
            beginTransaction(conn);
            for (String uid : users) {
                PreparedStatement statement = conn.prepareStatement("delete from users where userId=?");
                statement.setString(1, uid);
                statement.executeUpdate();
                statement.close();

                statement = conn.prepareStatement("delete from gusers where userId=?");
                statement.setString(1, uid);
                statement.executeUpdate();
                statement.close();
            }
            conn.commit();
            endTransaction(conn);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ee) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }

        for (String uid : users) {
            Path path = Paths.get(storageRoot.get(), uid);
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.deleteIfExists(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.deleteIfExists(dir);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void createGroup(String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("insert into groups(name) values (?)");
            statement.setString(1, name);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeGroup(long gid) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("delete from gusers where groupId=?");
            statement.setLong(1, gid);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("delete from groups where groupId=?");
            statement.setLong(1, gid);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long findGroupByName(String name) {
        long r_groupId = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select groupId from groups where name=?");
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_groupId = result.getLong("groupId");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_groupId;
    }

    @Override
    public List<Long> listGroups() {
        List<Long> groups = new LinkedList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select groupId from groups");
            ResultSet result = statement.executeQuery();
            while (result.next())
                groups.add(result.getLong("groupId"));
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return groups;
    }

    @Override
    public String getGroupName(long gid) {
        String r_name = null;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select name from groups where groupId=?");
            statement.setLong(1, gid);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_name = result.getString("name");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_name;
    }

    @Override
    public void setGroupName(long gid, String name) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("update groups set name=? where groupId=?");
            statement.setString(1, name);
            statement.setLong(2, gid);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Long> getUserJoinedGroups(String uid) {
        List<Long> groups = new LinkedList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select groupId from gusers where userId=?");
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            while (result.next())
                groups.add(result.getLong("groupId"));
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return groups;
    }

    @Override
    public List<String> getGroupUsers(long gid) {
        List<String> users = new LinkedList<>();
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select userId from gusers where groupId=?");
            statement.setLong(1, gid);
            ResultSet result = statement.executeQuery();
            while (result.next())
                users.add(result.getString("userId"));
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    @Override
    public void addUsersToGroup(long gid, List<String> users) {
        Connection conn = null;
        try {
            conn = getConnection();
            beginTransaction(conn);
            for (String userId : users) {
                PreparedStatement statement = conn.prepareStatement("insert ignore into gusers values (?, ?)");
                statement.setLong(1, gid);
                statement.setString(2, userId);
                statement.executeUpdate();
                statement.close();
            }
            conn.commit();
            endTransaction(conn);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ee) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public void removeUsersFromGroup(long gid, List<String> users) {
        Connection conn = null;
        try {
            conn = getConnection();
            beginTransaction(conn);
            for (String userId : users) {
                PreparedStatement statement = conn.prepareStatement("delete from gusers where userId=?");
                statement.setString(1, userId);
                statement.executeUpdate();
                statement.executeUpdate();
                statement.close();
            }
            conn.commit();
            endTransaction(conn);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception ee) {
                }
            }
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public String downloadBackpack(String backPackId) {
        String r_content = "";
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select content from backpack where backPackId=?");
            statement.setString(1, backPackId);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_content = result.getString("content");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_content;
    }

    @Override
    public void uploadBackpack(String backPackId, String content) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("replace into backpack values (?, ?)");
            statement.setString(1, backPackId);
            statement.setString(2, content);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void storeBuildStatus(String userId, long projectId, int progress) {
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("replace into build_status values (?, ?, ?)");
            statement.setString(1, userId);
            statement.setLong(2, projectId);
            statement.setInt(3, progress);
            statement.executeUpdate();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getBuildStatus(String userId, long projectId) {
        int r_progress = 0;
        try (Connection conn = getConnection()) {
            PreparedStatement statement = conn.prepareStatement("select progress from build_status where userId=? and projectId=?");
            statement.setString(1, userId);
            statement.setLong(2, projectId);
            ResultSet result = statement.executeQuery();
            if (result.next())
                r_progress = result.getInt("progress");
            result.close();
            statement.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return r_progress;
    }
}