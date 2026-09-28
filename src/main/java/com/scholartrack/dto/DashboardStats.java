package com.scholartrack.dto;

public record DashboardStats(
        long totalStudents,
        long totalSchemes,
        long totalApplications,
        long eligibleApplications,
        long ineligibleApplications,
        long underReview,
        long approved,
        long disbursed) {
}
