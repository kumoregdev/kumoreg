package org.kumoricon.model.order;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String orderId;
    private BigDecimal totalAmount;
    private Boolean paid;
    private PaymentType paymentType;
    private String notes;

    public enum PaymentType {
        CASH {
            public String toString() { return "Cash"; }
        }, CHECK {
            public String toString() { return "Check"; }
        }, MONEYORDER {
            public String toString() { return "Money Order"; }
        }, CREDIT {
            public String toString() { return "Credit Card"; }
        }, MANUAL {
            public String toString() { return "Manual/Free"; }
        }
    }

    public Order() {
        this.totalAmount = BigDecimal.ZERO;
        this.paid = false;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Boolean getPaid() { return paid; }
    public void setPaid(Boolean paid) { this.paid = paid; }

    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
}
