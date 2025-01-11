package com.gsc.gsc.bill.service.serviceInterface;

import com.gsc.gsc.bill.dto.*;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import com.gsc.gsc.model.*;
import com.gsc.gsc.model.view.ProductDetailsView;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

import static com.gsc.gsc.bill.BillConstants.NOT_PAID;
import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;

@Service
public class BillService implements IBillService {

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private UserService userService;
    @Autowired
    private BillRepository billRepository;
    @Autowired
    private BillTypeRepository billTypeRepository;
    @Autowired
    private CBillStatusRepository cBillStatusRepository;
    @Autowired
    private CBillsStatusTextRepository cBillsStatusTextRepository;
    @Autowired
    private BillProductRepository billProductRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailsRepository productDetailsRepository;
    @Autowired
    private ProductDetailsViewRepository productDetailsViewRepository;
    @Autowired
    private BillTypeTextRepository billTypeTextRepository;
    @Autowired
    private JobCardRepository jobCardRepository;
    @Autowired
    private JobCardProductRepository jobCardProductRepository;

    @Override
    public Optional<Car> getById(Integer id) {
        return Optional.empty();
    }

    @Override
    public ResponseEntity create(String token, ProductDTO dto) {
        return null;
    }


    public ResponseEntity getBillsData(Integer billId, Integer langId) {
        Optional<Bill> billOptional = billRepository.findById(billId);
        BillProductsDTO billProductsDTO = new BillProductsDTO();
        if (billOptional.isPresent()) {
            Optional<List<BillProduct>> billProductOptional = billProductRepository.findAllByBillId(billOptional.get().getId());
            if (billProductOptional.isPresent()) {
                List<BillProduct> billProduct = billProductOptional.get();
                List<ProductDetailsView> productList = new ArrayList<>();
                billProductsDTO.setBill(billOptional.get());
                for (int i = 0; i < billProduct.size(); i++) {
                    List<ProductDetailsView> productDetailsViewList = productDetailsViewRepository.findByProductIdAndLangId(billProduct.get(i).getProductId(), langId);
                    productList.add(productDetailsViewList.get(0));
                }
                billProductsDTO.setProducts(productList);
            }
        }
        return ResponseEntity.ok(billProductsDTO);
    }

