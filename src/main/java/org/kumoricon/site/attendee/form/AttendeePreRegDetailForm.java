package org.kumoricon.site.attendee.form;

import com.vaadin.ui.FormLayout;
import org.kumoricon.site.attendee.DetailFormHandler;

public class AttendeePreRegDetailForm extends AttendeeDetailForm {
    // Subclass the attendee detail form and hide the "parental consent form received" checkbox
    // so that it can be displayed down near the bottom of the form instead, and the "checked in"
    // checkbox because it doesn't make any sense here
    public AttendeePreRegDetailForm(DetailFormHandler handler) {
        super(handler);
    }

    @Override
    protected FormLayout buildCheckedIn() {
        FormLayout f = new FormLayout();
        // Don't show the consent form or checked in checkbox here
        return f;
    }

    @Override
    protected FormLayout buildNotes() {
        FormLayout f = new FormLayout();
        // Don't show history on prereg checkin screen
        return f;
    }
}
