//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.score;

import java.util.Comparator;

/**
 * Comparators for {@link ScoreProject}.
 */
public class ScoreProjectComparators {
    private ScoreProjectComparators() {}

    public static final Comparator<ScoreProject> COMPARE_BY_NAME_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            String p1Name = p1.getProject().getProjectName();
            String p2Name = p2.getProject().getProjectName();
            return p1Name.compareToIgnoreCase(p2Name); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_NAME_DESC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            String p1Name = p1.getProject().getProjectName();
            String p2Name = p2.getProject().getProjectName();
            return p2Name.compareToIgnoreCase(p1Name); // descending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SCORE_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Integer.compare(p1.getScore(), p2.getScore()); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SCORE_DESC = new Comparator<ScoreProject>() {
            @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Integer.compare(p2.getScore(), p1.getScore()); // descending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SUBMITTER_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            String p1Submitter = p1.getSubmitter();
            String p2Submitter = p2.getSubmitter();
            return p1Submitter.compareToIgnoreCase(p2Submitter); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SUBMITTER_DESC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            String p1Submitter = p1.getSubmitter();
            String p2Submitter = p2.getSubmitter();
            return p2Submitter.compareToIgnoreCase(p1Submitter); // descending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SUBMIT_TIME_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Long.compare(p1.getSubmitTime(), p2.getSubmitTime()); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SUBMIT_TIME_DESC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Long.compare(p2.getSubmitTime(), p1.getSubmitTime()); // descending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SCORED_TIME_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Long.compare(p1.getScoredTime(), p2.getScoredTime()); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SCORED_TIME_DESC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Long.compare(p2.getScoredTime(), p1.getScoredTime()); // descending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SIMILARITY_ASC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Float.compare(p1.getSimilarity(), p2.getSimilarity()); // ascending
        }
    };

    public static final Comparator<ScoreProject> COMPARE_BY_SIMILARITY_DESC = new Comparator<ScoreProject>() {
        @Override
        public int compare(ScoreProject p1, ScoreProject p2) {
            return Float.compare(p2.getSimilarity(), p1.getSimilarity()); // descending
        }
    };
}
