//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bundles user specific information about score to send it over an RPC.
 */
public class ScoreInfo implements IsSerializable {
    // Info of the project.
    private UserProject projectInfo;

    // The user's ID who submitted the project.
    private String submitterId;

    // The user's name who submitted the project.
    private String submitter;

    // The course's ID of the project.
    private int courseId;

    // The name of the course.
    private String courseName;

    // The date when the project submitted.
    private long submitTime;

    // The score of the project, default is -1.
    private int score;

    // The date when the project scored, default is 1.
    private long scoredTime;

    /**
     * Default constructor. This constructor is required by GWT.
     */
    @SuppressWarnings("unused")
    private ScoreInfo() {}

    public ScoreInfo(UserProject projectInfo, String submitterId, String submitter,
                     int courseId, String courseName, long submitTime, int score, long scoredTime) {
        this.projectInfo = projectInfo;
        this.submitterId = submitterId;
        this.submitter = submitter;
        this.courseId = courseId;
        this.courseName = courseName;
        this.submitTime = submitTime;
        this.score = score;
        this.scoredTime = scoredTime;
    }

    public UserProject getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(UserProject projectInfo) {
        this.projectInfo = projectInfo;
    }

    public String getSubmitterId() {
        return submitterId;
    }

    public void setSubmitterId(String submitterId) {
        this.submitterId = submitterId;
    }

    public String getSubmitter() {
        return submitter;
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public long getScoredTime() {
        return scoredTime;
    }

    public void setScoredTime(long scoredTime) {
        this.scoredTime = scoredTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScoreInfo scoreInfo = (ScoreInfo) o;

        return projectInfo.equals(scoreInfo.projectInfo);

    }

    @Override
    public int hashCode() {
        return projectInfo.hashCode();
    }

    @Override
    public String toString() {
        return "ScoreInfo{" +
                "projectInfo=" + projectInfo +
                ", submitterId='" + submitterId + '\'' +
                ", submitter='" + submitter + '\'' +
                ", courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", submitTime=" + submitTime +
                ", score=" + score +
                ", scoredTime=" + scoredTime +
                '}';
    }
}
