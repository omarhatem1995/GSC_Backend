package com.gsc.gsc.inventory.service;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.inventory.dto.CreateProductDTO;
import com.gsc.gsc.inventory.dto.GetProductDTO;
import com.gsc.gsc.inventory.dto.ProductWithSellerBrandDTO;
import com.gsc.gsc.inventory.model.POENumber;
import com.gsc.gsc.inventory.model.ProductModelSellerBrand;
import com.gsc.gsc.model.*;
import com.gsc.gsc.product.dto.GetProductsDTO;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.seller_brand.SellerBrandsDTO;
import com.gsc.gsc.utilities.ImgBBService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.utilities.Constants.GENERAL_BRAND_ID;

@Service
public class ProductServiceV2 {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;
    @Autowired
    private ProductBrandSellerBrandRepository productBrandSellerBrandRepository;
    @Autowired
    private StoreProductBrandSellerBrandRepository storeProductBrandSellerBrandRepository;
    @Autowired
    private POENumberRepository pOENumberRepository;
    @Autowired
    private ImgBBService imgBBService;
    @Autowired
    private ProductImagesRepository productImagesRepository;

    public ResponseEntity create(String token , CreateProductDTO dto) throws IOException {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {

            /*
             * Validate Product Data
             */
            ResponseEntity<?> validationResponse = validateProductForCreate(returnObject, dto);
            if (validationResponse != null) {

                return validationResponse;
            }
            String imageUrl = "";
            if (dto.getImage() != null && !dto.getImage().isEmpty()) {

                imageUrl = imgBBService.uploadImage(dto.getImage());

                ProductImages productImage = new ProductImages();
                productImage.setUrl(imageUrl);

            }
            Product product = new Product(dto,imageUrl);
            product = productRepository.save(product);
            dto.setProductId(product.getId());
            /*
             * Create Product In Inventory
             */
            ProductModelSellerBrand productModelSellerBrand = createProductInInventory(userId,dto);

            /*
            * Adding Store is Optional
            * */
            Optional<Store> storeOptional = storeRepository.findById(dto.getStoreId());
//            if(storeOptional.isPresent()){
//                /*
//                * Update productBrandSellerBrand with the storeProductBrandSellerBrand "ID"
//                * */
//
//                StoreProductBrandSellerBrand storeProductBrandSellerBrand = new StoreProductBrandSellerBrand();
//                storeProductBrandSellerBrand.setStoreId(storeOptional.get().getId());
//                storeProductBrandSellerBrand.setProductBrandSellerBrandId(productModelSellerBrand.getId());
//                storeProductBrandSellerBrand.setCreatedById(userId);
//                storeProductBrandSellerBrandRepository.save(storeProductBrandSellerBrand);
//            }

            /*
            * Map Data to GetProductDTO
            * */
            GetProductDTO createdProductDTO = mapFromProductBrandSellerToGetProduct(productModelSellerBrand);
            /*if (dto.getImage() != null && !dto.getImage().isEmpty()) {

                String imageUrl = imgBBService.uploadImage(dto.getImage());

                ProductImages productImage = new ProductImages();
                productImage.setProductId(Math.toIntExact(createdProductDTO.getId()));
                productImage.setUrl(imageUrl);
                productImage.setCounter(1);

                productImagesRepository.save(productImage);
            }*/
            createdProductDTO.setBrandImageUrl(null);
            createdProductDTO.setSellerBrandImageUrl(null);
            returnObject.setData(createdProductDTO);
            returnObject.setStatus(true);
            returnObject.setMessage("Created Successfully");

            return ResponseEntity.status(HttpStatus.OK).body(returnObject);
        }else{
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }



    }
    public ResponseEntity update(String token , CreateProductDTO dto) throws IOException {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {

            /*
             * Validate Product Data
             */
            ResponseEntity<?> validationResponse = validateProductForUpdate(returnObject, dto);
            if (validationResponse != null) {

                return validationResponse;
            }
            String imageUrl = "";
            if (dto.getImage() != null && !dto.getImage().isEmpty()) {

                imageUrl = imgBBService.uploadImage(dto.getImage());

                ProductImages productImage = new ProductImages();
                productImage.setUrl(imageUrl);

            }
            Product product = new Product(dto,imageUrl);
            product = productRepository.save(product);
            dto.setProductId(product.getId());
            /*
             * Create Product In Inventory
             */
            ProductModelSellerBrand productModelSellerBrand = createProductInInventory(userId,dto);

            /*
            * Adding Store is Optional
            * */
            Optional<Store> storeOptional = storeRepository.findById(dto.getStoreId());
//            if(storeOptional.isPresent()){
//                /*
//                * Update productBrandSellerBrand with the storeProductBrandSellerBrand "ID"
//                * */
//
//                StoreProductBrandSellerBrand storeProductBrandSellerBrand = new StoreProductBrandSellerBrand();
//                storeProductBrandSellerBrand.setStoreId(storeOptional.get().getId());
//                storeProductBrandSellerBrand.setProductBrandSellerBrandId(productModelSellerBrand.getId());
//                storeProductBrandSellerBrand.setCreatedById(userId);
//                storeProductBrandSellerBrandRepository.save(storeProductBrandSellerBrand);
//            }

            /*
            * Map Data to GetProductDTO
            * */
            GetProductDTO createdProductDTO = mapFromProductBrandSellerToGetProduct(productModelSellerBrand);
            /*if (dto.getImage() != null && !dto.getImage().isEmpty()) {

                String imageUrl = imgBBService.uploadImage(dto.getImage());

                ProductImages productImage = new ProductImages();
                productImage.setProductId(Math.toIntExact(createdProductDTO.getId()));
                productImage.setUrl(imageUrl);
                productImage.setCounter(1);

                productImagesRepository.save(productImage);
            }*/
            createdProductDTO.setBrandImageUrl(null);
            createdProductDTO.setSellerBrandImageUrl(null);
            returnObject.setData(createdProductDTO);
            returnObject.setStatus(true);
            returnObject.setMessage("Created Successfully");

            return ResponseEntity.status(HttpStatus.OK).body(returnObject);
        }else{
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }



    }

