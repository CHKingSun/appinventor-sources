package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClassInfo implements IsSerializable {
    private int courseId;

    private String userId;

    private String userName;

    /**
     * Default constructor. This constructor is required by GWT.
     */
    @SuppressWarnings("unused")
    private ClassInfo() {}

    public ClassInfo(int courseId, String userId, String userName) {
        this.courseId = courseId;
        this.userId = userId;
        this.userName = userName;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassInfo classInfo = (ClassInfo) o;

        if (courseId != classInfo.courseId) return false;
        return userId.equals(classInfo.userId);

    }

    @Override
    public int hashCode() {
        int result = courseId;
        result = 31 * result + userId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "courseId=" + courseId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}
