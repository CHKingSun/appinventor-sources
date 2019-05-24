package com.google.appinventor.client.utils;

import com.google.appinventor.client.widgets.TextButton;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class DeleteConfirmDialog extends DialogBox {

    public DeleteConfirmDialog(String caption, String text, Command action) {
        setText(caption);
        setStylePrimaryName("ode-DialogBox");
        setGlassEnabled(true);
        setAnimationEnabled(true);
        center();

        VerticalPanel panel = new VerticalPanel();
        panel.add(new Label(text));

        HorizontalPanel buttonPanel = new HorizontalPanel();
        TextButton okButton = new TextButton("OK");
        okButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                action.execute();
                DeleteConfirmDialog.this.hide(true);
            }
        });
        buttonPanel.add(okButton);

        TextButton cancelButton = new TextButton("Cancel");
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                DeleteConfirmDialog.this.hide(true);
            }
        });
        buttonPanel.add(cancelButton);
        panel.add(buttonPanel);

        setWidget(panel);
    }
}
