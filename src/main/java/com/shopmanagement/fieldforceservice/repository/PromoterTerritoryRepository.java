package com.shopmanagement.fieldforceservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.shopmanagement.fieldforceservice.model.Promoter;
import com.shopmanagement.fieldforceservice.model.PromoterTerritory;

public interface PromoterTerritoryRepository extends JpaRepository<PromoterTerritory, Long> {

    List<PromoterTerritory> findByPromoter(Promoter promoter);

    long countByPromoter(Promoter promoter);

    void deleteByPromoter(Promoter promoter);
}
