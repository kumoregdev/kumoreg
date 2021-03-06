package org.kumoricon.site.report.till;

import org.kumoricon.model.session.Session;
import org.kumoricon.model.session.SessionService;
import org.kumoricon.model.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class TillReportPresenter {
    private final SessionService sessionService;

    private static final Logger log = LoggerFactory.getLogger(TillReportPresenter.class);
    private static final ZoneId PACIFIC = ZoneId.of("America/Los_Angeles");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a").withZone(PACIFIC);


    @Autowired
    public TillReportPresenter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    void showAllTills(TillReportView view) {
        log.info("{} viewed Till Report", view.getCurrentUsername());
        long startTime = System.currentTimeMillis();
        StringBuilder output = new StringBuilder();

        List<Session> sessions = sessionService.getAllSessions();

        // Header
        output.append("<div class=\"kumoReport\">");
        output.append("<h2>Till Report</h2>");
        output.append(String.format("%s\n", Instant.now().atZone(PACIFIC).format(DATE_TIME_FORMATTER)));
        output.append("<table border=\"1\" cellpadding=\"2\"><tr>");
        output.append("<td>User</td>");
        output.append("<td>Session</td>");
        output.append("<td>Start</td>");
        output.append("<td>End</td>");
        output.append("<td>Total</td>");
        output.append("<td>Payments</td>");
        output.append("</tr>");

        for (Session session : sessions) {
            output.append("<tr>");
            User user = session.getUser();
            output.append(String.format("<td>%s %s (%s: %s)</td>",
                    user.getFirstName(), user.getLastName(), user.getId(), user.getUsername()));
            output.append(String.format("<td align=\"right\">%s</td>", session.getId()));
            output.append(String.format("<td align=\"right\">%s</td>", session.getStart().atZone(PACIFIC).format(DATE_TIME_FORMATTER)));
            if (session.getEnd() != null) {
                output.append(String.format("<td align=\"right\">%s</td>", session.getEnd().atZone(PACIFIC).format(DATE_TIME_FORMATTER)));
            } else {
                output.append("<td align=\"right\">open</td>");
            }
            output.append(String.format("<td align=\"right\">$%s</td>", sessionService.getTotalForSession(session)));
            output.append(String.format("<td align=\"right\"><pre>%s</pre></td>",
                    sessionService.buildTextTotalsForSession(session)));
            output.append("</tr>");
        }

        output.append("</table></div>");
        log.info("{} till report generated in {} ms",
                view.getCurrentUsername(), System.currentTimeMillis() - startTime);
        view.showData(output.toString());
    }
}
