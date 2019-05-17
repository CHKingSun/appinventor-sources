//
// Created by KingSun on 2019/04/29
//

package com.google.appinventor.client.explorer.youngandroid;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.OdeAdmin;
import com.google.appinventor.client.explorer.score.ScoreProject;
import com.google.appinventor.client.explorer.score.ScoreProjectComparators;
import com.google.appinventor.client.explorer.score.ScoreProjectManagerEventListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.appinventor.client.Ode.MESSAGES;

/**
 * The score project list shows all score projects in a table.
 *
 * <p> The score information will be shown in the table.
 *
 */
public class ScoreProjectList extends Composite implements ScoreProjectManagerEventListener {
    private enum SortField {
        NAME,
        SCORE,
        SUBMITTER,
        SUBMIT_TIME,
        SCORED_TIME,
        SIMILARITY,
    }
    private enum SortOrder {
        ASCENDING,
        DESCENDING,
    }

    private final Map<Integer, List<ScoreProject>> projectsMap;
    private final Set<ScoreProject> selectedProjects;
    private final Map<ScoreProject, ScoreProjectWidgets> projectWidgets;
    private List<ScoreProject> currentProjects;
    private int currentCourse;
    private SortField sortField;
    private SortOrder sortOrder;

    private final static DateTimeFormat dateTimeFormat =
            DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);

    // UI elements
    private final Grid table;
    private final Label nameSortIndicator;
    private final Label scoreSortIndicator;
    private final Label submitterSortIndicator;
    private final Label submitTimeSortIndicator;
    private final Label scoredTimeSortIndicator;
    private final Label similaritySortIndicator;

    private boolean isHideSubmitterColumn = false;
    private boolean isHideSubmitTimeColumn = false;
    private boolean isHideScoredTimeColumn = false;
    private boolean isHideSimilarityColumn = false;

    /**
     * Creates a new ProjectList
     */
    public ScoreProjectList() {
        projectsMap = new HashMap<>();
        currentCourse = -1;
        projectsMap.put(currentCourse, new ArrayList<ScoreProject>());
        currentProjects = new ArrayList<>();
        selectedProjects = new HashSet<>();
        projectWidgets = new HashMap<>();

        sortField = SortField.SUBMIT_TIME;
        sortOrder = SortOrder.ASCENDING;

        // Initialize UI
        table = new Grid(1, 7); // The table initially contains just the header row.
        table.addStyleName("ode-ProjectTable");
        table.setWidth("100%");
        table.setCellSpacing(0);
        nameSortIndicator = new Label("");
        scoreSortIndicator = new Label("");
        submitterSortIndicator = new Label("");
        submitTimeSortIndicator = new Label("");
        scoredTimeSortIndicator = new Label("");
        similaritySortIndicator = new Label("");
        refreshSortIndicators();
        setHeaderRow();

        ScrollPanel panel = new ScrollPanel();
        panel.setWidth("100%");
        panel.setHeight("540px");

        panel.add(table);
        initWidget(panel);

        // It is important to listen to project manager events as soon as possible.
        OdeAdmin.getInstance().getScoreProjectManager().addScoreProjectManagerEventListener(this);
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

        HorizontalPanel submitterHeader = new HorizontalPanel();
        final Label submitterHeaderLabel = new Label(MESSAGES.projectSubmitterHeader());
        submitterHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        submitterHeader.add(submitterHeaderLabel);
        submitterSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        submitterHeader.add(submitterSortIndicator);
        table.setWidget(0, 3, submitterHeader);

        HorizontalPanel submitTimeHeader = new HorizontalPanel();
        final Label submitTimeHeaderLabel = new Label(MESSAGES.projectSubmitTimeHeader());
        submitTimeHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        submitTimeHeader.add(submitTimeHeaderLabel);
        submitTimeSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        submitTimeHeader.add(submitTimeSortIndicator);
        table.setWidget(0, 4, submitTimeHeader);

        HorizontalPanel scoredTimeHeader = new HorizontalPanel();
        final Label scoredTimeHeaderLabel = new Label(MESSAGES.projectScoredTimeHeader());
        scoredTimeHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        scoredTimeHeader.add(scoredTimeHeaderLabel);
        scoredTimeSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        scoredTimeHeader.add(scoredTimeSortIndicator);
        table.setWidget(0, 5, scoredTimeHeader);

        HorizontalPanel similarityHeader = new HorizontalPanel();
        final Label similarityHeaderLabel = new Label(MESSAGES.projectSimilarityHeader());
        similarityHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        similarityHeader.add(similarityHeaderLabel);
        similaritySortIndicator.addStyleName("ode-ProjectHeaderLabel");
        similarityHeader.add(similaritySortIndicator);
        table.setWidget(0, 6, similarityHeader);

        MouseDownHandler mouseDownHandler = new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent e) {
                SortField clickedSortField = SortField.NAME;
                if (e.getSource() == scoreHeaderLabel || e.getSource() == scoreSortIndicator) {
                    clickedSortField = SortField.SCORE;
                } else if (e.getSource() == submitterHeaderLabel || e.getSource() == submitterSortIndicator) {
                    clickedSortField = SortField.SUBMITTER;
                } else if (e.getSource() == submitTimeHeaderLabel || e.getSource() == submitTimeSortIndicator) {
                    clickedSortField = SortField.SUBMIT_TIME;
                } else if (e.getSource() == scoredTimeHeaderLabel || e.getSource() == scoredTimeSortIndicator) {
                    clickedSortField = SortField.SCORED_TIME;
                } else if (e.getSource() == similarityHeaderLabel || e.getSource() == similaritySortIndicator) {
                    clickedSortField = SortField.SIMILARITY;
                }
                changeSortOrder(clickedSortField);
            }
        };
        nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
        nameSortIndicator.addMouseDownHandler(mouseDownHandler);
        scoreHeaderLabel.addMouseDownHandler(mouseDownHandler);
        scoreSortIndicator.addMouseDownHandler(mouseDownHandler);
        submitterHeaderLabel.addMouseDownHandler(mouseDownHandler);
        submitterSortIndicator.addMouseDownHandler(mouseDownHandler);
        submitTimeHeaderLabel.addMouseDownHandler(mouseDownHandler);
        submitTimeSortIndicator.addMouseDownHandler(mouseDownHandler);
        scoredTimeHeaderLabel.addMouseDownHandler(mouseDownHandler);
        scoredTimeSortIndicator.addMouseDownHandler(mouseDownHandler);
        similarityHeaderLabel.addMouseDownHandler(mouseDownHandler);
        similaritySortIndicator.addMouseDownHandler(mouseDownHandler);
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
                submitterSortIndicator.setText("");
                submitTimeSortIndicator.setText("");
                scoredTimeSortIndicator.setText("");
                similaritySortIndicator.setText("");
                break;
            case SCORE:
                nameSortIndicator.setText("");
                scoreSortIndicator.setText(text);
                submitterSortIndicator.setText("");
                submitTimeSortIndicator.setText("");
                scoredTimeSortIndicator.setText("");
                similaritySortIndicator.setText("");
                break;
            case SUBMITTER:
                nameSortIndicator.setText("");
                scoreSortIndicator.setText("");
                submitterSortIndicator.setText(text);
                submitTimeSortIndicator.setText("");
                scoredTimeSortIndicator.setText("");
                similaritySortIndicator.setText("");
                break;
            case SUBMIT_TIME:
                nameSortIndicator.setText("");
                scoreSortIndicator.setText("");
                submitterSortIndicator.setText("");
                submitTimeSortIndicator.setText(text);
                scoredTimeSortIndicator.setText("");
                similaritySortIndicator.setText("");
                break;
            case SCORED_TIME:
                nameSortIndicator.setText("");
                scoreSortIndicator.setText("");
                submitterSortIndicator.setText("");
                submitTimeSortIndicator.setText("");
                scoredTimeSortIndicator.setText(text);
                similaritySortIndicator.setText("");
                break;
            case SIMILARITY:
                nameSortIndicator.setText("");
                scoreSortIndicator.setText("");
                submitterSortIndicator.setText("");
                submitTimeSortIndicator.setText("");
                scoredTimeSortIndicator.setText("");
                similaritySortIndicator.setText(text);
                break;
        }
    }

    private void refreshRowHeader() {
        table.getWidget(0, 3).setVisible(!isHideSubmitterColumn);
        table.getWidget(0, 4).setVisible(!isHideSubmitTimeColumn);
        table.getWidget(0, 5).setVisible(!isHideScoredTimeColumn);
        table.getWidget(0, 6).setVisible(!isHideSimilarityColumn);
    }

    private class ScoreProjectWidgets {
        private final CheckBox checkBox;
        private final Label nameLabel;
        private final Label scoreLabel;
        private final Label submitterLabel;
        private final Label submitTimeLabel;
        private final Label scoredTimeLabel;
        private final Label similarityLabel;

        private ScoreProjectWidgets(final ScoreProject project) {
            checkBox = new CheckBox();
            checkBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(ValueChangeEvent<Boolean> event) {
                    boolean isChecked = event.getValue(); // auto-unbox from Boolean to boolean
                    int row = 1 + currentProjects.indexOf(project);
                    if (isChecked) {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                        selectedProjects.add(project);
                    } else {
                        table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                        selectedProjects.remove(project);
                    }
                    OdeAdmin.getInstance().getScoreProjectToolbar().updateButtons();
                }
            });

            nameLabel = new Label(project.getProject().getProjectName());
            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Ode ode = Ode.getInstance();
                    if (ode.screensLocked()) {
                        return;             // i/o in progress, ignore request
                    }
                    ode.openYoungAndroidProjectInDesigner(project.getProject());
                }
            });
            nameLabel.addStyleName("ode-ProjectNameLabel");

            scoreLabel = new Label("-");
            submitterLabel = new Label(project.getSubmitter());
            submitTimeLabel = new Label(dateTimeFormat.format(new Date(project.getSubmitTime())));
            scoredTimeLabel = new Label("-");
            similarityLabel = new Label("-");

            updateLabels(project);
        }

        private void updateLabels(ScoreProject project) {
            if (project.getScore() >= 0)
                scoreLabel.setText(Integer.toString(project.getScore()));
            if (project.getScoredTime() > 1000)
                scoredTimeLabel.setText(dateTimeFormat.format(new Date(project.getScoredTime())));
            if (project.getSimilarity() >= 0)
                similarityLabel.setText(Float.toString(project.getSimilarity()));
        }
    }

    public void updateScoreProjectWidgets(ScoreProject project) {
        projectWidgets.get(project).updateLabels(project);
        refreshTable(false);
    }

    public void updateSimilarityWidgets(List<ScoreProject> projects) {
        for (ScoreProject project : projects) {
            projectWidgets.get(project).updateLabels(project);
        }
        refreshTable(false);
    }

    public void refreshTable(boolean needToSort) {
        if (needToSort) {
            // Sort the projects.
            Comparator<ScoreProject> comparator;
            switch (sortField) {
                default:
                case NAME:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_NAME_ASC
                            : ScoreProjectComparators.COMPARE_BY_NAME_DESC;
                    break;
                case SCORE:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_SCORE_ASC
                            : ScoreProjectComparators.COMPARE_BY_SCORE_DESC;
                    break;
                case SUBMITTER:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_SUBMITTER_ASC
                            : ScoreProjectComparators.COMPARE_BY_SUBMITTER_DESC;
                    break;
                case SUBMIT_TIME:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_SUBMIT_TIME_ASC
                            : ScoreProjectComparators.COMPARE_BY_SUBMIT_TIME_DESC;
                    break;
                case SCORED_TIME:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_SCORED_TIME_ASC
                            : ScoreProjectComparators.COMPARE_BY_SCORED_TIME_DESC;
                    break;
                case SIMILARITY:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ScoreProjectComparators.COMPARE_BY_SIMILARITY_ASC
                            : ScoreProjectComparators.COMPARE_BY_SIMILARITY_DESC;
                    break;
            }
            currentProjects.sort(comparator);
        }

        refreshSortIndicators();
        refreshRowHeader();

        // Refill the table.
        table.resize(1 + currentProjects.size(), 7);
        int row = 1;
        for (ScoreProject project : currentProjects) {
            ScoreProjectWidgets pw = projectWidgets.get(project);
            if (selectedProjects.contains(project)) {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
                pw.checkBox.setValue(true);
            } else {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
                pw.checkBox.setValue(false);
            }

            table.setWidget(row, 0, pw.checkBox);
            table.setWidget(row, 1, pw.nameLabel);
            table.setWidget(row, 2, pw.scoreLabel);
            if (!isHideSubmitterColumn) table.setWidget(row, 3, pw.submitterLabel);
            else table.setWidget(row, 3, null);
            if (!isHideSubmitTimeColumn) table.setWidget(row, 4, pw.submitTimeLabel);
            else table.setWidget(row, 4, null);
            if (!isHideScoredTimeColumn) table.setWidget(row, 5, pw.scoredTimeLabel);
            else table.setWidget(row, 5, null);
            if (!isHideSimilarityColumn) table.setWidget(row, 6, pw.similarityLabel);
            else table.setWidget(row, 6, null);

            ++row;
        }

        OdeAdmin.getInstance().getScoreProjectToolbar().updateButtons();
    }

    // Trigger when some column hide
    public void refreshWidgetWidth() {
        int width = 30;
        if (!isHideSubmitterColumn) width += 20;
        if (!isHideSubmitTimeColumn) width += 20;
        if (!isHideScoredTimeColumn) width += 20;
        if (!isHideSimilarityColumn) width += 10;
        width = (int)((width < 70 ? 70 : width) * 0.36);
        OdeAdmin.getInstance().setAdminPanelWidth(width + "%", (99 - width) + "%");
    }

    public void changeCourse(int courseId) {
        if (currentCourse == courseId || !projectsMap.containsKey(courseId)) return;
        deselectAllProjects();
        currentCourse = courseId;
        currentProjects = projectsMap.get(currentCourse);
        refreshTable(true);
    }

    public void filterTime(Predicate<ScoreProject> predicate) {
        if (predicate == null) return;
        currentProjects = projectsMap.get(currentCourse).stream().filter(predicate).collect(Collectors.toList());
        refreshTable(true);
    }

    /**
     * Gets the number of projects
     *
     * @return the number of projects
     */
    public int getNumProjects() {
        return currentProjects.size();
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
    public List<ScoreProject> getSelectedProjects() {
        return new ArrayList<>(selectedProjects);
    }

    public void selectAllProjects() {
        for (ScoreProject project : currentProjects) {
            ScoreProjectWidgets widget = projectWidgets.get(project);
            if (!widget.checkBox.getValue()) {
                widget.checkBox.setValue(true, true);
            }
        }
    }

    public void deselectAllProjects() {
        for (ScoreProject project : currentProjects) {
            ScoreProjectWidgets widget = projectWidgets.get(project);
            if (widget.checkBox.getValue()) {
                widget.checkBox.setValue(false, true);
            }
        }
    }

    // ProjectManagerEventListener implementation

    @Override
    public void onScoreProjectAdded(ScoreProject project) {
        if (!projectsMap.containsKey(project.getCourseId())) {
            projectsMap.put(project.getCourseId(), new ArrayList<ScoreProject>());
        }
        projectsMap.get(project.getCourseId()).add(project);
        projectWidgets.put(project, new ScoreProjectWidgets(project));
        if (currentCourse == project.getCourseId()) {
            currentProjects.add(project);
            refreshTable(true);
        }
    }


    @Override
    public void onScoreProjectsLoaded() {

    }

    public void setHideSimilarityColumn(boolean hideSimilarityColumn) {
        isHideSimilarityColumn = hideSimilarityColumn;
        refreshWidgetWidth();
        refreshTable(false);
    }

    public void setHideSubmitterColumn(boolean hideSubmitterColumn) {
        isHideSubmitterColumn = hideSubmitterColumn;
        refreshWidgetWidth();
        refreshTable(false);
    }

    public void setHideSubmitTimeColumn(boolean hideSubmitTimeColumn) {
        isHideSubmitTimeColumn = hideSubmitTimeColumn;
        refreshWidgetWidth();
        refreshTable(false);
    }

    public void setHideScoredTimeColumn(boolean hideScoredTimeColumn) {
        isHideScoredTimeColumn = hideScoredTimeColumn;
        refreshWidgetWidth();
        refreshTable(false);
    }
}