    private GetProductDTO mapFromProductBrandSellerToGetProduct(ProductModelSellerBrand productModelSellerBrand) {
        GetProductDTO productDTO = new GetProductDTO();
        productDTO.setId(productModelSellerBrand.getId());
        productDTO.setProductId(productModelSellerBrand.getProductId());
        productDTO.setModelId(productModelSellerBrand.getModelId());
        productDTO.setSellerBrandId(productModelSellerBrand.getSellerBrandId());
        productDTO.setCreatedById(Math.toIntExact(productModelSellerBrand.getCreatedById()));
        productDTO.setPartNo(productModelSellerBrand.getPartNo());
        productDTO.setPrice(productModelSellerBrand.getPrice());
        productDTO.setCost(productModelSellerBrand.getCost());
        productDTO.setQuantity(productModelSellerBrand.getQuantity());

        List<POENumber> poeNumberList = pOENumberRepository.findAllByProductBrandSellerBrandId(productModelSellerBrand.getId());
        List<String> oeNumberList = new ArrayList<>();
        if(!poeNumberList.isEmpty()){
            for(POENumber poeNumber : poeNumberList) {
                oeNumberList.add(poeNumber.getCode());
            }
        }
        productDTO.setOeNumberList(oeNumberList);

        Optional<User> userOptional = userRepository.findById(Math.toIntExact(productModelSellerBrand.getCreatedById()));
        userOptional.ifPresent(user -> productDTO.setCreatedByName(user.getName()));

        Optional<Product> productOptional = productRepository.findById(productModelSellerBrand.getProductId());
        if(productOptional.isPresent()){
            productDTO.setProductNameEn(productOptional.get().getNameEn());
            productDTO.setProductNameAr(productOptional.get().getNameAr());
            productDTO.setProductDescriptionEn(productOptional.get().getDescriptionEn());
            productDTO.setProductDescriptionAr(productOptional.get().getDescriptionAr());
            productDTO.setProductTypeId(productOptional.get().getProductTypeId());
        }
        Optional<Model> modelOptional = modelRepository.findById(productModelSellerBrand.getModelId());
        if(modelOptional.isPresent()) {
            productDTO.setModelCode(modelOptional.get().getCode());
            productDTO.setModelNameEn(modelOptional.get().getNameEn());
            productDTO.setModelNameAr(modelOptional.get().getNameAr());
            Optional<Brand> brandOptional = brandRepository.findById(modelOptional.get().getBrandId());
            if (brandOptional.isPresent()) {
                productDTO.setBrandId(brandOptional.get().getId());
                productDTO.setBrandNameEn(brandOptional.get().getNameEn());
                productDTO.setProductNameAr(brandOptional.get().getNameAr());
                productDTO.setProductDescriptionEn(brandOptional.get().getDescriptionEn());
                productDTO.setProductDescriptionAr(brandOptional.get().getDescriptionAr());
                productDTO.setBrandImageUrl(brandOptional.get().getImageUrl());
            }
        }
        Optional<SellerBrand> sellerBrandOptional = sellerBrandRepository.findById(productModelSellerBrand.getSellerBrandId());
        if(sellerBrandOptional.isPresent()){
            productDTO.setSellerBrandCode(sellerBrandOptional.get().getCode());
            productDTO.setSellerBrandNameEn(sellerBrandOptional.get().getNameEn());
            productDTO.setSellerBrandNameAr(sellerBrandOptional.get().getNameAr());
            productDTO.setSellerBrandDescriptionEn(sellerBrandOptional.get().getDescriptionEn());
            productDTO.setSellerBrandDescriptionAr(sellerBrandOptional.get().getDescriptionAr());
            productDTO.setSellerBrandImageUrl(sellerBrandOptional.get().getImageUrl());
        }

        return productDTO;
    }

