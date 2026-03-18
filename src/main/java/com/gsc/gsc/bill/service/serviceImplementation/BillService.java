package com.gsc.gsc.bill.service.serviceImplementation;

import com.gsc.gsc.bill.dto.*;
import com.gsc.gsc.bill.service.serviceInterface.IBillService;
import com.gsc.gsc.car.dto.CarDTO;
import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.constants.ReturnObjectPaging;
import com.gsc.gsc.job_cards.dto.AddJobCardNotes;
import com.gsc.gsc.model.*;
import com.gsc.gsc.model.view.ProductDetailsView;
import com.gsc.gsc.product.dto.ProductDTO;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.gsc.gsc.constants.NotificationTypes;
import com.gsc.gsc.utilities.FirebaseMessagingService;
import com.gsc.gsc.utilities.NotificationMessage;

import java.util.Map;

import static com.gsc.gsc.bill.BillConstants.NOT_PAID;
import static com.gsc.gsc.bill.BillConstants.PAID;
import static com.gsc.gsc.constants.NotificationTypes.BILL;
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
    private PaymentRepository paymentRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductDetailsViewRepository productDetailsViewRepository;
    @Autowired
    private ProductManufacturerRepository manufacturerRepository;
    @Autowired
    private JobCardRepository jobCardRepository;
    @Autowired
    private JobCardProductRepository jobCardProductRepository;
    @Autowired
    private ProductImagesRepository productImagesRepository;
    @Autowired
    private BillNotesRepository billNotesRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private FirebaseMessagingService firebaseMessagingService;

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

    public ResponseEntity<?> getBills(
            String token,
            Integer selectedUserId,
            String search,
            String billNumber,
            String fromDate,
            String toDate,
            Integer langId,
            Pageable pageable) {

        Integer userIdFromToken = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userIdFromToken);

        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (user == null) {
            returnObject.setMessage("User not found");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
        }

        Page<GetBillsDTO> billsPage;
        Timestamp fromTimestamp = null;
        Timestamp toTimestamp = null;

        if (fromDate != null && !fromDate.isEmpty()) {
            fromTimestamp = Timestamp.valueOf(fromDate + " 00:00:00");
        }

        if (toDate != null && !toDate.isEmpty()) {
            toTimestamp = Timestamp.valueOf(toDate + " 23:59:59");
        }
        if (user.getAccountTypeId() == ADMIN_TYPE) {

            if (selectedUserId != null && selectedUserId > 0) {

                billsPage = billRepository.findByFilters(
                        selectedUserId,
                        search,
                        billNumber,
                        fromTimestamp,
                        toTimestamp,
                        pageable
                );

            } else {

                billsPage = billRepository.findByFilters(
                        null,
                        search,
                        billNumber,
                        fromTimestamp,
                        toTimestamp,
                        pageable
                );
            }

        } else {

            billsPage = billRepository.findByFilters(
                    userIdFromToken,
                    search,
                    billNumber,
                    fromTimestamp,
                    toTimestamp,
                    pageable
            );
        }

        List<GetBillsDTO> bills = billsPage.getContent();
        for (GetBillsDTO bill : bills) {
            // get the total from finalTotalPrice if exists, otherwise use total
            // billTotal = finalTotalPrice if exists, otherwise total
            Double billTotal = (bill.getFinalTotalPrice() != null && bill.getFinalTotalPrice() > 0)
                    ? bill.getFinalTotalPrice()
                    : bill.getTotal();


            // sum of payments
            Double totalPaid = paymentRepository.getTotalPaidByBillId(bill.getId().longValue());

            if (totalPaid == null) {
                totalPaid = 0.0;
            }

            Double remaining = billTotal - totalPaid;

            // set values in DTO
            bill.setBillTotal(billTotal);
            bill.setRemainingAmount(remaining);
        }

        returnObject.setTotalPages(billsPage.getTotalPages());
        returnObject.setTotalCount(billsPage.getTotalElements());
        returnObject.setMessage(billsPage.getNumberOfElements() + " Bills Loaded Successfully");
        returnObject.setData(bills);
        returnObject.setStatus(true);

        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> getBillsForAdminByToken(String token, Integer selectedUserId, String search, Integer langId, Pageable pageable) {
        Integer userId = getUserIdFromToken(token); // retrieved from token normally
        User user = userRepository.findUserById(userId);
        ReturnObjectPaging returnObject = new ReturnObjectPaging();

        if (user == null) {
            returnObject.setMessage("User not found");
            returnObject.setStatus(false);
            returnObject.setData(null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(returnObject);
        }

        if (user.getAccountTypeId() == ADMIN_TYPE) {
            Page<GetBillsDTO> billsPage;

            // ✅ Handle filtering logic
            if (selectedUserId != null && selectedUserId > 0) {
                // Search by user ID
                billsPage = billRepository.findAllByUserIdAndLangId(selectedUserId, langId, pageable);
            } else if (search != null && !search.trim().isEmpty()) {
                // Search by customer name (case insensitive)
                billsPage = billRepository.findAllByCustomerNameContainingIgnoreCaseAndLangId(search.trim(), langId, pageable);
            } else {
                // Default: get all
                billsPage = billRepository.findAllByLangId(langId, pageable);
            }

            // ✅ Handle empty results
            if (billsPage.isEmpty()) {
                returnObject.setMessage("No Bills Found");
                returnObject.setStatus(false);
                returnObject.setData(new ArrayList<>());
                return ResponseEntity.status(HttpStatus.OK).body(returnObject);
            }

            // ✅ Build success response
            returnObject.setTotalPages(billsPage.getTotalPages());
            returnObject.setTotalCount(billsPage.getTotalElements());
            returnObject.setMessage(billsPage.getSize() + " Bills Loaded Successfully");
            returnObject.setData(billsPage.getContent());
            returnObject.setStatus(true);

            return ResponseEntity.ok(returnObject);

        } else {
            returnObject.setMessage("Unauthorized");
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

    @Transactional
    public ResponseEntity<?> createProductBillByAdmin(String token, String adminMacAddress, String mobileVersion, AddBillDTO addBillDTO) {
        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if (addBillDTO.getUserId() == null || addBillDTO.getUserId() == 0) {
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("No user found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
        }
        Optional<BillType> billTypeOptional = billTypeRepository.findBillTypeByCode(String.valueOf(addBillDTO.getBillTypeId()));
        if (user.getAccountTypeId() == ADMIN_TYPE) {

            Bill bill = new Bill();
            String prefix;

            BillType billType = billTypeRepository
                    .findBillTypeByCode("2")
                    .orElseThrow(() -> new RuntimeException("Bill type not found"));
            Integer billTypeId = billType.getId();

            if (addBillDTO.getBillTypeId().equals(billTypeId)) {
                prefix = "J";
            } else {
                prefix = "U";
            }

            Optional<Bill> lastBillOptional = billRepository
                    .findTopByReferenceNumberStartingWithOrderByReferenceNumberDesc(prefix + userId + "B");

            if (lastBillOptional.isPresent()) {

                String lastRef = lastBillOptional.get().getReferenceNumber(); // U44B77

                String numberPart = lastRef.substring(lastRef.indexOf("B") + 1); // 77

                int number = Integer.parseInt(numberPart) + 1;

                bill.setReferenceNumber(prefix + user.getId() + "B" + number);

            } else {

                bill.setReferenceNumber(prefix + user.getId() + "B1");

            }

            if (!addBillDTO.getProductBillDTOList().isEmpty()) {

                for (ProductBillDTO productBillDTO : addBillDTO.getProductBillDTOList()) {

                    ProductManufacturer manufacturer;

                    if (productBillDTO.getSellerBrandId() == null) {
                        // Logic: Fallback to the record with the least quantity
                        manufacturer = manufacturerRepository
                                .findFirstByProductIdOrderByQuantityAsc(productBillDTO.getId())
                                .orElse(null);
                    } else {
                        // Logic: Original specific seller brand lookup
                        manufacturer = manufacturerRepository
                                .findBySellerBrandIdAndProductId(productBillDTO.getSellerBrandId(), productBillDTO.getId())
                                .orElse(null);
                    }

                    if (manufacturer == null) {
                        returnObject.setStatus(false);
                        returnObject.setMessage("Manufacturer not found for this product");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                    }

                    Integer availableQty = manufacturer.getQuantity();
                    Integer requestedQty = productBillDTO.getQuantity();

                    if (availableQty < requestedQty) {
                        returnObject.setStatus(false);
                        returnObject.setMessage("Forbidden: Not enough quantity for product "
                                + productBillDTO.getProductName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                    }

                    bill.setStatusId(NOT_PAID);
                    bill.setBillTypeId(billTypeOptional.get().getId());
                    bill.setCreatedBy(ADMIN_TYPE);
                    bill.setAdminNotes(addBillDTO.getPrivateNotes());
                    bill.setCustomerNotes(addBillDTO.getNotes());
                    bill.setTotal(addBillDTO.getTotal());
                    bill.setDiscount(addBillDTO.getDiscount());
                    bill.setDiscountType(addBillDTO.getDiscountType());
                    bill.setFinalTotalPrice(addBillDTO.getFinalTotalPrice());
                    bill.setDownPayment(addBillDTO.getDownPayment());
                    bill.setDate(addBillDTO.getDate());
                    bill.setUserId(addBillDTO.getUserId());
                    bill = billRepository.save(bill);

                    // ✅ Take price from DB not frontend
                    productBillDTO.setPrice(manufacturer.getPrice());

                    // ✅ Reduce quantity
                    manufacturer.setQuantity(availableQty - requestedQty);
                    manufacturer = manufacturerRepository.save(manufacturer);

                    BillProduct billProduct = new BillProduct();
                    billProduct.setProductId((productBillDTO.getId()));
                    billProduct.setBillId(bill.getId());
                    billProduct.setPrice((productBillDTO.getPrice()));
                    billProduct.setQuantity((productBillDTO.getQuantity()));
                    billProduct.setName((productBillDTO.getProductName()));
                    billProduct.setProductManufacturerId(manufacturer.getId());
                    billProductRepository.save(billProduct);
                }
            }

            if (addBillDTO.getNotes() != null && !addBillDTO.getNotes().trim().isEmpty()) {
                BillNotes billNotes = new BillNotes();
                billNotes.setIsPrivate(false);
                billNotes.setBillId(bill.getId());
                billNotes.setMessage(addBillDTO.getNotes());
                billNotes.setCreatedBy(ADMIN_TYPE);
                billNotes.setCustomerMobileVersion(mobileVersion);
                billNotesRepository.save(billNotes);
            }
            if (addBillDTO.getPrivateNotes() != null && !addBillDTO.getPrivateNotes().trim().isEmpty()) {
                BillNotes billNotes = new BillNotes();
                billNotes.setIsPrivate(true);
                billNotes.setBillId(bill.getId());
                billNotes.setMessage(addBillDTO.getPrivateNotes());
                billNotes.setCreatedBy(ADMIN_TYPE);
                billNotes.setCustomerMobileVersion(mobileVersion);
                billNotesRepository.save(billNotes);
            }
            List<BillProduct> billProductList = new ArrayList<>();
            if (addBillDTO.getOtherProductsDTOList() != null) {
                for (int i = 0; i < addBillDTO.getOtherProductsDTOList().size(); i++) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setBillId(bill.getId());
                    billProduct.setName(addBillDTO.getOtherProductsDTOList().get(i).getProductName());
                    billProduct.setProductId(999999);
                    billProduct.setPrice(Double.valueOf(addBillDTO.getOtherProductsDTOList().get(i).getPrice()));
                    billProduct.setQuantity(addBillDTO.getOtherProductsDTOList().get(i).getQuantity());
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
            }

            // Notify the customer about the new bill
            notifyUserForNewBill(bill);

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

    private void notifyUserForNewBill(Bill bill) {
        Optional<User> userOptional = userRepository.findById(bill.getUserId());
        if (userOptional.isPresent()) {
            User customer = userOptional.get();
            String title = "New Bill : " + bill.getReferenceNumber();
            String body  = "A new bill has been created for you with reference: " + bill.getReferenceNumber();
            if (customer.getFirebaseToken() != null) {
                NotificationMessage notificationMessage = new NotificationMessage();
                notificationMessage.setTitle(title);
                notificationMessage.setBody(body);
                notificationMessage.setData(Map.of("message", body));
                notificationMessage.setRecToken(customer.getFirebaseToken());
                String result = firebaseMessagingService.sendNotification(notificationMessage);
                Notification notification = new Notification();
                notification.setUserId(customer.getId());
                notification.setTitle(title);
                notification.setText(body);
                notification.setIsSent(!"Failed".equals(result));
                notification.setNotificationType(BILL);
                notificationRepository.save(notification);
            }
        }
    }

    public boolean checkExistingInvoice(JobCard jobCard) {
        List<Bill> billFound = billRepository.findAllByReferenceNumber("Inv" + jobCard.getCode());
        return !billFound.isEmpty();
    }

    public void createJobCardBill(Integer userId, JobCard jobCard, String adminName) {
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
        bill.setDownPayment(jobCard.getDownPayment());
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
                billProduct.setDiscount(jobCardProduct.getDiscount());
                billProduct.setProductId(jobCardProduct.getProductId());
                billProduct.setBillId(bill.getId());
                billProduct.setCustomerApprovedAt(jobCardProduct.getCustomerApprovedAt());
                billProduct.setCreatedBy(jobCardProduct.getCreatedBy());
                billProduct.setCustomerMobileVersion(jobCardProduct.getCustomerMobileVersion());
                billProducts.add(billProduct);
            }
            billProductRepository.saveAll(billProducts);
        }
        notifyCustomerForJobCardBill(userId, jobCard.getCode(), adminName);
    }

    private void notifyCustomerForJobCardBill(Integer userId, String jobCardCode, String adminName) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String title = "New Bill Created : " + jobCardCode;
            String body  = "A new Job Card bill has been created for you by " + adminName;
            if (user.getFirebaseToken() != null) {
                NotificationMessage notificationMessage = new NotificationMessage();
                notificationMessage.setTitle(title);
                notificationMessage.setBody(body);
                notificationMessage.setData(Map.of("message", body));
                notificationMessage.setRecToken(user.getFirebaseToken());
                String result = firebaseMessagingService.sendNotification(notificationMessage);
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setTitle(title);
                notification.setText(body);
                notification.setIsSent(!"Failed".equals(result));
                notification.setNotificationType(NotificationTypes.BILL);
                notificationRepository.save(notification);
            }
        }
    }

    public ResponseEntity<?> addPayment(String token, Integer billId, UpdateBillStatusDTO dto) {

        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);

        ReturnObject returnObject = new ReturnObject();

        Optional<Bill> billOptional = billRepository.findById(billId);

        if (!billOptional.isPresent()) {
            returnObject.setStatus(false);
            returnObject.setMessage("Bill Not Found");
            return ResponseEntity.badRequest().body(returnObject);
        }

        Payment payment = new Payment();
        payment.setBillId(Long.valueOf(billId));
        payment.setAmount(dto.getAmount());
        payment.setPaymentType(dto.getPaymentFlag());

        paymentRepository.save(payment);

        returnObject.setStatus(true);
        returnObject.setMessage("Payment Added Successfully");
        returnObject.setData(payment);

        return ResponseEntity.ok(returnObject);
    }

    public ResponseEntity<?> updateBillStatus(String token, Integer billId, UpdateBillStatusDTO updateBillStatusDTO) {

        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();

        if (!user.getAccountTypeId().equals(ADMIN_TYPE)) {
            returnObject.setStatus(false);
            returnObject.setMessage("This user isn't Authorized to update bills");
            return ResponseEntity.badRequest().body(returnObject);
        }

        Optional<Bill> existingBillOptional = billRepository.findById(billId);

        if (!existingBillOptional.isPresent()) {
            returnObject.setStatus(false);
            returnObject.setMessage("No Bill Found");
            return ResponseEntity.badRequest().body(returnObject);
        }

        Bill bill = existingBillOptional.get();

        // add payment if request contains payment
        if (updateBillStatusDTO != null
                && updateBillStatusDTO.getPaymentFlag() != null
                && updateBillStatusDTO.getPaymentFlag().equals("P")) {

            addPayment(token, billId, updateBillStatusDTO.getAmount(), "P");
        }
        if (updateBillStatusDTO.getPaymentFlag().equals("F")) {
            // full payment -> calculate remaining

            Double billTotal = bill.getFinalTotalPrice();
            if (billTotal == 0.0) {
                billTotal = bill.getTotal();
            }
            Double totalPaid = paymentRepository.getTotalPaid(Long.valueOf(billId));

            if (totalPaid == null) {
                totalPaid = 0.0;
            }

            Double remainingAmount = billTotal - totalPaid;

            if (remainingAmount > 0) {
                addPayment(token, billId, remainingAmount, "F");
            }
        }
        // calculate total payments
        List<Payment> payments = paymentRepository.findByBillId(Long.valueOf(billId));

        double totalPaid = payments.stream()
                .mapToDouble(Payment::getAmount)
                .sum();

        double billTotal = bill.getFinalTotalPrice();
        if (billTotal == 0.0) {
            billTotal = bill.getTotal();
        }
        if (totalPaid == 0) {
            bill.setStatusId(NOT_PAID);
        } else if (totalPaid < billTotal) {
            bill.setStatusId(NOT_PAID);
        } else {
            bill.setStatusId(PAID);
        }

        billRepository.save(bill);

        notifyCustomerForBillPayment(bill, totalPaid, billTotal);

        returnObject.setData(bill);
        returnObject.setStatus(true);
        returnObject.setMessage("Updated Successfully");

        return ResponseEntity.ok(returnObject);
    }

    private void notifyCustomerForBillPayment(Bill bill, double totalPaid, double billTotal) {
        Optional<User> userOptional = userRepository.findById(bill.getUserId());
        if (userOptional.isPresent()) {
            User customer = userOptional.get();
            String title = "Payment Received : " + bill.getReferenceNumber();
            String body;
            if (totalPaid >= billTotal) {
                body = "Your bill " + bill.getReferenceNumber() + " has been fully paid.";
            } else {
                body = "A payment has been made on your bill " + bill.getReferenceNumber()
                        + ". Paid: " + totalPaid + " / " + billTotal + ".";
            }
            if (customer.getFirebaseToken() != null) {
                NotificationMessage notificationMessage = new NotificationMessage();
                notificationMessage.setTitle(title);
                notificationMessage.setBody(body);
                notificationMessage.setData(Map.of("message", body));
                notificationMessage.setRecToken(customer.getFirebaseToken());
                String result = firebaseMessagingService.sendNotification(notificationMessage);
                Notification notification = new Notification();
                notification.setUserId(customer.getId());
                notification.setTitle(title);
                notification.setText(body);
                notification.setIsSent(!"Failed".equals(result));
                notification.setNotificationType(NotificationTypes.BILL);
                notificationRepository.save(notification);
            }
        }
    }

    public Payment addPayment(String token, Integer billId, Double amount, String paymentType) {

        Payment payment = new Payment();

        payment.setBillId(Long.valueOf(billId));
        payment.setAmount(amount);
        payment.setPaymentType(paymentType);

        return paymentRepository.save(payment);
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
            existingBill.setDiscountType(addBillDTO.getDiscountType());
            existingBill.setFinalTotalPrice(addBillDTO.getFinalTotalPrice());
            existingBill.setDownPayment(addBillDTO.getDownPayment());
            existingBill.setDate(addBillDTO.getDate());
            billRepository.save(existingBill);

            if (addBillDTO.getPrivateNotes() != null &&
                    !addBillDTO.getPrivateNotes().isEmpty()) {
                BillNotes billNotes = new BillNotes();
                billNotes.setMessage(addBillDTO.getPrivateNotes());
                billNotes.setIsPrivate(true);
                billNotes.setCreatedBy(userId);
                billNotes.setBillId(existingBill.getId());
                billNotesRepository.save(billNotes);
            }
            if (addBillDTO.getNotes() != null &&
                    !addBillDTO.getNotes().isEmpty()) {
                BillNotes billNotes = new BillNotes();
                billNotes.setMessage(addBillDTO.getNotes());
                billNotes.setIsPrivate(false);
                billNotes.setCreatedBy(userId);
                billNotes.setBillId(existingBill.getId());
                billNotesRepository.save(billNotes);
            }

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
                System.out.println("ProductQuantity : " + addBillDTO.getOtherProductsDTOList().get(i).getQuantity());
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

    @Transactional
    public ResponseEntity<?> createBill(String token, AddBillDTO addBillDTO) {
        Integer userId = getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        ReturnObject returnObject = new ReturnObject();
        if (user != null) {
            Bill bill = new Bill();
            String prefix;

            BillType billType = billTypeRepository
                    .findBillTypeByCode("2")
                    .orElseThrow(() -> new RuntimeException("Bill type not found"));

            Integer billTypeId = billType.getId();

            if (addBillDTO.getBillTypeId().equals(billTypeId.toString())) {
                prefix = "J";
            } else {
                prefix = "U";
            }

            Optional<Bill> lastBillOptional = billRepository
                    .findTopByReferenceNumberStartingWithOrderByReferenceNumberDesc(prefix + userId + "B");

            if (lastBillOptional.isPresent()) {

                String lastRef = lastBillOptional.get().getReferenceNumber(); // U44B77

                String numberPart = lastRef.substring(lastRef.indexOf("B") + 1); // 77

                int number = Integer.parseInt(numberPart) + 1;

                bill.setReferenceNumber(prefix + userId + "B" + number);

            } else {

                bill.setReferenceNumber(prefix + userId + "B1");

            }

            if (!addBillDTO.getProductBillDTOList().isEmpty()) {

                for (ProductBillDTO productBillDTO : addBillDTO.getProductBillDTOList()) {

                    ProductManufacturer manufacturer;

                    if (productBillDTO.getSellerBrandId() == null) {
                        // Logic: Fallback to the record with the least quantity
                        manufacturer = manufacturerRepository
                                .findFirstByProductIdOrderByQuantityAsc(productBillDTO.getId())
                                .orElse(null);
                    } else {
                        // Logic: Original specific seller brand lookup
                        manufacturer = manufacturerRepository
                                .findBySellerBrandIdAndProductId(productBillDTO.getSellerBrandId(), productBillDTO.getId())
                                .orElse(null);
                    }

                    if (manufacturer == null) {
                        returnObject.setStatus(false);
                        returnObject.setMessage("Manufacturer not found for this product");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
                    }

                    Integer availableQty = manufacturer.getQuantity();
                    Integer requestedQty = productBillDTO.getQuantity();

                    if (availableQty < requestedQty) {
                        returnObject.setStatus(false);
                        returnObject.setMessage("Forbidden: Not enough quantity for product "
                                + productBillDTO.getProductName());
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
                    }

                    bill.setStatusId(NOT_PAID);
                    bill.setBillTypeId(billTypeId);
                    bill.setCreatedBy(ADMIN_TYPE);
                    bill.setAdminNotes(addBillDTO.getPrivateNotes());
                    bill.setCustomerNotes(addBillDTO.getNotes());
                    bill.setTotal(addBillDTO.getTotal());
                    bill.setDiscount(addBillDTO.getDiscount());
                    bill.setDiscountType(addBillDTO.getDiscountType());
                    bill.setFinalTotalPrice(addBillDTO.getFinalTotalPrice());
                    bill.setDownPayment(addBillDTO.getDownPayment());
                    bill.setDate(addBillDTO.getDate());
                    bill.setUserId(addBillDTO.getUserId());
                    bill = billRepository.save(bill);

                    // ✅ Take price from DB not frontend
                    productBillDTO.setPrice(manufacturer.getPrice());

                    // ✅ Reduce quantity
                    manufacturer.setQuantity(availableQty - requestedQty);
                    manufacturer = manufacturerRepository.save(manufacturer);

                    BillProduct billProduct = new BillProduct();
                    billProduct.setProductId((productBillDTO.getId()));
                    billProduct.setBillId(bill.getId());
                    billProduct.setPrice((productBillDTO.getPrice()));
                    billProduct.setQuantity((productBillDTO.getQuantity()));
                    billProduct.setName((productBillDTO.getProductName()));
                    billProduct.setProductManufacturerId(manufacturer.getId());
                    billProductRepository.save(billProduct);
                }
            }
            List<Bill> allBillsByReferenceNumber = billRepository.findAllByReferenceNumber(bill.getReferenceNumber());
            if (!allBillsByReferenceNumber.isEmpty()) {
                returnObject.setData(null);
                returnObject.setStatus(false);
                returnObject.setMessage("Bill with same reference Number Already Created");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(returnObject);
            }
            bill.setStatusId(NOT_PAID);
            Optional<BillType> billTypeOptional = billTypeRepository.findBillTypeByCode(String.valueOf(addBillDTO.getBillTypeId()));
            if (billTypeOptional.isPresent()) {
                bill.setBillTypeId(billTypeOptional.get().getId());
            }
            bill.setCreatedBy(user.getAccountTypeId());
            bill.setAdminNotes(addBillDTO.getPrivateNotes());
            bill.setCustomerNotes(addBillDTO.getNotes());
            bill.setTotal(addBillDTO.getTotal());
            bill.setDiscount(addBillDTO.getDiscount());
            bill.setDiscountType(addBillDTO.getDiscountType());
            bill.setFinalTotalPrice(addBillDTO.getFinalTotalPrice());
            bill.setDownPayment(addBillDTO.getDownPayment());
            bill.setDate(addBillDTO.getDate());
            bill.setUserId(userId);
            bill = billRepository.save(bill);
            List<BillProduct> billProductList = new ArrayList<>();
            if (!addBillDTO.getProductBillDTOList().isEmpty()) {
                for (ProductBillDTO productBillDTO : addBillDTO.getProductBillDTOList()) {
                    BillProduct billProduct = new BillProduct();
                    billProduct.setProductId(productBillDTO.getId());
                    billProduct.setBillId(bill.getId());
                    billProduct.setPrice(productBillDTO.getPrice()); // from DB
                    billProduct.setQuantity(productBillDTO.getQuantity());
                    billProduct.setName(productBillDTO.getProductName());
                    billProduct.setDiscount(productBillDTO.getDiscount());

                    billProductList.add(billProduct);
                }
                billProductRepository.saveAll(billProductList);
            }
            if (!billType.getCode().equals("2")) {
                if (!addBillDTO.getOtherProductsDTOList().isEmpty()) {
                    for (OtherProductDTO productDTO : addBillDTO.getOtherProductsDTOList()) {
                        BillProduct billProduct = new BillProduct();
                        billProduct.setBillId(bill.getId());
                        billProduct.setProductId(999999);
                        billProduct.setPrice(Double.valueOf(productDTO.getPrice()));
                        billProduct.setName(productDTO.getProductName());
                        billProduct.setQuantity(productDTO.getQuantity());
                        billProduct.setDiscount(productDTO.getDiscount());
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
        return userService.getUserIdFromToken(token);
    }

    public ResponseEntity getBillsForAdminByTokenForUserId(String token, int langId, Integer userId, Pageable
            pageable) {
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
                    if (!addJobCardNotes.getNotes().isEmpty()) {
                        if (addJobCardNotes.isForCustomer())
                            bill.setCustomerNotes(addJobCardNotes.getNotes());
                        else
                            bill.setAdminNotes(addJobCardNotes.getNotes());
                    }
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
                for (BillProduct billProduct : billProducts) {
                    if (billProduct.getProductId() != null) {
                        ProductBillDTO productBillDTO = new ProductBillDTO(billProduct);

                        // 1️⃣ Fetch product info
                        Optional<Product> productOptional = productRepository.findById(billProduct.getProductId());
                        if (productOptional.isPresent()) {
                            Product product = productOptional.get();
                            List<ProductImages> productImages = productImagesRepository.findAllByProductId(product.getId());
                            productBillDTO.setProductName(product.getCode());
                            if (!productImages.isEmpty()) {
                                productBillDTO.setImageUrl(productImages.get(0).getUrl());
                            }
                        }
                        // 2️⃣ Fetch sellerBrandId from manufacturer
                        if (billProduct.getProductManufacturerId() != null) {
                            Optional<ProductManufacturer> manufacturerOptional =
                                    manufacturerRepository.findById(billProduct.getProductManufacturerId());
                            manufacturerOptional.ifPresent(manufacturer ->
                                    productBillDTO.setSellerBrandId(manufacturer.getSellerBrandId())
                            );
                        }
                        billProductList.add(productBillDTO);
                    }
                 else {
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
            addBillDTO.setUserId(bill.getUserId());
            User user = userRepository.findUserById(bill.getUserId());
            if (user != null) {
                addBillDTO.setUserName(user.getName());
            } else {
                addBillDTO.setUserName("");
            }
            addBillDTO.setDownPayment(bill.getDownPayment());
            if (bill.getBillTypeId() != null) {
                Optional<BillType> billTypeOptional = billTypeRepository.findBillTypeById(bill.getBillTypeId());
                if (billTypeOptional.isPresent())
                    addBillDTO.setBillTypeId(billTypeOptional.get().getCode());
            }
            addBillDTO.setDiscount(bill.getDiscount());
            addBillDTO.setDiscountType(bill.getDiscountType());
            addBillDTO.setFinalTotalPrice(bill.getFinalTotalPrice());
            addBillDTO.setDate(bill.getDate());
            addBillDTO.setTotal(bill.getTotal());
            addBillDTO.setPrivateNotes(bill.getAdminNotes());
            addBillDTO.setNotes(bill.getCustomerNotes());
            addBillDTO.setReferenceNumber(bill.getReferenceNumber());
            addBillDTO.setDate(String.valueOf(bill.getCreatedAt()));
            if (bill.getCarId() != null) {
                Optional<Car> carOptional = carRepository.findById(bill.getCarId());
                if (carOptional.isPresent()) {
                    Car car = carOptional.get();
                    addBillDTO.setCarData(car.getLicenseNumber() + " , " + car.getPlateNumber() + " , " + car.getCreationYear());
                }
            }
        }
        returnObject.setData(addBillDTO);
        returnObject.setMessage("Loaded Successfully");
        returnObject.setStatus(true);
        return ResponseEntity.ok(returnObject);
    }
}
