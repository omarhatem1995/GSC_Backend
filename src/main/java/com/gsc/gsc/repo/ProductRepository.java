package com.gsc.gsc.repo;

import com.gsc.gsc.inventory.dto.ProductWithSellerBrandDTO;
import com.gsc.gsc.model.Product;
import com.gsc.gsc.product.dto.GetProductsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    Optional<Product> findByCode(String code);

  /*  @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(p.id, p.code, pi.url, pm.brandId, pd.name, pd.description) " +
            "FROM Product p " +
            "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1 " +
            "LEFT JOIN ProductModel pm ON pm.productId = p.id " +
            "LEFT JOIN ProductModel pm ON pm.productId = p.id " +
            "LEFT JOIN ProductDetails pd ON pd.productId = p.id AND pd.langId = :langId " +
            "WHERE p.id <> 999999")

    List<GetProductsDTO> findProducts(@Param("langId") Integer langId, Pageable pageable);*/

    @Query("SELECT DISTINCT p " +
            "FROM Product p " +
            "JOIN ProductManufacturer pm ON p.id = pm.productId " +
            "LEFT JOIN ProductVehicle pv ON p.id = pv.productId " +
            "LEFT JOIN Model m ON pv.modelId = m.id " +
            "LEFT JOIN Brand b ON m.brandId = b.id " +
            "LEFT JOIN ProductOeNumber po ON p.id = po.productId " +
            "WHERE (:oeNumber IS NULL " +
            "       OR po.oeNumber LIKE CONCAT('%', :oeNumber, '%') " +
            "       OR p.code LIKE CONCAT('%', :oeNumber, '%') " +
            "       OR p.nameEn LIKE CONCAT('%', :oeNumber, '%') " +
            "       OR p.nameAr LIKE CONCAT('%', :oeNumber, '%')) " +
            "  AND (:brandId IS NULL OR b.id = :brandId) " +
            "  AND (:modelCode IS NULL OR m.code = :modelCode) " +
            "  AND (:yearFrom IS NULL OR :yearTo IS NULL " +
            "       OR (pv.yearFrom <= :yearTo AND pv.yearTo >= :yearFrom))")
    Page<Product> findProductsV3(
            @Param("brandId") Integer brandId,
            @Param("modelCode") String modelCode,
            @Param("yearFrom") Integer yearFrom,
            @Param("yearTo") Integer yearTo,
            @Param("oeNumber") String oeNumber,
            Pageable pageable
    );



    long count();
    @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(" +
            "p.id, p.code, pi.url, p.price, " +
            "CASE WHEN :langId = 1 THEN p.nameEn ELSE p.nameAr END," +
            "CASE WHEN :langId = 1 THEN p.descriptionEn ELSE p.descriptionAr END) " +
            "FROM Product p " +
            "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1")
    Page<GetProductsDTO> findProducts2(@Param("langId") Integer langId, Pageable pageable);
    @Query("SELECT new com.gsc.gsc.inventory.dto.ProductWithSellerBrandDTO(" +
            "p.id, p.code, p.nameEn, p.nameAr, p.descriptionEn , p.descriptionAr , " +
            "pb.partNo, pb.price, pb.cost, pb.quantity, " +
            "sb.id,sb.code, sb.nameEn, sb.nameAr, p.imageUrl) " +
            "FROM Product p " +
            "JOIN ProductModelSellerBrand pb ON p.id = pb.productId " +
            "JOIN SellerBrand sb ON pb.sellerBrandId = sb.id")
    Page<ProductWithSellerBrandDTO> findProductsWithSellerBrand(Pageable pageable);
    @Query("SELECT new com.gsc.gsc.inventory.dto.ProductWithSellerBrandDTO(" +
            "p.id, p.code, p.nameEn, p.nameAr, p.descriptionEn , p.descriptionAr, " +
            "pb.partNo, pb.price, pb.cost, pb.quantity, " +
            "sb.id, sb.code, sb.nameEn, sb.nameAr, sb.imageUrl) " +
            "FROM Product p " +
            "JOIN ProductModelSellerBrand pb ON p.id = pb.productId " +
            "JOIN SellerBrand sb ON pb.sellerBrandId = sb.id " +
            "WHERE sb.id = :sellerBrandId")
    Page<ProductWithSellerBrandDTO> findProductsBySellerBrandId(@Param("sellerBrandId") Integer sellerBrandId, Pageable pageable);

/*  @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(" +
          "p.id, p.code, pi.url,p.price, pb.brandId, " +
          "CASE WHEN :langId = 1 THEN p.nameEn ELSE p.nameAr END, " +
          "CASE WHEN :langId = 1 THEN  p.descriptionEn ELSE p.descriptionAr END) " +
          "FROM Product p " +
          "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1 " +
          "LEFT JOIN ProductModel pm ON pm.productId = p.id " +
          "LEFT JOIN Model m ON pm.modelId = m.id " +
          "LEFT JOIN ProductBrand pb ON pb.productId = p.id " +
          "WHERE p.id IN (" +
          "   SELECT DISTINCT p1.id " +
          "   FROM Product p1 " +
          "   WHERE p1.id <> 999999" +
          ") " +
          "AND (:brandId IS NULL OR pb.brandId = :brandId OR :brandId = 0) " +
          "AND (:creationYear IS NULL OR m.creationYear = :creationYear OR :creationYear = 0) " +
          "AND (:modelId IS NULL OR pm.modelId = :modelId OR :modelId = 0)")
  Page<GetProductsDTO> findProducts3(
          @Param("langId") Integer langId,
          @Param("brandId") Integer brandId,
          @Param("modelId") Integer modelId,
          @Param("creationYear") Integer creationYear,
          Pageable pageable
  );*/


  List<Product> findAllByProductTypeId(Integer productTypeId);




}