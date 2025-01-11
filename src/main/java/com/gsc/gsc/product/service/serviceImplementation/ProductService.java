package com.gsc.gsc.product.service.serviceImplementation;

import com.gsc.gsc.brand.dto.BrandDTO;
import com.gsc.gsc.brand.dto.ProductBrandDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.*;
import com.gsc.gsc.model.view.ProductDetailsView;
import com.gsc.gsc.product.dto.*;
import com.gsc.gsc.product.service.serviceInterface.IProductService;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.seller_brand.SellerBrandsDTO;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.*;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.utilities.Constants.GENERAL_BRAND_ID;
import static com.gsc.gsc.utilities.LanguageConstants.ARABIC;
import static com.gsc.gsc.utilities.LanguageConstants.ENGLISH;

@Service
public class ProductService implements IProductService {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductStoreRepository productStoreRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private PromoRepository promoRepository;
    @Autowired
    private ProductDetailsViewRepository productDetailsViewRepository;
    @Autowired
    private OeNumberRepository oeNumberRepository;
    @Autowired
    private ProductBrandRepository productBrandRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private ProductDetailsRepository productDetailsRepository;
    @Autowired
    private BillProductRepository billProductRepository;
    @Autowired
    private ProductSellerBrandRepository productSellerBrandRepository;
    @Autowired
    private ProductModelRepository productModelRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private BrandTextRepository brandTextRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    public ResponseEntity findProductsForStoreId(String token , Integer storeId){
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            Optional<List<ProductStore>> productsStoreList = productStoreRepository.findAllByStoreId(storeId);
            if(productsStoreList.isPresent()){
                returnObject.setMessage("Loaded Successfully");
                returnObject.setStatus(true);
                returnObject.setData(productsStoreList.get());
                return ResponseEntity.ok(returnObject);
            }else{
                returnObject.setMessage("No Products Added");
                returnObject.setData(null);
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);

            }
        }else{
            returnObject.setMessage("User UnAuthorized");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    @Override
    public ResponseEntity create(String token , ProductDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
        if(dto.getSellerBrandsList() == null ||  dto.getSellerBrandsList().isEmpty()){
            returnObject.setMessage("Seller brand list can't be empty");
            returnObject.setData(dto.getCode());
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

            Optional<Product> optionalProduct = productRepository.findByCode(dto.getCode());
            if (optionalProduct.isPresent()) {
                returnObject.setData(optionalProduct.get());
                returnObject.setMessage("This product code is already exists");
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
            Optional<Store> storeOptional = storeRepository.findById(dto.getStoreId());
            if(storeOptional.isPresent()) {
                Product product = new Product();
                ProductStore productStore = new ProductStore();

                product.setCode(dto.getCode());
                product.setPrice(dto.getPrice());
                product.setCost(dto.getCost());

//                updateForeignKeys(product, dto);

                product = productRepository.save(product);

                updateLanguage(product.getId(),dto);

                productStore.setProductId(product.getId());
                productStore.setStoreId(dto.getStoreId());
                productStore.setQuantity(dto.getQuantity());
                productStore.setLocation(dto.getLocation());
                for(String oeNumber : dto.getOeNumber()) {
                    productStore.setOeNumber(oeNumber);

                    // Check if OeNumber already exists for the given brandId, productId, and oeNumber
                    List<OeNumber> existingOeNumbers = oeNumberRepository.findAllByBrandIdAndProductIdAndOeNumber(product.getId(), dto.getBrandId(), oeNumber);

                    // If the list is empty, then the OeNumber doesn't exist and can be added
                    if (existingOeNumbers.isEmpty()) {
                        OeNumber newOeNumber = new OeNumber();
                        newOeNumber.setProductId(product.getId());
                        newOeNumber.setBrandId(dto.getBrandId());
                        newOeNumber.setOeNumber(oeNumber);
                        oeNumberRepository.save(newOeNumber);
                    }
                }
                productStoreRepository.save(productStore);


                ProductBrand productBrand = new ProductBrand();
                productBrand.setProductId(product.getId());
                productBrand.setPrice(dto.getPrice());
                productBrand.setInfo(dto.getInfo());
                productBrand.setQuantity(dto.getQuantity());
                productBrand.setBrandId(dto.getBrandId());
                if(dto.getPartNo() != null) {
                    productBrand.setPartNo(dto.getPartNo());
                }else{
                    productBrand.setPartNo(product.getCode());
                }
                productBrandRepository.save(productBrand);
                try {
                    for (int i = 0; i < dto.getSellerBrandsList().size(); i++) {
                        ProductSellerBrand productSellerBrand = new ProductSellerBrand();
                        productSellerBrand.setProductId(product.getId());
                        productSellerBrand.setSellerBrandsId(dto.getSellerBrandsList().get(i));
                        productSellerBrandRepository.save(productSellerBrand);
                    }
                }catch (Exception exception){
                    System.out.println("exception " + exception.getMessage());
                }

                dto.setId(product.getId());

                returnObject.setMessage("Created Successfully");
                returnObject.setStatus(true);
                returnObject.setData(dto);

                return ResponseEntity.ok(returnObject);
            }else{

                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("Store Doesn't exist");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
        }else{
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    private void updateLanguage(Integer productId ,ProductDTO productDTO) {
        ProductDetails productDetailsEn = new ProductDetails();
        ProductDetails productDetailsAr = new ProductDetails();
        productDetailsEn.setProductId(productId);
        productDetailsEn.setName(productDTO.getProductNameEn());
        productDetailsEn.setDescription(productDTO.getProductDescriptionEn());
        productDetailsEn.setLangId(ENGLISH);

        productDetailsAr.setProductId(productId);
        productDetailsAr.setName(productDTO.getProductNameAr());
        productDetailsAr.setDescription(productDTO.getProductDescriptionAr());
        productDetailsAr.setLangId(ARABIC);

        productDetailsRepository.save(productDetailsEn);
        productDetailsRepository.save(productDetailsAr);
    }

    public ResponseEntity<?> findOENumbersByBrandAndProductIds(OeNumber oeNumber){
        List<OeNumber> oeNumberList = oeNumberRepository.findAllByBrandIdAndProductId(oeNumber.getBrandId(),oeNumber.getProductId());
        ReturnObject returnObject = new ReturnObject();
        returnObject.setStatus(true);
        returnObject.setMessage("Successfully Loaded");
        returnObject.setData(oeNumberList);
        return ResponseEntity.ok(returnObject);
    }

    private void updateForeignKeys(Product product, ProductDTO dto) {
        Optional<Discount> discountOptional = discountRepository.findById(dto.getDiscountId());
        Optional<Promo> promoOptional = promoRepository.findById(dto.getPromoId());
        if(discountOptional.isPresent()){
            product.setDiscountId(dto.getDiscountId());
        }
        if(promoOptional.isPresent()){
            product.setPromoId(dto.getPromoId());
        }

    }

//    @Override
    public ResponseEntity<?> getProducts(String token ,Integer langId ,Pageable pageable) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        if (token != null) {
            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            Page<GetProductsDTO> productsDTOList = productRepository.findProducts2(langId, pageable);


            for (GetProductsDTO productsDTO : productsDTOList) {
                List<ProductSellerBrand> productSellerBrandList = productSellerBrandRepository.findAllByProductId(productsDTO.getId());
                List<SellerBrandsDTO> brands = new ArrayList<>();
                List<Integer> brandIds = new ArrayList<>();
                for(ProductSellerBrand productSellerBrand :productSellerBrandList) {
                    Optional<BrandDTO> brandDTOOptional = brandRepository.findBrandsByLangIdAndBrandId(ENGLISH, productSellerBrand.getSellerBrandsId());
                    if (brandDTOOptional.isPresent()) {
                        BrandDTO brandDTO = brandDTOOptional.get();
                        brandIds.add(productSellerBrand.getSellerBrandsId());
                        brands.add(new SellerBrandsDTO(productSellerBrand.getSellerBrandsId(),brandDTO.getBrandName(),brandDTO.getBrandDescription()));
                    }
                }
                productsDTO.setBrandIds(brandIds);
                productsDTO.setBrands(brands);
            }
            List<GetProductsDTO> finalProductsList = new ArrayList<>(productsDTOList.getContent());

            returnObject.setData(finalProductsList);
            returnObject.setTotalPages(productsDTOList.getTotalPages());
            returnObject.setTotalCount(productsDTOList.getTotalElements());
          return  ResponseEntity.ok(returnObject);
        }else{
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }
    public ResponseEntity<?> getProductsWithCount(String token ,Integer langId ,Pageable pageable) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            Page<GetProductsDTO> productsDTOList = productRepository.findProducts2(langId,pageable);

            Map<Integer, GetProductsDTO> productDTOMap = new HashMap<>();

            for (GetProductsDTO dto : productsDTOList) {
                if (productDTOMap.containsKey(dto.getId())) {
                    productDTOMap.get(dto.getId()).addBrandId(dto.getBrandIds().get(0));
                } else {
                    productDTOMap.put(dto.getId(), dto);
                }
            }

            List<GetProductsDTO> finalProductsList = new ArrayList<>(productDTOMap.values());
            ProductsDTO productsDTO = new ProductsDTO();
            productsDTO.setProductsList(finalProductsList);
            productsDTO.setSize(productRepository.count());

            returnObject.setData(productsDTO);
          return  ResponseEntity.ok(returnObject);
        }else{
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> getProductsByManufacture(String token, Integer brandId, Pageable pageable) {
        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        if (token != null) {
            Page<GetProductsDTO> productDTOS = productRepository.findProductsByManufactureId(1,brandId,pageable);
//            Page<ProductDetailsView> productDetailsViewList = productDetailsViewRepository.findAllByLangId(1, pageable);
            Map<Integer, GetProductsDTO> productDTOMap = new HashMap<>();

            for (GetProductsDTO dto : productDTOS) {
                if (productDTOMap.containsKey(dto.getId())) {
                    productDTOMap.get(dto.getId()).addBrandId(dto.getBrandIds().get(0));
                } else {
                    productDTOMap.put(dto.getId(), dto);
                }
            }

            List<GetProductsDTO> finalProductsList = new ArrayList<>(productDTOMap.values());

            returnObject.setData(finalProductsList);
            returnObject.setTotalPages(productDTOS.getTotalPages());
            returnObject.setTotalCount(productDTOS.getTotalElements());

            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }
    public ResponseEntity<?> findProductDetailsById(String token,Integer langId, Integer productId) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            List<ProductDetailsView> productDetailsViewList = productDetailsViewRepository.findByProductIdAndLangId(productId,langId);
            if(!productDetailsViewList.isEmpty()) {
                ProductDetailsDTO productDetailsDTO = new ProductDetailsDTO(productDetailsViewList.get(0));
                List<SellerBrand> sellerBrands = new ArrayList<>();
                List<CompatibleVehiclesDTO> compatibleVehiclesDTOList = new ArrayList<>();
                List<ProductSellerBrand> productSellerBrandsList =  productSellerBrandRepository.findAllByProductId(productDetailsViewList.get(0).getProductId());
                List<ProductBrandDTO> productBrands = new ArrayList<>();
                    List<ProductBrand> productBrand = productBrandRepository.findAllByProductId(productDetailsDTO.getProductId());
                    for(ProductBrand productBrand1 : productBrand){
                        productBrands.add(new ProductBrandDTO(
                                productBrand1.getId(),productBrand1.getBrandId(),productBrand1.getProductId(),
                                productBrand1.getPrice(),productBrand1.getQuantity(),productBrand1.getInfo(),
                                brandRepository.getById(productBrand1.getBrandId()).getImageUrl(),productBrand1.getPartNo()));
                    }
                if(!productSellerBrandsList.isEmpty())
                for (ProductSellerBrand productSellerBrand : productSellerBrandsList) {
                    SellerBrand sellerBrand = sellerBrandRepository.findById(productSellerBrand.getSellerBrandsId()).get();
                    sellerBrands.add(sellerBrand);
                }
                List<ProductModel> productModelList =  productModelRepository.findAllByProductId(productDetailsViewList.get(0).getProductId());
                if(!productModelList.isEmpty())
                for (ProductModel productModel : productModelList) {
                    Model model = modelRepository.findById(productModel.getModelId()).get();
                    CompatibleVehiclesDTO compatibleVehiclesDTO = new CompatibleVehiclesDTO();
                    compatibleVehiclesDTO.setVehicleCode(model.getCode());
                    compatibleVehiclesDTO.setCreationYear(model.getCreationYear());
                    compatibleVehiclesDTO.setBrandId(model.getBrandId());
                    compatibleVehiclesDTO.setBrandName(brandTextRepository.findByBrandIdAndLangId(model.getBrandId(), langId).get().getName());
                    compatibleVehiclesDTO.setImageUrl(brandRepository.findById(model.getBrandId()).get().getImageUrl());
                    compatibleVehiclesDTOList.add(compatibleVehiclesDTO);
                }
                productDetailsDTO.setProductBrandList(productBrands);
                productDetailsDTO.setCompatibleVehiclesDTOList(compatibleVehiclesDTOList);
                productDetailsDTO.setSellerBrandsList(sellerBrands);
                Optional<List<ProductStore>> productStoresOptional = productStoreRepository.findAllByProductId(productId);
                Optional<List<BillProduct>> billProducts = billProductRepository.findAllByProductId(productId);
                double minimum = 0.0;
                double maximum = 0.0;
                List<OeNumber> oeNumberList = oeNumberRepository.findAllByProductId(productId);
                if (productStoresOptional.isPresent()) {
                    Integer totalQuantity = 0;
                    for (int i = 0; i < productStoresOptional.get().size(); i++) {
                        totalQuantity += productStoresOptional.get().get(i).getQuantity();
                    }
                    if (billProducts.isPresent()) {
                        for (BillProduct product : billProducts.get()) {
                            if(product != null) {
                                if(product.getPrice() != null) {
                                    if (product.getPrice() > maximum) {
                                        maximum = product.getPrice();
                                    }
                                }else{
                                    maximum = 0;
                                }
                            }else{
                                maximum = 0;
                            }
                            if(product != null) {
                                if(product.getPrice() != null) {
                                    if (product.getPrice() < minimum) {
                                        minimum = product.getPrice();
                                    }
                                }else{
                                    minimum = 0;
                                }
                            }else{
                                minimum = 0;
                            }
                        }
                    }
                    productDetailsDTO.setCompatibleOeNumbersList(oeNumberList);
                    productDetailsDTO.setQuantity(totalQuantity);
                    productDetailsDTO.setMaximum(maximum);
                    productDetailsDTO.setMinimum(minimum);
                }
                returnObject.setMessage("Loaded Successfully");
                returnObject.setStatus(true);
                returnObject.setData(productDetailsDTO);
                return ResponseEntity.ok(returnObject);
            }else{
                returnObject.setMessage("There is no Info for this product");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }

        } else {
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }


    @Override
    public ResponseEntity update(String token, Integer productId, ProductDTO dto) {
            ReturnObject returnObject = new ReturnObject();
            Integer userId = getUserIdFromToken(token);
            User userAdmin = userRepository.findUserById(userId);

            if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                Optional<Product> existingProductOptional = productRepository.findById(productId);

                if (existingProductOptional.isPresent()) {
                    Product existingProduct = existingProductOptional.get();

                    // Check if the provided code is already in use by another product
                    Optional<Product> productWithSameCode = productRepository.findByCode(dto.getCode());
                    if (productWithSameCode.isPresent() && !productWithSameCode.get().getId().equals(productId)) {
                        returnObject.setData(productWithSameCode.get());
                        returnObject.setMessage("This product code is already in use");
                        returnObject.setStatus(false);
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                    }

                    // Update fields in the existing product
                    existingProduct.setCode(dto.getCode());
                    existingProduct.setPrice(dto.getPrice());
                    existingProduct.setCost(dto.getCost());

                    updateForeignKeys(existingProduct, dto);

                    // Save the updated product
                    existingProduct = productRepository.save(existingProduct);

                    // Update language information
                    updateLanguage(existingProduct.getId(), dto);

                    // Update information in the product store
                    Optional<ProductStore> productStoreOptional = productStoreRepository.findByProductId(existingProduct.getId());
                    if(productStoreOptional.isPresent()) {
                        ProductStore productStore = productStoreOptional.get();
                        productStore.setStoreId(dto.getStoreId());
                        productStore.setQuantity(dto.getQuantity());
                        if(!dto.getOeNumber().isEmpty())
                        productStore.setOeNumber(dto.getOeNumber().get(0));
                        productStore.setLocation(dto.getLocation());
                        productStoreRepository.save(productStore);
                    }
                    // Update information in the product brand
                    Optional<ProductBrand> productBrandOptional = productBrandRepository.findByProductId(existingProduct.getId());
                    if(productBrandOptional.isPresent()) {
                        ProductBrand productBrand = productBrandOptional.get();
                        productBrand.setPrice(dto.getPrice());
                        productBrand.setInfo(dto.getInfo());
                        productBrand.setQuantity(dto.getQuantity());
                        productBrand.setBrandId(dto.getBrandId());
                        productBrand.setPartNo(dto.getPartNo());
                        productBrandRepository.save(productBrand);
                    }
                    // Update information in the product seller brands
                    productSellerBrandRepository.deleteAllByProductId(existingProduct.getId());
                    if(dto.getSellerBrandsList() != null)
                    for (Integer sellerBrandId : dto.getSellerBrandsList()) {
                        ProductSellerBrand productSellerBrand = new ProductSellerBrand();
                        productSellerBrand.setProductId(existingProduct.getId());
                        productSellerBrand.setSellerBrandsId(sellerBrandId);
                        productSellerBrandRepository.save(productSellerBrand);
                    }

                    returnObject.setMessage("Product Updated Successfully");
                    returnObject.setStatus(true);
                    returnObject.setData(existingProduct);

                    return ResponseEntity.ok(returnObject);
                } else {
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                    returnObject.setMessage("Product with ID " + productId + " not found");

                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
            } else {
                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("User is not authorized");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }

    }

    @Override
    public ResponseEntity delete(Integer id) {
        return null;
    }


    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token + " ,  " + userIdString);
        return Integer.parseInt(userIdString);
    }
}
