package org.kumoricon.site.report.checkinbyhour;

import com.vaadin.navigator.View;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import org.kumoricon.BaseGridView;
import org.kumoricon.site.report.ReportView;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@ViewScope
@SpringView(name = CheckInByHourReportView.VIEW_NAME)
public class CheckInByHourReportView extends BaseGridView implements View, ReportView {
    public static final String VIEW_NAME = "checkInByHourReport";
    public static final String REQUIRED_RIGHT = "view_check_in_by_hour_report";

    private final CheckInByHourReportPresenter handler;

    private final Button btnRefresh = new Button("Refresh");
    private final Label data = new Label();

    @Autowired
    public CheckInByHourReportView(CheckInByHourReportPresenter handler) {
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        setColumns(4);
        setRows(1);
        setColumnExpandRatio(0, 10);
        setColumnExpandRatio(3, 10);
        btnRefresh.addClickListener((Button.ClickListener) clickEvent -> handler.fetchReportData(this));
        addComponent(data, 1, 0);
        addComponent(btnRefresh, 2, 0);
        data.setContentMode(ContentMode.HTML);
        handler.fetchReportData(this);
        data.setWidth("600px");
    }

    public void afterSuccessfulFetch(String reportData) {
        data.setValue(reportData);
    }

    public String getRequiredRight() {
        return REQUIRED_RIGHT;
    }
}
