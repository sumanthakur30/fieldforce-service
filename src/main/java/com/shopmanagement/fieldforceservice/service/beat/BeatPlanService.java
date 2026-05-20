package com.shopmanagement.fieldforceservice.service.beat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatAssignmentCreate;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatAssignmentResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatPlanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatPlanUpsert;
import com.shopmanagement.fieldforceservice.exception.NotFoundException;
import com.shopmanagement.fieldforceservice.model.BeatPlan;
import com.shopmanagement.fieldforceservice.model.Salesman;
import com.shopmanagement.fieldforceservice.model.SalesmanBeatAssignment;
import com.shopmanagement.fieldforceservice.repository.BeatPlanRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanBeatAssignmentRepository;
import com.shopmanagement.fieldforceservice.repository.SalesmanRepository;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class BeatPlanService {

    private final BeatPlanRepository beatPlanRepository;
    private final SalesmanBeatAssignmentRepository assignmentRepository;
    private final SalesmanRepository salesmanRepository;

    public BeatPlanService(
            BeatPlanRepository beatPlanRepository,
            SalesmanBeatAssignmentRepository assignmentRepository,
            SalesmanRepository salesmanRepository) {
        this.beatPlanRepository = beatPlanRepository;
        this.assignmentRepository = assignmentRepository;
        this.salesmanRepository = salesmanRepository;
    }

    @Transactional
    public BeatPlanResponse create(BeatPlanUpsert r) {
        long tenantId = TenantIds.require();
        BeatPlan beat = new BeatPlan();
        beat.setTenantId(tenantId);
        beat.setBeatCode("BT-" + UUID.randomUUID().toString().substring(0, 8));
        apply(beat, r);
        beat = beatPlanRepository.save(beat);
        beat.setBeatCode("BT-" + beat.getId());
        beat.touch();
        return toResponse(beatPlanRepository.save(beat));
    }

    @Transactional
    public BeatPlanResponse update(Long id, BeatPlanUpsert r) {
        BeatPlan beat = load(TenantIds.require(), id);
        apply(beat, r);
        beat.touch();
        return toResponse(beatPlanRepository.save(beat));
    }

    @Transactional(readOnly = true)
    public BeatPlanResponse get(Long id) {
        return toResponse(load(TenantIds.require(), id));
    }

    @Transactional(readOnly = true)
    public Page<BeatPlanResponse> search(Boolean active, String q, Pageable pageable) {
        return beatPlanRepository.search(TenantIds.require(), active, blank(q), pageable).map(this::toResponse);
    }

    @Transactional
    public BeatAssignmentResponse assign(Long beatId, BeatAssignmentCreate body) {
        long tenantId = TenantIds.require();
        BeatPlan beat = load(tenantId, beatId);
        Salesman salesman = salesmanRepository
                .findByTenantIdAndId(tenantId, body.salesmanId())
                .orElseThrow(() -> new NotFoundException("Salesman not found"));

        SalesmanBeatAssignment a = new SalesmanBeatAssignment();
        a.setTenantId(tenantId);
        a.setBeatPlan(beat);
        a.setSalesman(salesman);
        a.setDayOfWeek(body.dayOfWeek());
        a.setEffectiveFrom(body.effectiveFrom());
        a.setEffectiveTo(body.effectiveTo());
        a = assignmentRepository.save(a);
        return toAssignment(a);
    }

    @Transactional
    public void unassign(Long assignmentId) {
        SalesmanBeatAssignment a = assignmentRepository
                .findById(assignmentId)
                .filter(x -> x.getTenantId().equals(TenantIds.require()))
                .orElseThrow(() -> new NotFoundException("Beat assignment not found"));
        assignmentRepository.delete(a);
    }

    @Transactional(readOnly = true)
    public List<BeatAssignmentResponse> listAssignments(Long beatId) {
        return assignmentRepository.findByTenantIdAndBeatPlanId(TenantIds.require(), beatId).stream()
                .map(this::toAssignment)
                .toList();
    }

    private BeatPlan load(long tenantId, Long id) {
        return beatPlanRepository
                .findByTenantIdAndIdAndDeletedAtIsNull(tenantId, id)
                .orElseThrow(() -> new NotFoundException("Beat plan not found"));
    }

    private void apply(BeatPlan beat, BeatPlanUpsert r) {
        beat.setBeatName(r.beatName().trim());
        beat.setStateCode(blank(r.stateCode()));
        beat.setCityCode(blank(r.cityCode()));
        beat.setAreaDescription(blank(r.areaDescription()));
        if (r.active() != null) {
            beat.setActive(r.active());
        }
    }

    private BeatPlanResponse toResponse(BeatPlan b) {
        return new BeatPlanResponse(
                b.getId(),
                b.getBeatCode(),
                b.getBeatName(),
                b.getStateCode(),
                b.getCityCode(),
                b.getAreaDescription(),
                b.isActive(),
                b.getCreatedAt(),
                b.getUpdatedAt());
    }

    private BeatAssignmentResponse toAssignment(SalesmanBeatAssignment a) {
        return new BeatAssignmentResponse(
                a.getId(),
                a.getBeatPlan().getId(),
                a.getSalesman().getId(),
                a.getDayOfWeek(),
                a.getEffectiveFrom(),
                a.getEffectiveTo());
    }

    private static String blank(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
