package com.gsc.gsc.bill.pdf;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.job_cards.service.serviceImplementation.JobCardService;
import com.gsc.gsc.model.*;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.serviceImplementation.UserService;
import com.gsc.gsc.utilities.FontLoader;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.StreamUtil;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.BaseDirection;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;
import com.itextpdf.text.pdf.languages.LanguageProcessor;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.gsc.gsc.bill.BillConstants.NOT_PAID;
import static com.gsc.gsc.bill.BillConstants.PAID;
import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;
import static com.gsc.gsc.job_cards.JobCardConstants.*;

@Service
public class BillPdfGeneratorITextService {

    // ===================== DEPENDENCIES =====================

    @Autowired JobCardService jobCardService;
    @Autowired JobCardRepository jobCardRepository;
    @Autowired UserRepository userRepository;
    @Autowired UserService userService;
    @Autowired private CarRepository carRepository;
    @Autowired private ModelRepository modelRepository;
    @Autowired private BrandRepository brandRepository;
    @Autowired private JobCardNotesRepository jobCardNotesRepository;
    @Autowired private JobCardImagesRepository jobCardImagesRepository;
    @Autowired private JobCardProductRepository jobCardProductRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private BillRepository billRepository;
    @Autowired private BillProductRepository billProductRepository;
    @Autowired private BillNotesRepository billNotesRepository;
    @Autowired private BillTypeRepository billTypeRepository;

    // ===================== CONSTANTS =====================

    private static final Map<Integer, String> ACCOUNT_TYPE_MAP = Map.of(1, "User", 2, "Business", 3, "Admin");
    private static final Color HEADER_BG_COLOR = new DeviceRgb(230, 230, 230);

    // ===================== MAIN ENTRY POINT =====================

    public ReturnObject exportIText2(String token, int billId, String macAddress, Boolean includePrivateNotes) throws IOException {
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (includePrivateNotes == null) includePrivateNotes = false;

        String filePath = "/var/www/bills/BillPdf/Inv_" + billId + ".pdf";
        Document document = initializeDocument(filePath);
        ReturnObject returnObject = new ReturnObject();

        Optional<Bill> billOptional = billRepository.findById(billId);
        if (billOptional.isPresent()) {
            Bill bill = billOptional.get();
            User userCreator = userRepository.findUserById(bill.getUserId());
            String referenceNumber = cleanReferenceNumber(bill);
            Optional<JobCard> jobCardOptional = jobCardRepository.findByCode(referenceNumber);

            String[] customerInfo = resolveCustomerInfo(user);   // [0]=userName [1]=address [2]=phone
            String[] carInfo = resolveCarInfo(bill);             // [0]=carMake  [1]=carModel [2]=carKilos

            PdfFont arabicFont = createArabicFontForPdf();

            String billTypeName = "";
            if (bill.getBillTypeId() != null) {
                Optional<BillType> billTypeOptional = billTypeRepository.findById(bill.getBillTypeId());
                if (billTypeOptional.isPresent()) {
                    BillType billType = billTypeOptional.get();
                    billTypeName = billType.getNameEn() != null ? billType.getNameEn() : "";
                    if (billType.getNameAr() != null && !billType.getNameAr().isEmpty()) {
                        billTypeName += " / " + billType.getNameAr();
                    }
                }
            }

            addInvoiceHeader(document, bill, customerInfo[0], customerInfo[1], carInfo[0], customerInfo[2], userCreator, carInfo[1], billTypeName);

            List<BillNotes> billNotes = billNotesRepository.findAllByBillId(billId);
            if (!billNotes.isEmpty()) {
                document.add(buildNotesTable(billNotes, user, customerInfo[0], user.getAccountTypeId(), arabicFont, includePrivateNotes));
            }

            document.add(new Paragraph(" ").setFontSize(12f));
            Optional<List<BillProduct>> optionalBillProductList = billProductRepository.findAllByBillId(billId);
            document.add(buildProductsTable(optionalBillProductList, bill, user, customerInfo[0], arabicFont));

            addJobCardImages(document, jobCardOptional);

            document.close();
            returnObject.setData(filePath);
            returnObject.setStatus(true);
            returnObject.setMessage("Loaded Successfully");
        } else {
            document.close();
            returnObject.setData(null);
            returnObject.setStatus(false);
            returnObject.setMessage("No Job Card found");
        }
        return returnObject;
    }

