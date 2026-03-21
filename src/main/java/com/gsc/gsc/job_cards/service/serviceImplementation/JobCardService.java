package com.gsc.gsc.job_cards.service.serviceImplementation;

import com.gsc.gsc.bill.dto.OtherProductDTO;
import com.gsc.gsc.bill.dto.ProductBillDTO;
import com.gsc.gsc.bill.service.serviceImplementation.BillService;
import com.gsc.gsc.brand.dto.BrandDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import com.gsc.gsc.job_cards.dto.GetJobCardsDTO;
import com.gsc.gsc.job_cards.dto.JobCardNotesDTO;
import com.gsc.gsc.job_cards.dto.JobCardsDTO;
import com.gsc.gsc.model.*;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.ImgBBService;
import com.gsc.gsc.utilities.NotificationMessage;
import com.gsc.gsc.utilities.UniqueCodeGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.gsc.gsc.constants.NotificationTypes.JOB_CARD;
import static com.gsc.gsc.constants.UserTypes.*;
import static com.gsc.gsc.job_cards.JobCardConstants.*;

@Service
public class JobCardService {

    @Autowired
    private JobCardRepository jobCardRepository;
    @Autowired
    private JobCardProductRepository jobCardProductRepository;
    @Autowired
    private JobCardImagesRepository jobCardImagesRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    private final ModelMapper modelMapper;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private JobCardNotesRepository jobCardNotesRepository;
    @Autowired
    private ProductImagesRepository productImagesRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;
    @Autowired
    private BillService billService;
    @Autowired
    private ImgBBService imgBBService;
    @Autowired
    private NotificationRepository notificationRepository;

