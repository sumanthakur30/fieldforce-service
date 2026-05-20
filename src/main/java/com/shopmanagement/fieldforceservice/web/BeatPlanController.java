package com.shopmanagement.fieldforceservice.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatAssignmentCreate;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatAssignmentResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatPlanResponse;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.BeatPlanUpsert;
import com.shopmanagement.fieldforceservice.service.beat.BeatPlanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/beats")
public class BeatPlanController {

    private final BeatPlanService beatPlanService;

    public BeatPlanController(BeatPlanService beatPlanService) {
        this.beatPlanService = beatPlanService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeatPlanResponse create(@Valid @RequestBody BeatPlanUpsert body) {
        return beatPlanService.create(body);
    }

    @PutMapping("/{id}")
    public BeatPlanResponse update(@PathVariable Long id, @Valid @RequestBody BeatPlanUpsert body) {
        return beatPlanService.update(id, body);
    }

    @GetMapping("/{id}")
    public BeatPlanResponse get(@PathVariable Long id) {
        return beatPlanService.get(id);
    }

    @GetMapping
    public Page<BeatPlanResponse> list(
            @RequestParam(required = false) Boolean active, @RequestParam(required = false) String q, Pageable pageable) {
        return beatPlanService.search(active, q, pageable);
    }

    @PostMapping("/{beatId}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public BeatAssignmentResponse assign(@PathVariable Long beatId, @Valid @RequestBody BeatAssignmentCreate body) {
        return beatPlanService.assign(beatId, body);
    }

    @GetMapping("/{beatId}/assignments")
    public List<BeatAssignmentResponse> listAssignments(@PathVariable Long beatId) {
        return beatPlanService.listAssignments(beatId);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unassign(@PathVariable Long assignmentId) {
        beatPlanService.unassign(assignmentId);
    }
}