    // ===================== DOCUMENT INIT =====================

    private Document initializeDocument(String filePath) throws IOException {
        File file = new File(filePath);
        System.out.println("created File : " + filePath);
        try {
            boolean isFileCreated = file.createNewFile();
            if (isFileCreated) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists or an error occurred.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs()) {
                System.out.println("Directories created successfully.");
            } else {
                System.out.println("Failed to create directories. Check directory permissions.");
                throw new IOException("Failed to create required directories.");
            }
        }

        if (file.exists()) {
            System.out.println("File already exists. Checking write permissions...");
            if (!file.canWrite()) throw new IOException("File exists but is not writable.");
        } else {
            if (!file.createNewFile()) throw new IOException("Failed to create the PDF file.");
        }

        PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(file));
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        return new Document(pdfDocument);
    }

    // ===================== DATA RESOLVERS =====================

    private String cleanReferenceNumber(Bill bill) {
        String referenceNumber = bill.getReferenceNumber();
        if (referenceNumber != null) {
            referenceNumber = referenceNumber.replace("Inv", "");
            bill.setReferenceNumber(referenceNumber);
        }
        return referenceNumber;
    }

    private String[] resolveCustomerInfo(User user) {
        String userName = "...........";
        String address = "...........";
        String phone = "...........";
        if (user.getName() != null) userName = user.getName();
        if (user.getAddress() != null) address = user.getAddress();
        if (user.getPhone() != null) phone = user.getPhone();
        return new String[]{userName, address, phone};
    }

    private String[] resolveCarInfo(Bill bill) {
        String carMake = "...........";
        StringBuilder carModel = new StringBuilder("...........");
        String carKilosCovered = "...........";

        if (bill.getCarId() != null) {
            Optional<Car> carOptional = carRepository.findById(bill.getCarId());
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                carMake = car.getLicenseNumber();
                if (car.getModelId() != null) {
                    Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                    if (modelOptional.isPresent()) {
                        Model model = modelOptional.get();
                        carModel.setLength(0);
                        carModel.append(model.getCode()).append("/").append(model.getCreationYear());
                        if (model.getBrandId() != null) {
                            Optional<Brand> brandOptional = brandRepository.findById(model.getBrandId());
                            if (brandOptional.isPresent()) {
                                Brand brand = new Brand();
                                if (brand.getCode() != null) carModel.append("/").append(brand.getCode());
                                if (brand.getNameEn() != null) carModel.append("/").append(brand.getNameEn());
                            }
                        }
                    }
                }
                carKilosCovered = car.getCoveredKilos();
            }
        }
        return new String[]{carMake, carModel.toString(), carKilosCovered};
    }

    private String resolveProductName(BillProduct billProduct) {
        if (billProduct.getProductId() != null && billProduct.getName() == null) {
            Optional<Product> productOptional = productRepository.findById(billProduct.getProductId());
            if (productOptional.isPresent()) return productOptional.get().getCode();
        }
        return billProduct.getName() != null ? billProduct.getName() : "Product";
    }

    private String[] resolveCreatedByInfo(BillProduct billProduct, User user, String userName) {
        String createdBy = "Admin";
        String approvedByCustomerAt = "Not Yet";
        if (user != null && billProduct.getCreatedBy() != null) {
            if (billProduct.getCreatedBy().equals(user.getId())) {
                createdBy = userName;
                approvedByCustomerAt = "Customer Added it";
            } else if (billProduct.getCustomerApprovedAt() != null) {
                approvedByCustomerAt = billProduct.getCustomerApprovedAt().toString();
            }
        }
        return new String[]{createdBy, approvedByCustomerAt};
    }

    // ===================== TABLE BUILDERS =====================

    private Table buildNotesTable(List<BillNotes> billNotes, User user, String userName,
                                  Integer accountType, PdfFont arabicFont, boolean includePrivateNotes) {
        Table notesTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});
        notesTable.setWidthPercent(100);
        notesTable.addCell(new Cell().add("Message").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        notesTable.addCell(new Cell().add("Created By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        notesTable.addCell(new Cell().add("Customer Model Number").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        notesTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        notesTable.addCell(new Cell().add("Created At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));

        for (BillNotes note : billNotes) {
            Integer createdById = note.getCreatedBy();
            User userCreatedNote = userRepository.findUserById(createdById);
            Integer createdNoteAccountType = userCreatedNote.getAccountTypeId();
            System.out.println("Note : " + note.getId());
            if (!note.getIsPrivate()) {
                System.out.println("Note Not Private : " + note.getIsPrivate());
                addNotes(user, userName, notesTable, arabicFont, note, createdNoteAccountType);
            } else if (accountType.equals(ADMIN_TYPE) && includePrivateNotes) {
                System.out.println("Note is Private : " + note.getIsPrivate());
                addNotes(user, userName, notesTable, arabicFont, note, createdNoteAccountType);
            }
        }
        return notesTable;
    }

    private Table buildProductsTable(Optional<List<BillProduct>> optionalBillProductList,
                                     Bill bill, User user, String userName, PdfFont arabicFont) {
        if (!optionalBillProductList.isPresent() || optionalBillProductList.get().isEmpty()) {
            return buildEmptyProductsTable(bill);
        }

        List<BillProduct> billProductList = optionalBillProductList.get();
        Table productsTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});
        BigDecimal totalPrice = BigDecimal.ZERO;

        productsTable.addCell(new Cell().add("Product").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("QTY").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("PRICE").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("Added By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));

        for (BillProduct billProduct : billProductList) {
            String productName = resolveProductName(billProduct);
            String productPrice = "0";
            String productQuantity = "0";

            if (billProduct.getPrice() != null) {
                productPrice = String.valueOf(billProduct.getPrice());
                try {
                    totalPrice = totalPrice.add(new BigDecimal(productPrice).multiply(new BigDecimal(billProduct.getQuantity())));
                } catch (Exception e) {
                    System.out.println("Error calculating total: " + e.getMessage());
                }
            }
            if (billProduct.getQuantity() != null) productQuantity = String.valueOf(billProduct.getQuantity());

            String[] createdByInfo = resolveCreatedByInfo(billProduct, user, userName);
            billProduct.setName(productName);
            addProductWithLanguage(productsTable, arabicFont, billProduct, createdByInfo[0], createdByInfo[1], productQuantity, productPrice);
        }

        addSummaryRows(productsTable, bill);
        return productsTable;
    }

    private Table buildEmptyProductsTable(Bill bill) {
        // Use 5 columns to match addSummaryRows so boxes sit on the right side
        Table productsTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});

        // Discount: show "-" if null or zero; otherwise value + "%"
        boolean hasDiscount = bill.getDiscount() != null && bill.getDiscount() != 0.0;
        String discountDisplay = hasDiscount ? bill.getDiscount() + "%" : "-";

        // [empty x3] [Discount label] [value]
        productsTable.addCell(new Cell(1, 3).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell().add(new Paragraph("Discount").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(discountDisplay))
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // [empty x3] [Total label] [value]
        productsTable.addCell(new Cell(1, 3).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell().add(new Paragraph("Total").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(bill.getTotal().toString()).setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        return productsTable;
    }

    private void addSummaryRows(Table productsTable, Bill bill) {
        // Table has 5 columns: we push Discount and Total to the right by leaving 3 cols empty on the left

        // Discount row — show "-" if null or zero
        boolean hasDiscount = bill.getDiscount() != null && bill.getDiscount() != 0.0;
        String discountText = "-";
        String discountTypeText = "-";
        if (hasDiscount) {
            String discountType = bill.getDiscountType();
            Double discountValueBD = bill.getDiscount();
            if (discountType == null || discountType.trim().isEmpty() || discountType.equals("P")) {
                discountText = discountValueBD + "%";
                discountTypeText = "Percentage";
            } else if (discountType.equals("V")) {
                discountText = discountValueBD.toString();
                discountTypeText = "Value";
            }
        }
        // Row: [empty x3] [Discount label] [value]
        productsTable.addCell(new Cell(1, 3).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell().add(new Paragraph("Discount (" + discountTypeText + ")").setBold())
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(discountText))
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Down payment row — [empty x3] [label] [value]
        String downPayment = bill.getDownPayment() != null ? bill.getDownPayment().toString() : "0";
        productsTable.addCell(new Cell(1, 3).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell().add(new Paragraph("Down Payment").setBold())
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(downPayment))
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Total row — [empty x3] [label] [value]  — both on same line, right side
        productsTable.addCell(new Cell(1, 3).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell().add(new Paragraph("Total").setBold())
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(BigDecimal.valueOf(bill.getFinalTotalPrice()).toString()).setBold())
                .setTextAlignment(TextAlignment.RIGHT).setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
    }

    private void addJobCardImages(Document document, Optional<JobCard> jobCardOptional) throws IOException {
        if (!jobCardOptional.isPresent()) return;

        List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardOptional.get().getId());
        float maxWidth = 200f;
        float maxHeight = 200f;

        if (!jobCardImages.isEmpty()) {
            document.add(new Paragraph("Job Card Images:").setFontSize(12f).setBold());
            for (JobCardImages jobCardImage : jobCardImages) {
                try {
                    URL url = new URL(jobCardImage.getUrl());
                    System.out.println("Url : " + url);
                    InputStream inputStream = url.openStream();
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    if (bufferedImage == null) throw new IOException("Image format not recognized");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    String format = String.valueOf(url).toLowerCase().contains("png") ? "png" : "png";
                    ImageIO.write(bufferedImage, format, baos);
                    byte[] imageBytes = baos.toByteArray();

                    Image image = new Image(ImageDataFactory.create(imageBytes)).setAutoScale(true);
                    float originalWidth = image.getImageWidth();
                    float originalHeight = image.getImageHeight();

                    if (originalWidth > maxWidth || originalHeight > maxHeight) {
                        float widthRatio = maxWidth / originalWidth;
                        float heightRatio = maxHeight / originalHeight;
                        float scalingFactor = Math.min(widthRatio, heightRatio);
                        image.scaleToFit(originalWidth * scalingFactor, originalHeight * scalingFactor);
                    }
                    document.add(image);
                    document.add(new Paragraph("\n")).setWidth(200f).setHeight(200f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ===================== PDF ELEMENT HELPERS =====================

    public PdfFont createArabicFontForPdf() {
        try {
            Font font = FontLoader.loadArabicFont();
            InputStream fontStream = FontLoader.class.getResourceAsStream("/font/NotoNaskhArabic-Regular.ttf");
            PdfFont arabicFont = PdfFontFactory.createFont(IOUtils.toByteArray(fontStream), PdfEncodings.IDENTITY_H, true);
            System.out.println("Arabic font successfully created for PDF");
            return arabicFont;
        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Arabic font for PDF: " + e.getMessage());
        }
    }

    Cell createCell(String text, boolean bold) {
        Paragraph p = new Paragraph(text);
        if (bold) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER);
    }

    private void addInvoiceHeader(Document document, Bill bill, String userName,
                                  String address, String carMake, String phone,
                                  User user, String carModel, String billTypeName) throws IOException {
        InputStream logoStream = getClass().getResourceAsStream("/image/logo2.png");
        if (logoStream == null) throw new IOException("Logo image not found in resources!");

        ImageData imageData = ImageDataFactory.create(StreamUtil.inputStreamToArray(logoStream));
        Image logo = new Image(imageData).scaleToFit(60, 60);

        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 3})).useAllAvailableWidth();
        headerTable.addCell(new Cell().add(logo).setBorder(Border.NO_BORDER).setVerticalAlignment(VerticalAlignment.MIDDLE));

        PdfFont arabicFont = createArabicFontForPdf();
        Paragraph companyName = new Paragraph("German Service Center").setFontSize(16f).setBold().setTextAlignment(TextAlignment.CENTER);
        Paragraph contactInfo = new Paragraph(
                "Tel (Dir): +974 44662202 - Tel: +974 44665445\n" +
                "Fax: +974 44664544 - M: +974 33513370\n" +
                "P.O.Box: 203144 Doha-Qatar - E: info@gsc-qatar.com\n" +
                "www.gsc-qatar.com"
        ).setFontSize(10f).setTextAlignment(TextAlignment.CENTER);

        LanguageProcessor al = new ArabicLigaturizer();
        Paragraph arabicTitle = new Paragraph(al.process("قطع غيار"))
                .setFont(arabicFont).setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setPadding(5)
                .setBorder(new SolidBorder(1));

        Cell rightCell = new Cell()
                .add(companyName).add(contactInfo)
                .add(new Paragraph(" "))
                .add(arabicTitle)
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);
        headerTable.addCell(rightCell);
        document.add(headerTable);
        document.add(new Paragraph(" "));

        float[] customerColWidths = {2f, 3f, 2f, 3f};
        Table customerTable = new Table(customerColWidths);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        customerTable.addCell(createCell("No.", true));
        customerTable.addCell(createCell(String.valueOf(bill.getId()), false));
        customerTable.addCell(createCell("Date:", true));
        customerTable.addCell(createCell(bill.getCreatedAt() != null ? bill.getCreatedAt().toString() : "", false));

        customerTable.addCell(createCell("Name", true));
        customerTable.addCell(createCell(userName, false));
        customerTable.addCell(createCell("Reg. No.", true));
        customerTable.addCell(createCell(carMake, false));

        customerTable.addCell(createCell("Mobile", true));
        customerTable.addCell(createCell(phone, false));
        customerTable.addCell(createCell("Car Type", true));
        customerTable.addCell(createCell(carMake, false));

        customerTable.addCell(createCell("Category", true));
        String accountTypeName = user.getAccountTypeId() != null ? ACCOUNT_TYPE_MAP.getOrDefault(user.getAccountTypeId(), "") : "";
        customerTable.addCell(createCell(accountTypeName, false));
        customerTable.addCell(createCell("Car Model", true));
        customerTable.addCell(createCell(carModel, false));

        customerTable.addCell(createCell("Address", true));
        customerTable.addCell(createCell(address, false));
        customerTable.addCell(createCell("Year Model", true));
        customerTable.addCell(createCell("...........", false));

        customerTable.addCell(createCell("Bill Type", true));
        customerTable.addCell(createCell(billTypeName.isEmpty() ? "-" : billTypeName, false));
        customerTable.addCell(createCell("", false));
        customerTable.addCell(createCell("", false));

        document.add(customerTable);
    }

    private void addNotes(User user, String userName, Table notesTable, PdfFont arabicFont, BillNotes note, Integer accountType) {
        String message = note.getMessage();
        if (message.matches(".*\\p{InArabic}.*")) {
            try {
                LanguageProcessor al = new ArabicLigaturizer();
                Cell cell = new Cell()
                        .add(new Paragraph(al.process(message)).setPadding(5))
                        .setFont(arabicFont).setFontSize(12f)
                        .setBorder(new SolidBorder(1))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setPaddingLeft(5).setPaddingRight(5).setPaddingTop(5).setPaddingBottom(5);
                notesTable.addCell(cell);
            } catch (Exception exception) {
                System.out.println("Exception : " + exception.getMessage());
            }
        } else {
            notesTable.addCell(new Cell().add(message).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        }

        System.out.println("Note : " + note.getCustomerMobileVersion());
        System.out.println("Note : " + note.getMessage());

        String createdBy = "Admin";
        String approvedByCustomerAt = "Not Yet";
        String approvedByCustomersDevice = "Not Yet";
        if (user != null) {
            if (accountType.equals(USER_TYPE)) {
                createdBy = userName;
                approvedByCustomerAt = "Created By Customer : " + note.getCustomerMobileVersion();
                approvedByCustomersDevice = "Created By Customer : " + note.getCustomerMobileVersion();
            } else {
                if (note.getApprovedByCustomerAt() != null) {
                    approvedByCustomerAt = note.getApprovedByCustomerAt().toString();
                    approvedByCustomersDevice = note.getCustomerMobileVersion();
                }
            }
        }

        notesTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(approvedByCustomersDevice).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(note.getCreatedAt().toString()).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
    }

    private void addProductWithLanguage(Table productsTable, PdfFont arabicFont, BillProduct product,
                                        String createdBy, String approvedByCustomerAt,
                                        String productQuantity, String productPrice) {
        String productName = product.getName();
        if (productName.matches(".*\\p{InArabic}.*")) {
            try {
                LanguageProcessor al = new ArabicLigaturizer();
                Cell cell = new Cell()
                        .add(new Paragraph(al.process(productName)).setPadding(5))
                        .setFont(arabicFont).setFontSize(12f)
                        .setBorder(new SolidBorder(1))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setPaddingLeft(5).setPaddingRight(5).setPaddingTop(5).setPaddingBottom(5);
                productsTable.addCell(cell);
            } catch (Exception exception) {
                System.out.println("Exception : " + exception.getMessage());
            }
        } else {
            productsTable.addCell(new Cell().add(productName).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        }
        productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));
    }
}
