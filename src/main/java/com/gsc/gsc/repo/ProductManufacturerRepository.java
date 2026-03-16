package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductManufacturer;
import com.gsc.gsc.product.dto.CompatibleVehiclesDTO;
import com.gsc.gsc.product.dto.ProductManufacturerDTO;
import com.gsc.gsc.product.dto.ProductPriceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductManufacturerRepository extends JpaRepository<ProductManufacturer, Integer> {

    List<ProductManufacturer> findAllByProductId( Integer productId);
    @Query("SELECT new com.gsc.gsc.product.dto.ProductManufacturerDTO(pm.id, pm.quantity, pm.price, b.nameEn) " +
            "FROM ProductManufacturer pm " +
            "JOIN SellerBrand b ON pm.sellerBrandId = b.id " +
            "WHERE pm.productId = :productId")
    List<ProductManufacturerDTO> findAllByProductIdWithBrand(@Param("productId") Integer productId);
    @Query("SELECT new com.gsc.gsc.product.dto.ProductPriceDTO(pm.productId, MIN(pm.price)) " +
            "FROM ProductManufacturer pm " +
            "WHERE pm.productId IN :productIds " +
            "GROUP BY pm.productId")
    List<ProductPriceDTO> findMinPriceByProductIds(@Param("productIds") List<Integer> productIds);
    Optional<ProductManufacturer> findBySellerBrandIdAndProductId(Integer sellerBrandId, Integer productId);
    Optional<ProductManufacturer> findFirstByProductIdOrderByQuantityAsc(Integer productId);
}