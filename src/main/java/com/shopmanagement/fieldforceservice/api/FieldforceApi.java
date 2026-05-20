package com.shopmanagement.fieldforceservice.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.shopmanagement.fieldforceservice.model.ApprovalStatus;
import com.shopmanagement.fieldforceservice.model.CommissionBeneficiaryType;
import com.shopmanagement.fieldforceservice.model.CommissionEntryStatus;
import com.shopmanagement.fieldforceservice.model.FieldPersonStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class FieldforceApi {

    private FieldforceApi() {
    }

    public record PromoterUpsert(
            @NotBlank String fullName,
            @NotBlank String mobile,
            String email,
            String address,
            @NotBlank String stateCode,
            @NotBlank String cityCode,
            String profilePhotoUrl,
            @NotNull LocalDate joiningDate,
            String pan,
            String aadhaarMasked,
            String gstin,
            String bankAccountName,
            String bankIfsc,
            String bankAccountNumber) {
    }

    public record PromoterResponse(
            Long id,
            String promoterCode,
            String fullName,
            String mobile,
            String email,
            String address,
            String stateCode,
            String cityCode,
            String profilePhotoUrl,
            LocalDate joiningDate,
            FieldPersonStatus status,
            String pan,
            String aadhaarMasked,
            String gstin,
            String bankAccountName,
            String bankIfsc,
            String bankAccountNumber,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record PromoterDetailResponse(
            PromoterResponse promoter,
            long territoryCount,
            long salesmanCount,
            long registeredShopCount,
            BigDecimal pendingCommissionTotal) {
    }

    public record TerritoryUpsert(@NotBlank String stateCode, @NotBlank String cityCode) {
    }

    public record TerritoryResponse(Long id, String stateCode, String cityCode, Instant createdAt) {
    }

    public record SalesmanUpsert(
            @NotNull Long promoterId,
            @NotBlank String fullName,
            @NotBlank String mobile,
            String email,
            @NotBlank String stateCode,
            @NotBlank String cityCode,
            @NotNull LocalDate joiningDate) {
    }

    public record SalesmanResponse(
            Long id,
            String salesmanCode,
            Long promoterId,
            String promoterCode,
            String fullName,
            String mobile,
            String email,
            String stateCode,
            String cityCode,
            LocalDate joiningDate,
            FieldPersonStatus status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record ShopRegistrationUpsert(
            @NotBlank String externalShopId,
            @NotBlank String shopName,
            String ownerName,
            String ownerMobile,
            String address,
            String stateCode,
            String cityCode,
            Long promoterId,
            Long salesmanId,
            Instant registeredAt) {
    }

    public record ShopRegistrationResponse(
            Long id,
            String externalShopId,
            String shopName,
            String ownerName,
            String ownerMobile,
            String address,
            String stateCode,
            String cityCode,
            Instant registeredAt,
            Long promoterId,
            Long salesmanId,
            ApprovalStatus approvalStatus,
            Instant updatedAt) {
    }

    public record ApprovalPatch(@NotNull ApprovalStatus approvalStatus) {
    }

    public record CommissionPlanUpsert(
            @NotBlank String name,
            BigDecimal promoterFixedAmount,
            BigDecimal promoterPercent,
            BigDecimal salesmanFixedAmount,
            BigDecimal salesmanPercent,
            @NotNull LocalDate effectiveFrom,
            LocalDate effectiveTo,
            boolean active) {
    }

    public record CommissionPlanResponse(
            Long id,
            String name,
            BigDecimal promoterFixedAmount,
            BigDecimal promoterPercent,
            BigDecimal salesmanFixedAmount,
            BigDecimal salesmanPercent,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            boolean active,
            Instant createdAt) {
    }

    public record DashboardSummary(
            long promoters,
            long salesmen,
            long shops,
            long shopsPendingApproval,
            long pendingCommissionLines,
            BigDecimal pendingCommissionAmount) {
    }

    public record CommissionEntryResponse(
            Long id,
            Long shopRegistrationId,
            CommissionBeneficiaryType beneficiaryType,
            Long beneficiaryId,
            BigDecimal amount,
            CommissionEntryStatus status,
            LocalDate periodMonth,
            Instant createdAt,
            Instant paidAt,
            String payoutReference) {
    }
}
