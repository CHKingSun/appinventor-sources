package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.explorer.project.Project;
import com.google.appinventor.client.explorer.project.ProjectComparators;
import com.google.appinventor.client.widgets.TextButton;
import com.google.appinventor.client.widgets.properties.EditableProperties;
import com.google.appinventor.client.widgets.properties.EditableProperty;
import com.google.appinventor.client.widgets.properties.TextPropertyEditor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.google.appinventor.client.Ode.MESSAGES;

public class ProjectSearchDialog extends DialogBox {

    public interface SearchAction {
        void onProjectSelected(long projectId);
    }

    private enum SortField {
        NAME,
        DATE_CREATED,
    }
    private enum SortOrder {
        ASCENDING,
        DESCENDING,
    }

    private final SearchAction searchAction;

    private final List<Project> projects;
    private final Map<Project, SimpleProjectWidgets> projectWidgets;
    private List<Project> currentProjects;
    private Project selectProject;

    private SortField sortField;
    private SortOrder sortOrder;

    private EditableProperty wordsProperty;

    // UI elements
    private final Grid table;
    private final Label nameSortIndicator;
    private final Label dateCreatedSortIndicator;

    public ProjectSearchDialog(SearchAction action) {
        setText("Select Project To Analyse");
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        setHeight("360px");
        setWidth("300px");
        center();

        searchAction = action;

        projects = Ode.getInstance().getProjectManager().getProjects();
        projectWidgets = new HashMap<>(projects.size());
        for (Project project : projects) {
            projectWidgets.put(project, new SimpleProjectWidgets(project));
        }

        currentProjects = projects;
        sortField = SortField.NAME;
        sortOrder = SortOrder.ASCENDING;

        // Initialize UI
        table = new Grid(1, 2); // The table initially contains just the header row.
        table.addStyleName("ode-ProjectTable");
        table.setWidth("100%");
        table.setCellSpacing(0);
        nameSortIndicator = new Label("");
        dateCreatedSortIndicator = new Label("");
        refreshSortIndicators();
        setHeaderRow();
        refreshTable(true);

        initializeUi();
    }

    private void initializeUi() {
        VerticalPanel dialog = new VerticalPanel();
        EditableProperties properties = new EditableProperties(true);
        dialog.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

        HorizontalPanel panel = new HorizontalPanel();
        TextPropertyEditor editor = new TextPropertyEditor();
        properties.addProperty("Words", "", "Words", editor, EditableProperty.TYPE_NORMAL);
        wordsProperty = properties.getExistingProperty("Words");
        panel.add(editor);
        panel.setCellHorizontalAlignment(editor, HorizontalPanel.ALIGN_LEFT);

        TextButton searchButton = new TextButton("Search");
        searchButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new NameFilterAction().execute();
            }
        });
        panel.add(searchButton);
        panel.setCellHorizontalAlignment(searchButton, HorizontalPanel.ALIGN_RIGHT);
        dialog.add(panel);

        ScrollPanel scrollPanel = new ScrollPanel();
        scrollPanel.setWidth("100%");
        scrollPanel.setHeight("360px");
        scrollPanel.add(table);
        dialog.add(scrollPanel);

        TextButton cancelButton = new TextButton(MESSAGES.cancelButton());
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ProjectSearchDialog.this.hide();
            }
        });
        dialog.add(cancelButton);

        setWidget(dialog);
    }

    private class SimpleProjectWidgets {
        private final Label nameLabel;
        private final Label dateCreatedLabel;

        private SimpleProjectWidgets(Project project) {
            nameLabel = new Label(project.getProjectName());
            nameLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    selectProject = project;
                    refreshTable(false);
                }
            });
            nameLabel.addDoubleClickHandler(new DoubleClickHandler() {
                @Override
                public void onDoubleClick(DoubleClickEvent event) {
                    searchAction.onProjectSelected(project.getProjectId());
                    ProjectSearchDialog.this.hide();
                }
            });
            nameLabel.addStyleName("ode-ProjectNameLabel");

            DateTimeFormat dateTimeFormat = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_TIME_SHORT);

            Date dateCreated = new Date(project.getDateCreated());
            dateCreatedLabel = new Label(dateTimeFormat.format(dateCreated));
        }
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
        table.setWidget(0, 0, nameHeader);

        HorizontalPanel dateCreatedHeader = new HorizontalPanel();
        final Label dateCreatedHeaderLabel = new Label(MESSAGES.projectDateCreatedHeader());
        dateCreatedHeaderLabel.addStyleName("ode-ProjectHeaderLabel");
        dateCreatedHeader.add(dateCreatedHeaderLabel);
        dateCreatedSortIndicator.addStyleName("ode-ProjectHeaderLabel");
        dateCreatedHeader.add(dateCreatedSortIndicator);
        table.setWidget(0, 1, dateCreatedHeader);

        MouseDownHandler mouseDownHandler = new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent e) {
                SortField clickedSortField = SortField.NAME;
                if (e.getSource() == dateCreatedHeaderLabel || e.getSource() == dateCreatedSortIndicator) {
                    clickedSortField = SortField.DATE_CREATED;
                }
                changeSortOrder(clickedSortField);
            }
        };
        nameHeaderLabel.addMouseDownHandler(mouseDownHandler);
        nameSortIndicator.addMouseDownHandler(mouseDownHandler);
        dateCreatedHeaderLabel.addMouseDownHandler(mouseDownHandler);
        dateCreatedSortIndicator.addMouseDownHandler(mouseDownHandler);
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
                dateCreatedSortIndicator.setText("");
                break;
            case DATE_CREATED:
                dateCreatedSortIndicator.setText(text);
                nameSortIndicator.setText("");
                break;
        }
    }

    private void refreshTable(boolean needToSort) {
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
                case DATE_CREATED:
                    comparator = (sortOrder == SortOrder.ASCENDING)
                            ? ProjectComparators.COMPARE_BY_DATE_CREATED_ASCENDING
                            : ProjectComparators.COMPARE_BY_DATE_CREATED_DESCENDING;
                    break;
            }
            Collections.sort(currentProjects, comparator);
        }

        refreshSortIndicators();

        // Refill the table.
        table.resize(1 + currentProjects.size(), 2);
        int row = 1;
        for (Project project : currentProjects) {
            SimpleProjectWidgets pw = projectWidgets.get(project);
            if (selectProject == project) {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowHighlighted");
            } else {
                table.getRowFormatter().setStyleName(row, "ode-ProjectRowUnHighlighted");
            }
            table.setWidget(row, 0, pw.nameLabel);
            table.setWidget(row, 1, pw.dateCreatedLabel);

            row++;
        }
    }

    private class NameFilterAction implements Command {

        @Override
        public void execute() {
            String words = wordsProperty.getValue().toLowerCase();
            currentProjects = projects.stream().filter(new Predicate<Project>() {
                @Override
                public boolean test(Project project) {
                    return project.getProjectName().toLowerCase().contains(words);
                }
            }).collect(Collectors.toList());
            refreshTable(false);
        }
    }
}
