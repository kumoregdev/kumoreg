package org.kumoricon.model.badge;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class AgeRange {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private int id;
    @NotNull
    private String name;
    private int minAge;
    private int maxAge;
    private String stripeColor;
    private String stripeText;
    private BigDecimal cost;

    public AgeRange(String name, int minAge, int maxAge, BigDecimal cost, String stripeColor, String stripeText) {
        this.name = name;
        setMinAge(minAge);
        setMaxAge(maxAge);
        setCost(cost);
        this.stripeColor = stripeColor;
        this.stripeText = stripeText;
    }

    public AgeRange() { this("", 0, 255, BigDecimal.ZERO, "", ""); }
    public AgeRange(String name, int minAge, int maxAge) { this(name, minAge, maxAge, BigDecimal.ZERO, "", ""); }
    public AgeRange(String name, int minAge, int maxAge, String cost, String stripeColor, String stripeText) {
        this(name, minAge, maxAge, new BigDecimal(cost), stripeColor, stripeText);
    }
    public AgeRange(String name, int minAge, int maxAge, double cost, String stripeColor, String stripeText) {
        this(name, minAge, maxAge, new BigDecimal(cost), stripeColor, stripeText);
    }

    public String getStripeColor() { return stripeColor; }
    public void setStripeColor(String stripeColor) { this.stripeColor = stripeColor; }

    public String getStripeText() { return stripeText; }
    public void setStripeText(String stripeText) { this.stripeText = stripeText; }


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) {
        if (minAge < 0) {
            this.minAge = 0;
        } else if (minAge > 255) {
            this.minAge = 255;
        } else {
            this.minAge = minAge;
        }
    }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) {
        if (maxAge < 0) {
            this.maxAge = 0;
        } else if (maxAge > 255) {
            this.maxAge = 255;
        } else {
            this.maxAge = maxAge;
        }
    }

    public BigDecimal getCost() { return cost; }
    public void setCost(double cost) { setCost(BigDecimal.valueOf(cost)); }
    public void setCost(String cost) { setCost(new BigDecimal(cost)); }
    public void setCost(BigDecimal cost) {
        if (cost.compareTo(BigDecimal.ZERO) >= 0) {
            this.cost = cost;
        } else {
            this.cost = BigDecimal.ZERO;
        }
    }

    public boolean isValidForAge(long age) {
        return (age >= minAge && age <= maxAge);
    }

    public boolean isValidForAge(Integer age) {
        return age != null && (age >= minAge && age <= maxAge);
    }

    public String toString() {
        return String.format("%s (%s-%s): $%s", name, minAge, maxAge, cost.setScale(2).toString());
    }
}
