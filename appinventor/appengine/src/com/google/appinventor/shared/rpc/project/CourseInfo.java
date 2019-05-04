//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Bundles user specific information about a course to send it over an RPC.
 */
public class CourseInfo implements IsSerializable {
    // Course's ID, unique.
    private int courseId;

    // Course name.
    private String courseName;

    // Admin's ID who teaches this course
    private String adminId;

    // Admin's name who teaches this course
    private String adminName;

    /**
     * Default constructor. This constructor is required by GWT.
     */
    @SuppressWarnings("unused")
    private CourseInfo() {}

    public CourseInfo(int courseId, String courseName, String adminId, String adminName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.adminId = adminId;
        this.adminName = adminName;
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

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CourseInfo that = (CourseInfo) o;

        return courseId == that.courseId;

    }

    @Override
    public int hashCode() {
        return courseId;
    }

    @Override
    public String toString() {
        return "CourseInfo{" +
                "courseId=" + courseId +
                ", courseName='" + courseName + '\'' +
                ", adminName='" + adminName + '\'' +
                '}';
    }
}
