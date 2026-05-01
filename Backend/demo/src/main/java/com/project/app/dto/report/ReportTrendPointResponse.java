package com.project.app.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ReportTrendPointResponse {

    private LocalDate date;
    private BigDecimal value;

    public ReportTrendPointResponse() {
    }

    public ReportTrendPointResponse(LocalDate date, BigDecimal value) {
        this.date = date;
        this.value = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}