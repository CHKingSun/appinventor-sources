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

    public String getSubmitter() { return scoreInfo.getSubmitter(); }

    public String getCourseName() { return  scoreInfo.getCourseName(); }

    public int getScore() { return  scoreInfo.getScore(); }

    public long getSubmitTime() { return scoreInfo.getSubmitTime(); }

    public long getScoredTime() { return scoreInfo.getScoredTime(); }
}
