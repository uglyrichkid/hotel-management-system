package com.project.app.service.report;

import com.project.app.dto.report.BookingReportResponse;
import com.project.app.dto.report.OccupancyReportResponse;
import com.project.app.dto.report.RevenueReportResponse;

import java.time.LocalDate;

public interface ReportService {

    BookingReportResponse getBookingReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo);

    RevenueReportResponse getRevenueReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo);

    OccupancyReportResponse getOccupancyReport(Long hotelId, LocalDate dateFrom, LocalDate dateTo);
}