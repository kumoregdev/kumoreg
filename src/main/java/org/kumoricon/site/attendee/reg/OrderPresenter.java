package org.kumoricon.site.attendee.reg;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.kumoricon.model.attendee.Attendee;
import org.kumoricon.model.attendee.AttendeeRepository;
import org.kumoricon.model.badge.Badge;
import org.kumoricon.model.badge.BadgeRepository;
import org.kumoricon.model.blacklist.BlacklistService;
import org.kumoricon.model.order.Order;
import org.kumoricon.model.order.OrderRepository;
import org.kumoricon.model.order.Payment;
import org.kumoricon.model.order.PaymentRepository;
import org.kumoricon.model.user.User;
import org.kumoricon.model.user.UserRepository;
import org.kumoricon.service.validate.AttendeeValidator;
import org.kumoricon.service.validate.PaymentValidator;
import org.kumoricon.site.BaseView;
import org.kumoricon.site.attendee.AttendeePrintView;
import org.kumoricon.site.attendee.BadgePrintingPresenter;
import org.kumoricon.site.attendee.PrintBadgeHandler;
import org.kumoricon.site.attendee.window.PrintBadgeWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderPresenter extends BadgePrintingPresenter implements PrintBadgeHandler {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private AttendeeValidator attendeeValidator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BlacklistService blacklistService;

    @Autowired
    private PaymentRepository paymentRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderPresenter.class);

    public OrderPresenter() {
    }

    public void createNewOrder(OrderView view) {
        Order order = new Order();
        order.setOrderId(order.generateOrderId());
        log.info("{} created new order {}", view.getCurrentUser(), order);
        order = orderRepository.save(order);
        view.navigateTo(view.VIEW_NAME + "/" + order.getId());
    }

    public void showOrder(OrderView view, int id) {
        Order order = orderRepository.findOne(id);
        if (order != null) {
            log.info("{} viewed order {}", view.getCurrentUsername(), order);
            view.afterSuccessfulFetch(order);
        } else {
            log.error("{} tried to view order {} and it was not found.", view.getCurrentUsername(), id);
            view.notifyError("Error: order " + id + " not found.");
        }
    }

    public void savePayment(OrderView view, Payment payment) {
        Order order = view.getOrder();
        PaymentValidator.validate(payment);
        // Only update user, time and location if they're null - otherwise someone could be saving
        // changes to an existing payment
        if (payment.getPaymentTakenBy() == null) {
            payment.setPaymentTakenBy(view.getCurrentUser());
        }
        if (payment.getPaymentTakenAt() == null) {
            payment.setPaymentTakenAt(LocalDateTime.now());
        }
        if (payment.getPaymentLocation() == null) {
            payment.setPaymentLocation(view.getCurrentClientIPAddress());
        }
        log.info("{} saved payment {} to {}", view.getCurrentUsername(), payment, order);
        order.addPayment(payment);
        Order saved = orderRepository.save(order);
        view.afterSuccessfulFetch(saved);
    }

    public void deletePayment(OrderView view, Payment payment) {
        Order order = view.getOrder();
        log.info("{} removed payment {} from {}", view.getCurrentUsername(), payment, order);
        order.removePayment(payment);
        paymentRepository.delete(payment);
        Order saved = orderRepository.save(order);
        view.afterSuccessfulFetch(saved);
    }

    public void cancelOrder(OrderView view) {
        Order order = view.getOrder();
        if (order.getAttendeeList().size() == 0 && !order.getPaid()) {
            log.info("{} canceled empty order {}. It was deleted.", view.getCurrentUsername(), order);
            orderRepository.delete(order);
        }
        view.navigateTo("");
    }

    public void addNewAttendee(OrderView view) {
        log.info("{} created new attendee", view.getCurrentUsername());
        Attendee newAttendee = new Attendee();
        Order order = view.getOrder();

        newAttendee.setBadgeNumber(generateBadgeNumber(view));
        newAttendee.setOrder(order);

        // If the order already has attendees, carry over emergency contact information from the
        // last attendee added
        if (order != null && order.getAttendees() != null && order.getAttendees().size() > 0) {
            Attendee lastAttendee = order.getAttendees().get(order.getAttendees().size() -1);
            newAttendee.setEmergencyContactFullName(lastAttendee.getEmergencyContactFullName());
            newAttendee.setEmergencyContactPhone(lastAttendee.getEmergencyContactPhone());
        }

        selectAttendee(view, newAttendee);
    }

    public void addAttendeeToOrder(OrderView view, Attendee attendee) {
        Order order = view.getOrder();

        if (!blacklistService.isOnBlacklist(attendee)) {
            log.info("{} added attendee {} to order {}", view.getCurrentUsername(), attendee, order);
            order.addAttendee(attendee);
        } else {
            if (view.currentUserHasRight("at_con_registration_blacklist")) {
                log.info("{} added blacklisted attendee {} to order {}", view.getCurrentUsername(), attendee, order);
                view.showBlacklistConfirmationWindow();
                order.addAttendee(attendee);
            } else {
                view.showBlacklistWarningWindow();
                log.info("{} tried to add {} to {} but attendee is on blacklist. Attendee not added.",
                        view.getCurrentUser(), attendee, order);
            }
        }
        order.setTotalAmount(getOrderTotal(order));
        order = orderRepository.save(order);
        view.afterSuccessfulFetch(order);

    }

    private static BigDecimal getOrderTotal(Order order) {
        // Just get the total for all the attendees instead of keeping a running total
        // and adding the latest amount to it. Keeping a running total made testing a pain
        // if a value somehow got corrupt along the way
        BigDecimal total = BigDecimal.ZERO;
        for (Attendee a : order.getAttendeeList()) {
            total = total.add(a.getPaidAmount());
        }
        return total;
    }

    public void removeAttendeeFromOrder(OrderView view, Attendee attendee) {
        if (attendee != null && !attendee.getCheckedIn()) {
            String name = attendee.getName();
            Order order = view.getOrder();
            log.info("{} removed attendee {} from order {}. Attendee deleted.", view.getCurrentUsername(), attendee, order);
            order.removeAttendee(attendee);
            attendee.setOrder(null);

            order.setTotalAmount(getOrderTotal(order));
            Order result = orderRepository.save(order);
            view.afterSuccessfulFetch(result);
            attendeeRepository.delete(attendee);
            view.notify(name + " deleted");
            view.afterSuccessfulFetch(order);
        }
    }

    public void takeMoney(OrderView view) {
        Order currentOrder = view.getOrder();
        if (currentOrder.getAttendeeList().size() == 0) {
            view.notify("Error: No attendees in order");
            return;
        }

        // If the order total is $0 (all badges are free), just set payment type to cash automatically
        if (currentOrder.getTotalAmount().compareTo(currentOrder.getTotalPaid()) < 0) {
            view.notify("Error: money received less than order total");
        } else {
            orderComplete(view, currentOrder);
        }
    }

    public void orderComplete(OrderView view, Order currentOrder) {

        List<Attendee> badgesToPrint = new ArrayList<>();
        for (Attendee a : currentOrder.getAttendees()) {
            if (!a.getCheckedIn()) {
                badgesToPrint.add(a);
            }
        }
        log.info("{} saved order {} with {} badges to print",
                view.getCurrentUsername(), currentOrder);

        currentOrder.paymentComplete(view.getCurrentUser());

        orderRepository.save(currentOrder);

        if (badgesToPrint.size() > 0) {
            showAttendeeBadgeWindow(view, badgesToPrint);
        } else {
            view.navigateTo("/");
        }
    }

    public void selectAttendee(OrderView view, Attendee attendee) {
        log.info("{} viewed attendee {}", view.getCurrentUsername(), attendee);
        List<Badge> badgeTypesUserCanSee = new ArrayList<>();
        for (Badge badge : badgeRepository.findByVisibleTrue()) {
            if (badge.getRequiredRight() == null || view.currentUserHasRight(badge.getRequiredRight())) {
                badgeTypesUserCanSee.add(badge);
            }
        }
        view.showAttendeeDetail(attendee, badgeTypesUserCanSee);
    }

    @Transactional
    private String generateBadgeNumber(OrderView view) {
        User user = userRepository.findOne(view.getCurrentUser().getId());
        String badgeNumber = String.format("%1S%2$05d", user.getBadgePrefix(), user.getNextBadgeNumber());

        log.info("{} generated badge number {}", view.getCurrentUsername(), badgeNumber);
        userRepository.save(user);
        view.setLoggedInUser(user);
        return badgeNumber;
    }

    public void saveAuthNumberClicked(OrderView view, String value) {
        Order order = view.getOrder();
        log.info("{} set credit card authorization number {} for {}", view.getCurrentUsername(), value, order);
        String oldNotes = "";
        if (order.getNotes() != null) { oldNotes = order.getNotes(); }
        order.setNotes("Credit card authorization number: " + value + "\n" + oldNotes);
        orderComplete(view, order);
    }

    public Boolean validate(Attendee attendee) throws ValueException {
        if (attendee.isMinor()) {   // Move parent form received in to attendeeValidator???
            if (attendee.getParentFormReceived() == null || !attendee.getParentFormReceived()) {
                throw new ValueException("Error: Parental consent form has not been received");
            }
        }
        return attendeeValidator.validate(attendee);
    }

    @Override
    public void showAttendeeBadgeWindow(AttendeePrintView view, List<Attendee> attendeeList) {
        log.info("{} printing badge(s) for: {}", view.getCurrentUsername(), attendeeList);
        printBadges((BaseView) view, attendeeList);
        view.showPrintBadgeWindow(attendeeList);
    }

    @Override
    public void badgePrintSuccess(PrintBadgeWindow printBadgeWindow, List<Attendee> attendees) {
        BaseView view = printBadgeWindow.getParentView();
        log.info("{} reported badge(s) printed successfully for {}",
                printBadgeWindow.getParentView().getCurrentUser(), attendees);
        printBadgeWindow.close();
        view.notify("Order Complete");
        view.navigateTo("/");
    }

    @Override
    public void reprintBadges(PrintBadgeWindow printBadgeWindow, List<Attendee> attendeeList) {
        log.info("{} printing badge(s) for {} (reprint during order)",
                printBadgeWindow.getParentView().getCurrentUser(), attendeeList);
        printBadges(printBadgeWindow.getParentView(), attendeeList);
    }
}
