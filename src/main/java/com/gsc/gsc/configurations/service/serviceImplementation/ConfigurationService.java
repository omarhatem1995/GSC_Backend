package com.gsc.gsc.configurations.service.serviceImplementation;

import com.gsc.gsc.configurations.dto.*;
import com.gsc.gsc.configurations.dto.vehicle.CreateVehicleDTO;
import com.gsc.gsc.configurations.dto.vehicle.VehicleModelDTO;
import com.gsc.gsc.configurations.dto.vehicle.VehicleTableDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.model.*;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.utilities.ImgBBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;

@Service
public class ConfigurationService {

    @Autowired
    ModelRepository modelRepository;
    @Autowired
    ColorRepository colorRepository;
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    ProductModelRepository productModelRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    private SellerBrandRepository sellerBrandRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ImgBBService imgBBService;
    public ResponseEntity findAllModels() {
        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(modelRepository.findDistinctByCode());
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getAllManufacturers(Integer langId){
        ReturnObject returnObject = new ReturnObject();
        List<SellerBrand> sellerBrandList = sellerBrandRepository.findAll();
        List<SellerBrandDTO> sellerBrandDTO = new ArrayList<>();
        for(SellerBrand sellerBrand : sellerBrandList){
            sellerBrandDTO.add(new SellerBrandDTO(sellerBrand,langId));
        }
        returnObject.setData(sellerBrandDTO);
        returnObject.setStatus(true);
        returnObject.setMessage("Loaded Successfully");
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getBrandsWithModelsAndColors(Integer langId) {
        List<CBrandDTO> brands = brandRepository.findCBrandsByLangId(langId);

        for (CBrandDTO brand : brands) {
            // Fetch all models for this brand
            List<Model> modelEntities = modelRepository.findModelsByBrandId(brand.getId());

            // Group models by code to collect all creation years
            Map<String, List<Integer>> modelYearsMap = modelEntities.stream()
                    .collect(Collectors.groupingBy(
                            Model::getCode,
                            Collectors.mapping(Model::getCreationYear, Collectors.toList())
                    ));

            // Build model DTOs including colors
            List<CModelDTO> models = modelEntities.stream()
                    .map(model -> {
                        CModelDTO dto = new CModelDTO();
                        dto.setId(model.getId());
                        dto.setCode(model.getCode());
                        dto.setYears(Collections.singletonList(model.getCreationYear()));

                        // Fetch colors for this exact model id
                        List<Color> colors = colorRepository.findColorsByModelId(model.getId());
                        List<CColorDTO> colorDTOs = colors.stream()
                                .map(c -> new CColorDTO(c.getId(), c.getName()))
                                .collect(Collectors.toList());
                        dto.setColors(colorDTOs);

                        return dto;
                    })
                    .collect(Collectors.toList());
            brand.setModels(models);
        }

        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setData(brands);
        returnObject.setStatus(true);

        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getVehiclesTable(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        List<VehicleTableDTO> rows = brandPage.getContent().stream()
                .map(brand -> new VehicleTableDTO(
                        brand.getId(),
                        brand.getCode(),
                        brand.getNameEn(),
                        brand.getNameAr(),
                        brand.getImageUrl(),
                        modelRepository.countByBrandId(brand.getId()),
                        brand.getCreatedAt(),
                        brand.getUpdatedAt()
                ))
                .collect(Collectors.toList());

        ReturnObjectPaging returnObject = new ReturnObjectPaging();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(rows);
        returnObject.setTotalPages(brandPage.getTotalPages());
        returnObject.setTotalCount(brandPage.getTotalElements());
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity findAllBrands(Integer langId) {
        ReturnObject returnObject = new ReturnObject();
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        returnObject.setData(brandRepository.findBrandsByLangId(langId));
        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity addManufacturer(String token, CreateManufacturerDTO createManufacturerDTO) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (user != null) {
            if (user.getAccountTypeId() == ADMIN_TYPE){
                String imageUrl;
                try {
                    imageUrl = imgBBService.uploadImage(createManufacturerDTO.getImage());
                } catch (IOException e) {
                    returnObject.setMessage("Couldn't Upload Image");
                    returnObject.setStatus(false);
                    returnObject.setData(createManufacturerDTO);
                    return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
                SellerBrand sellerBrandExists = sellerBrandRepository.findByCode(createManufacturerDTO.getCode());

                if(sellerBrandExists != null){
                    returnObject.setMessage("Seller Brand with Same code already exists");
                    returnObject.setStatus(false);
                    returnObject.setData(sellerBrandExists);
                    return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }

                SellerBrand sellerBrand = new SellerBrand(createManufacturerDTO);
                sellerBrand.setImageUrl(imageUrl);
                sellerBrand.setCreatedBy(userId);
                sellerBrand = sellerBrandRepository.save(sellerBrand);
                returnObject.setData(sellerBrand);
                returnObject.setStatus(true);
                returnObject.setMessage("Added Seller Brand Successfully");
                return ResponseEntity.ok(returnObject);
            }else{
                returnObject.setMessage("User is not an admin");
                returnObject.setStatus(false);
                returnObject.setData(createManufacturerDTO);
                return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }else{
            returnObject.setMessage("User not found");
            returnObject.setStatus(false);
            returnObject.setData(createManufacturerDTO);
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity addVehicles(String token, CreateVehicleDTO createVehicleDTO) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (user != null) {
            if (user.getAccountTypeId() == ADMIN_TYPE) {
                if (brandRepository.existsByCode(createVehicleDTO.getCode())) {
                    returnObject.setStatus(false);
                    returnObject.setMessage("Brand with this code already exists");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                }
                Brand brand = new Brand(createVehicleDTO);
                brand = brandRepository.save(brand);
                for (VehicleModelDTO vehicleModelDTO : createVehicleDTO.getVehicleModelList()) {

                    for (String year : vehicleModelDTO.getYears()) {

                        Integer creationYear = Integer.valueOf(year);

                        // Check if model + year already exists
                        boolean exists = modelRepository.existsByBrandIdAndCodeAndCreationYear(
                                brand.getId(),
                                vehicleModelDTO.getCode(),
                                creationYear
                        );

                        if (exists) {
                            continue; // skip creation
                        }

                        // Only create if it doesn't exist
                        Model model = new Model();
                        model.setBrandId(brand.getId());
                        model.setCode(vehicleModelDTO.getCode());
                        model.setNameEn(vehicleModelDTO.getNameEn());
                        model.setNameAr(vehicleModelDTO.getNameAr());
                        model.setCreationYear(creationYear);

                        modelRepository.save(model);
                    }
                }
                returnObject.setMessage("Created Successfully");
                returnObject.setStatus(true);
                returnObject.setData(createVehicleDTO);
                return  ResponseEntity.status(HttpStatus.OK).body(returnObject);
            }else{
                returnObject.setMessage("User is not an admin");
                returnObject.setStatus(false);
                returnObject.setData(createVehicleDTO);
                return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }else{
            returnObject.setMessage("User not found");
            returnObject.setStatus(false);
            returnObject.setData(createVehicleDTO);
            return  ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }
}
