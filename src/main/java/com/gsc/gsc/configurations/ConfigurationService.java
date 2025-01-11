package com.gsc.gsc.configurations;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.product.dto.GetProductsDTO;
import com.gsc.gsc.repo.BrandRepository;
import com.gsc.gsc.repo.ModelRepository;
import com.gsc.gsc.repo.ProductModelRepository;
import com.gsc.gsc.repo.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigurationService {

    @Autowired
    ModelRepository modelRepository;
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    ProductModelRepository productModelRepository;
    @Autowired
    ProductRepository productRepository;

    public ResponseEntity<?> getProducts(String token , Integer langId,Integer brandId , Integer modelId,Integer creationYear,Pageable pageable) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        if (token != null) {
            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            Page<GetProductsDTO> productsDTOList = productRepository.findProducts3(langId,brandId,modelId,creationYear,pageable);

            Map<Integer, GetProductsDTO> productDTOMap = new HashMap<>();

            for (GetProductsDTO dto : productsDTOList) {
                if (productDTOMap.containsKey(dto.getId())) {
                    productDTOMap.get(dto.getId()).addBrandId(dto.getBrandIds().get(0));
                } else {
                    productDTOMap.put(dto.getId(), dto);
                }
            }

            List<GetProductsDTO> finalProductsList = new ArrayList<>(productDTOMap.values());
            returnObject.setTotalPages(productsDTOList.getTotalPages());
            returnObject.setTotalCount(productsDTOList.getTotalElements());
            returnObject.setData(finalProductsList);
            return  ResponseEntity.ok(returnObject);
        }else{
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity findAllModels(){
        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(modelRepository.findAll());
        return ResponseEntity.ok(returnObject);
    }
    public ResponseEntity findAllBrands(Integer langId){
        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(brandRepository.findBrandsByLangId(langId));
        return ResponseEntity.ok(returnObject);
    }
}
