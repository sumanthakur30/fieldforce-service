package com.shopmanagement.fieldforceservice.service.activity;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ActivityCreate;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.ActivityResponse;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.FieldActivity;
import com.shopmanagement.fieldforceservice.model.FieldActivityType;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.repository.FieldActivityRepository;
import com.shopmanagement.fieldforceservice.repository.PromoterRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.service.CommissionAccrualService;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class FieldActivityService {

    private final FieldActivityRepository activityRepository;
    private final BusinessLeadRepository leadRepository;
    private final SalesmanRepository salesmanRepository;
    private final PromoterRepository promoterRepository;
    private final CommissionAccrualService commissionAccrualService;

    public FieldActivityService(
            FieldActivityRepository activityRepository,
            BusinessLeadRepository leadRepository,
            SalesmanRepository salesmanRepository,
            PromoterRepository promoterRepository,
            CommissionAccrualService commissionAccrualService) {
        this.activityRepository = activityRepository;
        this.leadRepository = leadRepository;
        this.salesmanRepository = salesmanRepository;
        this.promoterRepository = promoterRepository;
        this.commissionAccrualService = commissionAccrualService;
    }

    @Transactional
    public ActivityResponse log(Long leadId, ActivityCreate body) {
        long tenantId = TenantIds.require();
        BusinessLead lead = leadRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, leadId)
                .orElseThrow(() -> new NotFoundException("Lead not found"));

        FieldActivity activity = new FieldActivity();
        activity.setTenantId(tenantId);
        activity.setBusinessLead(lead);
        activity.setActivityType(body.activityType());
        activity.setActivityAt(body.activityAt() != null ? body.activityAt() : Instant.now());
        activity.setGpsLatitude(body.gpsLatitude());
        activity.setGpsLongitude(body.gpsLongitude());
        activity.setNotes(body.notes());
        activity.setNextFollowupDate(body.nextFollowupDate());
        activity.setPhotoUrl(body.photoUrl());

        if (body.salesmanId() != null) {
            Salesman sm = salesmanRepository
                    .findByTenantIdAndId(tenantId, body.salesmanId())
                    .orElseThrow(() -> new NotFoundException("Salesman not found"));
            activity.setSalesman(sm);
        } else if (lead.getAssignedSalesman() != null) {
            activity.setSalesman(lead.getAssignedSalesman());
        }
        if (body.promoterId() != null) {
            Promoter p = promoterRepository
                    .findByTenantIdAndId(tenantId, body.promoterId())
                    .orElseThrow(() -> new NotFoundException("Promoter not found"));
            activity.setPromoter(p);
        } else if (lead.getCreatedByPromoter() != null) {
            activity.setPromoter(lead.getCreatedByPromoter());
        }

        activity = activityRepository.save(activity);
        advanceLeadStatusFromActivity(lead, body.activityType());
        if (body.activityType() == FieldActivityType.DEMO) {
            commissionAccrualService.accrueForDemoCompleted(lead);
        }
        lead.touch();
        leadRepository.save(lead);
        return toResponse(activity);
    }

    private void advanceLeadStatusFromActivity(BusinessLead lead, FieldActivityType type) {
        if (lead.getLeadStatus() == LeadStatus.CONVERTED || lead.getLeadStatus() == LeadStatus.REJECTED) {
            return;
        }
        switch (type) {
            case VISIT -> {
                if (lead.getLeadStatus() == LeadStatus.NEW) {
                    lead.setLeadStatus(LeadStatus.CONTACTED);
                }
            }
            case CALL -> {
                if (lead.getLeadStatus() == LeadStatus.NEW) {
                    lead.setLeadStatus(LeadStatus.CONTACTED);
                }
            }
            case DEMO -> lead.setLeadStatus(LeadStatus.DEMO_GIVEN);
            case FOLLOWUP -> lead.setLeadStatus(LeadStatus.FOLLOWUP);
            default -> {
            }
        }
    }

    @Transactional(readOnly = true)
    public java.util.List<ActivityResponse> listForLead(Long leadId) {
        long tenantId = TenantIds.require();
        return activityRepository.findByTenantIdAndBusinessLeadIdAndDeletedAtIsNullOrderByActivityAtDesc(tenantId, leadId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> filter(
            Long leadId, Long salesmanId, FieldActivityType type, Instant from, Instant to, Pageable pageable) {
        return activityRepository
                .filter(TenantIds.require(), leadId, salesmanId, type, from, to, pageable)
                .map(this::toResponse);
    }

    private ActivityResponse toResponse(FieldActivity a) {
        return new ActivityResponse(
                a.getId(),
                a.getBusinessLead().getId(),
                a.getActivityType(),
                a.getActivityAt(),
                a.getGpsLatitude(),
                a.getGpsLongitude(),
                a.getNotes(),
                a.getNextFollowupDate(),
                a.getPhotoUrl(),
                a.getSalesman() == null ? null : a.getSalesman().getId(),
                a.getPromoter() == null ? null : a.getPromoter().getId(),
                a.getCreatedAt());
    }
}