    @Override
    public ResponseEntity<?> getAllBillsByToken(String token, Integer langId) {
        Integer userId = getUserIdFromToken(token);
        ReturnObject returnObject = new ReturnObject();
        try {
            returnObject.setMessage("Loaded");
            returnObject.setStatus(true);
            returnObject.setData(billRepository.findAllByUserIdAndLangId(userId, langId));
            return ResponseEntity.ok(returnObject);

        } catch (Exception exception) {
            returnObject.setMessage("Error " + exception.getMessage());
            returnObject.setStatus(false);
            returnObject.setData(new ArrayList<>());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);

        }
    }

    public ResponseEntity<?> getBillsForAdminByToken(String token, Integer langId, Pageable pageable) {
        Integer userId = 2;
        User user = userRepository.findUserById(userId);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (user.getAccountTypeId() == ADMIN_TYPE) {
//            Page<Bill> billsPage = billRepository.findAll(pageable);
            Page<GetBillsDTO> billsPage = billRepository.findAllByLangId(1, pageable);

            if (billsPage.isEmpty()) {
                returnObject.setMessage("No Bills Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
            } else {

             /*   List<GetBillsDTO> billsDTOList = new ArrayList<>();

                for(Bill bill : billsPage){
                    GetBillsDTO billsDTO = new GetBillsDTO();
                    billsDTO.setCreatedBy(bill.getCreatedBy());
                    billsDTO.setReferenceNumber(bill.getReferenceNumber());
                    billsDTO.setId(bill.getId());
                    billsDTO.setCustomerNotes(bill.getCustomerNotes());
                    billsDTO.setCreatedAt(bill.getCreatedAt());
                    billsDTO.setAdminNotes(bill.getAdminNotes());
                    billsDTO.setTotal(bill.getTotal());
                    Optional<BillType> billTypeOptional = billTypeRepository.findBillTypeById(bill.getBillTypeId());
                    Optional<CBillStatus> billStatusOptional = cBillStatusRepository.findById(bill.getStatusId());
                    billStatusOptional.ifPresent(cBillStatus -> billsDTO.setStatus(cBillStatus.getCode()));
                    if(billTypeOptional.isPresent()) {
                        billsDTO.setBillTypeName(billTypeOptional.get().getCode());
                        Optional<BillTypeText> billTypeTextOptional = billTypeTextRepository.findById(bill.getStatusId());
                        billTypeTextOptional.ifPresent(billTypeText -> billsDTO.setBillTypeCode(billTypeText.getName()));
                    }
                    if(bill.getCarId() != null) {
                        Car car = findCarById(bill.getCarId());
                        if(car != null) {
                            billsDTO.setCarLicenseNumber(car.getLicenseNumber());
                            Model model = findModelById(car.getModelId());
                            if(model != null) {
                                billsDTO.setCarCode(model.getCode());
                            }
                        }
                    }
                    billsDTOList.add(billsDTO);
                }*/

                returnObject.setTotalPages(billsPage.getTotalPages());
                returnObject.setTotalCount(billsPage.getTotalElements());
                returnObject.setMessage(billsPage.getSize() + " Bills Loaded Successfully");
                returnObject.setData(billsPage.getContent());
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            }
        } else {
            returnObject.setMessage("UnAuthorized");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public Car findCarById(Integer carId) {
        Optional<Car> carOptional = carRepository.findById(carId);
        if (carOptional.isPresent()) {
            return carOptional.get();
        } else {
            return null;
        }
    }

    public Model findModelById(Integer modelId) {
        Optional<Model> modelOptional = modelRepository.findById(modelId);
        if (modelOptional.isPresent()) {
            return modelOptional.get();
        } else {
            return null;
        }
    }

    public ResponseEntity<?> createBillByAdmin(String token, AddBillDTO addBillDTO) {
        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        Optional<List<Bill>> billList = billRepository.findAllByUserId(userId);
        ReturnObject returnObject = new ReturnObject();
        if (addBillDTO.getUserId() == null) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("No user found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        if (user.getAccountTypeId() == ADMIN_TYPE) {
            Bill bill = new Bill();
            if (billList.isPresent() && !billList.get().isEmpty()) {
                bill.setReferenceNumber(billList.get().get(0).getReferenceNumber() + 1);
            } else {
                bill.setReferenceNumber("123xeo2");
            }
            bill.setStatusId(1);
            Optional<BillType> billType = billTypeRepository.findBillTypeByCode(String.valueOf(addBillDTO.getBillTypeId()));
            if (billType.isPresent()) {
                bill.setBillTypeId(billType.get().getId());
                bill.setCreatedBy(ADMIN_TYPE);
                bill.setAdminNotes(addBillDTO.getPrivateNotes());
                bill.setCustomerNotes(addBillDTO.getNotes());
                bill.setTotal(addBillDTO.getTotal());
                bill.setDiscount(addBillDTO.getDiscount());
                bill.setDate(addBillDTO.getDate());
                bill.setUserId(addBillDTO.getUserId());
                bill = billRepository.save(bill);
                List<BillProduct> billProductList = new ArrayList<>();
                for (int i = 0; i < addBillDTO.getProductBillDTOList().size(); i++) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setProductId(addBillDTO.getProductBillDTOList().get(i).getProductId());
                    billProduct.setBillId(bill.getId());
                    billProductList.add(billProduct);
                    billProductRepository.save(billProduct);
                }
                for (int i = 0; i < addBillDTO.getOtherProductsDTOList().size(); i++) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setBillId(bill.getId());
                    billProduct.setProductId(999999);
                    billProductList.add(billProduct);
                    try {
                        // Your save operation here
                        billProductRepository.save(billProduct);
                    } catch (Exception e) {
                        if (e instanceof SQLIntegrityConstraintViolationException) {
                            SQLIntegrityConstraintViolationException sqlException = (SQLIntegrityConstraintViolationException) e;
                            System.out.println("SQL Statement causing the exception: " + sqlException.getSQLState());
                            // Log or print the SQL statement for further analysis
                        }
                    }
                }
            } else {
                returnObject.setData(addBillDTO);
                returnObject.setStatus(false);
                returnObject.setMessage("Bill type not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }

            returnObject.setData(bill);
            returnObject.setStatus(true);
            returnObject.setMessage("Created Successfully!");
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized to add bills");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public void createJobCardBill(Integer userId, JobCard jobCard) {
        Bill bill = new Bill();
        bill.setReferenceNumber("Inv" + jobCard.getCode());
        bill.setStatusId(NOT_PAID);
        bill.setCreatedBy(ADMIN_TYPE);
        Optional<BillType> billTypeOptional = billTypeRepository.findBillTypeByCode("3");
        Bill finalBill = bill;
        billTypeOptional.ifPresent(billType -> finalBill.setBillTypeId(billType.getId()));
        bill.setCarId(jobCard.getCarId());
        bill.setUserId(userId);
        bill.setTotal(jobCard.getPrice());
        bill = billRepository.save(bill);
        Optional<List<JobCardProduct>> jobCardProductListOptional = jobCardProductRepository.findAllByJobCardId(jobCard.getId());
        if (jobCardProductListOptional.isPresent()) {
            List<JobCardProduct> jobCardProductList = jobCardProductListOptional.get();
            ArrayList<BillProduct> billProducts = new ArrayList<>();
            for (JobCardProduct jobCardProduct : jobCardProductList) {
                BillProduct billProduct = new BillProduct();
                billProduct.setName(jobCardProduct.getName());
                billProduct.setQuantity(jobCardProduct.getQuantity());
                billProduct.setPrice(Double.valueOf(jobCardProduct.getPrice()));
                billProduct.setProductId(jobCardProduct.getProductId());
                billProduct.setBillId(bill.getId());
                billProduct.setCustomerApprovedAt(jobCardProduct.getCustomerApprovedAt());
                billProduct.setCreatedBy(jobCardProduct.getCreatedBy());
                billProduct.setCustomerMobileVersion(jobCardProduct.getCustomerMobileVersion());
                billProducts.add(billProduct);
            }
            billProductRepository.saveAll(billProducts);
        }
    }

    public ResponseEntity<?> updateBillByAdmin(String token, AddBillDTO addBillDTO, Integer billId) {
        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        Optional<Bill> existingBillOptional = billRepository.findById(billId);
        ReturnObject returnObject = new ReturnObject();

        if (!existingBillOptional.isPresent()) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Bill not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
        }

        Bill existingBill = existingBillOptional.get();

  /*      if (!(existingBill.getUserId() == (addBillDTO.getUserId()))) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("Invalid request. User mismatch.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }*/

        if (user.getAccountTypeId() == ADMIN_TYPE) {
            // Update existing bill properties
            existingBill.setAdminNotes(addBillDTO.getPrivateNotes());
            existingBill.setCustomerNotes(addBillDTO.getNotes());
            existingBill.setTotal(addBillDTO.getTotal());
            existingBill.setDiscount(addBillDTO.getDiscount());
            existingBill.setDate(addBillDTO.getDate());

            // Clear existing products associated with the bill
            billProductRepository.deleteAllByBillId(existingBill.getId());

            // Save updated bill
            existingBill = billRepository.save(existingBill);

            // Save new products associated with the bill
            for (int i = 0; i < addBillDTO.getProductBillDTOList().size(); i++) {
                BillProduct billProduct = new BillProduct();
                billProduct.setProductId(addBillDTO.getProductBillDTOList().get(i).getProductId());
                billProduct.setPrice(addBillDTO.getProductBillDTOList().get(i).getPrice());
                billProduct.setQuantity(addBillDTO.getProductBillDTOList().get(i).getQuantity());
                billProduct.setBillId(existingBill.getId());
                billProductRepository.save(billProduct);
            }

            for (int i = 0; i < addBillDTO.getOtherProductsDTOList().size(); i++) {
                BillProduct billProduct = new BillProduct();
                billProduct.setBillId(existingBill.getId());
                billProduct.setProductId(null);
                billProduct.setQuantity(addBillDTO.getOtherProductsDTOList().get(i).getQuantity());
                billProduct.setPrice(Double.valueOf(addBillDTO.getOtherProductsDTOList().get(i).getPrice()));
                billProduct.setName(addBillDTO.getOtherProductsDTOList().get(i).getProductName());
                try {
                    // Your save operation here
                    billProductRepository.save(billProduct);
                } catch (Exception e) {
                    if (e instanceof SQLIntegrityConstraintViolationException) {
                        SQLIntegrityConstraintViolationException sqlException = (SQLIntegrityConstraintViolationException) e;
                        System.out.println("SQL Statement causing the exception: " + sqlException.getSQLState());
                        // Log or print the SQL statement for further analysis
                    }
                }
            }

            returnObject.setData(existingBill);
            returnObject.setStatus(true);
            returnObject.setMessage("Updated Successfully!");
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized to update bills");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    public ResponseEntity<?> createBill(String token, AddBillDTO addBillDTO) {
        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        Optional<List<Bill>> billList = billRepository.findAllByUserId(userId);

        ReturnObject returnObject = new ReturnObject();
        if (user != null) {
            Bill bill = new Bill();
            String newReferenceNumber;
            if (billList.isPresent() && !billList.get().isEmpty()) {
                List<Bill> bills = billList.get();
                // Sort bills by reference number to get the highest one
                Bill lastBill = bills.stream()
                        .max(Comparator.comparing(Bill::getReferenceNumber))
                        .orElse(null);
                if (lastBill != null) {
                    int lastRefNumber = Integer.parseInt(lastBill.getReferenceNumber().replaceAll("[^0-9]", ""));
                    newReferenceNumber = "U" + userId + "B" + (lastRefNumber + 1);
                } else {
                    newReferenceNumber = "U" + userId + "B1";
                }
            } else {
                newReferenceNumber = "U" + userId + "B1";
            }
            bill.setReferenceNumber(newReferenceNumber);
            bill.setStatusId(1);
            Optional<BillType> billType = billTypeRepository.findBillTypeByCode(String.valueOf(addBillDTO.getBillTypeId()));
            bill.setBillTypeId(billType.get().getId());
            bill.setCreatedBy(ADMIN_TYPE);
            bill.setAdminNotes(addBillDTO.getPrivateNotes());
            bill.setCustomerNotes(addBillDTO.getNotes());
            bill.setTotal(addBillDTO.getTotal());
            bill.setDiscount(addBillDTO.getDiscount());
            bill.setDate(addBillDTO.getDate());
            bill.setUserId(userId);
            bill = billRepository.save(bill);
            List<BillProduct> billProductList = new ArrayList<>();
            if (!addBillDTO.getProductBillDTOList().isEmpty()) {
                for (ProductBillDTO productBillDTO : addBillDTO.getProductBillDTOList()) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setProductId(productBillDTO.getProductId());
                    billProduct.setBillId(bill.getId());
                    billProductList.add(billProduct);
                }
                billProductRepository.saveAll(billProductList);
            }
            if (!addBillDTO.getOtherProductsDTOList().isEmpty()) {
                for (OtherProductDTO productDTO : addBillDTO.getOtherProductsDTOList()) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setBillId(bill.getId());
                    billProduct.setProductId(999999);
                    billProduct.setPrice(Double.valueOf(productDTO.getPrice()));
                    billProduct.setName(productDTO.getProductName());
                    billProduct.setQuantity(productDTO.getQuantity());
                    billProductList.add(billProduct);
                    try {
                        // Your save operation here
                        billProductRepository.save(billProduct);
                    } catch (Exception e) {
                        if (e instanceof SQLIntegrityConstraintViolationException) {
                            SQLIntegrityConstraintViolationException sqlException = (SQLIntegrityConstraintViolationException) e;
                            System.out.println("SQL Statement causing the exception: " + sqlException.getSQLState());
                        }
                    }
                }
            }

            returnObject.setData(bill);
            returnObject.setStatus(true);
            returnObject.setMessage("Created Successfully!");
            return ResponseEntity.ok(returnObject);
        } else {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("User Not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
    }

    @Override
    public ResponseEntity update(String token, Integer id, CarDTO dto) {
        return null;
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

    public ResponseEntity getBillsForAdminByTokenForUserId(String token, int langId, Integer userId, Pageable pageable) {
        Integer userAdminId = getUserIdFromToken(token);
        User userAdmin = userRepository.findUserById(userAdminId);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (userAdmin.getAccountTypeId() == ADMIN_TYPE) {
            Page<GetBillsDTO> billsPage = billRepository.findAllByLangIdAndUserId(langId, userId, pageable);
            Long countOfAllBills = billsPage.getTotalElements();
            returnObject.setTotalPages(billsPage.getTotalPages());
            returnObject.setTotalCount(billsPage.getTotalElements());
            if (billsPage.isEmpty()) {
                returnObject.setMessage("No Bills Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.OK).body(returnObject);
            } else {
                returnObject.setMessage(countOfAllBills + " Bills Loaded Successfully");
                returnObject.setData(billsPage.getContent());
                returnObject.setStatus(true);
                return ResponseEntity.ok(returnObject);
            }
        } else {
            returnObject.setMessage("Un Authorized");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }

    }

    public ResponseEntity<?> changeBillStatus(String token, Integer billId, BillStatusDTO billStatusDTO) {
        ReturnObject returnObject = new ReturnObject();
        if (token != null) {
            Integer userIdFromToken = getUserIdFromToken(token);
            if (userRepository.findUserById(userIdFromToken).getAccountTypeId() == ADMIN_TYPE) {
                Optional<Bill> billOptional = billRepository.findById(billId);
                if (billOptional.isPresent()) {
                    Bill bill = billOptional.get();
                    if (!Objects.equals(bill.getStatusId(), billStatusDTO.getBillStatusId())) {
                        bill.setStatusId(billStatusDTO.getBillStatusId());
                        billRepository.save(bill);
                        returnObject.setStatus(true);
                        returnObject.setData(bill);
                        returnObject.setMessage("Status changed Successfully " + billId);
                        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                    } else {
                        bill.setStatusId(billStatusDTO.getBillStatusId());
                        billRepository.save(bill);
                        returnObject.setStatus(false);
                        returnObject.setData(bill);
                        returnObject.setMessage("This is the already set status " + billId);
                        return ResponseEntity.status(HttpStatus.OK).body(returnObject);
                    }
                } else {
                    returnObject.setStatus(false);
                    returnObject.setData("No bill with " + billId);
                    returnObject.setMessage("No bill found");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                }
            } else {
                returnObject.setStatus(false);
                returnObject.setData(null);
                returnObject.setMessage("Not admin");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
            }
        } else {
            returnObject.setStatus(false);
            returnObject.setData(null);
            returnObject.setMessage("Not token found");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
    }

    public ResponseEntity<?> addAdminNotes(String token, Integer jobCardId, AddJobCardNotes addJobCardNotes) {
        ReturnObject returnObject = new ReturnObject();
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (user != null) {
            if (user.getAccountTypeId() == ADMIN_TYPE) {
                Optional<Bill> billOptional = billRepository.findById(jobCardId);
                if (billOptional.isPresent()) {
                    Bill bill = billOptional.get();
                    if (addJobCardNotes.isForCustomer())
                        bill.setCustomerNotes(addJobCardNotes.getNotes());
                    else
                        bill.setAdminNotes(addJobCardNotes.getNotes());
                    bill = billRepository.save(bill);
                    returnObject.setStatus(true);
                    returnObject.setData(bill);
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
                Optional<Bill> billOptional = billRepository.findById(jobCardId);
                if (billOptional.isPresent()) {
                    Bill bill = billOptional.get();
                    bill.setCustomerNotes(addJobCardNotes.getNotes());
                    bill = billRepository.save(bill);
                    returnObject.setStatus(true);
                    returnObject.setData(bill);
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

    public ResponseEntity getBillsData(Integer billId) {
        ReturnObject returnObject = new ReturnObject();
        Optional<Bill> billOptional = billRepository.findById(billId);
        AddBillDTO addBillDTO = new AddBillDTO();
        if (billOptional.isPresent()) {
            Bill bill = billOptional.get();
            Optional<List<BillProduct>> billProductOptional = billProductRepository.findAllByBillId(billOptional.get().getId());
            if (billProductOptional.isPresent()) {
                List<BillProduct> billProducts = billProductOptional.get();
                ArrayList<ProductBillDTO> billProductList = new ArrayList<>();
                ArrayList<OtherProductDTO> otherProductDTOArrayList = new ArrayList<>();
                for(BillProduct billProduct : billProducts){
                    if(billProduct.getProductId() != null){
                        ProductBillDTO productBillDTO = new ProductBillDTO(billProduct);
                        billProductList.add(productBillDTO);
                    }else{
                        OtherProductDTO otherProductDTO = new OtherProductDTO(billProduct);
                        otherProductDTOArrayList.add(otherProductDTO);
                    }
                }
                addBillDTO.setProductBillDTOList(billProductList);
                addBillDTO.setOtherProductsDTOList(otherProductDTOArrayList);
            }
            String referenceNumber = bill.getReferenceNumber();
            if (referenceNumber != null) {
                // Remove "Inv" from anywhere in the string
                referenceNumber = referenceNumber.replace("Inv", "");
                // Set it back to the bill object if needed
                bill.setReferenceNumber(referenceNumber);
            }
            addBillDTO.setUserId(billOptional.get().getUserId());
            Double downPayment = 0.0;
            Optional<JobCard> jobCardOptional = jobCardRepository.findByCode(referenceNumber);
            if(jobCardOptional.isPresent()) {
                JobCard jobCard = jobCardOptional.get();
                if (jobCard.getDownPayment() != null) {
                    downPayment = jobCard.getDownPayment();
                }
            }
            addBillDTO.setDownPayment(downPayment);
            addBillDTO.setBillTypeId(billOptional.get().getBillTypeId());
            addBillDTO.setDiscount(billOptional.get().getDiscount());
            addBillDTO.setDate(billOptional.get().getDate());
            addBillDTO.setTotal(billOptional.get().getTotal());
            addBillDTO.setPrivateNotes(billOptional.get().getAdminNotes());
            addBillDTO.setNotes(billOptional.get().getCustomerNotes());
            addBillDTO.setReferenceNumber(billOptional.get().getReferenceNumber());
            Optional<Car> carOptional = carRepository.findById(billOptional.get().getCarId());
            if(carOptional.isPresent()) {
                Car car = carOptional.get();
                addBillDTO.setCarData(car.getLicenseNumber() + " , " + car.getPlateNumber() + " , " + car.getCreationYear());
            }
        }
        returnObject.setData(addBillDTO);
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        return ResponseEntity.ok(returnObject);
    }
}
