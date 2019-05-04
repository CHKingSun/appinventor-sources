// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.explorer.project.ProjectManagerEventListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

import java.util.*;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The project list shows all projects in a table.
 *
 * <p> The project name, date created, and date modified will be shown in the table.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class ScoreProjectList extends Composite implements ProjectManagerEventListener {
    private enum SortField {
        NAME,
        SCORE,
    }
    private enum SortOrder {
        ASCENDING,
        DESCENDING,
    }

    private final List<Project> projects;
    private final List<Project> selectedProjects;
    private final Map<Project, ProjectWidgets> projectWidgets;
    private SortField sortField;
    private SortOrder sortOrder;

    // UI elements
    private final Grid table;
    private final Label nameSortIndicator;
    private final Label scoreSortIndicator;

    /**
     * Creates a new ProjectList
     */
    public ScoreProjectList() {
        projects = new ArrayList<>();
        selectedProjects = new ArrayList<>();
        projectWidgets = new HashMap<>();

        sortField = SortField.NAME;
        sortOrder = SortOrder.ASCENDING;

        // Initialize UI
        table = new Grid(1, 3); // The table initially contains just the header row.
        table.addStyleName("ode-ProjectTable");
        table.setWidth("100%");
        table.setCellSpacing(0);
        nameSortIndicator = new Label("");
        scoreSortIndicator = new Label("");
        refreshSortIndicators();
        setHeaderRow();

        ScrollPanel panel = new ScrollPanel();
        panel.setWidth("100%");
        panel.setHeight("540px");

        panel.add(table);
        initWidget(panel);

        // It is important to listen to project manager events as soon as possible.
        Ode.getInstance().getProjectManager().addProjectManagerEventListener(this);
    }

    /**
     * Adds the header row to the table.
     *
     */
    private void setHeaderRow() {
        table.getRowFormatter().setStyleName(0, "ode-ProjectHeaderRow");

        HorizontalPanel nameHeader = new HorizontalPanel();
        final Label nameHeaderLabel = new Label(MESSAGES.projectNameHeader());
        nameHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        nameHeader.add(nameHeaderLabel);
        nameSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        nameHeader.add(nameSortIndicator);
        table.setWidget(0, 1, nameHeader);

        HorizontalPanel scoreHeader = new HorizontalPanel();
        final Label scoreHeaderLabel = new Label(MESSAGES.projectScoreHeader());
        scoreHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        scoreHeader.add(scoreHeaderLabel);
        scoreSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        scoreHeader.add(scoreSortIndicator);
        table.setWidget(0, 2, scoreHeader);

        MouseDownHandler mouseDownHandler = new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent e) {
                SortField clickedSortField = SortField.NAME;
                if (e.getSource() == scoreHeaderLabel || e.getSource() == scoreSortIndicator) {
                    clickedSortField = SortField.SCORE;
                }
                changeSortOrder(clickedSortField);
            }
        };
        nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
        nameSortIndicator.addMouseDownHandler(mouseDownHandler);
        scoreHeaderLabel.addMouseDownHandler(mouseDownHandler);
        scoreSortIndicator.addMouseDownHandler(mouseDownHandler);
    }

    private void changeSortOrder(SortField clickedSortField) {
        if (sortField != clickedSortField) {
            sortField = clickedSortField;
            sortOrder = SortOrder.ASCENDING;
        } else {
            if (sortOrder == SortOrder.ASCENDING) {
                sortOrder = SortOrder.DESCENDING;
            } else {
                sortOrder = SortOrder.ASCENDING;
            }
        }
        refreshTable(true);
    }

    private void refreshSortIndicators() {
        String text = (sortOrder == SortOrder.ASCENDING)
                ? "\u25B2"      // up-pointing triangle
                : "\u25BC";     // down-pointing triangle
        switch (sortField) {
            case NAME:
                nameSortIndicator.setText(text);
                scoreSortIndicator.setText("");
                break;
            case SCORE:
                scoreSortIndicator.setText(text);
                nameSortIndicator.setText("");
                break;
        }
    }

    private class ProjectWidgets {
        final CheckBox checkBox;
        final Label nameLabel;
        final Label scoreLable;

        private ProjectWidgets(final Project project) {
            checkBox = new CheckBox();
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
                    int row = 1 + projects.indexOf(project);
                    if (isChecked) {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                        selectedProjects.add(project);
                    } else {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                        selectedProjects.remove(project);
                    }
                    Ode.getInstance().updateProjectToolbarButtons();
                }
            });

            nameLabel = new Label(project.getProjectName());
            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Ode ode = Ode.getInstance();
                    if (ode.screensLocked()) {
                        return;             // i/o in progress, ignore request
                    }
                    ode.openYoungAndroidProjectInDesigner(project);
                }
            });
            nameLabel.addStyleName("ode-ProjectNameLabel");

            scoreLable = new Label();
        }
    }

    // TODO(user): This method was made public so it can be called
    // directly from from Ode when the Project List View is selected
    // from another view.  Ode now clears any selected projects and
    // calls this to refresh the table as a result. Not sure this is
    // correct thing do to. The alternative is to add a call to the
    // ProjectManagerEventListener interface that this is the
    // implementation of.
    public void refreshTable(boolean needToSort) {
        if (needToSort) {
            // Sort the projects.
            Comparator<Project> comparator;
            switch (sortField) {
                default:
                case NAME:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ProjectComparators.COMPARE_BY_NAME_ASCENDING
                            : ProjectComparators.COMPARE_BY_NAME_DESCENDING;
                    break;
                case SCORE:
                    // TODO add score
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ProjectComparators.COMPARE_BY_DATE_CREATED_ASCENDING
                            : ProjectComparators.COMPARE_BY_DATE_CREATED_DESCENDING;
                    break;
            }
            Collections.sort(projects, comparator);
        }

        refreshSortIndicators();

        // Refill the table.
        table.resize(1 + projects.size(), 3);
        int row = 1;
        for (Project project : projects) {
            ProjectWidgets pw = projectWidgets.get(project);
            if (selectedProjects.contains(project)) {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                pw.checkBox.setValue(true);
            } else {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                pw.checkBox.setValue(false);
            }
            table.setWidget(row, 0, pw.checkBox);
            table.setWidget(row, 1, pw.nameLabel);
            table.setWidget(row, 2, pw.scoreLable);

            row++;
        }

        Ode.getInstance().updateProjectToolbarButtons();
    }

    /**
     * Gets the number of projects
     *
     * @return the number of projects
     */
    public int getNumProjects() {
        return projects.size();
    }

    /**
     * Gets the number of selected projects
     *
     * @return the number of selected projects
     */
    public int getNumSelectedProjects() {
        return selectedProjects.size();
    }

    /**
     * Returns the list of selected projects
     *
     * @return the selected projects
     */
    public List<Project> getSelectedProjects() {
        return selectedProjects;
    }

    public void setlectAllProjects() {
        for (ProjectWidgets widget : projectWidgets.values()) {
            widget.checkBox.setValue(true, true);
        }
    }

    public void DesetlectAllProjects() {
        for (ProjectWidgets widget : projectWidgets.values()) {
            widget.checkBox.setValue(false, true);
        }
    }

    // ProjectManagerEventListener implementation

    @Override
    public void onProjectAdded(Project project) {
        projects.add(project);
        projectWidgets.put(project, new ProjectWidgets(project));
        refreshTable(true);
    }
    @Override
    public void onProjectRemoved(Project project) {
        projects.remove(project);
        projectWidgets.remove(project);

        refreshTable(false);

        selectedProjects.remove(project);
        Ode.getInstance().updateProjectToolbarButtons();
    }

    @Override
    public void onProjectsLoaded() {
        // This can be empty
    }
    public void onProjectPublishedOrUnpublished() {
        refreshTable(false);
    }

    public void setScoreHeaderVisible(boolean visible){
        table.getWidget(0, 2).setVisible(visible);
    }
}
