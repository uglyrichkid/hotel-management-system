package com.project.app.dto.report;

import java.math.BigDecimal;

public class ReportAmountItemResponse {

    private String label;
    private BigDecimal amount;

    public ReportAmountItemResponse() {
    }

    public ReportAmountItemResponse(String label, BigDecimal amount) {
        this.label = label;
        this.amount = amount;
    }

    public String getLabel() {
        return label;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}