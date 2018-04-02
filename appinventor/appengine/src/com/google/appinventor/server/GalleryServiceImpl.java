// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import java.io.IOException;
import java.util.List;

import com.google.appinventor.shared.rpc.project.Email;
import com.google.appinventor.shared.rpc.project.GalleryApp;
import com.google.appinventor.shared.rpc.project.GalleryAppListResult;
import com.google.appinventor.shared.rpc.project.GalleryComment;
import com.google.appinventor.shared.rpc.project.GalleryModerationAction;
import com.google.appinventor.shared.rpc.project.GalleryReportListResult;
import com.google.appinventor.shared.rpc.project.GalleryService;
import com.google.appinventor.shared.rpc.project.GallerySettings;

/**
 * The implementation of the RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 */
public class GalleryServiceImpl extends OdeRemoteServiceServlet implements GalleryService {

    private final GallerySettings settings;

    public GalleryServiceImpl() {
        // By default Gallery is disabled
        settings = new GallerySettings();
    }

    @Override
    public GallerySettings loadGallerySettings() {
        return settings;
    }

    /**
     * Publishes a gallery app
     * @param projectId id of the project being published
     * @param projectName name of project
     * @param title title of new gallery app
     * @param description description of new gallery app
     * @return a {@link GalleryApp} for new galleryApp
     */
    @Override
    public GalleryApp publishApp(long projectId, String title, String projectName, String description, String moreInfo, String credit)  throws IOException {
        return null;
    }

    /**
     * update a gallery app
     * @param app info about app being updated
     * @param newImage  true if the user has submitted a new image
     */
    @Override
    public void updateApp(GalleryApp app, boolean newImage) throws IOException {

    }
    /**
     * update a gallery app's meta data
     * @param app info about app being updated
     *
     */
    @Override
    public void updateAppMetadata(GalleryApp app) {

    }

    /**
     * update a gallery app's source (aia)
     * @param galleryId id of gallery app to be updated
     * @param projectId id of project so we can grab source
     * @param projectName name of project, this is name in new aia
     */
    @Override
    public void updateAppSource (long galleryId, long projectId, String projectName) throws IOException {

    }

    /**
     * index all gallery apps (admin method)
     * @param count the max number of apps to index
     */
    @Override
    public void indexAll(int count) {

    }

    /**
     * Returns total number of galleryApps
     * @return number of galleryApps
     */
    @Override
    public Integer getNumApps() {
        return 0;
    }

    /**
     * Returns a wrapped class which contains list of most recently
     * updated galleryApps and total number of results in database
     * @param start starting index
     * @param count number of apps to return
     * @return list of GalleryApps
     */
    @Override
    public GalleryAppListResult getRecentApps(int start,int count) {
        return null;
    }

    /**
     * Returns a wrapped class which contains list of featured gallery app
     * @param start start index
     * @param count count number
     * @return list of gallery app
     */
    public GalleryAppListResult getFeaturedApp(int start, int count){
        return null;
    }

    /**
     * Returns a wrapped class which contains list of tutorial gallery app
     * @param start start index
     * @param count count number
     * @return list of gallery app
     */
    public GalleryAppListResult getTutorialApp(int start, int count){
        return null;
    }

    /**
     * check if app is featured already
     * @param galleryId gallery id
     * @return true if featured, otherwise false
     */
    public boolean isFeatured(long galleryId){
        return false;
    }

    /**
     * check if app is tutorial already
     * @param galleryId gallery id
     * @return true if tutorial, otherwise false
     */
    public boolean isTutorial(long galleryId){
        return false;
    }

    /**
     * mark an app as featured
     * @param galleryId gallery id
     * @return true if successful
     */
    public boolean markAppAsFeatured(long galleryId){
        return false;
    }

    /**
     * mark an app as tutorial
     * @param galleryId gallery id
     * @return true if successful
     */
    public boolean markAppAsTutorial(long galleryId){
        return false;
    }

    /**
     * Returns a wrapped class which contains a list of galleryApps
     * by a particular developer and total number of results in database
     * @param userId id of the developer
     * @param start starting index
     * @param count number of apps to return
     * @return list of GalleryApps
     */
    @Override
    public GalleryAppListResult getDeveloperApps(String userId, int start,int count) {
        return null;
    }

    /**
     * Returns a GalleryApp object for the given id
     * @param galleryId  gallery ID as received by
     *                   {@link #getRecentGalleryApps()}
     *
     * @return  gallery app object
     */
    @Override
    public GalleryApp getApp(long galleryId) {
        return null;
    }

    /**
     * Returns a wrapped class which contains a list of galleryApps and
     * total number of results in database
     * @param keywords keywords to search for
     * @param start starting index
     * @param count number of apps to return
     * @return list of GalleryApps
     */
    @Override
    public GalleryAppListResult findApps(String keywords, int start, int count) {
        return null;
    }

    /**
     * Returns a wrapped class which contains a list of most downloaded
     * gallery apps and total number of results in database
     * @param start starting index
     * @param count number of apps to return
     * @return list of GalleryApps
     */
    @Override
    public GalleryAppListResult getMostDownloadedApps(int start, int count) {
        return null;
    }

