package com.project.app.dto.report;

public class ReportCountItemResponse {

    private String label;
    private long count;

    public ReportCountItemResponse() {
    }

    public ReportCountItemResponse(String label, long count) {
        this.label = label;
        this.count = count;
    }

    public String getLabel() {
        return label;
    }

    public long getCount() {
        return count;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCount(long count) {
        this.count = count;
    }
}