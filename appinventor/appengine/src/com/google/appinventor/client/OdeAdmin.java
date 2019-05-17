package com.google.appinventor.client;

import com.google.appinventor.client.boxes.ClassListBox;
import com.google.appinventor.client.boxes.CourseListBox;
import com.google.appinventor.client.boxes.PaletteBox;
import com.google.appinventor.client.boxes.ScoreProjectListBox;
import com.google.appinventor.client.explorer.score.ClassManager;
import com.google.appinventor.client.explorer.score.ScoreProjectManager;
import com.google.appinventor.client.explorer.youngandroid.ClassToolbar;
import com.google.appinventor.client.explorer.youngandroid.CourseToolbar;
import com.google.appinventor.client.explorer.youngandroid.ScoreProjectToolbar;
import com.google.appinventor.shared.rpc.user.User;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class OdeAdmin extends Ode {

    //Just something changed in Admin page
    private ScoreProjectManager scoreProjectManager;
    private ClassManager classManager;

    private ScoreProjectToolbar scoreProjectToolbar;
    private CourseToolbar courseToolbar;
    private ClassToolbar classToolbar;
    private HorizontalPanel adminPanel;
    private int courseTabIndex;

    /**
     * Returns global instance of OdeAdmin.
     *
     * @return  global OdeAdmin instance
     */
    public static OdeAdmin getInstance() {
        return (OdeAdmin)Ode.getInstance();
    }

    public ScoreProjectManager getScoreProjectManager() { return scoreProjectManager; }

    public ClassManager getClassManager() {
        return classManager;
    }

    public ScoreProjectToolbar getScoreProjectToolbar() {
        return scoreProjectToolbar;
    }

    public CourseToolbar getCourseToolbar() {
        return courseToolbar;
    }

    public ClassToolbar getClassToolbar() {
        return classToolbar;
    }

    @Override
    protected boolean initializeAdmin(User user) {
        if (!user.getIsAdmin()) {
            String locale = Window.Location.getParameter("locale");
            String repo = Window.Location.getParameter("repo");
            String galleryId = Window.Location.getParameter("galleryId");
            String separator = "?";
            String uri = "/";
            if (locale != null && !locale.equals("")) {
                uri += separator + "locale=" + locale;
                separator = "&";
            }
            if (repo != null && !repo.equals("")) {
                uri += separator + "repo=" + repo;
                separator = "&";
            }
            if (galleryId != null && !galleryId.equals("")) {
                uri += separator + "galleryId=" + galleryId;
            }
            Window.Location.replace(uri);
            Window.alert("You are not admin!");
            return false;           // likely not reached
        }
        isAdminMode = true;
        return true;
    }

    @Override
    protected void loadAdminModule() {
        scoreProjectManager = new ScoreProjectManager();
        classManager = new ClassManager();
    }

    @Override
    protected void initializeUi() {
        super.initializeUi();

        PaletteBox.getPaletteBox().setHide(true);

        adminPanel = new HorizontalPanel();

        // Projects tab
        VerticalPanel pVertPanel = new VerticalPanel();
        pVertPanel.setWidth("100%");
        pVertPanel.setSpacing(0);
        scoreProjectToolbar = new ScoreProjectToolbar();
        pVertPanel.add(scoreProjectToolbar);
        pVertPanel.add(ScoreProjectListBox.getScoreProjectListBox());
        adminPanel.add(pVertPanel);
        adminPanel.setCellWidth(pVertPanel, "36%");

        VerticalPanel spacePanel = new VerticalPanel();
        spacePanel.setSpacing(0);
        adminPanel.add(spacePanel);
        adminPanel.setCellWidth(spacePanel, "1%");

        // Design tab
        VerticalPanel dVertPanel = new VerticalPanel();
        dVertPanel.setWidth("100%");
        dVertPanel.setSpacing(0);
        dVertPanel.add(Ode.getInstance().getDesignToolbar());
        dVertPanel.add(Ode.getInstance().getWorkColumns());
        adminPanel.add(dVertPanel);
        adminPanel.setCellWidth(dVertPanel, "63%");
        setProjectView(adminPanel);

        HorizontalPanel panel = new HorizontalPanel();
        panel.setWidth("100%");

        VerticalPanel coursePanel = new VerticalPanel();
        coursePanel.setWidth("100%");
        coursePanel.setSpacing(0);
        courseToolbar = new CourseToolbar();
        coursePanel.add(courseToolbar);
        coursePanel.add(CourseListBox.getCourseListBox());
        panel.add(coursePanel);

        VerticalPanel classPanel = new VerticalPanel();
        classPanel.setWidth("100%");
        classPanel.setSpacing(0);
        classToolbar = new ClassToolbar();
        classPanel.add(classToolbar);
        classPanel.add(ClassListBox.getClassListBox());
        panel.add(classPanel);

        courseTabIndex = deckPanel.getWidgetCount();
        deckPanel.add(panel);

        switchToProjectsView();
        Window.setTitle("App Inventor Admin");
    }

    public void switchToCourseView() {
        deckPanel.showWidget(courseTabIndex);
    }

    public void setAdminPanelWidth(String projectTabWidth, String designTabWidth) {
        adminPanel.setCellWidth(adminPanel.getWidget(0), projectTabWidth);
        adminPanel.setCellWidth(adminPanel.getWidget(2), designTabWidth);
    }
}
