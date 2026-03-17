package com.gsc.gsc.job_cards.pdf;

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
import com.itextpdf.layout.property.*;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;
import static com.gsc.gsc.job_cards.JobCardConstants.*;

@Service
public class JobCardPdfGeneratorITextService {
    @Autowired
    JobCardService jobCardService;
    @Autowired
    JobCardRepository jobCardRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserService userService;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private ModelRepository modelRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private JobCardNotesRepository jobCardNotesRepository;
    @Autowired
    private JobCardImagesRepository jobCardImagesRepository;
    @Autowired
    private JobCardProductRepository jobCardProductRepository;
    @Autowired
    private ProductRepository productRepository;

    private static final Map<Integer, String> ACCOUNT_TYPE_MAP = Map.of(
            1, "User",
            2, "Business",
            3, "Admin"
    );

    public PdfFont createArabicFontForPdf() {
        try {
            // Load the Arabic font using the FontLoader utility class
            Font font = FontLoader.loadArabicFont();

            // Create a PdfFont from the loaded AWT font
            // Use the font stream directly to create a PdfFont if needed
            InputStream fontStream = FontLoader.class.getResourceAsStream("/font/NotoNaskhArabic-Regular.ttf");

            // Create the PdfFont using PdfFontFactory
            PdfFont arabicFont = PdfFontFactory.createFont(IOUtils.toByteArray(fontStream), PdfEncodings.IDENTITY_H, true);

            System.out.println("Arabic font successfully created for PDF");
            return arabicFont;

        } catch (IOException | FontFormatException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create Arabic font for PDF: " + e.getMessage());
        }
    }

