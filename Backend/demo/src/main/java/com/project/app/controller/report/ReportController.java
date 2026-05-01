package com.project.app.controller.report;

import com.project.app.dto.report.BookingReportResponse;
import com.project.app.dto.report.OccupancyReportResponse;
import com.project.app.dto.report.RevenueReportResponse;
import com.project.app.service.report.ReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/bookings")
    public BookingReportResponse getBookingReport(
            @RequestParam Long hotelId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return reportService.getBookingReport(hotelId, dateFrom, dateTo);
    }

    @GetMapping("/revenue")
    public RevenueReportResponse getRevenueReport(
            @RequestParam Long hotelId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return reportService.getRevenueReport(hotelId, dateFrom, dateTo);
    }

    @GetMapping("/occupancy")
    public OccupancyReportResponse getOccupancyReport(
            @RequestParam Long hotelId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        return reportService.getOccupancyReport(hotelId, dateFrom, dateTo);
    }
}