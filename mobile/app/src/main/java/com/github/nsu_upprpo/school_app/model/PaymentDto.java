package com.github.nsu_upprpo.school_app.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class PaymentDto {

    @SerializedName("id")
    private String id;

    @SerializedName("amount")
    private BigDecimal amount;

    @SerializedName("childName")
    private String childName;

    @SerializedName("groupName")
    private String groupName;

    @SerializedName("period")
    private String period;

    @SerializedName("status")
    private String status;

    @SerializedName("type")
    private String type;

    @SerializedName("coversFrom")
    private String coversFrom;

    @SerializedName("coversTo")
    private String coversTo;

    @SerializedName("dueDate")
    private String dueDate;

    @SerializedName("submittedAt")
    private String submittedAt;

    @SerializedName("confirmedAt")
    private String confirmedAt;

    @SerializedName("rejectionReason")
    private String rejectionReason;

    public String getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getChildName() {
        return childName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getPeriod() {
        return period;
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getCoversFrom() {
        return coversFrom;
    }

    public String getCoversTo() {
        return coversTo;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getSubmittedAt() {
        return submittedAt;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}