package com.shopmanagement.fieldforceservice.service.lead;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCandidate;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCheckRequest;
import com.shopmanagement.fieldforceservice.api.FieldforceLeadApi.DuplicateCheckResult;
import com.shopmanagement.fieldforceservice.model.BusinessLead;
import com.shopmanagement.fieldforceservice.model.LeadStatus;
import com.shopmanagement.fieldforceservice.repository.BusinessLeadRepository;
import com.shopmanagement.fieldforceservice.support.GeoDistance;
import com.shopmanagement.fieldforceservice.support.MobileNormalizer;
import com.shopmanagement.fieldforceservice.support.TenantIds;

@Service
public class DuplicateLeadDetectionService {

    private final BusinessLeadRepository leadRepository;
    private final double gpsThresholdMeters;
    private final double nameSimilarityThreshold;

    public DuplicateLeadDetectionService(
            BusinessLeadRepository leadRepository,
            @Value("${fieldforce.duplicate.gps-threshold-meters:100}") double gpsThresholdMeters,
            @Value("${fieldforce.duplicate.name-similarity:0.85}") double nameSimilarityThreshold) {
        this.leadRepository = leadRepository;
        this.gpsThresholdMeters = gpsThresholdMeters;
        this.nameSimilarityThreshold = nameSimilarityThreshold;
    }

    @Transactional(readOnly = true)
    public DuplicateCheckResult check(DuplicateCheckRequest req) {
        long tenantId = TenantIds.require();
        List<DuplicateCandidate> candidates = new ArrayList<>();
        Set<Long> seen = new LinkedHashSet<>();

        String normalized = MobileNormalizer.normalize(req.mobile());
        if (!normalized.isBlank()) {
            for (BusinessLead lead : leadRepository.findByTenantIdAndMobileNormalizedAndDeletedAtIsNull(tenantId, normalized)) {
                if (seen.add(lead.getId())) {
                    candidates.add(candidate(lead, "MOBILE_MATCH", null));
                }
            }
        }

        if (req.gstin() != null && !req.gstin().isBlank()) {
            for (BusinessLead lead :
                    leadRepository.findByTenantIdAndGstinIgnoreCaseAndDeletedAtIsNull(tenantId, req.gstin().trim())) {
                if (seen.add(lead.getId())) {
                    candidates.add(candidate(lead, "GSTIN_MATCH", null));
                }
            }
        }

        if (req.businessName() != null && !req.businessName().isBlank()) {
            String needle = req.businessName().trim().toLowerCase();
            String nameFragment = needle.substring(0, Math.min(needle.length(), 20));
            leadRepository
                    .search(
                            tenantId,
                            null,
                            null,
                            null,
                            null,
                            null,
                            BusinessLeadService.likePattern(nameFragment),
                            BusinessLeadService.rawLikePattern(nameFragment),
                            org.springframework.data.domain.Pageable.ofSize(20))
                    .forEach(lead -> {
                        if (seen.add(lead.getId()) && nameSimilar(needle, lead.getBusinessName())) {
                            Double dist = gpsDistance(req, lead);
                            if (dist == null || dist <= gpsThresholdMeters) {
                                candidates.add(candidate(lead, "NAME_SIMILARITY", dist));
                            }
                        }
                    });
        }

        if (req.gpsLatitude() != null && req.gpsLongitude() != null) {
            leadRepository
                    .findByTenantIdAndDeletedAtIsNull(tenantId, org.springframework.data.domain.Pageable.ofSize(200))
                    .forEach(lead -> {
                        if (lead.getGpsLatitude() == null || lead.getGpsLongitude() == null) {
                            return;
                        }
                        double dist = GeoDistance.meters(
                                req.gpsLatitude(), req.gpsLongitude(), lead.getGpsLatitude(), lead.getGpsLongitude());
                        if (dist <= gpsThresholdMeters && seen.add(lead.getId())) {
                            candidates.add(candidate(lead, "GPS_PROXIMITY", dist));
                        }
                    });
        }

        return new DuplicateCheckResult(!candidates.isEmpty(), candidates);
    }

    private boolean nameSimilar(String a, String b) {
        if (b == null) {
            return false;
        }
        String x = a.toLowerCase();
        String y = b.toLowerCase();
        if (x.equals(y) || x.contains(y) || y.contains(x)) {
            return true;
        }
        int maxLen = Math.max(x.length(), y.length());
        if (maxLen == 0) {
            return false;
        }
        return (double) longestCommonSubstring(x, y) / maxLen >= nameSimilarityThreshold;
    }

    private static int longestCommonSubstring(String a, String b) {
        int best = 0;
        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                int k = 0;
                while (i + k < a.length() && j + k < b.length() && a.charAt(i + k) == b.charAt(j + k)) {
                    k++;
                }
                best = Math.max(best, k);
            }
        }
        return best;
    }

    private static Double gpsDistance(DuplicateCheckRequest req, BusinessLead lead) {
        if (req.gpsLatitude() == null || req.gpsLongitude() == null) {
            return null;
        }
        return GeoDistance.meters(req.gpsLatitude(), req.gpsLongitude(), lead.getGpsLatitude(), lead.getGpsLongitude());
    }

    private static DuplicateCandidate candidate(BusinessLead lead, String reason, Double distanceMeters) {
        return new DuplicateCandidate(
                lead.getId(),
                lead.getLeadCode(),
                lead.getBusinessName(),
                lead.getMobile(),
                lead.getLeadStatus(),
                reason,
                distanceMeters);
    }
}