    public void createFile(String filePath) {

        File file = new File(filePath);
        System.out.println("created File : " + filePath);
        try {
            // File.createNewFile() Method Used
            boolean isFileCreated = file.createNewFile();
            if (isFileCreated) {
                System.out.println("File created successfully.");
            } else {
                System.out.println("File already exists or an error occurred.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ReturnObject exportIText2(String token, int jobCardId, String macAddress,Boolean includePrivateNotes) throws IOException {
        Integer userId = userService.getUserIdFromToken(token) ;
        User user = userRepository.findUserById(userId);
        Integer accountType = user.getAccountTypeId();

        if(includePrivateNotes == null){
            includePrivateNotes = false;
        }

        String filePath = "/var/www/jobcards/JobCardPdf/jobCard_" + jobCardId + ".pdf";
        File file = new File(filePath);
        createFile(filePath);
//        createDummy();
        // Ensure the parent directory exists
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
            if (!file.canWrite()) {
                throw new IOException("File exists but is not writable.");
            }
        } else {
            if (!file.createNewFile()) {
                throw new IOException("Failed to create the PDF file.");
            }
        }

        PdfWriter pdfWriter = new PdfWriter(new FileOutputStream(file));
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        pdfDocument.setDefaultPageSize(PageSize.A4);
        Document document = new Document(pdfDocument);

        ReturnObject returnObject = new ReturnObject();

        Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
        if (jobCardOptional.isPresent()) {
            JobCard jobCard = jobCardOptional.get();
            String userName = "...........";
            String address = "...........";
            String phone = "...........";
            String carMake = "...........";
            String downPayment = "0";
            if(jobCard.getDownPayment() != null){
                downPayment = jobCard.getDownPayment().toString();
            }
            StringBuilder carModel = new StringBuilder("...........");
            String carKilosCovered = "...........";
            User userCreator = userRepository.findUserById(jobCard.getUserId());
            if (userCreator.getName() != null)
                userName = userCreator.getName();
            if (userCreator.getAddress() != null)
                address = userCreator.getAddress();
            if (userCreator.getPhone() != null)
                phone = userCreator.getPhone();
            Optional<Car> carOptional = carRepository.findById(jobCard.getCarId());
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
                                if (brand.getCode() != null) {
                                    carModel.append("/").append(brand.getCode());
                                }
                                if (brand.getNameEn() != null) {
                                    carModel.append("/").append(brand.getNameEn());
                                }
                            }
                        }
                    }
                }
                carKilosCovered = car.getCoveredKilos();
            }

            List<JobCardNotes> jobCardNotes = jobCardNotesRepository.findAllByJobCardId(jobCardId);

            addInvoiceHeader(document, jobCard, userName, address, carMake, phone, userCreator, String.valueOf(carModel));
            document.add(new Paragraph("\n\n"));
            PdfFont arabicFont = createArabicFontForPdf();
            Color headerBgColor = new DeviceRgb(230, 230, 230); // light gray background
            if (!jobCardNotes.isEmpty()) {
                Table notesTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});  // 5 columns
                notesTable.setWidthPercent(100);
                notesTable.addCell(new Cell().add("Message").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                notesTable.addCell(new Cell().add("Created By").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                notesTable.addCell(new Cell().add("Customer Model Number").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                notesTable.addCell(new Cell().add("Approved By Customer At").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                notesTable.addCell(new Cell().add("Created At").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));

                for (JobCardNotes note : jobCardNotes) {
                    Integer createdById = note.getCreatedBy();
                    User userCreatedNote = userRepository.findUserById(createdById);
                    Integer createdNoteAccountType = userCreatedNote.getAccountTypeId();
                    System.out.println("Note : " + note.getId());
                    if (!note.getIsPrivate()) {
                        System.out.println("Note Not Private : " + note.getIsPrivate());
                        addNotes(user, userName, notesTable, arabicFont, note,createdNoteAccountType);
                    } else {
                        if (accountType.equals(ADMIN_TYPE) && includePrivateNotes) {
                            System.out.println("Note is Private : " + note.getIsPrivate());
                            addNotes(user, userName, notesTable, arabicFont, note,createdNoteAccountType);
                        }
                    }
                }
                document.add(notesTable);
            }

            Optional<List<JobCardProduct>> optionalJobCardProductList = jobCardProductRepository.findAllByJobCardId(jobCardId);
            document.add(new Paragraph(" ").setFontSize(12f));
            if (optionalJobCardProductList.isPresent()) {
                List<JobCardProduct> jobCardProductList = optionalJobCardProductList.get();
                if (!jobCardProductList.isEmpty()) {
                    BigDecimal totalPrice = BigDecimal.ZERO;
                    Table productsTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});  // 3 columns
                    productsTable.addCell(new Cell().add("Product").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                    productsTable.addCell(new Cell().add("QTY").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                    productsTable.addCell(new Cell().add("PRICE").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                    productsTable.addCell(new Cell().add("Added By").setBold().setTextAlignment(TextAlignment.CENTER).setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));
                    productsTable.addCell(new Cell().add("Approved By Customer At").setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(headerBgColor));

                    for (JobCardProduct jobCardProduct : jobCardProductList) {
                        if (jobCardProduct.getProductId() != null) {
                            String productName = "Product";
                            String productPrice = "0";
                            String productQuantity = "0";
                            if (jobCardProduct.getName() == null) {
                                Optional<Product> productOptional = productRepository.findById(jobCardProduct.getProductId());
                                if(productOptional.isPresent()) {
                                    productName = productOptional.get().getCode();
                                }
                            }
                            if (jobCardProduct.getPrice() != null) {
                                productPrice = jobCardProduct.getPrice();
                                try {
                                    totalPrice = totalPrice.add(new BigDecimal(productPrice).multiply(new BigDecimal(jobCardProduct.getQuantity())));
                                } catch (Exception e) {
                                    System.out.println("Error calculating total: " + e.getMessage());
                                }
                            }
                            if (jobCardProduct.getQuantity() != null) {
                                productQuantity = String.valueOf(jobCardProduct.getQuantity());
                            }
                            String createdBy = "Admin";
                            String approvedByCustomerAt = "Not Yet";
                            if (user != null) {
                                if (jobCardProduct.getCreatedBy() != null) {
                                    if (jobCardProduct.getCreatedBy().equals(user.getId())) {
                                        createdBy = userName;
                                        approvedByCustomerAt = "Customer Added it";
                                    } else {
                                        if (jobCardProduct.getCustomerApprovedAt() != null) {
                                            approvedByCustomerAt = jobCardProduct.getCustomerApprovedAt().toString();
                                        } else {
                                            approvedByCustomerAt = "Not Yet";
                                        }
                                    }
                                }
                            }
                            // Assuming JobCardNote has fields: getMessage(), getCreatedBy(), getCreatedAt()
                            jobCardProduct.setName(productName);
                            addProductWithLanguage(productsTable, arabicFont, jobCardProduct,createdBy,approvedByCustomerAt,productQuantity,productPrice);
                        }else{
                            String productName = "Product";
                            String productPrice = "0";
                            String productQuantity = "0";
                            if (jobCardProduct.getName() != null)
                                productName = jobCardProduct.getName();
                            if (jobCardProduct.getPrice() != null) {
                                productPrice = jobCardProduct.getPrice();
                                try {
                                    totalPrice = totalPrice.add(new BigDecimal(productPrice).multiply(new BigDecimal(jobCardProduct.getQuantity())));
                                } catch (Exception e) {
                                    System.out.println("Error calculating total: " + e.getMessage());
                                }
                            }
                            if (jobCardProduct.getQuantity() != null) {
                                productQuantity = String.valueOf(jobCardProduct.getQuantity());
                            }
                            String createdBy = "Admin";
                            String approvedByCustomerAt = "Not Yet";
                            if (user != null) {
                                if (jobCardProduct.getCreatedBy() != null) {
                                    if (jobCardProduct.getCreatedBy().equals(user.getId())) {
                                        createdBy = userName;
                                        approvedByCustomerAt = "Customer Added it";
                                    } else {
                                        if (jobCardProduct.getCustomerApprovedAt() != null) {
                                            approvedByCustomerAt = jobCardProduct.getCustomerApprovedAt().toString();
                                        } else {
                                            approvedByCustomerAt = "Not Yet";
                                        }
                                    }
                                }
                            }

                        addProductWithLanguage(productsTable, arabicFont, jobCardProduct,createdBy,approvedByCustomerAt,productQuantity,productPrice);
                        }
                    }
                    Cell totalLabel = new Cell(1, 4) // span across first 4 columns
                            .add(new Paragraph("Total").setBold().setTextAlignment(TextAlignment.LEFT))
                            .setBorder(new SolidBorder(1)).setBackgroundColor(headerBgColor);

                    Cell totalValue = new Cell()
                            .add(new Paragraph(totalPrice.toString()).setBold().setTextAlignment(TextAlignment.CENTER))
                            .setBorder(new SolidBorder(1)).setBackgroundColor(headerBgColor);

                    productsTable.addCell(totalLabel);
                    productsTable.addCell(totalValue);
                    document.add(productsTable);
                }
            }

            List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardId);

            float pdfWidth = 200f;  // max width per image
            float pdfHeight = 200f; // max height per image

            if (!jobCardImages.isEmpty()) {
                document.add(new Paragraph("Job Card Images:").setFontSize(12f).setBold());

                // Create a 2-column table for side-by-side images
                Table table = new Table(2);
                table.setWidth(UnitValue.createPercentValue(100)); // table spans full page width
                table.setMarginTop(10f);

                for (JobCardImages jobCardImage : jobCardImages) {
                    try {
                        URL url = new URL(jobCardImage.getUrl());
                        byte[] imageBytes = url.openStream().readAllBytes();
                        Image image = new Image(ImageDataFactory.create(imageBytes));
                        image.scaleToFit(pdfWidth, pdfHeight);
                        image.setHorizontalAlignment(HorizontalAlignment.CENTER);

                        // Wrap image inside a cell
                        Cell cell = new Cell().add(image);
                        cell.setPadding(5f);
                        cell.setBorder(Border.NO_BORDER);
                        table.addCell(cell);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // If odd number of images, add an empty cell to complete the row
                if (jobCardImages.size() % 2 != 0) {
                    table.addCell(new Cell().setBorder(Border.NO_BORDER));
                }

                document.add(table);
            }

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
    Cell createCell(String text, boolean bold) {
        Paragraph p = new Paragraph(text);
        if (bold) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER);
    }
    private void addInvoiceHeader(Document document , JobCard jobCard ,
                                  String userName,
                                  String address,String carMake , String phone,
                                  User user, String carModel) throws IOException {
        // === HEADER START ===
        InputStream logoStream = getClass().getResourceAsStream("/image/logo2.png");
        if (logoStream == null) {
            throw new IOException("Logo image not found in resources!");
        }
        ImageData imageData = ImageDataFactory.create(StreamUtil.inputStreamToArray(logoStream));
        Image logo = new Image(imageData).scaleToFit(60, 60);

// === Create header with table (2 columns: logo | text) ===
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1, 3})).useAllAvailableWidth();

