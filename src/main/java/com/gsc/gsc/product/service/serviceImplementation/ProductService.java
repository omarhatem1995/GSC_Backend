package com.gsc.gsc.product.service.serviceImplementation;

import com.gsc.gsc.admin.service.serviceImplementation.AdminPermissionService;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.*;
import com.gsc.gsc.product.dto.*;
import com.gsc.gsc.product.dto.productList.ProductListDTO;
import com.gsc.gsc.product.service.serviceInterface.IProductService;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.utilities.ImgBBService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
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
    private ProductVehicleRepository productVehicleRepository;
    @Autowired
    private ProductManufacturerRepository productManufacturerRepository;
    @Autowired
    private ProductOeNumberRepository productOeNumberRepository;
    @Autowired
    private DiscountRepository discountRepository;
    @Autowired
    private PromoRepository promoRepository;
    @Autowired
    private OeNumberRepository oeNumberRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;

    @Autowired
    private ImgBBService imgBBService;
    @Autowired
    private ProductImagesRepository productImagesRepository;
    @Autowired
    private ModelColorRepository modelColorRepository;
    @Autowired
    private AdminPermissionService adminPermissionService;
    @Autowired
    private BillProductRepository billProductRepository;

    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Transactional
    public ResponseEntity<ReturnObject> createProduct(String token, CreateProductRequest request) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId == null || userAdmin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setMessage("This is not admin user");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if (!adminPermissionService.canAddProducts(userId)) {
            returnObject.setMessage("You do not have permission to add products");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        if (productRepository.findByCode(request.getCode()).isPresent()) {
            returnObject.setMessage("Product with the same code already exists");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }

        Product product = new Product();
        product.setCode(request.getCode());
        product.setNameEn(request.getProductNameEn());
        product.setNameAr(request.getProductNameAr());
        product.setDescriptionEn(request.getProductDescriptionEn());
        product.setDescriptionAr(request.getProductDescriptionAr());
        product.setPrice(request.getPrice());
        product.setCost(request.getCost());
        product.setDiscountId(request.getDiscountId());
        product.setPromoId(request.getPromoId());
        product = productRepository.save(product);

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = imgBBService.uploadImage(request.getImage());
                ProductImages productImage = new ProductImages();
                productImage.setProductId(product.getId());
                productImage.setUrl(imageUrl);
                productImage.setCounter(1);
                productImagesRepository.save(productImage);
                product.setImageUrl(imageUrl);
                productRepository.save(product);
            } catch (IOException ioException) {
                System.out.println("Error uploading image: " + request.getCode());
            }
        }

        // manufacturers
        if (request.getManufacturers() != null) {
            for (ManufacturerRequest m : request.getManufacturers()) {
                Optional<SellerBrand> brandOptional = sellerBrandRepository.findById(m.getSellerBrandId());
                if (brandOptional.isEmpty()) {
                    returnObject.setMessage("Seller Brand not found: " + m.getSellerBrandId());
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
                ProductManufacturer pm = new ProductManufacturer();
                pm.setProductId(product.getId());
                pm.setSellerBrandId(m.getSellerBrandId());
                pm.setPrice(m.getPrice());
                pm.setQuantity(m.getQuantity());
                productManufacturerRepository.save(pm);
            }
        }

        // OE numbers
        if (request.getOeNumber() != null) {
            for (String oe : request.getOeNumber()) {
                ProductOeNumber oeNumber = new ProductOeNumber();
                oeNumber.setProductId(product.getId());
                oeNumber.setOeNumber(oe);
                productOeNumberRepository.save(oeNumber);
            }
        }

        // vehicles
        if (request.getVehicles() != null) {
            for (VehicleCompatibilityRequest v : request.getVehicles()) {
                ProductVehicle pv = new ProductVehicle();
                pv.setProductId(product.getId());
                pv.setModelId(v.getModelId());
                pv.setYearFrom(v.getYearFrom());
                pv.setYearTo(v.getYearTo());
                productVehicleRepository.save(pv);
            }
        }

        returnObject.setStatus(true);
        returnObject.setData(product.getId());
        returnObject.setMessage("Added Product Successfully");
        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
    }



    public ResponseEntity<?> findOENumbersByBrandAndProductIds(OeNumber oeNumber) {
        List<OeNumber> oeNumberList = oeNumberRepository.findAllByBrandIdAndProductId(oeNumber.getBrandId(), oeNumber.getProductId());
        ReturnObject returnObject = new ReturnObject();
        returnObject.setStatus(true);
        returnObject.setMessage("Successfully Loaded");
        returnObject.setData(oeNumberList);
        return ResponseEntity.ok(returnObject);
    }

    private void updateForeignKeys(Product product, ProductDTO dto) {
//        Optional<Discount> discountOptional = discountRepository.findById(dto.getDiscountId());
//        Optional<Promo> promoOptional = promoRepository.findById(dto.getPromoId());
//        if (discountOptional.isPresent()) {
//            product.setDiscountId(dto.getDiscountId());
//        }
//        if (promoOptional.isPresent()) {
//            product.setPromoId(dto.getPromoId());
//        }

    }

    //    @Override

    public ResponseEntity<?> getProductsWithCount(String token, Integer langId, Pageable pageable) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            returnObject.setMessage("Loaded Successfully");
            returnObject.setStatus(true);
            Page<GetProductsDTO> productsDTOList = productRepository.findProducts2(langId, pageable);

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
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setMessage("There is no Token");
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    @Override
    @Transactional
    public ResponseEntity update(String token, Integer productId, ProductDTO dto) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);

        if (userId != null && userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            if (!adminPermissionService.canMaintainPrices(userId)) {
                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("You do not have permission to maintain product prices");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
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

                // Update core product fields
                existingProduct.setCode(dto.getCode());
                existingProduct.setPrice(dto.getPrice());
                existingProduct.setCost(dto.getCost());
                existingProduct.setNameEn(dto.getProductNameEn());
                existingProduct.setNameAr(dto.getProductNameAr());
                existingProduct.setDescriptionEn(dto.getProductDescriptionEn());
                existingProduct.setDescriptionAr(dto.getProductDescriptionAr());
                updateForeignKeys(existingProduct, dto);

                // Handle image upload if a new image was provided
                if (dto.getImage() != null && !dto.getImage().isEmpty()) {
                    try {
                        String imageUrl = imgBBService.uploadImage(dto.getImage());
                        ProductImages productImage = new ProductImages();
                        productImage.setProductId(existingProduct.getId());
                        productImage.setUrl(imageUrl);
                        productImage.setCounter(1);
                        productImagesRepository.save(productImage);
                        existingProduct.setImageUrl(imageUrl);
                    } catch (IOException ioException) {
                        System.out.println("Error uploading image: " + dto.getCode());
                    }
                }

                existingProduct = productRepository.save(existingProduct);

                // Update manufacturers — upsert approach to avoid FK violation with bill_product
                if (dto.getManufacturers() != null) {
                    // Collect the sellerBrandIds coming in from the request
                    Set<Integer> incomingSellerBrandIds = dto.getManufacturers().stream()
                            .map(ManufacturerRequest::getSellerBrandId)
                            .collect(Collectors.toSet());

                    // Validate all incoming seller brands exist
                    for (ManufacturerRequest m : dto.getManufacturers()) {
                        Optional<SellerBrand> brandOptional = sellerBrandRepository.findById(m.getSellerBrandId());
                        if (brandOptional.isEmpty()) {
                            returnObject.setMessage("Seller Brand not found: " + m.getSellerBrandId());
                            returnObject.setData(null);
                            returnObject.setStatus(false);
                            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                        }
                    }

                    // Delete existing manufacturers that are NOT in the new list,
                    // but only if they are not referenced by any bill_product row
                    List<ProductManufacturer> existingManufacturers =
                            productManufacturerRepository.findAllByProductId(existingProduct.getId());
                    for (ProductManufacturer existing : existingManufacturers) {
                        if (!incomingSellerBrandIds.contains(existing.getSellerBrandId())) {
                            if (!billProductRepository.existsByProductManufacturerId(existing.getId())) {
                                productManufacturerRepository.delete(existing);
                            }
                            // If it IS referenced in a bill, leave it in place to preserve history
                        }
                    }

                    // Upsert: update existing or create new for each incoming manufacturer
                    for (ManufacturerRequest m : dto.getManufacturers()) {
                        Optional<ProductManufacturer> existingPm =
                                productManufacturerRepository.findBySellerBrandIdAndProductId(
                                        m.getSellerBrandId(), existingProduct.getId());
                        ProductManufacturer pm = existingPm.orElseGet(ProductManufacturer::new);
                        pm.setProductId(existingProduct.getId());
                        pm.setSellerBrandId(m.getSellerBrandId());
                        pm.setPrice(m.getPrice());
                        pm.setQuantity(m.getQuantity());
                        productManufacturerRepository.save(pm);
                    }
                }

                // Update vehicles — delete old, save new
                if (dto.getVehicles() != null) {
                    productVehicleRepository.deleteAllByProductId(existingProduct.getId());
                    for (VehicleCompatibilityRequest v : dto.getVehicles()) {
                        ProductVehicle pv = new ProductVehicle();
                        pv.setProductId(existingProduct.getId());
                        pv.setModelId(v.getModelId());
                        pv.setYearFrom(v.getYearFrom());
                        pv.setYearTo(v.getYearTo());
                        productVehicleRepository.save(pv);
                    }
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
    public ResponseEntity<?> getProductsV3(GetProductsRequest request) {
        ReturnObjectPaging returnObjectPaging = new ReturnObjectPaging();

        // 1️⃣ Fetch paginated products
        Page<Product> productList = productRepository.findProductsV3(
                request.getBrandId(),
                request.getModelCode(),
                request.getYearFrom(),
                request.getYearTo(),
                request.getOeNumber(),
                PageRequest.of(request.getPage(), request.getSize())
        );

        List<Product> products = productList.getContent();

        // 2️⃣ Map to DTO
        List<ProductListDTO> productDTOs = products.stream().map(product -> {
            ProductListDTO dto = new ProductListDTO();
            dto.setId(product.getId());
            dto.setCode(product.getCode());
            dto.setNameEn(product.getNameEn());
            dto.setNameAr(product.getNameAr());
            dto.setDescriptionEn(product.getDescriptionEn());
            dto.setDescriptionAr(product.getDescriptionAr());
            dto.setImageUrl(product.getImageUrl());
            dto.setPrice(product.getPrice());
            dto.setCost(product.getCost());
            dto.setDiscountId(product.getDiscountId());
            dto.setPromoId(product.getPromoId());
            dto.setProductTypeId(product.getProductTypeId());

            // 3️⃣ Fetch manufacturer info for this product
            List<ProductManufacturerDTO> manufacturers = productManufacturerRepository
                    .findAllByProductIdWithBrand(product.getId());

            dto.setManufacturers(manufacturers);

            return dto;
        }).collect(Collectors.toList());

        // 4️⃣ Return paging response
        returnObjectPaging.setTotalPages(productList.getTotalPages());
        returnObjectPaging.setStatus(true);
        returnObjectPaging.setTotalCount(productList.getTotalElements());
        returnObjectPaging.setData(productDTOs);

        return ResponseEntity.ok(returnObjectPaging);
    }
    public ResponseEntity<?> getProductByIdV3(Integer productId) {
        ReturnObject returnObject = new ReturnObject();
        Optional<Product> productOptional = productRepository.findById(productId);

        if(productOptional.isEmpty()){
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("Product Not found");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        Product product = productOptional.get();
        ProductDetailsDTO productDetailsDTO = new ProductDetailsDTO();
        productDetailsDTO.setId(productId);
        productDetailsDTO.setCode(product.getCode());
        productDetailsDTO.setNameEn(product.getNameEn());
        productDetailsDTO.setImageUrl(product.getImageUrl());


        List<ProductOeNumber> productOeNumberList = productOeNumberRepository.findAllByProductId(productId);

        if(!productOeNumberList.isEmpty()) {
            ArrayList<String> compatibleOeNumbers = new ArrayList<>();
            for(ProductOeNumber productOeNumber : productOeNumberList){
                compatibleOeNumbers.add(productOeNumber.getOeNumber());
            }
            productDetailsDTO.setOeNumbers(compatibleOeNumbers);
        }

        List<ProductManufacturer> productManufacturerList = productManufacturerRepository.findAllByProductId(productId);
        if(!productManufacturerList.isEmpty()) {
            ArrayList<ProductManufacturerDTO> manufacturerList = new ArrayList<>();
            for(ProductManufacturer productManufacturer : productManufacturerList){
                ProductManufacturerDTO productManufacturerDTO = new ProductManufacturerDTO();
                productManufacturerDTO.setPrice(productManufacturer.getPrice());
                productManufacturerDTO.setQuantity(productManufacturer.getQuantity());
                Optional<SellerBrand> sellerBrandOptional = sellerBrandRepository.findById(productManufacturer.getSellerBrandId());
                productManufacturerDTO.setSellerBrandId(productManufacturer.getSellerBrandId());
                if(sellerBrandOptional.isPresent()){
                    productManufacturerDTO.setSellerBrandName(sellerBrandOptional.get().getNameEn());
                    productManufacturerDTO.setSellerBrandImageUrl(sellerBrandOptional.get().getImageUrl());
                }
                manufacturerList.add(productManufacturerDTO);
            }
            productDetailsDTO.setManufacturers(manufacturerList);
        }

        List<ProductVehicle> productVehicleList = productVehicleRepository.findAllByProductId(productId);
        if(!productVehicleList.isEmpty()){
            ArrayList<CompatibleVehiclesDTO> compatibleVehiclesDTOArrayList = new ArrayList<>();
            for(ProductVehicle productVehicle : productVehicleList){
                CompatibleVehiclesDTO compatibleVehiclesDTO = new CompatibleVehiclesDTO();
                Optional<Model> modelOptional = modelRepository.findById(productVehicle.getModelId());
                if(modelOptional.isPresent()){
                    Model model = modelOptional.get();
                    compatibleVehiclesDTO.setVehicleCode(model.getCode());
                    compatibleVehiclesDTO.setCreationYear(model.getCreationYear());
                    compatibleVehiclesDTO.setBrandId(model.getBrandId());
                    Optional<Brand> brandOptional = brandRepository.findById(model.getBrandId());
                    if(brandOptional.isPresent()){
                        Brand brand = brandOptional.get();
                        compatibleVehiclesDTO.setBrandName(brand.getNameEn());
                        compatibleVehiclesDTO.setBrandImageUrl(brand.getImageUrl());
                    }
                }
                compatibleVehiclesDTOArrayList.add(compatibleVehiclesDTO);
            }
            productDetailsDTO.setCompatibleVehicles(compatibleVehiclesDTOArrayList);
        }
        returnObject.setMessage("Loaded Successfully");
        returnObject.setData(productDetailsDTO);
        returnObject.setStatus(true);
        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
    }

    public ResponseEntity<?> getProductByIdForAdmin(String token, Integer productId) {
        ReturnObject returnObject = new ReturnObject();

        Integer userId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        if (userId == null || userAdmin == null || userAdmin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setMessage("User is not authorized");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        Optional<Product> productOptional = productRepository.findById(productId);
        if (productOptional.isEmpty()) {
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("Product Not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
        }

        Product product = productOptional.get();
        ProductAdminDetailsDTO dto = new ProductAdminDetailsDTO();
        dto.setId(product.getId());
        dto.setCode(product.getCode());
        dto.setPrice(product.getPrice());
        dto.setCost(product.getCost());
        dto.setDiscountId(product.getDiscountId());
        dto.setPromoId(product.getPromoId());
        dto.setProductNameEn(product.getNameEn());
        dto.setProductNameAr(product.getNameAr());
        dto.setProductDescriptionEn(product.getDescriptionEn());
        dto.setProductDescriptionAr(product.getDescriptionAr());
        dto.setImageUrl(product.getImageUrl());

        // OE numbers — same key name as ProductDTO.oeNumber
        List<ProductOeNumber> oeList = productOeNumberRepository.findAllByProductId(productId);
        if (!oeList.isEmpty()) {
            List<String> oeNumbers = new ArrayList<>();
            for (ProductOeNumber oe : oeList) {
                oeNumbers.add(oe.getOeNumber());
            }
            dto.setOeNumber(oeNumbers);
        }

        // Manufacturers — sellerBrandId, price, quantity (matches ManufacturerRequest / CreateProductRequest shape)
        List<ProductManufacturer> manufacturerList = productManufacturerRepository.findAllByProductId(productId);
        if (!manufacturerList.isEmpty()) {
            List<ManufacturerRequest> manufacturers = new ArrayList<>();
            for (ProductManufacturer pm : manufacturerList) {
                manufacturers.add(new ManufacturerRequest(pm.getSellerBrandId(), pm.getPrice(), pm.getQuantity()));
            }
            dto.setManufacturers(manufacturers);
        }

        // Vehicles — modelId, yearFrom, yearTo (matches VehicleCompatibilityRequest / CreateProductRequest shape)
        List<ProductVehicle> vehicleList = productVehicleRepository.findAllByProductId(productId);
        if (!vehicleList.isEmpty()) {
            List<VehicleCompatibilityRequest> vehicles = new ArrayList<>();
            for (ProductVehicle pv : vehicleList) {
                vehicles.add(new VehicleCompatibilityRequest(pv.getModelId(), pv.getYearFrom(), pv.getYearTo()));
            }
            dto.setVehicles(vehicles);
        }

        returnObject.setMessage("Loaded Successfully");
        returnObject.setData(dto);
        returnObject.setStatus(true);
        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
    }

    public Integer getUserIdFromToken(String token) {

        Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
        String userIdString = claims.getSubject();
        System.out.println("getProperty " + token + " ,  " + userIdString);
        return Integer.parseInt(userIdString);
    }
}
