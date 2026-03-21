package com.gsc.gsc.repo;

import com.gsc.gsc.model.FeaturedBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeaturedBrandRepository extends JpaRepository<FeaturedBrand, Integer> {

    List<FeaturedBrand> findAllByOrderByDisplayOrderAsc();

    void deleteAllBySellerBrandIdIn(List<Integer> sellerBrandIds);
}
