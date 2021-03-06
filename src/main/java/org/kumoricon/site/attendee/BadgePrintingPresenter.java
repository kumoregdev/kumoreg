package org.kumoricon.site.attendee;

import org.kumoricon.BaseGridView;
import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.user.User;
import org.kumoricon.model.user.UserRepository;
import org.kumoricon.service.print.BadgePrintService;
import org.kumoricon.service.print.formatter.BadgePrintFormatter;
import org.kumoricon.site.BaseView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.print.PrintException;
import java.util.List;

/**
 * Common methods for printing badges used across multiple presenters
 */
public class BadgePrintingPresenter {
    protected static final Logger log = LoggerFactory.getLogger(BadgePrintingPresenter.class);

    @Autowired
    protected BadgePrintService badgePrintService;

    @Autowired
    protected UserRepository userRepository;

    /**
     * Print badges for the given attendees and display any error or result messages
     * @param view Current view
     * @param attendeeList Attendees to print badges for
     */
    protected void printBadges(BaseView view, List<Attendee> attendeeList) {
        try {
            String result = badgePrintService.printBadgesForAttendees(
                    attendeeList, view.getCurrentClientIPAddress());
            view.notify(result);
            log.info(result + " during badge print for {}", view.getCurrentUsername());
        } catch (PrintException e) {
            log.error("Error printing badges for {}", view.getCurrentUsername(), e);
            view.notifyError(e.getMessage());
        }
    }

    protected void printBadges(BaseGridView view, List<Attendee> attendeeList) {
        try {
            String result = badgePrintService.printBadgesForAttendees(
                    attendeeList, view.getCurrentClientIPAddress());
            view.notify(result);
            log.info(result + " during badge print for {}", view.getCurrentUsername());
        } catch (PrintException e) {
            log.error("Error printing badges for {}", view.getCurrentUsername(), e);
            view.notifyError(e.getMessage());
        }
    }

    public BadgePrintFormatter getBadgeFormatter(BaseGridView view, List<Attendee> attendees) {
        return badgePrintService.getCurrentBadgeFormatter(attendees, view.getCurrentClientIPAddress());
    }

    public User findUser(String username) {
        return userRepository.findOneByUsernameIgnoreCase(username);
    }
}
