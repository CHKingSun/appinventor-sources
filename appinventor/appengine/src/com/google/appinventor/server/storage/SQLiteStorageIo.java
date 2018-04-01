package com.google.appinventor.server.storage;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.shared.rpc.AdminInterfaceException;
import com.google.appinventor.shared.rpc.BlocksTruncatedException;
import com.google.appinventor.shared.rpc.Motd;
import com.google.appinventor.shared.rpc.Nonce;
import com.google.appinventor.shared.rpc.admin.AdminUser;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.user.SplashConfig;
import com.google.appinventor.shared.rpc.user.User;
import org.sqlite.SQLiteConfig;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.*;

import java.sql.*;

public class SQLiteStorageIo implements StorageIo {
    static final Flag<Boolean> requireTos = Flag.createFlag("require.tos", false);
    static final Flag<String> storageRoot = Flag.createFlag("storage.root", "");
    static final String DATABASE = storageRoot.get() + "/data.sqlite";
    private ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    public SQLiteStorageIo(){
        try{
            Class.forName("org.sqlite.JDBC");
            if(!new File(DATABASE).exists())
                createDatabase();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void createDatabase(){
        try(Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE)){
            beginTransaction(conn);

            Statement statement = conn.createStatement();
            // users
            statement.executeUpdate("create table users(userId string primary key, email string, name string, "
                    + "visited timestamp, settings string, tosAccepted boolean, isAdmin boolean, "
                    + "sessionId string, password string)");
            statement.executeUpdate("create index index_email on users(email)");

            // nonce
            statement.executeUpdate("create table nonces(nonceValue string primary key, userId string, "
                    + "projectId integer, timestamp timestamp)");
            statement.executeUpdate("create index index_ndate on nonces(timestamp)");

            // pwdata
            statement.executeUpdate("create table pwdata(userId string, email string, timestamp timestamp)");
            statement.executeUpdate("create index index_pdate on pwdata(timestamp)");

            // rendezvous
            statement.executeUpdate("create table rendezvous(key string primary key, ipaddr string, timestamp timestamp)");

            // groups
            statement.executeUpdate("create table groups(groupId integer primary key autoincrement, name string)");
            statement.executeUpdate("create table gusers(groupId integer, userId string)");
            statement.executeUpdate("create index index_ggroupId on gusers(groupId)");
            statement.executeUpdate("create index index_guserId on gusers(userId)");

            // backpack
            statement.executeUpdate("create table backpack(backpackId string primary key, content string)");

            statement.close();
            conn.commit();
            endTransaction(conn);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Connection getDatabaseConnection(){
        Connection conn = connectionHolder.get();
        if(conn == null) {
            try {
                SQLiteConfig config = new SQLiteConfig();
                config.setSynchronous(SQLiteConfig.SynchronousMode.NORMAL);
                conn = DriverManager.getConnection("jdbc:sqlite:" + DATABASE, config.toProperties());
                connectionHolder.set(conn);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return conn;
    }

    public void beginTransaction(Connection conn){
        try{
            conn.setAutoCommit(false);
        }catch(Exception e){}
    }

    public void endTransaction(Connection conn){
        try{
            conn.setAutoCommit(true);
        }catch(Exception e){}
    }

    public void closeConnection(Connection conn){
        if(conn != null) {
            if(connectionHolder.get() == conn)
                connectionHolder.remove();
            try {
                conn.close();
            }catch(Exception e){}
            conn = null;
        }
    }

    @Override
    public User getUser(String userId) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select * from users where userId=?")){
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if(result.next()){
                String r_userId = result.getString("userId");
                String r_email = result.getString("email");
                String r_name = result.getString("name");
                boolean r_tosAccepted = result.getBoolean("tosAccepted") || (!requireTos.get());
                boolean r_isAdmin = result.getBoolean("isAdmin");
                String r_sessionId = result.getString("sessionId");
                String r_password = result.getString("password");
                result.close();

                User user = new User(r_userId, r_email, r_name, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                        r_tosAccepted, r_isAdmin, User.USER, r_sessionId);
                user.setPassword(r_password);
                return user;
            }
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return createUser(userId, null);
    }

    @Override
    public User getUser(String userId, String email) {
        User user = getUser(userId);
        if(!user.getUserEmail().equals(email))
            setUserEmail(userId, email);
        return user;
    }

    @Override
    public User getUserFromEmail(String email) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select * from users where email=?")){
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if(result.next()){
                String r_userId = result.getString("userId");
                String r_email = result.getString("email");
                String r_name = result.getString("name");
                boolean r_tosAccepted = result.getBoolean("tosAccepted") || (!requireTos.get());
                boolean r_isAdmin = result.getBoolean("isAdmin");
                String r_sessionId = result.getString("sessionId");
                String r_password = result.getString("password");
                result.close();

                User user = new User(r_userId, r_email, r_name, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                        r_tosAccepted, r_isAdmin, User.USER, r_sessionId);
                user.setPassword(r_password);
                return user;
            }
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return createUser(null, email);
    }

    private User createUser(String userId, String email){
        if(userId == null)
            userId = UUID.randomUUID().toString();
        User user = new User(userId, email, null, "", User.DEFAULT_EMAIL_NOTIFICATION_FREQUENCY,
                false, false, User.USER, "");

        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("insert into users values (?, ?, ?, ?, ?, ?, ?, ?, ?)")){
            statement.setString(1, userId);
            statement.setString(2, email);
            statement.setString(3, null);
            statement.setDate(4, new Date(System.currentTimeMillis()));
            statement.setString(5, "{}");
            statement.setBoolean(6, false);
            statement.setBoolean(7, false);
            statement.setString(8, "");
            statement.setString(9, "");
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return user;
    }

    @Override
    public void setUserEmail(String userId, String email) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set email=? where userId=?")){
            statement.setString(1, email);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void setTosAccepted(String userId) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set tosAccepted=? where userId=?")){
            statement.setBoolean(1, true);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void setUserSessionId(String userId, String sessionId) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set sessionId=? where userId=?")){
            statement.setString(1, sessionId);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void setUserPassword(String userId, String password) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set password=? where userId=?")){
            statement.setString(1, password);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public String loadSettings(String userId) {
        Connection conn = getDatabaseConnection();
        String r_settings = "";
        try(PreparedStatement statement = conn.prepareStatement("select settings from users where userId=?")){
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_settings = result.getString("settings");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_settings;
    }

    @Override
    public void setUserName(String userId, String name) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set name=? where userId=?")){
            statement.setString(1, name);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public String getUserName(String userId) {
        Connection conn = getDatabaseConnection();
        String r_name = "user";
        try(PreparedStatement statement = conn.prepareStatement("select name from users where userId=?")){
            statement.setString(1, userId);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_name = result.getString("name");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
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
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update users set settings=? where userId=?")){
            statement.setString(1, settings);
            statement.setString(2, userId);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public long createProject(String userId, Project project, String projectSettings) {
        return 0;
    }

    @Override
    public void deleteProject(String userId, long projectId) {

    }

    @Override
    public List<Long> getProjects(String userId) {
        return null;
    }

    @Override
    public void setProjectGalleryId(String userId, long projectId, long galleryId) {

    }

    @Override
    public void setProjectAttributionId(String userId, long projectId, long attributionId) {

    }

    @Override
    public String loadProjectSettings(String userId, long projectId) {
        return null;
    }

    @Override
    public void storeProjectSettings(String userId, long projectId, String settings) {

    }

    @Override
    public String getProjectType(String userId, long projectId) {
        return "YoungAndroid";
    }

    @Override
    public UserProject getUserProject(String userId, long projectId) {
        return null;
    }

    @Override
    public List<UserProject> getUserProjects(String userId, List<Long> projectIds) {
        return null;
    }

    @Override
    public String getProjectName(String userId, long projectId) {
        return null;
    }

    @Override
    public long getProjectDateModified(String userId, long projectId) {
        return 0;
    }

    @Override
    public String getProjectHistory(String userId, long projectId) {
        return null;
    }

    @Override
    public long getProjectDateCreated(String userId, long projectId) {
        return 0;
    }

    @Override
    public void addFilesToUser(String userId, String... fileIds) {

    }

    @Override
    public List<String> getUserFiles(String userId) {
        return null;
    }

    @Override
    public void uploadUserFile(String userId, String fileId, String content, String encoding) {

    }

    @Override
    public void uploadRawUserFile(String userId, String fileName, byte[] content) {

    }

    @Override
    public String downloadUserFile(String userId, String fileId, String encoding) {
        return null;
    }

    @Override
    public byte[] downloadRawUserFile(String userId, String fileName) {
        return new byte[0];
    }

    @Override
    public void deleteUserFile(String userId, String fileId) {

    }

    @Override
    public int getMaxJobSizeBytes() {
        // return 5242880;
        return 20*1024*1024;
    }

    @Override
    public void addSourceFilesToProject(String userId, long projectId, boolean changeModDate, String... fileIds) {

    }

    @Override
    public void addOutputFilesToProject(String userId, long projectId, String... fileIds) {

    }

    @Override
    public void removeSourceFilesFromProject(String userId, long projectId, boolean changeModDate, String... fileIds) {

    }

    @Override
    public void removeOutputFilesFromProject(String userId, long projectId, String... fileIds) {

    }

    @Override
    public List<String> getProjectSourceFiles(String userId, long projectId) {
        return null;
    }

    @Override
    public List<String> getProjectOutputFiles(String userId, long projectId) {
        return null;
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
        return 0;
    }

    @Override
    public long uploadFileForce(long projectId, String fileId, String userId, String content, String encoding) {
        return 0;
    }

    @Override
    public long uploadRawFile(long projectId, String fileId, String userId, boolean force, byte[] content) throws BlocksTruncatedException {
        return 0;
    }

    @Override
    public long uploadRawFileForce(long projectId, String fileId, String userId, byte[] content) {
        return 0;
    }

    @Override
    public long deleteFile(String userId, long projectId, String fileId) {
        return 0;
    }

    @Override
    public String downloadFile(String userId, long projectId, String fileId, String encoding) {
        return null;
    }

    @Override
    public void recordCorruption(String userId, long projectId, String fileId, String message) {

    }

    @Override
    public byte[] downloadRawFile(String userId, long projectId, String fileId) {
        return new byte[0];
    }

    @Override
    public String uploadTempFile(byte[] content) throws IOException {
        return null;
    }

    @Override
    public InputStream openTempFile(String fileName) throws IOException {
        return null;
    }

    @Override
    public void deleteTempFile(String fileName) throws IOException {

    }

    @Override
    public Motd getCurrentMotd() {
        return null;
    }

    @Override
    public ProjectSourceZip exportProjectSourceZip(String userId, long projectId, boolean includeProjectHistory, boolean includeAndroidKeystore, @Nullable String zipName, boolean includeYail, boolean includeScreenShots, boolean forGallery, boolean fatalError) throws IOException {
        return null;
    }

    @Override
    public String findUserByEmail(String email) throws NoSuchElementException {
        Connection conn = getDatabaseConnection();
        String r_userId = null;
        try(PreparedStatement statement = conn.prepareStatement("select userId from users where email=?")){
            statement.setString(1, email);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_userId = result.getString("userId");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        if(r_userId != null)
            return r_userId;
        else
            throw new NoSuchElementException();
    }

    @Override
    public String findIpAddressByKey(String key) {
        Connection conn = getDatabaseConnection();
        String r_ipaddr = null;
        try(PreparedStatement statement = conn.prepareStatement("select ipaddr from rendezvous where key=?")){
            statement.setString(1, key);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_ipaddr =result.getString("ipaddr");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_ipaddr;
    }

    @Override
    public void storeIpAddressByKey(String key, String ipAddress) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("insert or replace into rendezvous values (?, ?, ?)")){
            statement.setString(1, key);
            statement.setString(2, ipAddress);
            statement.setDate(3, new Date(System.currentTimeMillis()));
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
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
        Connection conn = getDatabaseConnection();
        Nonce nonce = null;
        try(PreparedStatement statement = conn.prepareStatement("select * from nonces where nonceValue=?")){
            statement.setString(1, nonceValue);
            ResultSet result = statement.executeQuery();
            if(result.next()) {
                String r_nonceValue = result.getString("nonceValue");
                String r_userId = result.getString("userId");
                long r_projectId = result.getLong("projectId");
                Date r_timestamp = result.getDate("timestamp");
                nonce = new Nonce(r_nonceValue, r_userId, r_projectId, r_timestamp);
            }
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return nonce;
    }

    @Override
    public void storeNonce(String nonceValue, String userId, long projectId) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("insert into nonces values (?, ?, ?, ?)")){
            statement.setString(1, nonceValue);
            statement.setString(2, userId);
            statement.setLong(3, projectId);
            statement.setDate(4, new Date(System.currentTimeMillis()));
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void cleanupNonces() {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("delete from nonces where timestamp<?")){
            statement.setDate(1, new Date(System.currentTimeMillis() - 3*60*60*1000L));
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
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
        return null;
    }

    @Override
    public StoredData.PWData createPWData(String email) {
        Connection conn = getDatabaseConnection();

        StoredData.PWData pwData = new StoredData.PWData();
        pwData.id = UUID.randomUUID().toString();
        pwData.email = email;
        pwData.timestamp = new Date(System.currentTimeMillis());
        try(PreparedStatement statement = conn.prepareStatement("insert into pwdata values (?, ?, ?)")){
            statement.setString(1, pwData.id);
            statement.setString(2, pwData.email);
            statement.setDate(3, (java.sql.Date)pwData.timestamp);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return pwData;
    }

    @Override
    public StoredData.PWData findPWData(String uid) {
        Connection conn = getDatabaseConnection();
        StoredData.PWData pwData = null;
        try(PreparedStatement statement = conn.prepareStatement("select * from pwdata where userId=?")){
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            if(result.next()){
                pwData = new StoredData.PWData();
                pwData.id = result.getString("userId");
                pwData.email = result.getString("email");
                pwData.timestamp = result.getDate("timestamp");
            }
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return pwData;
    }

    @Override
    public void cleanuppwdata() {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("delete from pwdata where timestamp<?")){
            statement.setDate(1, new Date(System.currentTimeMillis() - 60*60*1000L));
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
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
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select userId from users")){
            ResultSet result = statement.executeQuery();
            while(result.next())
                users.add(result.getString("userId"));
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return users;
    }

    @Override
    public long getUserLastVisited(String uid) {
        Connection conn = getDatabaseConnection();
        long r_visited = System.currentTimeMillis();
        try(PreparedStatement statement = conn.prepareStatement("select visited from users where userId=?")){
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_visited = result.getDate("visited").getTime();
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_visited;
    }

    @Override
    public void removeUser(String uid) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("delete from users where userId=?")){
            statement.setString(1, uid);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void createGroup(String name) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("insert into groups(name) values (?)")){
            statement.setString(1, name);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void removeGroup(long gid) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("delete from gusers where groupId=?")){
            statement.setLong(1, gid);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("delete from groups where groupId=?")){
            statement.setLong(1, gid);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public long findGroupByName(String name) {
        Connection conn = getDatabaseConnection();
        long r_groupId = 0;
        try(PreparedStatement statement = conn.prepareStatement("select groupId from groups where name=?")){
            statement.setString(1, name);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_groupId = result.getLong("groupId");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_groupId;
    }

    @Override
    public List<Long> listGroups() {
        List<Long> groups = new LinkedList<>();
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select groupId from groups")){
            ResultSet result = statement.executeQuery();
            while(result.next())
                groups.add(result.getLong("groupId"));
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return groups;
    }

    @Override
    public String getGroupName(long gid) {
        Connection conn = getDatabaseConnection();
        String r_name = null;
        try(PreparedStatement statement = conn.prepareStatement("select name from groups where groupId=?")){
            statement.setLong(1, gid);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_name = result.getString("name");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_name;
    }

    @Override
    public void setGroupName(long gid, String name) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("update groups set name=? where groupId=?")){
            statement.setString(1, name);
            statement.setLong(2, gid);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public List<Long> getUserJoinedGroups(String uid) {
        List<Long> groups = new LinkedList<>();
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select groupId from gusers where userId=?")){
            statement.setString(1, uid);
            ResultSet result = statement.executeQuery();
            while(result.next())
                groups.add(result.getLong("groupId"));
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return groups;
    }

    @Override
    public List<String> getGroupUsers(long gid) {
        List<String> users = new LinkedList<>();
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("select userId from gusers where groupId=?")){
            statement.setLong(1, gid);
            ResultSet result = statement.executeQuery();
            while(result.next())
                users.add(result.getString("userId"));
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return users;
    }

    @Override
    public void addUsersToGroup(long gid, List<String> users) {
        Connection conn = getDatabaseConnection();
        try {
            beginTransaction(conn);
            for (String userId : users) {
                PreparedStatement statement = conn.prepareStatement("insert into gusers values (?, ?)");
                statement.setLong(1, gid);
                statement.setString(2, userId);
                statement.executeUpdate();
                statement.close();
            }
            conn.commit();
            endTransaction(conn);
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public void removeUsersFromGroup(long gid, List<String> users) {
        Connection conn = getDatabaseConnection();
        try{
            beginTransaction(conn);
            for(String userId : users) {
                PreparedStatement statement = conn.prepareStatement("delete from gusers where userId=?");
                statement.setString(1, userId);
                statement.executeUpdate();
                statement.executeUpdate();
                statement.close();
            }
            conn.commit();
            endTransaction(conn);
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }

    @Override
    public String downloadBackpack(String backPackId) {
        Connection conn = getDatabaseConnection();
        String r_content = "";
        try(PreparedStatement statement = conn.prepareStatement("select content from backpack where backPackId=?")){
            statement.setString(1, backPackId);
            ResultSet result = statement.executeQuery();
            if(result.next())
                r_content = result.getString("content");
            result.close();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }

        return r_content;
    }

    @Override
    public void uploadBackpack(String backPackId, String content) {
        Connection conn = getDatabaseConnection();
        try(PreparedStatement statement = conn.prepareStatement("insert or replace into backpack values (?, ?)")){
            statement.setString(1, backPackId);
            statement.setString(2, content);
            statement.executeUpdate();
        }catch(Exception e){
            e.printStackTrace();
            closeConnection(conn);
        }
    }
}