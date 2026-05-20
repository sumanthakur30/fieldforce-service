package com.shopmanagement.fieldforceservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.shopmanagement.fieldforceservice.model.AttendanceStatus;
import com.shopmanagement.fieldforceservice.model.ConversionStage;
import com.shopmanagement.fieldforceservice.model.FieldActivityType;
import com.shopmanagement.fieldforceservice.model.LeadPriority;
import com.shopmanagement.fieldforceservice.model.LeadSource;
import com.shopmanagement.fieldforceservice.model.LeadStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class FieldforceLeadApi {

    private FieldforceLeadApi() {
    }

    public record LeadUpsert(
            @NotBlank String businessName,
            String ownerName,
            @NotBlank String mobile,
            String alternateMobile,
            String businessType,
            String gstin,
            String address,
            String city,
            String stateCode,
            String pincode,
            BigDecimal gpsLatitude,
            BigDecimal gpsLongitude,
            LeadSource leadSource,
            Long createdByPromoterId,
            Long assignedSalesmanId,
            LeadPriority priority,
            String remarks,
            LocalDate expectedConversionDate) {
    }

    public record LeadResponse(
            Long id,
            String leadCode,
            String businessName,
            String ownerName,
            String mobile,
            String alternateMobile,
            String businessType,
            String gstin,
            String address,
            String city,
            String stateCode,
            String pincode,
            BigDecimal gpsLatitude,
            BigDecimal gpsLongitude,
            LeadSource leadSource,
            Long createdByPromoterId,
            Long assignedSalesmanId,
            LeadStatus leadStatus,
            LeadPriority priority,
            String remarks,
            LocalDate expectedConversionDate,
            Instant convertedAt,
            String externalMerchantId,
            String externalShopId,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record LeadStatusPatch(@NotNull LeadStatus leadStatus) {
    }

    public record DuplicateCheckRequest(
            String mobile,
            String gstin,
            String businessName,
            BigDecimal gpsLatitude,
            BigDecimal gpsLongitude) {
    }

    public record DuplicateCandidate(
            Long leadId,
            String leadCode,
            String businessName,
            String mobile,
            LeadStatus leadStatus,
            String matchReason,
            Double distanceMeters) {
    }

    public record DuplicateCheckResult(boolean hasDuplicates, List<DuplicateCandidate> candidates) {
    }

    public record ActivityCreate(
            @NotNull FieldActivityType activityType,
            Instant activityAt,
            BigDecimal gpsLatitude,
            BigDecimal gpsLongitude,
            String notes,
            LocalDate nextFollowupDate,
            String photoUrl,
            Long salesmanId,
            Long promoterId) {
    }

    public record ActivityResponse(
            Long id,
            Long leadId,
            FieldActivityType activityType,
            Instant activityAt,
            BigDecimal gpsLatitude,
            BigDecimal gpsLongitude,
            String notes,
            LocalDate nextFollowupDate,
            String photoUrl,
            Long salesmanId,
            Long promoterId,
            Instant createdAt) {
    }

    public record BeatPlanUpsert(
            @NotBlank String beatName,
            String stateCode,
            String cityCode,
            String areaDescription,
            Boolean active) {
    }

    public record BeatPlanResponse(
            Long id,
            String beatCode,
            String beatName,
            String stateCode,
            String cityCode,
            String areaDescription,
            boolean active,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record BeatAssignmentCreate(
            @NotNull Long salesmanId,
            @Min(1) @Max(7) int dayOfWeek,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo) {
    }

    public record BeatAssignmentResponse(
            Long id,
            Long beatPlanId,
            Long salesmanId,
            int dayOfWeek,
            LocalDate effectiveFrom,
            LocalDate effectiveTo) {
    }

    public record AttendanceCheckIn(
            @NotNull Long salesmanId,
            BigDecimal latitude,
            BigDecimal longitude) {
    }

    public record AttendanceCheckOut(
            @NotNull Long salesmanId,
            BigDecimal latitude,
            BigDecimal longitude) {
    }

    public record AttendanceResponse(
            Long id,
            Long salesmanId,
            LocalDate attendanceDate,
            Instant checkInAt,
            Instant checkOutAt,
            AttendanceStatus status) {
    }

    public record GpsLogCreate(
            @NotNull Long salesmanId,
            @NotNull BigDecimal latitude,
            @NotNull BigDecimal longitude,
            BigDecimal accuracyMeters,
            Long leadId,
            Long fieldActivityId) {
    }

    public record ConversionUpsert(
            @NotNull ConversionStage stage,
            String subscriptionPlanCode,
            Boolean kycVerified,
            String merchantPayloadJson) {
    }

    public record ConversionCompleteRequest(
            String subscriptionPlanCode,
            String merchantPayloadJson,
            String ownerEmail,
            String ownerUsername) {
    }

    public record ConversionResponse(
            Long id,
            Long leadId,
            ConversionStage currentStage,
            String subscriptionPlanCode,
            boolean kycVerified,
            String externalMerchantId,
            String externalShopId,
            String failureReason,
            Instant startedAt,
            Instant completedAt,
            Boolean ownerInviteEmailQueued,
            String inviteExpiresAt) {
    }

    public record ShopCreationResult(
            String externalMerchantId,
            String externalShopId,
            Boolean ownerInviteEmailQueued,
            String inviteExpiresAt) {
    }

    public record PerformanceTargetUpsert(
            @NotNull LocalDate targetMonth,
            Long promoterId,
            Long salesmanId,
            int leadTarget,
            int conversionTarget,
            BigDecimal revenueTarget) {
    }

    public record PerformanceTargetResponse(
            Long id,
            LocalDate targetMonth,
            Long promoterId,
            Long salesmanId,
            int leadTarget,
            int conversionTarget,
            BigDecimal revenueTarget) {
    }

    public record LeadFunnelAnalytics(Map<LeadStatus, Long> countsByStatus, long totalLeads, long convertedLeads) {
    }

    public record PerformanceAnalytics(
            long leadsCreated,
            long conversions,
            double conversionRatePercent,
            long activitiesLogged,
            long attendanceDays) {
    }

    public record ExtendedDashboardSummary(
            long promoters,
            long salesmen,
            long leads,
            long leadsConverted,
            long activitiesThisMonth,
            long shopsLegacy,
            long shopsPendingApproval,
            long pendingCommissionLines,
            BigDecimal pendingCommissionAmount) {
    }
}