    public JobCardService(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    public ResponseEntity<?> addAdminNotes(String token, Integer jobCardId, AddJobCardNotes addJobCardNotes) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (user != null) {
            if (user.getAccountTypeId() == ADMIN_TYPE) {
                Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
                if (jobCardOptional.isPresent()) {
                    if (addJobCardNotes.getNotes().trim().isEmpty()) {
                        returnObject.setStatus(false);
                        returnObject.setData(null);
                        returnObject.setMessage("Can't Add Empty Notes");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                    }
                    JobCard jobCard = jobCardOptional.get();
                    JobCardNotes jobCardNotes = new JobCardNotes();
                    jobCardNotes.setJobCardId(jobCardId);
                    if (addJobCardNotes.getNotes() != null && !addJobCardNotes.getNotes().trim().isEmpty()) {
                        if (addJobCardNotes.isForCustomer()) {
                            jobCard.setCustomerNotes(addJobCardNotes.getNotes());
                            jobCardNotes.setMessage(addJobCardNotes.getNotes());
                            jobCardNotes.setIsPrivate(false);
                            jobCardNotes.setCreatedBy(userId);
                        } else {
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setMessage(addJobCardNotes.getNotes());
                            jobCardNotes.setIsPrivate(true);
                        }
                        jobCardNotes = jobCardNotesRepository.save(jobCardNotes);
                    }
                    jobCard = jobCardRepository.save(jobCard);

                    returnObject.setStatus(true);
                    returnObject.setData(jobCard);
                    returnObject.setMessage("Notes Added Successfully");
                    return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                } else {
                    returnObject.setMessage("No Job Card found");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
            } else {
                returnObject.setMessage("User Not Admin");
                returnObject.setData(null);
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            returnObject.setMessage("User doesn't exist");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity<?> addCustomerNotes(String token, Integer jobCardId, AddJobCardNotes addJobCardNotes) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (user != null) {
            if (user.getAccountTypeId() == USER_TYPE) {
                Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
                if (jobCardOptional.isPresent()) {
                    if (addJobCardNotes.getNotes().trim().isEmpty()) {
                        returnObject.setStatus(false);
                        returnObject.setData(addJobCardNotes);
                        returnObject.setMessage("Can't Add Empty Notes");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                    }
                    JobCard jobCard = jobCardOptional.get();
                    jobCard.setCustomerNotes(addJobCardNotes.getNotes());
                    JobCardNotes jobCardNotes = new JobCardNotes();
                    jobCardNotes.setMessage(addJobCardNotes.getNotes());
                    jobCardNotes.setJobCardId(jobCardId);
                    jobCardNotes.setIsPrivate(false);
                    jobCardNotes.setCreatedBy(userId);
                    jobCardNotesRepository.save(jobCardNotes);
                    jobCard = jobCardRepository.save(jobCard);
                    returnObject.setStatus(true);
                    returnObject.setData(jobCard);
                    returnObject.setMessage("Notes Added Successfully");
                    return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                } else {
                    returnObject.setMessage("No Job Card found");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
            } else {
                returnObject.setMessage("User Not Admin");
                returnObject.setData(null);
                returnObject.setStatus(false);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            returnObject.setMessage("User doesn't exist");
            returnObject.setData(null);
            returnObject.setStatus(false);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity<?> getJobCardsForUser(
            String token,
            Integer lang,
            int page,
            int size,
            String searchQuery,
            Integer carId // ✅ Added optional carId filter
    ) {
        Integer userId = userService.getUserIdFromToken(token);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (userId == null) {
            returnObject.setMessage("User Id is Invalid");
            returnObject.setData(Collections.emptyList());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // --- Fetch JobCards with optional searchQuery and carId ---
        Page<JobCard> jobCardPage;
        if (carId != null && searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Both searchQuery and carId
            jobCardPage = jobCardRepository.findByUserIdAndCarIdAndCodeContainingIgnoreCase(
                    userId, carId, searchQuery.trim(), pageable);
        } else if (carId != null) {
            // Filter by carId only
            jobCardPage = jobCardRepository.findByUserIdAndCarId(userId, carId, pageable);
        } else if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Filter by searchQuery only
            jobCardPage = jobCardRepository.findByUserIdAndCodeContainingIgnoreCase(
                    userId, searchQuery.trim(), pageable);
        } else {
            // No filters
            jobCardPage = jobCardRepository.findAllByUserId(userId, pageable);
        }

        // --- Map JobCards to DTOs ---
        List<GetJobCardsDTO> jobCardsDTOList = jobCardPage.getContent()
                .stream()
                .map(jobCard -> {
                    // --- Car Data ---
                    String carData = "";
                    if (jobCard.getCarId() != null) {
                        carData = getCarData(jobCard.getCarId(), lang); // your helper method
                    }

                    // --- JobCard Images (optional) ---
                    // List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCard.getId());

                    // --- JobCard Notes (optional) ---
                /*
                List<JobCardNotes> jobCardNotesList = jobCardNotesRepository.findAllByJobCardId(jobCard.getId());
                List<JobCardNotesDTO> jobCardNotesDTOList = jobCardNotesList.stream()
                        .map(note -> mapNoteToDTO(note))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                */

                    return new GetJobCardsDTO(
                            jobCard,
                            null, // Replace null with jobCardImages if needed
                            carData,
                            null  // Replace null with jobCardNotesDTOList if needed
                    );
                })
                .collect(Collectors.toList());

        // --- Prepare return object ---
        returnObject.setMessage("Loaded Successfully");
        returnObject.setData(jobCardsDTOList);
        returnObject.setTotalCount(jobCardPage.getTotalElements());
        returnObject.setTotalPages(jobCardPage.getTotalPages());

        return ResponseEntity.ok(returnObject);
    }

    private String getCarData(Integer carId, Integer lang) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) return "";

        Car car = carOpt.get();
        Optional<Model> modelOpt = modelRepository.findById(car.getModelId());
        if (modelOpt.isEmpty()) return "";

        Model model = modelOpt.get();
        String carData = "[" + model.getCode() + "]";

        Optional<BrandDTO> brandOpt = brandRepository.findBrandsByLangIdAndBrandId(lang, model.getBrandId());
        if (brandOpt.isPresent()) {
            carData += " " + brandOpt.get().getBrandName();
        }

        return carData;
    }

    private JobCardNotesDTO mapNoteToDTO(JobCardNotes note) {
        if (note.getCreatedBy() == null) return null;

        Optional<User> userOpt = userRepository.findById(note.getCreatedBy());
        if (userOpt.isEmpty()) return null;

        User user = userOpt.get();
        boolean isPrivate = note.getIsPrivate();

        String userType = null;
        if ((user.getAccountTypeId().equals(BUSINESS_TYPE) || user.getAccountTypeId().equals(USER_TYPE)) && !isPrivate) {
            userType = "User";
        } else if (user.getAccountTypeId().equals(ADMIN_TYPE)) {
            userType = "Admin";
        }

        if (userType == null) return null;

        return new JobCardNotesDTO(note, user.getName(), userType);
    }

    public ResponseEntity<?> getJobCardsForAdmin(
            String token,
            Integer lang,
            int page,
            int size,
            String search,
            Integer carId
    ) {

        Integer userId = userService.getUserIdFromToken(token);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (userId == null) {
            returnObject.setMessage("Invalid User");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }

        User userAdmin = userRepository.findUserById(userId);

        if (userAdmin.getAccountTypeId() != ADMIN_TYPE) {
            returnObject.setMessage("No Admin");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // ✅ SEARCH BY CODE ONLY
        Page<JobCard> jobCardPage = jobCardRepository.findJobCards(
                carId,
                search == null ? "" : search.trim(),
                pageable
        );

        List<GetJobCardsDTO> jobCardDTOs = jobCardPage.getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        for (GetJobCardsDTO getJobCardsDTO : jobCardDTOs) {

/*            List<JobCardNotes> jobCardNotesList =
                    jobCardNotesRepository.findAllByJobCardId(getJobCardsDTO.getId());

            List<JobCardNotesDTO> jobCardNotesDTOList = new ArrayList<>();

            for (JobCardNotes jobCardNotes : jobCardNotesList) {

                if (jobCardNotes.getCreatedBy() != null) {

                    Optional<User> jobCardCreatedByUser =
                            userRepository.findById(jobCardNotes.getCreatedBy());

                    if (jobCardCreatedByUser.isPresent()) {

                        String userType =
                                jobCardCreatedByUser.get().getAccountTypeId().equals(ADMIN_TYPE)
                                        ? "Admin"
                                        : "User";

                        JobCardNotesDTO jobCardNotesDTO =
                                new JobCardNotesDTO(
                                        jobCardNotes,
                                        jobCardCreatedByUser.get().getName(),
                                        userType
                                );

                        jobCardNotesDTOList.add(jobCardNotesDTO);
                    }
                }
            }
            getJobCardsDTO.setJobCardNotes(jobCardNotesDTOList);*/

            if (getJobCardsDTO.getCarId() != null) {
                Optional<Car> carOpt = carRepository.findById(getJobCardsDTO.getCarId());
                carOpt.ifPresent(car -> {
                    getJobCardsDTO.setCarData(getCarData(car.getId(), 1));
                });
            }
        }

        returnObject.setMessage("Loaded Successfully");
        returnObject.setData(jobCardDTOs);
        returnObject.setTotalCount(jobCardPage.getTotalElements());
        returnObject.setTotalPages(jobCardPage.getTotalPages());

        return ResponseEntity.ok(returnObject);
    }

    private GetJobCardsDTO mapToDto(JobCard jobCard) {
        GetJobCardsDTO dto = new GetJobCardsDTO();
        // Map fields from jobCard to dto
        dto.setId(jobCard.getId());
        dto.setCode(jobCard.getCode());
        dto.setDate(jobCard.getDate());
        dto.setPrice(jobCard.getPrice());
        dto.setPrivateNotes(jobCard.getPrivateNotes());
        dto.setDownPayment(jobCard.getDownPayment());
        dto.setCarId(jobCard.getCarId());
        dto.setIsTestDrive(jobCard.getIsTestDrive());
        dto.setJobCardStatusId(jobCard.getJobCardStatusId());
        dto.setCreatedAt(jobCard.getCreatedAt());
        dto.setUpdatedAt(jobCard.getUpdatedAt());
        // Add more fields as necessary
        return dto;
    }

    private GetJobCardsDTO convertToGetJobCardDTO(JobCard jobCard) {
        GetJobCardsDTO jobCardDTO = modelMapper.map(jobCard, GetJobCardsDTO.class);

        jobCardDTO.setListOfJobCardsImages(fetchJobCardImages(jobCard.getId()));

        return jobCardDTO;
    }

    private List<String> fetchJobCardImages(Integer jobCardId) {
        List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardId);
        List<String> jobCardImagesString = new ArrayList<>();
        for (int i = 0; i < jobCardImages.size(); i++) {
            jobCardImagesString.add(jobCardImages.get(i).getUrl());
        }
        return jobCardImagesString;
    }

    public ResponseEntity<?> getJobCardsByUserId(String token, Integer lang, Integer userId) {
        Integer userAdminId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userAdminId);
        ReturnObject returnObject = new ReturnObject();

        if (userAdminId != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                List<JobCard> jobCardList = jobCardRepository.findAllByUserId(userId);
                String carData = "";
                List<GetJobCardsDTO> jobCardsDTOList = new ArrayList<>();
                if (!jobCardList.isEmpty()) {
                    for (int i = 0; i < jobCardList.size(); i++) {
                        List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardList.get(i).getId());
                        if (jobCardList.get(i).getCarId() != null) {
                            Optional<Car> carOptional = carRepository.findById(jobCardList.get(i).getCarId());
                            Car car;
                            if (carOptional.isPresent()) {
                                car = carOptional.get();
                                Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                                if (modelOptional.isPresent()) {
                                    Optional<BrandDTO> brandDTOOptional = brandRepository.findBrandsByLangIdAndBrandId(1, modelOptional.get().getBrandId());
                                    carData = "[" + modelOptional.get().getCode() + "]";
                                    if (brandDTOOptional.isPresent()) {
                                        carData = carData + " " + brandDTOOptional.get().getBrandName();
                                    }
                                }
                            }
                        }
                        List<JobCardNotes> jobCardNotesList = jobCardNotesRepository.findAllByJobCardId(jobCardList.get(i).getId());
                        List<JobCardNotesDTO> jobCardNotesDTOList = new ArrayList<>();
                        for (JobCardNotes jobCardNotes : jobCardNotesList) {
                            if (jobCardNotes.getCreatedBy() != null) {
                                Optional<User> jobCardCreatedByUser = userRepository.findById(jobCardNotes.getCreatedBy());
                                if (jobCardCreatedByUser.isPresent()) {
                                    String userType = "User";
                                    if (jobCardCreatedByUser.get().getAccountTypeId().equals(BUSINESS_TYPE) || jobCardCreatedByUser.get().getAccountTypeId().equals(USER_TYPE)) {
                                        userType = "User";
                                    } else if (jobCardCreatedByUser.get().getAccountTypeId().equals(ADMIN_TYPE)) {
                                        userType = "Admin";
                                    }
                                    JobCardNotesDTO jobCardNotesDTO = new JobCardNotesDTO(jobCardNotes, jobCardCreatedByUser.get().getName(), userType);
                                    jobCardNotesDTOList.add(jobCardNotesDTO);
                                }
                            }
                        }
                        jobCardsDTOList.add(new GetJobCardsDTO(jobCardList.get(i), jobCardImages, carData, jobCardNotesDTOList));
                    }
                }
                returnObject.setMessage("Loaded Successfully");
                returnObject.setData(jobCardsDTOList);
                return ResponseEntity.ok(returnObject);
            } else {
                returnObject.setMessage("No Admin");
                returnObject.setData(jobCardRepository.findAll());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            // Handle the case where userId is null
            returnObject.setMessage("Invalid User");
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }
    }

    public ResponseEntity getJobCardsDetails(String token, Integer jobCardId) {
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        Boolean isUserAdmin = false;
        if (user.getAccountTypeId() == ADMIN_TYPE) {
            isUserAdmin = true;
        }
        Optional<List<JobCardProduct>> jobCardProductsOptional = jobCardProductRepository.findAllByJobCardId(jobCardId);
        Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
        ReturnObject returnObject = new ReturnObject();
        if(jobCardOptional.isPresent()) {
            JobCard jobCard = jobCardOptional.get();
            JobCardsDTO jobCardsDTO = new JobCardsDTO();
            if (jobCardProductsOptional.isPresent()) {
                jobCardsDTO.setCode(jobCard.getCode());
                List<JobCardProduct> jobCardProductList = jobCardProductsOptional.get();
                List<ProductBillDTO> productBillDTOList = new ArrayList<>();
                List<OtherProductDTO> otherProductDTOList = new ArrayList<>();
                List<JobCardNotesDTO> jobCardNotesDTOList = new ArrayList<>();
                for (int i = 0; i < jobCardProductList.size(); i++) {
                    if (jobCardProductList.get(i).getProductId() != null) {
                        Optional<Product> productOptional = productRepository.findById(jobCardProductList.get(i).getProductId());
                        if (productOptional.isPresent()) {
                            Product product = productOptional.get();

                            String productImageUrl = product.getImageUrl();
                            productBillDTOList.add(new ProductBillDTO(product.getId(), product.getCode(), product.getPrice(), 0, 0, jobCardProductList.get(i).getQuantity(), product.getId(), productImageUrl, product.getCode(), jobCardProductList.get(i).getCreatedBy()));

                        }
                    } else {
                        otherProductDTOList.add(new OtherProductDTO(jobCardProductList.get(i).getId(), jobCardProductList.get(i).getQuantity(), jobCardProductList.get(i).getName(), jobCardProductList.get(i).getPrice(), jobCardProductList.get(i).getCreatedBy()));
                    }
                }
                List<JobCardNotes> jobCardNotesList = jobCardNotesRepository.findAllByJobCardId(jobCardId);
                for (JobCardNotes jobCardNotes : jobCardNotesList) {
                    if (jobCardNotes.getCreatedBy() != null) {
                        Optional<User> jobCardCreatedByUser = userRepository.findById(jobCardNotes.getCreatedBy());
                        if (jobCardCreatedByUser.isPresent()) {
                            String userType = "User";
                            if (jobCardCreatedByUser.get().getAccountTypeId().equals(BUSINESS_TYPE) || jobCardCreatedByUser.get().getAccountTypeId().equals(USER_TYPE)) {
                                userType = "User";
                                JobCardNotesDTO jobCardNotesDTO = new JobCardNotesDTO(jobCardNotes, jobCardCreatedByUser.get().getName(), userType);
                                jobCardNotesDTOList.add(jobCardNotesDTO);
                            } else if (jobCardCreatedByUser.get().getAccountTypeId().equals(ADMIN_TYPE)) {
                                userType = "Admin";
                                JobCardNotesDTO jobCardNotesDTO = new JobCardNotesDTO(jobCardNotes, jobCardCreatedByUser.get().getName(), userType);
                                if (jobCardNotes.getIsPrivate()) {
                                    if (isUserAdmin) {
                                        jobCardNotesDTOList.add(jobCardNotesDTO);
                                    }
                                } else {
                                    jobCardNotesDTOList.add(jobCardNotesDTO);
                                }
                            }
                        }
                    }
                }
                jobCardsDTO.setOtherProductsDTOList(otherProductDTOList);
                jobCardsDTO.setProductBillDTOList(productBillDTOList);
                jobCardsDTO.setJobCardStatus(String.valueOf(jobCard.getJobCardStatusId()));
                jobCardsDTO.setDate(String.valueOf(jobCard.getDate()));
                jobCardsDTO.setCarId(jobCard.getCarId());
                Optional<Car> carOptional = carRepository.findById(jobCard.getCarId());
                if (carOptional.isPresent()) {
                    Car car = carOptional.get();
                    Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                    if (modelOptional.isPresent()) {
                        jobCardsDTO.setCarData(modelOptional.get().getCode());
                        Optional<Brand> brandOptional = brandRepository.findById(modelOptional.get().getBrandId());
                        if (brandOptional.isPresent()) {
                            jobCardsDTO.setCarData(jobCardsDTO.getCarData() + " , " + modelOptional.get().getCreationYear());
                        }
                    }
                }
                List<JobCardImages> jobCardImageList = jobCardImagesRepository.findAllByJobCardId(jobCardId);
                List<String> jobCardImageUrlList = new ArrayList<>();
                for (JobCardImages jobCardImages : jobCardImageList) {
                    jobCardImageUrlList.add(jobCardImages.getUrl());
                }
                jobCardsDTO.setJobCardNotes(jobCardNotesDTOList);
                jobCardsDTO.setPrice(jobCard.getPrice());
                jobCardsDTO.setDownPayment(jobCard.getDownPayment());
                jobCardsDTO.setUserId(jobCard.getUserId());
                jobCardsDTO.setIsTestDrive(jobCard.getIsTestDrive());
                jobCardsDTO.setJobCardsUrl(jobCardImageUrlList);
                jobCardsDTO.setPrivateNotes(jobCard.getPrivateNotes());

                returnObject.setMessage("Loaded Successfully");
                returnObject.setStatus(true);
                returnObject.setData(jobCardsDTO);
                return ResponseEntity.ok(returnObject);
            } else {
                returnObject.setMessage("Job Card Not found");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        }else{
            returnObject.setMessage("Job Card Not found");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity<?> create(String token, JobCardsDTO jobCardsDTO, List<MultipartFile> images) {
        Integer userId = userService.getUserIdFromToken(token);
        if (jobCardsDTO.getCarId() != null) {
            Optional<Car> carByLicenseNumber = carRepository.findById(jobCardsDTO.getCarId());
            if (carByLicenseNumber.isPresent()) {

                jobCardsDTO.setUserId(userId);
                jobCardsDTO.setDate(LocalDate.now().toString());
                jobCardsDTO.setCode(UniqueCodeGenerator.generateNewJobCardCode(jobCardRepository.findFirstByOrderByIdDesc().getCode()));
                JobCard jobCard = new JobCard(jobCardsDTO, 4, USER_TYPE);
                jobCard.setDownPayment(0.0); //customer can't add down payment
                jobCard = jobCardRepository.save(jobCard);

                if (jobCardsDTO.getCustomerNotes() != null && !jobCard.getCustomerNotes().isEmpty()) {
                    JobCardNotes jobCardNotes = new JobCardNotes();
                    jobCardNotes.setMessage(jobCard.getCustomerNotes());
                    jobCardNotes.setJobCardId(jobCard.getId());
                    jobCardNotes.setIsPrivate(false);
                    jobCardNotes.setCreatedBy(userId);
                    jobCardNotesRepository.save(jobCardNotes);
                }

                if (images != null && !images.isEmpty()) {
                    List<String> uploadedUrls = new ArrayList<>();
                    for (MultipartFile image : images) {
                        try {
                            // Upload to your service, e.g., ImgBB or other
                            String imageUrl = imgBBService.uploadImage(image);
                            uploadedUrls.add(imageUrl);

                            // Save image info in JobCardImages table
                            JobCardImages jobCardImage = new JobCardImages(imageUrl, jobCard.getId());
                            jobCardImagesRepository.save(jobCardImage);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    jobCardsDTO.setJobCardsUrl(uploadedUrls);
                }
                if (jobCardsDTO.getOtherProductsDTOList() != null) {
                    for (int i = 0; i < jobCardsDTO.getOtherProductsDTOList().size(); i++) {
                        JobCardProduct jobCardProduct = new JobCardProduct(jobCardsDTO.getOtherProductsDTOList().get(i), jobCard.getId(), USER_TYPE);
                        jobCardProductRepository.save(jobCardProduct);
                    }
                }
                if (jobCardsDTO.getProductBillDTOList() != null) {
                    for (int i = 0; i < jobCardsDTO.getProductBillDTOList().size(); i++) {
                        JobCardProduct jobCardProduct = new JobCardProduct(jobCardsDTO.getProductBillDTOList().get(i), jobCard.getId(), USER_TYPE);
                        jobCardProductRepository.save(jobCardProduct);
                    }
                }
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("New Job Card Created Successfully");
                returnObject.setData(jobCard);
                return ResponseEntity.ok(returnObject);
            } else {
                ReturnObject returnObject = new ReturnObject();
                returnObject.setMessage("Car Doesn't Exist");
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);

            }
        } else {
            jobCardsDTO.setUserId(userId);
            jobCardsDTO.setCode(UniqueCodeGenerator.generateNewJobCardCode(jobCardRepository.findFirstByOrderByIdDesc().getCode()));
            JobCard jobCard = new JobCard(jobCardsDTO, 4, USER_TYPE);
            jobCard = jobCardRepository.save(jobCard);
            if (!jobCardsDTO.getJobCardsUrl().isEmpty()) {
                List<JobCardImages> jobCardImagesList = new ArrayList<>();
                for (int i = 0; i < jobCardsDTO.getJobCardsUrl().size(); i++) {
                    JobCardImages jobCardImage = new JobCardImages(jobCardsDTO.getJobCardsUrl().get(i), jobCard.getId());
                    jobCardImagesList.add(jobCardImage);
                }
                jobCardImagesRepository.saveAll(jobCardImagesList);
            }
            if (jobCardsDTO.getOtherProductsDTOList() != null) {
                for (int i = 0; i < jobCardsDTO.getOtherProductsDTOList().size(); i++) {
                    JobCardProduct jobCardProduct = new JobCardProduct(jobCardsDTO.getOtherProductsDTOList().get(i), jobCard.getId(), USER_TYPE);
                    jobCardProductRepository.save(jobCardProduct);
                }
            }
            if (jobCardsDTO.getProductBillDTOList() != null) {
                for (int i = 0; i < jobCardsDTO.getProductBillDTOList().size(); i++) {
                    JobCardProduct jobCardProduct = new JobCardProduct(jobCardsDTO.getProductBillDTOList().get(i), jobCard.getId(), USER_TYPE);
                    jobCardProductRepository.save(jobCardProduct);
                }
            }
            ReturnObject returnObject = new ReturnObject();
            returnObject.setMessage("New Job Card Created Successfully");
            returnObject.setData(jobCard);
            return ResponseEntity.ok(returnObject);
        }
    }

    public ReturnObject createByAdmin(String token, JobCardsDTO jobCardsDTO, List<MultipartFile> images) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();

        if (userId == null) {
            returnObject.setMessage("User Doesn't Exist");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return returnObject;
        }
        if (userAdmin != null) {
            if (userAdmin.getAccountTypeId() != ADMIN_TYPE) {
                returnObject.setMessage("User Not Admin");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return returnObject;
            }
        } else {
            returnObject.setMessage("Can't find the admin user");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return returnObject;
        }

        Optional<Car> carById = carRepository.findById(jobCardsDTO.getCarId());
        if (carById.isEmpty()) {
            returnObject.setMessage("Car Doesn't Exist");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return returnObject;
        }

        jobCardsDTO.setDate(LocalDate.now().toString());
        jobCardsDTO.setCode(
                UniqueCodeGenerator.generateNewJobCardCode(
                        jobCardRepository.findFirstByOrderByIdDesc().getCode()
                )
        );

        JobCard jobCard = new JobCard(jobCardsDTO, PENDING_CUSTOMER_APPROVAL, ADMIN_TYPE);
        jobCard = jobCardRepository.save(jobCard);

        // ✅ Handle images
        if (images != null && !images.isEmpty()) {
            List<String> uploadedUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                try {
                    // Upload to your service, e.g., ImgBB or other
                    String imageUrl = imgBBService.uploadImage(image);
                    uploadedUrls.add(imageUrl);

                    // Save image info in JobCardImages table
                    JobCardImages jobCardImage = new JobCardImages(imageUrl, jobCard.getId());
                    jobCardImagesRepository.save(jobCardImage);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            jobCardsDTO.setJobCardsUrl(uploadedUrls);
        }

        // Handle Other Products
        if (jobCardsDTO.getOtherProductsDTOList() != null) {
            for (OtherProductDTO otherProductDTO : jobCardsDTO.getOtherProductsDTOList()) {
                JobCardProduct jobCardProduct = new JobCardProduct(otherProductDTO, jobCard.getId(), ADMIN_TYPE);
                jobCardProductRepository.save(jobCardProduct);
            }
        }

        // Handle Product Bills
        if (jobCardsDTO.getProductBillDTOList() != null) {
            for (ProductBillDTO productBillDTO : jobCardsDTO.getProductBillDTOList()) {
                Optional<Product> existingProduct = productRepository.findById(productBillDTO.getId());
                if (existingProduct.isPresent()) {
                    JobCardProduct jobCardProduct = new JobCardProduct(productBillDTO, jobCard.getId(), ADMIN_TYPE);
                    jobCardProductRepository.save(jobCardProduct);
                } else {
                    System.out.println("Product with ID " + productBillDTO.getId() + " does not exist.");
                }
            }
        }

        // Handle Notes
        if (jobCardsDTO.getCustomerNotes() != null && !jobCardsDTO.getCustomerNotes().isEmpty()) {
            JobCardNotes notes = new JobCardNotes(userId, jobCardsDTO.getCustomerNotes(), false, jobCard.getId());
            jobCardNotesRepository.save(notes);
        }
        if (jobCardsDTO.getPrivateNotes() != null && !jobCardsDTO.getPrivateNotes().isEmpty()) {
            JobCardNotes privateNotes = new JobCardNotes(userId, jobCardsDTO.getPrivateNotes(), true, jobCard.getId());
            jobCardNotesRepository.save(privateNotes);
        }

        returnObject.setMessage("New Job Card Created Successfully");
        returnObject.setStatus(true);
        returnObject.setData(jobCard);
        return returnObject;
    }
  /*  public ReturnObject createByAdmin(String token, JobCardsDTO jobCardsDTO) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if (userId != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                Optional<Car> carByLicenseNumber = carRepository.findById(jobCardsDTO.getCarId());
                if (carByLicenseNumber.isPresent()) {
                    jobCardsDTO.setDate(LocalDate.now().toString());
                    jobCardsDTO.setCode(UniqueCodeGenerator.generateNewJobCardCode(jobCardRepository.findFirstByOrderByIdDesc().getCode()));
                    JobCard jobCard = new JobCard(jobCardsDTO, PENDING_CUSTOMER_APPROVAL, ADMIN_TYPE);
                    jobCard = jobCardRepository.save(jobCard);
                    if (jobCardsDTO.getJobCardsUrl() != null) {
                        for (int i = 0; i < jobCardsDTO.getJobCardsUrl().size(); i++) {
                            JobCardImages jobCardImages = new JobCardImages(jobCardsDTO.getJobCardsUrl().get(i), jobCard.getId());
                            jobCardImagesRepository.save(jobCardImages);
                        }
                    }
                    if (jobCardsDTO.getOtherProductsDTOList() != null) {
                        for (int i = 0; i < jobCardsDTO.getOtherProductsDTOList().size(); i++) {
                            JobCardProduct jobCardProduct = new JobCardProduct(jobCardsDTO.getOtherProductsDTOList().get(i), jobCard.getId(), ADMIN_TYPE);
                            jobCardProductRepository.save(jobCardProduct);
                        }
                    }
                    if (jobCardsDTO.getProductBillDTOList() != null) {
                        for (ProductBillDTO productBillDTO : jobCardsDTO.getProductBillDTOList()) {
                            // Check if the productId exists in the product table
                            Optional<Product> existingProduct = productRepository.findById(productBillDTO.getProductId());

                            if (existingProduct.isPresent()) {
                                // If product exists, create the JobCardProduct
                                JobCardProduct jobCardProduct = new JobCardProduct(productBillDTO, jobCard.getId(), ADMIN_TYPE);
                                jobCardProductRepository.save(jobCardProduct);
                            } else {
                                // Handle the case where the product doesn't exist
                                // You can either log a warning, throw an exception, or skip this entry
                                System.out.println("Product with ID " + productBillDTO.getProductId() + " does not exist.");
                            }
                        }
                    }
                    if (jobCardsDTO.getCustomerNotes() != null) {
                        if (!jobCardsDTO.getCustomerNotes().isEmpty()) {
                            JobCardNotes jobCardNotes = new JobCardNotes();
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setMessage(jobCardsDTO.getCustomerNotes());
                            jobCardNotes.setIsPrivate(false);
                            jobCardNotes.setJobCardId(jobCard.getId());
                            jobCardNotesRepository.save(jobCardNotes);
                        }
                    }
                    if (jobCardsDTO.getPrivateNotes() != null) {
                        if (!jobCardsDTO.getPrivateNotes().isEmpty()) {
                            JobCardNotes jobCardNotes = new JobCardNotes();
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setMessage(jobCardsDTO.getPrivateNotes());
                            jobCardNotes.setIsPrivate(true);
                            jobCardNotes.setJobCardId(jobCard.getId());
                            jobCardNotesRepository.save(jobCardNotes);
                        }
                    }
                    returnObject.setMessage("New Job Card Created Successfully");
                    returnObject.setData(jobCard);
                    returnObject.setStatus(true);
//                    return ResponseEntity.ok(returnObject);
                } else {
                    returnObject.setMessage("Car Doesn't Exist");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
//                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);

                }
            } else {
                returnObject.setMessage("User Not Admin");
                returnObject.setData(null);
                returnObject.setStatus(false);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
        } else {
            returnObject.setMessage("User Doesn't Exist");
            returnObject.setStatus(false);
            returnObject.setData(null);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);

        }
        return returnObject;
    }*/

    public ReturnObject updateByAdmin(String token, JobCardsDTO jobCardsDTO, Integer jobCardId, List<MultipartFile> images) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();

        if (userId != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                Optional<JobCard> existingJobCardOptional = jobCardRepository.findById(jobCardId);

                if (existingJobCardOptional.isPresent()) {
                    JobCard existingJobCard = existingJobCardOptional.get();

                    // Update job card properties
                    existingJobCard.setIsTestDrive(jobCardsDTO.getIsTestDrive());
                    existingJobCard.setPrice(jobCardsDTO.getPrice());
                    existingJobCard.setDownPayment(jobCardsDTO.getDownPayment());
                    existingJobCard.setCarId(jobCardsDTO.getCarId());
                    if (existingJobCard.getJobCardStatusId().equals(NEW)) {
                        existingJobCard.setJobCardStatusId(PENDING_CUSTOMER_APPROVAL); //Pending Customer
                    } else if (existingJobCard.getJobCardStatusId().equals(APPROVED)) {
                        existingJobCard.setJobCardStatusId(COMPLETED); //Completed
                        //Create Invoice
                        if (!billService.checkExistingInvoice(existingJobCard)) {
                            billService.createJobCardBill(jobCardsDTO.getUserId(), existingJobCard, userAdmin.getName());
                        } else {
                            returnObject.setMessage("Invoice Already Created");
                            returnObject.setStatus(false);
                        }
                    }
                    // Save updated job card
                    jobCardRepository.save(existingJobCard);
                    if (images != null && !images.isEmpty()) {
                        List<String> uploadedUrls = new ArrayList<>();
                        for (MultipartFile image : images) {
                            try {
                                // Upload to your service, e.g., ImgBB or other
                                String imageUrl = imgBBService.uploadImage(image);
                                uploadedUrls.add(imageUrl);

                                // Save image info in JobCardImages table
                                JobCardImages jobCardImage = new JobCardImages(imageUrl, jobCardId);
                                jobCardImagesRepository.save(jobCardImage);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        jobCardsDTO.setJobCardsUrl(uploadedUrls);
                    }
                    // Save new images associated with the job card
          /*          if (jobCardsDTO.getJobCardsUrl() != null) {
                        for (String imageUrl : jobCardsDTO.getJobCardsUrl()) {
                            JobCardImages jobCardImages = new JobCardImages(imageUrl, existingJobCard.getId());
                            jobCardImagesRepository.save(jobCardImages);
                        }
                    }*/

                    // Clear existing products associated with the job card
                    jobCardProductRepository.deleteAllByJobCardId(existingJobCard.getId());

                    // Save new products associated with the job card
                    if (jobCardsDTO.getOtherProductsDTOList() != null) {
                        for (OtherProductDTO otherProductDTO : jobCardsDTO.getOtherProductsDTOList()) {
                            JobCardProduct jobCardProduct = new JobCardProduct(otherProductDTO, existingJobCard.getId(), ADMIN_TYPE);
                            jobCardProductRepository.save(jobCardProduct);
                        }
                    }

                    // Save new products associated with the job card
                    if (jobCardsDTO.getProductBillDTOList() != null) {
                        for (ProductBillDTO productBillDTO : jobCardsDTO.getProductBillDTOList()) {
                            JobCardProduct jobCardProduct = new JobCardProduct(productBillDTO, existingJobCard.getId(), ADMIN_TYPE);
                            jobCardProductRepository.save(jobCardProduct);
                        }
                    }
                    notifyUserForJobCardUpdate(existingJobCard);
                    if (jobCardsDTO.getCustomerNotes() != null) {
                        if (!jobCardsDTO.getCustomerNotes().trim().isEmpty()) {
                            JobCardNotes jobCardNotes = new JobCardNotes();
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setMessage(jobCardsDTO.getCustomerNotes());
                            jobCardNotes.setIsPrivate(false);
                            jobCardNotes.setJobCardId(existingJobCard.getId());
                            jobCardNotesRepository.save(jobCardNotes);
                        }
                    }
                    if (jobCardsDTO.getPrivateNotes() != null) {
                        if (!jobCardsDTO.getPrivateNotes().trim().isEmpty()) {
                            JobCardNotes jobCardNotes = new JobCardNotes();
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setMessage(jobCardsDTO.getPrivateNotes());
                            jobCardNotes.setIsPrivate(true);
                            jobCardNotes.setJobCardId(existingJobCard.getId());
                            jobCardNotesRepository.save(jobCardNotes);
                        }
                    }
                    returnObject.setMessage("Job Card Updated Successfully");
                    returnObject.setData(existingJobCard);
                    returnObject.setStatus(true);
                } else {
                    returnObject.setMessage("Job Card Doesn't Exist");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                }
            } else {
                returnObject.setMessage("User Not Admin");
                returnObject.setData(null);
                returnObject.setStatus(false);
            }
        } else {
            returnObject.setMessage("User Doesn't Exist");
            returnObject.setStatus(false);
            returnObject.setData(null);
        }

        return returnObject;
    }

    public ReturnObject submitByAdmin(String token, Integer jobCardId) {
        Integer userId = userService.getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();

        if (userId != null) {
            if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
                Optional<JobCard> existingJobCardOptional = jobCardRepository.findById(jobCardId);

                if (existingJobCardOptional.isPresent()) {
                    JobCard existingJobCard = existingJobCardOptional.get();

                    // Update job card properties
                    existingJobCard.setJobCardStatusId(COMPLETED); //Completed
                    //Create Invoice
                    if (!billService.checkExistingInvoice(existingJobCard)) {
                        billService.createJobCardBill(existingJobCard.getUserId(), existingJobCard, userAdmin.getName());
                    } else {
                        returnObject.setMessage("Invoice Already Created");
                        returnObject.setStatus(false);
                    }
                    // Save updated job card
                    jobCardRepository.save(existingJobCard);


                    notifyUserForJobCardUpdate(existingJobCard);
                    returnObject.setMessage("Job Card Updated Successfully");
                    returnObject.setData(existingJobCard);
                    returnObject.setStatus(true);
                } else {
                    returnObject.setMessage("Job Card Doesn't Exist");
                    returnObject.setData(null);
                    returnObject.setStatus(false);
                }
            } else {
                returnObject.setMessage("User Not Admin");
                returnObject.setData(null);
                returnObject.setStatus(false);
            }
        } else {
            returnObject.setMessage("User Doesn't Exist");
            returnObject.setStatus(false);
            returnObject.setData(null);
        }

        return returnObject;
    }

    public void getCount() {
        List<JobCardNotes> adminNotesNotYetApprovedByCustomer = jobCardNotesRepository.findAllByJobCardIdAndIsPrivateAndApprovedByCustomerAtIsNullAndCreatedByNot(26, false, 1);
        System.out.println(adminNotesNotYetApprovedByCustomer.size());
    }

    public ResponseEntity updateStatusByUser(String token, Integer jobCardId, JobCardsDTO jobCardsDTO, List<MultipartFile> images) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            Integer userId = userService.getUserIdFromToken(token);
            Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
            if (jobCardOptional.isPresent()) {
                JobCard jobCard = jobCardOptional.get();
                if (Objects.equals(userId, jobCard.getUserId())) {
                    if (jobCard.getJobCardStatusId() == PENDING_CUSTOMER_APPROVAL) {
                        jobCard.setJobCardStatusId(APPROVED);
                        jobCardRepository.save(jobCard);
                        List<JobCardNotes> adminNotesNotYetApprovedByCustomer = jobCardNotesRepository.findAllByJobCardIdAndIsPrivateAndApprovedByCustomerAtIsNullAndCreatedByNot(jobCard.getId(), false, userId);
                        System.out.println("getAdminNotes Count : " + adminNotesNotYetApprovedByCustomer.size());
                        if (!adminNotesNotYetApprovedByCustomer.isEmpty()) {
                            for (JobCardNotes jobCardNotes : adminNotesNotYetApprovedByCustomer) {
                                jobCardNotes.setApprovedByCustomerAt(Timestamp.from(Instant.now()));
                                jobCardNotes.setCustomerMobileVersion(jobCardsDTO.getCustomerMobileVersion());
                                jobCardNotes.setCustomerMobileMacAddress(jobCardsDTO.getCustomerMobileMacAddress());
                                jobCardNotesRepository.save(jobCardNotes);
                            }
                        }
                        Optional<List<JobCardProduct>> adminProductsNotYetApprovedByCustomerOptional = jobCardProductRepository.findAllByJobCardIdAndCustomerApprovedAt(jobCardId, null);
                        if (adminProductsNotYetApprovedByCustomerOptional.isPresent()) {
                            List<JobCardProduct> jobCardProductList = adminProductsNotYetApprovedByCustomerOptional.get();
                            if (!jobCardProductList.isEmpty()) {
                                for (JobCardProduct jobCardProduct : jobCardProductList) {
                                    jobCardProduct.setCustomerApprovedAt(Timestamp.from(Instant.now()));
                                    jobCardProduct.setCustomerMobileVersion(jobCardsDTO.getCustomerMobileVersion());
                                    jobCardProductRepository.save(jobCardProduct);
                                }
                            }
                        }

                        if (jobCardsDTO.getCustomerNotes() != null) {
                            JobCardNotes jobCardNotes = new JobCardNotes();
                            jobCardNotes.setMessage(jobCardsDTO.getCustomerNotes());
                            jobCardNotes.setJobCardId(jobCard.getId());
                            jobCardNotes.setIsPrivate(false);
                            jobCardNotes.setCreatedBy(userId);
                            jobCardNotes.setApprovedByCustomerAt(Timestamp.from(Instant.now()));
                            jobCardNotes.setCustomerMobileVersion(jobCardsDTO.getCustomerMobileVersion());
                            jobCardNotes.setCustomerMobileMacAddress(jobCardsDTO.getCustomerMobileMacAddress());
                            jobCardNotesRepository.save(jobCardNotes);
                        }
                        returnObject.setMessage("Job Card Approved Successfully");
                        returnObject.setData(null);
                        returnObject.setStatus(true);
                        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                    }
                    if (userId != null) {
                        Optional<JobCard> existingJobCardOptional = jobCardRepository.findById(jobCardId);

                        if (existingJobCardOptional.isPresent()) {
                            JobCard existingJobCard = existingJobCardOptional.get();
                            existingJobCard.setIsTestDrive(jobCardsDTO.getIsTestDrive());
                            existingJobCard.setPrice(jobCardsDTO.getPrice());
                            existingJobCard.setCarId(jobCardsDTO.getCarId());
                            if (!existingJobCard.getJobCardStatusId().equals(NEW)) {
                                returnObject.setMessage("Job Card Doesn't Exist");
                                returnObject.setData(null);
                                returnObject.setStatus(false);
                                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                            }
                            jobCardRepository.save(existingJobCard);
                            if (images != null && !images.isEmpty()) {
                                System.out.println(images.size());
                                List<String> uploadedUrls = new ArrayList<>();
                                for (MultipartFile image : images) {
                                    try {
                                        // Upload to your service, e.g., ImgBB or other
                                        String imageUrl = imgBBService.uploadImage(image);
                                        uploadedUrls.add(imageUrl);
                                        // Save image info in JobCardImages table
                                        JobCardImages jobCardImage = new JobCardImages(imageUrl, jobCardId);
                                        jobCardImagesRepository.save(jobCardImage);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                jobCardsDTO.setJobCardsUrl(uploadedUrls);
                            }
                            // Clear existing products associated with the job card
                            jobCardProductRepository.deleteAllByJobCardId(existingJobCard.getId());
                            if (jobCardsDTO.getOtherProductsDTOList() != null) {
                                for (OtherProductDTO otherProductDTO : jobCardsDTO.getOtherProductsDTOList()) {
                                    JobCardProduct jobCardProduct = new JobCardProduct(otherProductDTO, existingJobCard.getId(), ADMIN_TYPE);
                                    jobCardProductRepository.save(jobCardProduct);
                                }
                            }
                            if (jobCardsDTO.getProductBillDTOList() != null) {
                                for (ProductBillDTO productBillDTO : jobCardsDTO.getProductBillDTOList()) {
                                    JobCardProduct jobCardProduct = new JobCardProduct(productBillDTO, existingJobCard.getId(), ADMIN_TYPE);
                                    jobCardProductRepository.save(jobCardProduct);
                                }
                            }
                            if (jobCardsDTO.getCustomerNotes() != null) {
                                if (!jobCardsDTO.getCustomerNotes().isEmpty()) {
                                    JobCardNotes jobCardNotes = new JobCardNotes();
                                    jobCardNotes.setCreatedBy(userId);
                                    jobCardNotes.setMessage(jobCardsDTO.getCustomerNotes());
                                    jobCardNotes.setIsPrivate(false);
                                    jobCardNotes.setJobCardId(existingJobCard.getId());
                                    jobCardNotesRepository.save(jobCardNotes);
                                }
                            }
                            if (jobCardsDTO.getPrivateNotes() != null) {
                                if (!jobCardsDTO.getPrivateNotes().isEmpty()) {
                                    JobCardNotes jobCardNotes = new JobCardNotes();
                                    jobCardNotes.setCreatedBy(userId);
                                    jobCardNotes.setMessage(jobCardsDTO.getPrivateNotes());
                                    jobCardNotes.setIsPrivate(true);
                                    jobCardNotes.setJobCardId(existingJobCard.getId());
                                    jobCardNotesRepository.save(jobCardNotes);
                                }
                            }
                            notifyUserForJobCardUpdate(existingJobCard);
                            returnObject.setMessage("Job Card Updated Successfully");
                            returnObject.setData(existingJobCard);
                            returnObject.setStatus(true);
                        } else {
                            returnObject.setMessage("Job Card Doesn't Exist");
                            returnObject.setData(null);
                            returnObject.setStatus(false);
                        }
                    }

                    returnObject.setMessage("Job Card has been Approved");
                    returnObject.setStatus(true);
                    returnObject.setData(jobCard);
                    return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                }
                else {
                    returnObject.setMessage("Only User has created JobCard can update it");
                    returnObject.setStatus(false);
                    returnObject.setData(null);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
            } else {
                returnObject.setMessage("No Job Card is not Found");
                returnObject.setStatus(false);
                returnObject.setData(null);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            returnObject.setMessage("No Job Card is not Found");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(returnObject);
        }

    }

    private void notifyUserForJobCardUpdate(JobCard existingJobCard) {
        Optional<User> userOptional = userRepository.findById(existingJobCard.getUserId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String title = "Updated Job Card : " + existingJobCard.getCode();
            String body  = "Your Job Card has been updated by admin with price: " + existingJobCard.getPrice();

            if (user.getFirebaseToken() != null) {
                NotificationMessage notificationMessage = new NotificationMessage();
                notificationMessage.setTitle(title);
                notificationMessage.setBody(body);
                notificationMessage.setData(Map.of("message", body));
                notificationMessage.setRecToken(user.getFirebaseToken());
                System.out.println("Notification Sent to : " + user.getId() + " , token : " + user.getFirebaseToken());
                String result = firebaseMessagingService.sendNotification(notificationMessage);

                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setTitle(title);
                notification.setText(body);
                notification.setIsSent(!"Failed".equals(result));
                notification.setNotificationType(JOB_CARD);
                notificationRepository.save(notification);
            }
        }
    }
}
