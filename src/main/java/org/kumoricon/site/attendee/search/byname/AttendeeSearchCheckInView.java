package org.kumoricon.site.attendee.search.byname;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import org.kumoricon.site.attendee.CheckInView;
import org.kumoricon.site.attendee.search.AttendeeSearchPresenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriTemplate;

import java.util.Map;

@ViewScope
@SpringView(name = AttendeeSearchCheckInView.TEMPLATE)
public class AttendeeSearchCheckInView extends CheckInView implements View {
    public static final String VIEW_NAME = "attendeeSearch";
    public static final String REQUIRED_RIGHT = "pre_reg_check_in";

    public static final String TEMPLATE = "attendeeSearch/{searchString}/{attendeeId}/checkin";
    public static final UriTemplate URI_TEMPLATE = new UriTemplate(TEMPLATE);

    protected Integer attendeeId;
    protected String searchString;

    @Autowired
    public AttendeeSearchCheckInView(AttendeeSearchPresenter handler) {
        super(handler);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

        Map<String, String> map = URI_TEMPLATE.match(viewChangeEvent.getViewName());

        this.searchString = map.get("searchString");
        try {
            attendeeId = Integer.parseInt(map.get("attendeeId"));
        } catch (NumberFormatException ex) {
            notifyError("Bad attendee id: must be an integer");
            close();
        }
        handler.showAttendee(this, attendeeId);
    }

    @Override
    protected void btnCheckInClicked() {
        handler.checkInAttendee(this, attendee);
        if (attendee.getOrder() != null) {
            navigateTo(SearchByNameView.VIEW_NAME + "/" + attendee.getOrder().getOrderId() + "/" + attendeeId + "/badge");
        } else {
            // TODO: if staff get an orderID set, remove this
            navigateTo(SearchByNameView.VIEW_NAME + "/" + "staff" + "/" + attendeeId + "/badge");
        }
    }

    @Override
    protected void btnEditClicked() {
        if (attendee.getOrder() != null) {
            navigateTo(SearchByNameView.VIEW_NAME + "/" + attendee.getOrder().getOrderId() + "/" + attendeeId);
        } else {
            // TODO: if staff get an orderID set, remove this
            navigateTo(SearchByNameView.VIEW_NAME + "/" + "staff" + "/" + attendeeId);
        }

    }

    @Override
    public void close() {
        if (attendeeId != null) {
            navigateTo(SearchByNameView.VIEW_NAME + "/" + searchString + "/" + attendeeId);
        } else {
            navigateTo(SearchByNameView.VIEW_NAME + "/" + searchString);
        }
    }

    @Override
    public String getRequiredRight() {
        return REQUIRED_RIGHT;
    }
}
