package com.dem.Inventory.permission;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface FeatureRepository extends JpaRepository<Feature, Long> {
    Optional<Feature> findByFeatureKeyIgnoreCase(String featureKey);
    List<Feature> findAllByCategoryOrderByGroupNameAscLabelAsc(FeatureCategory category);
    List<Feature> findAllByOrderByCategoryAscGroupNameAscLabelAsc();
}
