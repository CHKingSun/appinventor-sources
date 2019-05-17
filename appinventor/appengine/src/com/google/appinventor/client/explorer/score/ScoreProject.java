//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.score;

import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.shared.rpc.project.ScoreInfo;

/**
 * This class represents a score project.
 */
public final class ScoreProject {
    private final ScoreInfo scoreInfo;
    private final Project project;

    public ScoreProject(ScoreInfo scoreInfo) {
        this.scoreInfo = scoreInfo;
        this.project = new Project(scoreInfo.getProjectInfo());
    }

    public long getProjectId() {
        return project.getProjectId();
    }

    public Project getProject() { return project; }

    public int getCourseId() { return scoreInfo.getCourseId(); }

    public String getSubmitter() { return scoreInfo.getSubmitter(); }

    public String getCourseName() { return  scoreInfo.getCourseName(); }

    public void setScore(int score) { scoreInfo.setScore(score); }

    public int getScore() { return  scoreInfo.getScore(); }

    public long getSubmitTime() { return scoreInfo.getSubmitTime(); }

    public void setScoredTime(long scoredTime) { scoreInfo.setScoredTime(scoredTime); }

    public long getScoredTime() { return scoreInfo.getScoredTime(); }

    public void setSimilarity(float similarity) { scoreInfo.setSimilarity(similarity); }

    public float getSimilarity() { return scoreInfo.getSimilarity(); }
}
