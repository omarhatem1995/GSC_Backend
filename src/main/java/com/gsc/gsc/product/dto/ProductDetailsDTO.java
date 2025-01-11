package com.gsc.gsc.product.dto;

import com.gsc.gsc.brand.dto.BrandDTO;
import com.gsc.gsc.brand.dto.ProductBrandDTO;
import com.gsc.gsc.model.OeNumber;
import com.gsc.gsc.model.ProductBrand;
import com.gsc.gsc.model.SellerBrand;
import com.gsc.gsc.model.view.ProductDetailsView;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailsDTO {
    private Integer productId;
    private String imageUrl;
    private String partNumber;
    private BigDecimal price;
    private Integer quantity;
    private Double minimum;
    private Double maximum;
    private Double cost;
    private List<OeNumber> compatibleOeNumbersList;
    private List<CompatibleVehiclesDTO> compatibleVehiclesDTOList;
    private List<ProductBrandDTO> productBrandList;
    private List<SellerBrand> sellerBrandsList;
    public ProductDetailsDTO(ProductDetailsView productDetailsView) {
        this.productId = productDetailsView.getProductId();
        this.partNumber = productDetailsView.getCode();
        this.price = productDetailsView.getPrice();
        this.cost = productDetailsView.getCost();
//        this.quantity = productDetailsView.getQuantity();
//        this.minimum = productDetailsView.getMinimum();
//        this.maximum = productDetailsView.getMaximum();
//        this.cost = productDetailsView.getCost();
    }
}
