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
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.Border;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.BaseDirection;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.property.VerticalAlignment;
import com.itextpdf.layout.renderer.DocumentRenderer;
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
        document.getPdfDocument().addEventHandler(PdfDocumentEvent.END_PAGE, new PageNumberHandler());
        ReturnObject returnObject = new ReturnObject();

        Optional<Bill> billOptional = billRepository.findById(billId);
        if (billOptional.isPresent()) {
            Bill bill = billOptional.get();
            User userCreator = userRepository.findUserById(bill.getUserId());
            String referenceNumber = cleanReferenceNumber(bill);
            Optional<JobCard> jobCardOptional = jobCardRepository.findByCode(referenceNumber);

            String[] customerInfo = resolveCustomerInfo(user);   // [0]=userName [1]=address [2]=phone
            String[] carInfo = resolveCarInfo(bill);             // [0]=licenseNumber [1]=modelCode [2]=modelYear [3]=brandNameEn

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

            // customerInfo: [0]=name [1]=address [2]=phone
            // carInfo:      [0]=licenseNumber [1]=modelCode [2]=modelYear [3]=brandNameEn
            addInvoiceHeader(document, bill, customerInfo[0], customerInfo[1], customerInfo[2],
                    carInfo[0], carInfo[1], carInfo[2], carInfo[3], userCreator, billTypeName);

            List<BillNotes> billNotes = billNotesRepository.findAllByBillId(billId);
            if (!billNotes.isEmpty()) {
                document.add(buildNotesTable(billNotes, user, customerInfo[0], user.getAccountTypeId(), arabicFont, includePrivateNotes));
            }

            document.add(new Paragraph(" ").setFontSize(12f));
            Optional<List<BillProduct>> optionalBillProductList = billProductRepository.findAllByBillId(billId);
            document.add(buildProductsTable(optionalBillProductList, bill, user, customerInfo[0], arabicFont));

            addJobCardImages(document, jobCardOptional);
            addSignatureSection(document);

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
        // [0]=licenseNumber  [1]=modelCode  [2]=modelYear  [3]=brandNameEn
        String licenseNumber = "...........";
        String modelCode     = "...........";
        String modelYear     = "...........";
        String brandNameEn   = "...........";

        if (bill.getCarId() != null) {
            Optional<Car> carOptional = carRepository.findById(bill.getCarId());
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                if (car.getLicenseNumber() != null) licenseNumber = car.getLicenseNumber();

                if (car.getModelId() != null) {
                    Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                    if (modelOptional.isPresent()) {
                        Model model = modelOptional.get();
                        if (model.getCode() != null) modelCode = model.getCode();
                        if (model.getCreationYear() != null) modelYear = model.getCreationYear().toString();

                        if (model.getBrandId() != null) {
                            Optional<Brand> brandOptional = brandRepository.findById(model.getBrandId());
                            if (brandOptional.isPresent()) {
                                Brand brand = brandOptional.get(); // fix: was incorrectly new Brand()
                                if (brand.getNameEn() != null) brandNameEn = brand.getNameEn();
                            }
                        }
                    }
                }
            }
        }
        return new String[]{licenseNumber, modelCode, modelYear, brandNameEn};
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
        // 6 columns now: Product | QTY | PRICE | Item Discount | Added By | Approved By Customer At
        Table productsTable = new Table(new float[]{200f, 100f, 100f, 100f, 150f, 150f});
        BigDecimal totalPrice = BigDecimal.ZERO;

        productsTable.addCell(new Cell().add("Product").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("QTY").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("PRICE").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("Discount %").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
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

            String itemDiscount = (billProduct.getDiscount() != null && billProduct.getDiscount() != 0.0)
                    ? billProduct.getDiscount() + "%" : "-";

            String[] createdByInfo = resolveCreatedByInfo(billProduct, user, userName);
            billProduct.setName(productName);
            addProductWithLanguage(productsTable, arabicFont, billProduct, createdByInfo[0], createdByInfo[1], productQuantity, productPrice, itemDiscount);
        }

        addSummaryRows(productsTable, bill);
        return productsTable;
    }

    private Table buildEmptyProductsTable(Bill bill) {
        // 6 columns to match the products table
        Table productsTable = new Table(new float[]{200f, 100f, 100f, 100f, 150f, 150f});

        // Discount: show "-" if null or zero; otherwise value + "%"
        boolean hasDiscount = bill.getDiscount() != null && bill.getDiscount() != 0.0;
        String discountDisplay = hasDiscount ? bill.getDiscount() + "%" : "-";

        // Layout: [empty x2] [label spans 3] [value] — value is flush to the right edge
        productsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Discount").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(discountDisplay))
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Total row
        String totalValue = resolveFinalTotal(bill);
        productsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Total").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(totalValue).setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        return productsTable;
    }

    private void addSummaryRows(Table productsTable, Bill bill) {
        // Table has 6 columns: {200, 100, 100, 100, 150, 150}
        // Summary boxes sit on the right: [empty x1] [label spans 3 — wide] [value] [empty x1]

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
        // [empty x2] [label spans 3] [value] — flush to right edge
        productsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Discount (" + discountTypeText + ")").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(discountText))
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Down payment row
        String downPayment = bill.getDownPayment() != null ? bill.getDownPayment().toString() : "0";
        productsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Down Payment").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(downPayment))
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Total row — finalTotalPrice, fallback to total if 0.0
        String totalValue = resolveFinalTotal(bill);
        productsTable.addCell(new Cell(1, 2).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Total").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(totalValue).setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
    }

    private String resolveFinalTotal(Bill bill) {
        if (bill.getFinalTotalPrice() != null && bill.getFinalTotalPrice() != 0.0) {
            return BigDecimal.valueOf(bill.getFinalTotalPrice()).toString();
        }
        if (bill.getTotal() != null) {
            return bill.getTotal().toString();
        }
        return "0";
    }

    // Fixed image dimensions — every image in the grid will be exactly this size
    private static final float IMG_CELL_WIDTH  = 165f;
    private static final float IMG_CELL_HEIGHT = 130f;
    private static final int   IMG_COLS        = 3;

    private void addJobCardImages(Document document, Optional<JobCard> jobCardOptional) throws IOException {
        if (!jobCardOptional.isPresent()) return;

        List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardOptional.get().getId());
        if (jobCardImages.isEmpty()) return;

        // 3-column grid — each cell has a fixed size so all images are uniform
        Table imageGrid = new Table(new float[]{IMG_CELL_WIDTH, IMG_CELL_WIDTH, IMG_CELL_WIDTH})
                .setWidthPercent(100);

        int count = 0;
        for (JobCardImages jobCardImage : jobCardImages) {
            Cell imageCell;
            try {
                URL url = new URL(jobCardImage.getUrl());
                InputStream inputStream = url.openStream();
                BufferedImage bufferedImage = ImageIO.read(inputStream);

                if (bufferedImage == null) throw new IOException("Unrecognized image format");

                // Normalise to JPEG bytes (handles PNG, HEIC, etc. uniformly)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

                // scaleToFit guarantees the image never exceeds the cell box (scales both up & down)
                Image image = new Image(ImageDataFactory.create(imageBytes))
                        .scaleToFit(IMG_CELL_WIDTH - 8f, IMG_CELL_HEIGHT - 8f)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER);

                imageCell = new Cell()
                        .add(image)
                        .setBorder(new SolidBorder(1))
                        .setHeight(IMG_CELL_HEIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setPadding(4f);

            } catch (IOException e) {
                // Placeholder cell if image fails to load
                System.out.println("Failed to load image: " + jobCardImage.getUrl() + " — " + e.getMessage());
                imageCell = new Cell()
                        .add(new Paragraph("Image unavailable").setFontSize(9f)
                                .setTextAlignment(TextAlignment.CENTER))
                        .setBorder(new SolidBorder(1))
                        .setHeight(IMG_CELL_HEIGHT)
                        .setVerticalAlignment(VerticalAlignment.MIDDLE);
            }

            imageGrid.addCell(imageCell);
            count++;
        }

        // Pad last row with empty cells so borders are consistent
        int remainder = count % IMG_COLS;
        if (remainder != 0) {
            for (int i = remainder; i < IMG_COLS; i++) {
                imageGrid.addCell(new Cell()
                        .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1))
                        .setHeight(IMG_CELL_HEIGHT));
            }
        }

        // Wrap header + grid together so the title never orphans at the bottom of a page
        Div imageSection = new Div().setKeepTogether(true);
        imageSection.add(new Paragraph(" "));
        imageSection.add(new Paragraph("Job Card Images")
                .setFontSize(12f).setBold()
                .setBorderBottom(new SolidBorder(1))
                .setMarginBottom(6f));
        imageSection.add(imageGrid);
        document.add(imageSection);
    }

    private void addSignatureSection(Document document) {
        // Flush pending layout so we can read the current Y cursor
        document.flush();

        float pageHeight    = document.getPdfDocument().getDefaultPageSize().getHeight();
        float bottomMargin  = document.getBottomMargin();
        float sigHeight     = 90f;  // estimated height of the signature block

        // How far down the page are we right now?
        DocumentRenderer renderer = (DocumentRenderer) document.getRenderer();
        float currentY = renderer.getCurrentArea().getBBox().getTop();

        // Space left between current cursor and bottom margin
        float remaining = currentY - bottomMargin - sigHeight;
        if (remaining > 0) {
            // Push the signature to the very bottom of this page
            document.add(new Paragraph(" ").setHeight(remaining).setMargin(0).setPadding(0));
        }
        // If remaining <= 0 there isn't enough room; the signature will naturally flow
        // to the next page and land near the top — add a page break in that case
        else {
            document.add(new com.itextpdf.layout.element.AreaBreak());
            // Now push it to the bottom of the fresh page
            float freshRemaining = (pageHeight - document.getTopMargin() - bottomMargin) - sigHeight;
            if (freshRemaining > 0) {
                document.add(new Paragraph(" ").setHeight(freshRemaining).setMargin(0).setPadding(0));
            }
        }

        // Two-column signature table: Technician | Customer
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();

        Cell leftSig = new Cell()
                .add(new Paragraph(" ").setFontSize(10f))
                .add(new Paragraph("_________________________").setFontSize(11f))
                .add(new Paragraph("Signature").setBold().setFontSize(11f))
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(10f);

        Cell rightSig = new Cell()
                .add(new Paragraph(" ").setFontSize(10f))
                .add(new Paragraph("_________________________").setFontSize(11f).setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph("Customer's Signature").setBold().setFontSize(11f).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER)
                .setPaddingRight(10f);

        sigTable.addCell(leftSig);
        sigTable.addCell(rightSig);
        document.add(sigTable);
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
                                  String address, String phone,
                                  String licenseNumber, String modelCode, String modelYear, String brandNameEn,
                                  User user, String billTypeName) throws IOException {
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
        customerTable.addCell(createCell(String.valueOf(bill.getReferenceNumber()), false));
        customerTable.addCell(createCell("Date:", true));
        customerTable.addCell(createCell(bill.getCreatedAt() != null ? bill.getCreatedAt().toString() : "", false));

        customerTable.addCell(createCell("Name", true));
        customerTable.addCell(createCell(userName, false));
        customerTable.addCell(createCell("Plate No.", true));
        customerTable.addCell(createCell(licenseNumber, false));

        customerTable.addCell(createCell("Mobile", true));
        customerTable.addCell(createCell(phone, false));
        customerTable.addCell(createCell("Car Type", true));
        customerTable.addCell(createCell(brandNameEn, false));

        customerTable.addCell(createCell("Category", true));
        String accountTypeName = user.getAccountTypeId() != null ? ACCOUNT_TYPE_MAP.getOrDefault(user.getAccountTypeId(), "") : "";
        customerTable.addCell(createCell(accountTypeName, false));
        customerTable.addCell(createCell("Car Model", true));
        customerTable.addCell(createCell(modelCode, false));

        customerTable.addCell(createCell("Address", true));
        customerTable.addCell(createCell(address, false));
        customerTable.addCell(createCell("Year Model", true));
        customerTable.addCell(createCell(modelYear, false));

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

    // ===================== PAGE NUMBER HANDLER =====================

    private static class PageNumberHandler implements IEventHandler {
        @Override
        public void handleEvent(Event event) {
            PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
            PdfDocument pdfDoc = docEvent.getDocument();
            PdfPage page = docEvent.getPage();
            int pageNumber = pdfDoc.getPageNumber(page);

            Rectangle pageSize = page.getPageSize();
            try {
                PdfCanvas pdfCanvas = new PdfCanvas(
                        page.newContentStreamAfter(), page.getResources(), pdfDoc);
                new Canvas(pdfCanvas, pdfDoc, pageSize)
                        .showTextAligned(
                                new Paragraph("Page " + pageNumber).setFontSize(9f),
                                pageSize.getWidth() / 2f,
                                20f,
                                TextAlignment.CENTER)
                        .close();
            } catch (Exception e) {
                System.out.println("Page number handler error: " + e.getMessage());
            }
        }
    }

    private void addProductWithLanguage(Table productsTable, PdfFont arabicFont, BillProduct product,
                                        String createdBy, String approvedByCustomerAt,
                                        String productQuantity, String productPrice, String itemDiscount) {
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
        productsTable.addCell(new Cell().add(itemDiscount).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));
    }
}