// Left column: logo
        headerTable.addCell(new Cell()
                .add(logo)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)); // center align vertically

// Right column: text block (company name + contact info + Arabic title)
        PdfFont arabicFont = createArabicFontForPdf();
        Paragraph companyName = new Paragraph("German Service Center")
                .setFontSize(16f)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        Paragraph contactInfo = new Paragraph(
                "Tel (Dir): +974 44662202 - Tel: +974 44665445\n" +
                        "Fax: +974 44664544 - M: +974 33513370\n" +
                        "P.O.Box: 203144 Doha-Qatar - E: info@gsc-qatar.com\n" +
                        "www.gsc-qatar.com"
        ).setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER);
        LanguageProcessor al = new ArabicLigaturizer();
        Paragraph arabicTitle = new Paragraph(al.process("قطع غيار"))
                .setFont(arabicFont)
                .setFontSize(12f)
                .setTextAlignment(TextAlignment.CENTER)
                .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                .setPadding(5)
                .setBorder(new SolidBorder(1)); // border only around this text

// Group them in one cell
        Cell rightCell = new Cell()
                .add(companyName)
                .add(contactInfo)
                .add(new Paragraph(" ")) // spacer
                .add(arabicTitle)        // border only here
                .setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.CENTER);

        headerTable.addCell(rightCell);

