package com.gsc.gsc.inventory.mapper;

import com.gsc.gsc.inventory.dto.ProductWithSellerBrandDTO;
import com.gsc.gsc.product.dto.GetProductsDTO;
import com.gsc.gsc.configurations.dto.seller_brand.SellerBrandsDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

        public static GetProductsDTO toGetProductsDTO(ProductWithSellerBrandDTO newDto) {
            GetProductsDTO oldDto = new GetProductsDTO();

            oldDto.setId(newDto.getProductId());
            oldDto.setCode(newDto.getProductCode());
            oldDto.setImageUrl(newDto.getSellerBrandImage()); // assuming seller brand image acts as product image
            oldDto.setPrice(newDto.getPrice());

            oldDto.setProductName(newDto.getProductNameEn()); // or combine En/Ar if you need
            oldDto.setProductDescription(newDto.getProductNameAr()); // or use description field if exists

            // map seller brand info
            List<Integer> brandIds = new ArrayList<>();
            List<SellerBrandsDTO> brands = new ArrayList<>();

            if (newDto.getSellerBrandCode() != null) {
                // example: brand id not directly in newDto, but you may resolve it elsewhere
                // here assuming code can be mapped to an integer id
                Integer fakeBrandId = newDto.getSellerBrandCode().hashCode();

                brandIds.add(fakeBrandId);
                brands.add(new SellerBrandsDTO(
                        fakeBrandId,
                        newDto.getSellerBrandNameEn(),
                        newDto.getSellerBrandNameAr()
                ));
            }

            oldDto.setBrandIds(brandIds);
            oldDto.setBrands(brands);

            return oldDto;
        }

        public static List<GetProductsDTO> toGetProductsDTOList(List<ProductWithSellerBrandDTO> newDtos) {
            return newDtos.stream()
                    .map(ProductMapper::toGetProductsDTO)
                    .collect(Collectors.toList());
        }
    }