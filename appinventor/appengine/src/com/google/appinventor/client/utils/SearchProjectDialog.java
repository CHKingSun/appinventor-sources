package com.google.appinventor.client.utils;

import com.google.appinventor.client.explorer.project.Project;

import java.util.List;

public class SearchProjectDialog {

    public interface SearchAction {
        void onProjectSelected(long projectId);
    }

    public SearchProjectDialog(List<Project> projects, SearchAction action) {

    }
}