    /**
     * Returns a wrapped class which contains a list of most liked
     * gallery apps and total number of results in database
     * @param start starting index
     * @param count number of apps to return
     * @return list of GalleryApps
     */
    @Override
    public GalleryAppListResult getMostLikedApps(int start, int count) {
        return null;
    }

    /**
     * Deletes a new gallery app
     * @param galleryId id of app to delete
     */
    @Override
    public void deleteApp(long galleryId) {

    }

    /**
     * record fact that app was downloaded
     * @param galleryId id of app that was downloaded
     */
    @Override
    public void appWasDownloaded(long galleryId) {

    }

    /**
     * Returns the comments for an app
     * @param galleryId  gallery ID as received by
     *                   {@link #getRecentGalleryApps()}
     * @return  a list of comments
     */

    @Override
    public List<GalleryComment> getComments(long galleryId) {
        return null;
    }

    /**
     * publish a comment for a gallery app
     * @param galleryId the id of the app
     * @param comment the comment
     */
    @Override
    public long publishComment(long galleryId, String comment) {
        return 0;
    }

    /**
     * increase likes for a gallery app
     * @param galleryId the id of the app
     * @return num of like
     */
    @Override
    public int increaseLikes(long galleryId) {
        return 0;
    }

    /**
     * decrease likes for a gallery app
     * @param galleryId the id of the app
     * @return num of like
     */
    @Override
    public int decreaseLikes(long galleryId) {
        return 0;
    }

    /**
     * get num of likes for a gallery app
     * @param galleryId the id of the app
     */
    @Override
    public int getNumLikes(long galleryId) {
        return 0;
    }

    /**
     * check if an app is liked by a user
     * @param galleryId the id of the app
     */
    @Override
    public boolean isLikedByUser(long galleryId) {
        return false;
    }

    /**
     * salvage the gallery app by given galleryId
     */
    @Override
    public void salvageGalleryApp(long galleryId) {

    }

    /**
     * adds a report (flag) to a gallery app
     * @param galleryId id of gallery app that was commented on
     * @param report report
     * @return the id of the new report
     */
    @Override
    public long addAppReport(GalleryApp app, String reportText) {
        return 0;
    }

    /**
     * gets recent reports
     * @param start start index
     * @param count number to retrieve
     * @return the list of reports
     */
    @Override
    public GalleryReportListResult getRecentReports(int start, int count) {
        return null;
    }

    /**
     * gets existing reports
     * @param start start index
     * @param count number to retrieve
     * @return the list of reports
     */
    @Override
    public GalleryReportListResult getAllAppReports(int start, int count){
        return null;
    }

    /**
     * check if an app is reprted by a user
     * @param galleryId the id of the app
     */
    @Override
    public boolean isReportedByUser(long galleryId) {
        return false;
    }

    /**
     * save attribution for a gallery app
     * @param galleryId the id of the app
     * @param attributionId the id of the attribution app
     * @return num of like
     */
    @Override
    public long saveAttribution(long galleryId, long attributionId) {
        return 0;
    }

    /**
     * get the attribution id for a gallery app
     * @param galleryId the id of the app
     * @return attribution id
     */
    @Override
    public long remixedFrom(long galleryId) {
        return 0;
    }

    /**
     * get the children ids of an app
     * @param galleryId the id of the app
     * @return list of children gallery app
     */
    @Override
    public List<GalleryApp> remixedTo(long galleryId) {
        return null;
    }

    /**
     * mark an report as resolved
     * @param reportId the id of the app
     */
    @Override
    public boolean markReportAsResolved(long reportId, long galleryId) {
        return false;
    }

    /**
     * deactivate app
     * @param galleryId the id of the gallery app
     */
    @Override
    public boolean deactivateGalleryApp(long galleryId) {
        return false;
    }

    /**
     * check if gallery app is Activated
     * @param galleryId the id of the gallery app
     */
    @Override
    public boolean isGalleryAppActivated(long galleryId){
        return false;
    }

    /**
     * Send an email to user
     * @param senderId id of user sending this email
     * @param receiverId id of user receiving this email
     * @param receiverEmail receiver of email
     * @param title title of email
     * @param body body of email
     */
    @Override
    public long sendEmail(String senderId, String receiverId, String receiverEmail, String title, String body) {
        return 0;
    }

    /**
     * Get email based on emailId
     * @param emailId id of the email
     * @return Email email
     */
    @Override
    public Email getEmail(long emailId) {
        return null;
    }

    /**
     * check if ready to send app stats to user
     * @param userId
     * @param galleryId
     * @param adminEmail
     * @param currentHost
     */
    public boolean checkIfSendAppStats(String userId, long galleryId, String adminEmail, String currentHost){
        return false;
    }

    /**
     * Store moderation actions based on actionType
     * @param reportId
     * @param galleryId
     * @param emailId
     * @param moderatorId
     * @param actionType
     */
    public void storeModerationAction(long reportId, long galleryId, long emailId, String moderatorId, int actionType, String moderatorName, String emailPreview){

    }

    /**
     * Get moderation actions based on given reportId
     * @param reportId
     */
    public List<GalleryModerationAction> getModerationActions(long reportId){
        return null;
    }

    /**
     * It will return a dev server serving url for given image url
     * @param url image url
     */
    @Override
    public String getBlobServingUrl(String url) {
        return null;
    }

}