// Add header table to document
        document.add(headerTable);

// Spacer after header
        document.add(new Paragraph(" "));

// Customer Info Table
        float[] customerColWidths = {2f, 3f, 2f, 3f};
        Table customerTable = new Table(customerColWidths);
        customerTable.setWidth(UnitValue.createPercentValue(100));

        customerTable.addCell(createCell("No.", true));
        customerTable.addCell(createCell(String.valueOf(jobCard.getId()), false));
        customerTable.addCell(createCell("Date:", true));
        customerTable.addCell(createCell(jobCard.getCreatedAt() != null ? jobCard.getCreatedAt().toString() : "", false));

        customerTable.addCell(createCell("Name", true));
        customerTable.addCell(createCell(userName, false));
        customerTable.addCell(createCell("Reg. No.", true));
        customerTable.addCell(createCell(carMake, false));

        customerTable.addCell(createCell("Mobile", true));
        customerTable.addCell(createCell(phone, false));
        customerTable.addCell(createCell("Car Type", true));
        customerTable.addCell(createCell(carMake, false));

        customerTable.addCell(createCell("Category", true));
        String accountTypeName = "";
        if (user.getAccountTypeId() != null) {
            accountTypeName = ACCOUNT_TYPE_MAP.getOrDefault(user.getAccountTypeId(), "");
        }

        customerTable.addCell(createCell(accountTypeName, false));
        customerTable.addCell(createCell("Car Model", true));
        customerTable.addCell(createCell(carModel, false));

        customerTable.addCell(createCell("Address", true));
        customerTable.addCell(createCell(address, false));
        customerTable.addCell(createCell("Year Model", true));
        customerTable.addCell(createCell("...........", false));// fill from car if available

        document.add(customerTable);