    private ProductModelSellerBrand createProductInInventory(Integer userId, CreateProductDTO dto) {
        ProductModelSellerBrand productModelSellerBrand = new ProductModelSellerBrand();
        productModelSellerBrand.setProductId(dto.getProductId());
        productModelSellerBrand.setSellerBrandId(dto.getSellerBrandId());
        productModelSellerBrand.setModelId(dto.getBrandId());
        productModelSellerBrand.setPartNo(dto.getPartNo());
        productModelSellerBrand.setPrice(dto.getPrice());
        productModelSellerBrand.setCost(dto.getCost());
        productModelSellerBrand.setQuantity(dto.getQuantity());
        productModelSellerBrand.setCreatedById(Long.valueOf(userId));
        productModelSellerBrand = productBrandSellerBrandRepository.save(productModelSellerBrand);
        if(!dto.getOeNumberList().isEmpty()){
            for(String code : dto.getOeNumberList()) {
                POENumber poeNumber = new POENumber();
                poeNumber.setCreatedById(userId);
                poeNumber.setCode(code);
                poeNumber.setProductBrandSellerBrandId(productModelSellerBrand.getId());
                pOENumberRepository.save(poeNumber);
            }
        }
        return productModelSellerBrand;
    }

    private ResponseEntity<?> validateProductForUpdate(ReturnObject returnObject , CreateProductDTO dto) {
        Optional<Product> productOptional = productRepository.findByCode(dto.getPartNo());
        if(productOptional.isEmpty()){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("No Product with the same code found");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getBrandId() != null) {
            Optional<Brand> brandOptional = brandRepository.findById(dto.getBrandId());
            if(brandOptional.isEmpty()){
                dto.setBrandId(GENERAL_BRAND_ID);
            }
        }else{
            dto.setBrandId(GENERAL_BRAND_ID);
        }
        if(dto.getSellerBrandId() == null) {
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Seller Brand Id Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getPartNo() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Part Number Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getPrice() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Price Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getCost() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Cost Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getOeNumberList() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("OE Numbers Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
/*        Optional<ProductModelSellerBrand> productBrandSellerBrandOptional = productBrandSellerBrandRepository.findByProductIdAndModelIdAndSellerBrandId(dto.getProductId(),
                dto.getBrandId(),dto.getSellerBrandId());
        if(productBrandSellerBrandOptional.isPresent()){
                dto.setImage(null);
                returnObject.setData(productBrandSellerBrandOptional.get());
                returnObject.setMessage("Already there is a product defined with the same IDs");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }*/
        return null;
    }
    private ResponseEntity<?> validateProductForCreate(ReturnObject returnObject , CreateProductDTO dto) {
        Optional<Product> productOptional = productRepository.findByCode(dto.getPartNo());
        if(productOptional.isPresent()){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Product with the same code already exists");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getBrandId() != null) {
            Optional<Brand> brandOptional = brandRepository.findById(dto.getBrandId());
            if(brandOptional.isEmpty()){
                dto.setBrandId(GENERAL_BRAND_ID);
            }
        }else{
            dto.setBrandId(GENERAL_BRAND_ID);
        }
        if(dto.getSellerBrandId() == null) {
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Seller Brand Id Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getPartNo() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Part Number Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getPrice() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Price Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getCost() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("Cost Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if(dto.getOeNumberList() == null){
            dto.setImage(null);
            returnObject.setData(dto);
            returnObject.setMessage("OE Numbers Can't be empty");
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
/*        Optional<ProductModelSellerBrand> productBrandSellerBrandOptional = productBrandSellerBrandRepository.findByProductIdAndModelIdAndSellerBrandId(dto.getProductId(),
                dto.getBrandId(),dto.getSellerBrandId());
        if(productBrandSellerBrandOptional.isPresent()){
                dto.setImage(null);
                returnObject.setData(productBrandSellerBrandOptional.get());
                returnObject.setMessage("Already there is a product defined with the same IDs");
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }*/
        return null;
    }


    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token + " ,  " + userIdString);
        return Integer.parseInt(userIdString);
    }

    public ResponseEntity getProducts(String token, int langId, Pageable pageable) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (token == null) {
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        // query new DTOs (each row = product + sellerBrand)
        Page<ProductWithSellerBrandDTO> productPage = productRepository.findProductsWithSellerBrand(pageable);

// map + group into old DTOs
        Map<Integer, GetProductsDTO> groupedProducts = new LinkedHashMap<>();

        for (ProductWithSellerBrandDTO dto : productPage.getContent()) {
            GetProductsDTO product = groupedProducts.get(dto.getProductId());

            if (product == null) {
                // create base DTO
                product = new GetProductsDTO();
                product.setId(dto.getProductId());
                product.setCode(dto.getProductCode());
                product.setImageUrl(dto.getSellerBrandImage()); // use seller brand image for now
                product.setPrice(dto.getPrice());
                product.setProductName(dto.getProductNameEn());
                product.setProductDescription(dto.getProductDescriptionEn());
                product.setBrandIds(new ArrayList<>());
                product.setBrands(new ArrayList<>());
            }

            Integer brandId = dto.getSellerBrandId();
            if (brandId != null && !product.getBrandIds().contains(brandId)) {
                product.getBrandIds().add(brandId);
                product.getBrands().add(new SellerBrandsDTO(
                        brandId,
                        dto.getSellerBrandNameEn(),
                        dto.getSellerBrandNameAr()
                ));
            }

            groupedProducts.put(dto.getProductId(), product);
        }

        List<GetProductsDTO> finalProductsList = new ArrayList<>(groupedProducts.values());

        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(finalProductsList);
        returnObject.setTotalPages(productPage.getTotalPages());
        returnObject.setTotalCount(productPage.getTotalElements());

        return ResponseEntity.ok(returnObject);
    }


    public ResponseEntity<?> getProductsBySellerBrandId(String token, Integer brandId, Pageable pageable) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (token == null) {
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        // query new DTOs (each row = product + sellerBrand)
        Page<ProductWithSellerBrandDTO> productPage = productRepository.findProductsBySellerBrandId(brandId,pageable);

// map + group into old DTOs
        Map<Integer, GetProductsDTO> groupedProducts = new LinkedHashMap<>();

        for (ProductWithSellerBrandDTO dto : productPage.getContent()) {
            GetProductsDTO product = groupedProducts.get(dto.getProductId());

            if (product == null) {
                // create base DTO
                product = new GetProductsDTO();
                product.setId(dto.getProductId());
                product.setCode(dto.getProductCode());
                product.setImageUrl(dto.getSellerBrandImage()); // use seller brand image for now
                product.setPrice(dto.getPrice());
                product.setProductName(dto.getProductNameEn());
                product.setProductDescription(dto.getProductNameAr());
                product.setBrandIds(new ArrayList<>());
                product.setBrands(new ArrayList<>());
            }

            if (brandId != null && !product.getBrandIds().contains(brandId)) {
                product.getBrandIds().add(brandId);
                product.getBrands().add(new SellerBrandsDTO(
                        brandId,
                        dto.getSellerBrandNameEn(),
                        dto.getSellerBrandNameAr()
                ));
            }

            groupedProducts.put(dto.getProductId(), product);
        }

        List<GetProductsDTO> finalProductsList = new ArrayList<>(groupedProducts.values());

        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(finalProductsList);
        returnObject.setTotalPages(productPage.getTotalPages());
        returnObject.setTotalCount(productPage.getTotalElements());

        return ResponseEntity.ok(returnObject);
    }
}
