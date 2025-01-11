package com.gsc.gsc.repo;

import com.gsc.gsc.model.ProductBrand;
import com.gsc.gsc.product.dto.CompatibleVehiclesDTO;
import com.gsc.gsc.user.dto.PointsDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductBrandRepository extends JpaRepository<ProductBrand, Integer> {

/*    @Query("SELECT DISTINCT NEW com.gsc.gsc.product.dto.CompatibleVehiclesDTO(pb.productId, bt.name, m.code, m.creationYear)" +
            " FROM ProductBrand pb" +
            " LEFT JOIN ProductModel pm ON pm.productId = pb.productId" +
            " LEFT JOIN Model m ON m.id = pm.modelId" +
            " LEFT JOIN BrandText bt ON bt.brandId = m.brandId" +
            " WHERE pb.productId = :productId AND bt.langId = 1")
    List<CompatibleVehiclesDTO> findBrandsByProductId(Integer productId);*/

    List<ProductBrand> findAllByProductId(Integer productId);
    Optional<ProductBrand> findByProductId(Integer productId);
}