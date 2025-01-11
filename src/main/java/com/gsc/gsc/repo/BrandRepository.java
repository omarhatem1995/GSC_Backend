package com.gsc.gsc.repo;

import com.gsc.gsc.bill.dto.GetBillsDTO;
import com.gsc.gsc.brand.dto.BrandDTO;
import com.gsc.gsc.model.Brand;
import com.gsc.gsc.model.BrandText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {

    @Query("SELECT NEW com.gsc.gsc.brand.dto.BrandDTO(b.id, b.code, b.imageUrl," +
            " bs.name,bs.description) " +
            "FROM Brand b " +
            "JOIN BrandText bs on b.id = bs.brandId " +
            "WHERE bs.langId = :langId")
    Optional<List<BrandDTO>> findBrandsByLangId(Integer langId);
    @Query("SELECT NEW com.gsc.gsc.brand.dto.BrandDTO(b.id, b.code, b.imageUrl," +
            " bs.name,bs.description) " +
            "FROM Brand b " +
            "JOIN BrandText bs on b.id = bs.brandId " +
            "WHERE bs.langId = :langId AND b.id =:brandId")
    Optional<BrandDTO> findBrandsByLangIdAndBrandId(Integer langId,Integer brandId);

    Optional<Brand> findById(Integer id);


}