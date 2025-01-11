package com.gsc.gsc.repo;

import com.gsc.gsc.model.view.ProductDetailsView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDetailsViewRepository extends PagingAndSortingRepository<ProductDetailsView, Integer> {
    Page<ProductDetailsView> findAllByLangId(Integer langId, Pageable pageable);
    List<ProductDetailsView> findByProductIdAndLangId(Integer productId , Integer langId);

}