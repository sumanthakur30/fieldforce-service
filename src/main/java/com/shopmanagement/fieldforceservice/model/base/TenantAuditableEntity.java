package com.shopmanagement.fieldforceservice.model.base;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class TenantAuditableEntity {

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void softDelete() {
        deletedAt = Instant.now();
        updatedAt = Instant.now();
    }

    public void touch() {
        updatedAt = Instant.now();
    }
}
