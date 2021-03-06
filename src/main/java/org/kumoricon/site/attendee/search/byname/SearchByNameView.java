package org.kumoricon.site.attendee.search.byname;

import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.renderers.HtmlRenderer;
import org.kumoricon.BaseGridView;
import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.site.ButtonField;
import org.kumoricon.site.attendee.search.SearchPresenter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

@ViewScope
@SpringView(name = SearchByNameView.VIEW_NAME)
public class SearchByNameView extends BaseGridView implements View {
    public static final String VIEW_NAME = "attendeeSearch";
    public static final String REQUIRED_RIGHT = "attendee_search";

    private final SearchPresenter handler;

    private ButtonField txtSearch = new ButtonField();
    private Grid<Attendee> tblResult = new Grid<>();
    private Grid.Column<Attendee, String> checkInLinkColumn;

    @Autowired
    public SearchByNameView(SearchPresenter handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        setRows(2);
        setColumns(5);
        setHeight("90%");
        setRowExpandRatio(1, 10);

        addComponent(txtSearch, 1, 0, 2, 0);

        txtSearch.addClickListener((Button.ClickListener) clickEvent -> navigateTo(VIEW_NAME + "/" + txtSearch.getValue()));
        txtSearch.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        txtSearch.setButtonCaption("Search");
        txtSearch.setPlaceholder("Search by name");

        tblResult.setWidth("95%");
        tblResult.setHeight("90%");

        tblResult.addColumn(Attendee::getFirstName).setCaption("First Name");
        tblResult.addColumn(Attendee::getLastName).setCaption("Last Name");
        tblResult.addColumn(Attendee::getLegalFirstName).setCaption("Legal First Name");
        tblResult.addColumn(Attendee::getLegalLastName).setCaption("Legal Last Name");
        tblResult.addColumn(Attendee::getFanName).setCaption("Fan Name");
        tblResult.addColumn(Attendee::getBadgeNumber).setCaption("Badge Number");
        tblResult.addColumn(Attendee::getAge).setCaption("Age");
        tblResult.addColumn(Attendee::getZip).setCaption("Zip");
        tblResult.addColumn(Attendee::getCheckedIn).setCaption("Checked In");
        if (currentUserHasRight("pre_reg_check_in")) {
            checkInLinkColumn = tblResult.addColumn(attendee -> {
                        if (!attendee.getCheckedIn()) {
                            return "<a href='#!" + VIEW_NAME + "/" + txtSearch.getValue().replace(" ", "%20") + "/" + attendee.getId() + "/checkin'>Check In</a>";
                        } else {
                            return "";
                        }},
                    new HtmlRenderer());
        }

        tblResult.addStyleName("kumoHandPointer");
        tblResult.addItemClickListener(itemClickEvent -> {
            // Don't navigate away if a link in the table was clicked, just follow that link
            if (checkInLinkColumn != itemClickEvent.getColumn()) {
                navigateTo(AttendeeSearchDetailView.VIEW_NAME +
                        "/" + txtSearch.getValue() +
                        "/" + itemClickEvent.getItem().getId());
            }
        });

        addComponent(tblResult, 0, 1, 4, 1);
        txtSearch.focus();
    }


    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        super.enter(viewChangeEvent);
        String parameters = viewChangeEvent.getParameters();
        if (parameters == null || parameters.equals("")) {
            txtSearch.clear();
        } else {
            String searchString = viewChangeEvent.getParameters();
            if (txtSearch.getValue() != null && !txtSearch.getValue().equals(searchString)) {
                txtSearch.setValue(searchString);
                handler.searchFor(this, searchString);
            }
        }
    }

    public void afterSuccessfulFetch(List<Attendee> attendees) {
        tblResult.setItems(attendees);
        txtSearch.selectAll();
    }

    @Override
    public void refresh() {
        handler.searchFor(this, txtSearch.getValue());
    }

    public String getRequiredRight() { return REQUIRED_RIGHT; }
}
