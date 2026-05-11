package com.github.nsu_upprpo.school_app.model;

public class ParentPaymentItem {
    private final String month;
    private final String amount;
    private final boolean paid;

    public ParentPaymentItem(String month, String amount, boolean paid) {
        this.month = month;
        this.amount = amount;
        this.paid = paid;
    }

    public String getMonth() { return month; }
    public String getAmount() { return amount; }
    public boolean isPaid() { return paid; }
}