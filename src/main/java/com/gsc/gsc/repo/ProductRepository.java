package com.gsc.gsc.repo;

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

    long count();
    @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(" +
            "p.id, p.code, pi.url,p.price,  pd.name, pd.description) " +
            "FROM Product p " +
            "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1 " +
            "LEFT JOIN ProductDetails pd ON pd.productId = p.id AND pd.langId = :langId " +
            "WHERE p.id IN (" +
            "   SELECT DISTINCT p1.id " +
            "   FROM Product p1 " +
            "   WHERE p1.id <> 999999" +
            ")")
    Page<GetProductsDTO> findProducts2(@Param("langId") Integer langId, Pageable pageable);


  @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(" +
          "p.id, p.code, pi.url,p.price, pb.brandId, pd.name, pd.description) " +
          "FROM Product p " +
          "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1 " +
          "LEFT JOIN ProductDetails pd ON pd.productId = p.id AND pd.langId = :langId " +
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
  );
  @Query("SELECT new com.gsc.gsc.product.dto.GetProductsDTO(" +
          "p.id, p.code, pi.url,p.price, pb.brandId, pd.name, pd.description) " +
          "FROM Product p " +
          "LEFT JOIN ProductImages pi ON p.id = pi.productId AND pi.counter = 1 " +
          "LEFT JOIN ProductDetails pd ON pd.productId = p.id AND pd.langId = :langId " +
          "LEFT JOIN ProductModel pm ON pm.productId = p.id " +
          "LEFT JOIN Model m ON pm.modelId = m.id " +
          "LEFT JOIN ProductBrand pb ON pb.productId = p.id " +
          "JOIN ProductSellerBrand psb ON psb.productId = p.id " +
          "WHERE p.id IN (" +
          "   SELECT DISTINCT p1.id " +
          "   FROM Product p1 " +
          "   WHERE p1.id <> 999999" +
          ") " +
          "AND (:sellerBrandsId IS NULL OR psb.sellerBrandsId = :sellerBrandsId OR :sellerBrandsId = 0)")
  Page<GetProductsDTO> findProductsByManufactureId(
          @Param("langId") Integer landId,
          @Param("sellerBrandsId") Integer sellerBrandsId,
          Pageable pageable
  );


  List<Product> findAllByProductTypeId(Integer productTypeId);




}