// === HEADER END ===
    }

    private void addNotes(User user, String userName, Table notesTable, PdfFont arabicFont, JobCardNotes note,Integer accountType) {
        String message = note.getMessage();
        Color whiteBgColor  = new DeviceRgb(255, 255, 255);
        if (message.matches(".*\\p{InArabic}.*")) {
            // Set the Arabic font for the message
            try {
                LanguageProcessor al = new ArabicLigaturizer();
                Cell cell = new Cell()
                        .add(new Paragraph(al.process(message)).setPadding(5))
                        .setFont(arabicFont)
                        .setFontSize(12f)
                        .setBorder(new SolidBorder(1))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                        .setPaddingLeft(5)
                        .setPaddingRight(5)
                        .setPaddingTop(5)
                        .setPaddingBottom(5)
                        .setBackgroundColor(whiteBgColor);

                notesTable.addCell(cell);
            } catch (Exception exception) {
                System.out.println("Exception : " + exception.getMessage());
            }
        } else {
            // Use the default font for non-Arabic text
            notesTable.addCell(new Cell().add(message).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        }
        System.out.println("Note : " + note.getCustomerMobileVersion());
        System.out.println("Note : " + note.getCustomerMobileMacAddress());
        System.out.println("Note : " + note.getMessage());
        String createdBy = "Admin";
        String approvedByCustomerAt = "Not Yet";
        String approvedByCustomersDevice = "Not Yet";
        if (user != null) {
            if (accountType.equals(USER_TYPE)) {
                createdBy = userName;
                if(note.getCustomerMobileVersion() != null) {
                    Timestamp approvedAt = note.getApprovedByCustomerAt();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                    approvedByCustomerAt = sdf.format(approvedAt);
                    approvedByCustomersDevice =  note.getCustomerMobileVersion();
                }
            } else {
                if (note.getApprovedByCustomerAt() != null) {
                    Timestamp approvedAt = note.getApprovedByCustomerAt();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                    approvedByCustomerAt = sdf.format(approvedAt);
                    approvedByCustomersDevice = note.getCustomerMobileVersion();
                }
            }
        }

        notesTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(approvedByCustomersDevice).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(note.getCreatedAt().toString()).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
    }
    private void addProductWithLanguage(Table productsTable, PdfFont arabicFont, JobCardProduct product,String createdBy,String approvedByCustomerAt,String productQuantity,String productPrice) {
        String productName = product.getName();
        if(productName == null){
            productsTable.addCell(new Cell().add("Product :").setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        }else {
            if (productName.matches(".*\\p{InArabic}.*")) {
                // Set the Arabic font for the message
                try {
                    LanguageProcessor al = new ArabicLigaturizer();
                    Cell cell = new Cell()
                            .add(new Paragraph(al.process(productName)).setPadding(5))
                            .setFont(arabicFont)
                            .setFontSize(12f)
                            .setBorder(new SolidBorder(1))
                            .setTextAlignment(TextAlignment.RIGHT)
                            .setBaseDirection(BaseDirection.RIGHT_TO_LEFT)
                            .setPaddingLeft(5)
                            .setPaddingRight(5)
                            .setPaddingTop(5)
                            .setPaddingBottom(5);

                    productsTable.addCell(cell);
                } catch (Exception exception) {
                    System.out.println("Exception : " + exception.getMessage());
                }
            } else {
                // Use the default font for non-Arabic text
                productsTable.addCell(new Cell().add(productName).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
            }
        }
        productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));

    }
}
