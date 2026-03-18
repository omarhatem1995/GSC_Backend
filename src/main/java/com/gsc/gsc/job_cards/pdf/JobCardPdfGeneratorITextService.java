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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static com.gsc.gsc.constants.UserTypes.ADMIN_TYPE;
import static com.gsc.gsc.constants.UserTypes.USER_TYPE;
import static com.gsc.gsc.job_cards.JobCardConstants.*;

@Service
public class JobCardPdfGeneratorITextService {

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

    // ===================== CONSTANTS =====================

    private static final Map<Integer, String> ACCOUNT_TYPE_MAP = Map.of(1, "User", 2, "Business", 3, "Admin");
    private static final Color HEADER_BG_COLOR = new DeviceRgb(230, 230, 230);
    private static final float IMG_CELL_WIDTH  = 165f;
    private static final float IMG_CELL_HEIGHT = 130f;
    private static final int   IMG_COLS        = 3;

    // ===================== MAIN ENTRY POINT =====================

    public ReturnObject exportIText2(String token, int jobCardId, String macAddress, Boolean includePrivateNotes) throws IOException {
        Integer userId = userService.getUserIdFromToken(token);
        User user = userRepository.findUserById(userId);
        if (includePrivateNotes == null) includePrivateNotes = false;

        String filePath = "/var/www/jobcards/JobCardPdf/jobCard_" + jobCardId + ".pdf";
        Document document = initializeDocument(filePath);
        document.getPdfDocument().addEventHandler(PdfDocumentEvent.END_PAGE, new PageNumberHandler());
        ReturnObject returnObject = new ReturnObject();

        Optional<JobCard> jobCardOptional = jobCardRepository.findById(jobCardId);
        if (jobCardOptional.isPresent()) {
            JobCard jobCard = jobCardOptional.get();
            User userCreator = userRepository.findUserById(jobCard.getUserId());

            String[] customerInfo = resolveCustomerInfo(userCreator); // [0]=name [1]=address [2]=phone
            String[] carInfo      = resolveCarInfo(jobCard);          // [0]=license [1]=modelCode [2]=year [3]=brandNameEn

            PdfFont arabicFont = createArabicFontForPdf();

            addInvoiceHeader(document, jobCard,
                    customerInfo[0], customerInfo[1], customerInfo[2],
                    carInfo[0], carInfo[1], carInfo[2], carInfo[3],
                    userCreator);

            document.add(new Paragraph(" ").setFontSize(12f));

            List<JobCardNotes> jobCardNotes = jobCardNotesRepository.findAllByJobCardId(jobCardId);
            if (!jobCardNotes.isEmpty()) {
                document.add(buildNotesTable(jobCardNotes, user, customerInfo[0], user.getAccountTypeId(), arabicFont, includePrivateNotes));
            }

            document.add(new Paragraph(" ").setFontSize(12f));

            Optional<List<JobCardProduct>> optionalProductList = jobCardProductRepository.findAllByJobCardId(jobCardId);
            document.add(buildProductsTable(optionalProductList, jobCard, user, customerInfo[0], arabicFont));

            addJobCardImages(document, jobCardId);
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
        System.out.println("Creating file: " + filePath);

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            if (file.getParentFile().mkdirs()) {
                System.out.println("Directories created successfully.");
            } else {
                System.out.println("Failed to create directories.");
                throw new IOException("Failed to create required directories.");
            }
        }

        if (file.exists()) {
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

    private String[] resolveCustomerInfo(User user) {
        String userName = "...........";
        String address  = "...........";
        String phone    = "...........";
        if (user.getName() != null)    userName = user.getName();
        if (user.getAddress() != null) address  = user.getAddress();
        if (user.getPhone() != null)   phone    = user.getPhone();
        return new String[]{userName, address, phone};
    }

    private String[] resolveCarInfo(JobCard jobCard) {
        // [0]=licenseNumber  [1]=modelCode  [2]=modelYear  [3]=brandNameEn
        String licenseNumber = "...........";
        String modelCode     = "...........";
        String modelYear     = "...........";
        String brandNameEn   = "...........";

        if (jobCard.getCarId() != null) {
            Optional<Car> carOptional = carRepository.findById(jobCard.getCarId());
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                if (car.getLicenseNumber() != null) licenseNumber = car.getLicenseNumber();

                if (car.getModelId() != null) {
                    Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                    if (modelOptional.isPresent()) {
                        Model model = modelOptional.get();
                        if (model.getCode() != null)         modelCode = model.getCode();
                        if (model.getCreationYear() != null) modelYear = model.getCreationYear().toString();

                        if (model.getBrandId() != null) {
                            Optional<Brand> brandOptional = brandRepository.findById(model.getBrandId());
                            if (brandOptional.isPresent()) {
                                Brand brand = brandOptional.get();
                                if (brand.getNameEn() != null) brandNameEn = brand.getNameEn();
                            }
                        }
                    }
                }
            }
        }
        return new String[]{licenseNumber, modelCode, modelYear, brandNameEn};
    }

    private String resolveProductName(JobCardProduct product) {
        if (product.getProductId() != null && product.getName() == null) {
            Optional<Product> productOptional = productRepository.findById(product.getProductId());
            if (productOptional.isPresent()) return productOptional.get().getCode();
        }
        return product.getName() != null ? product.getName() : "Product";
    }

    private String[] resolveCreatedByInfo(JobCardProduct product, User user, String userName) {
        String createdBy = "Admin";
        String approvedByCustomerAt = "Not Yet";
        if (user != null && product.getCreatedBy() != null) {
            if (product.getCreatedBy().equals(user.getId())) {
                createdBy = userName;
                approvedByCustomerAt = "Customer Added it";
            } else if (product.getCustomerApprovedAt() != null) {
                approvedByCustomerAt = product.getCustomerApprovedAt().toString();
            }
        }
        return new String[]{createdBy, approvedByCustomerAt};
    }

    // ===================== TABLE BUILDERS =====================

    private Table buildNotesTable(List<JobCardNotes> jobCardNotes, User user, String userName,
                                  Integer accountType, PdfFont arabicFont, boolean includePrivateNotes) {
        Table notesTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});
        notesTable.setWidthPercent(100);
        notesTable.addCell(new Cell().add("Message").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        notesTable.addCell(new Cell().add("Created By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        notesTable.addCell(new Cell().add("Customer Model Number").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        notesTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        notesTable.addCell(new Cell().add("Created At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        for (JobCardNotes note : jobCardNotes) {
            Integer createdById = note.getCreatedBy();
            User userCreatedNote = userRepository.findUserById(createdById);
            Integer createdNoteAccountType = userCreatedNote.getAccountTypeId();
            if (!note.getIsPrivate()) {
                addNotes(user, userName, notesTable, arabicFont, note, createdNoteAccountType);
            } else if (accountType.equals(ADMIN_TYPE) && includePrivateNotes) {
                addNotes(user, userName, notesTable, arabicFont, note, createdNoteAccountType);
            }
        }
        return notesTable;
    }

    private Table buildProductsTable(Optional<List<JobCardProduct>> optionalProductList,
                                     JobCard jobCard, User user, String userName, PdfFont arabicFont) {
        // 5 columns: Product | QTY | PRICE | Added By | Approved By Customer At
        Table productsTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});
        BigDecimal totalPrice = BigDecimal.ZERO;

        productsTable.addCell(new Cell().add("Product").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("QTY").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("PRICE").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("Added By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
        productsTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));

        if (optionalProductList.isPresent() && !optionalProductList.get().isEmpty()) {
            for (JobCardProduct product : optionalProductList.get()) {
                String productName     = resolveProductName(product);
                String productPrice    = product.getPrice() != null ? product.getPrice() : "0";
                String productQuantity = product.getQuantity() != null ? String.valueOf(product.getQuantity()) : "0";

                try {
                    totalPrice = totalPrice.add(new BigDecimal(productPrice).multiply(new BigDecimal(product.getQuantity())));
                } catch (Exception e) {
                    System.out.println("Error calculating total: " + e.getMessage());
                }

                product.setName(productName);
                String[] createdByInfo = resolveCreatedByInfo(product, user, userName);
                addProductWithLanguage(productsTable, arabicFont, product, createdByInfo[0], createdByInfo[1], productQuantity, productPrice);
            }
        }

        addSummaryRows(productsTable, jobCard, totalPrice);
        return productsTable;
    }

    private void addSummaryRows(Table productsTable, JobCard jobCard, BigDecimal totalPrice) {
        // 5-column table: [empty x1] [label spans 3] [value] — flush to right edge

        // Down payment row
        String downPayment = jobCard.getDownPayment() != null ? jobCard.getDownPayment().toString() : "0";
        productsTable.addCell(new Cell(1, 1).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Down Payment").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(downPayment))
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));

        // Total row
        productsTable.addCell(new Cell(1, 1).setBorder(Border.NO_BORDER));
        productsTable.addCell(new Cell(1, 3).add(new Paragraph("Total").setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
        productsTable.addCell(new Cell().add(new Paragraph(totalPrice.toString()).setBold())
                .setBorder(new SolidBorder(1)).setBackgroundColor(HEADER_BG_COLOR));
    }

    // ===================== IMAGES =====================

    private void addJobCardImages(Document document, int jobCardId) throws IOException {
        List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardId);
        if (jobCardImages.isEmpty()) return;

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

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpg", baos);
                byte[] imageBytes = baos.toByteArray();

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

        // Pad last row so the grid is always even
        int remainder = count % IMG_COLS;
        if (remainder != 0) {
            for (int i = remainder; i < IMG_COLS; i++) {
                imageGrid.addCell(new Cell()
                        .setBorder(new SolidBorder(new DeviceRgb(200, 200, 200), 1))
                        .setHeight(IMG_CELL_HEIGHT));
            }
        }

        // Wrap title + grid so they never split across pages
        Div imageSection = new Div().setKeepTogether(true);
        imageSection.add(new Paragraph(" "));
        imageSection.add(new Paragraph("Job Card Images")
                .setFontSize(12f).setBold()
                .setBorderBottom(new SolidBorder(1))
                .setMarginBottom(6f));
        imageSection.add(imageGrid);
        document.add(imageSection);
    }

    // ===================== SIGNATURE =====================

    private void addSignatureSection(Document document) {
        document.flush();

        float pageHeight   = document.getPdfDocument().getDefaultPageSize().getHeight();
        float bottomMargin = document.getBottomMargin();
        float sigHeight    = 90f;

        DocumentRenderer renderer = (DocumentRenderer) document.getRenderer();
        float currentY = renderer.getCurrentArea().getBBox().getTop();

        float remaining = currentY - bottomMargin - sigHeight;
        if (remaining > 0) {
            document.add(new Paragraph(" ").setHeight(remaining).setMargin(0).setPadding(0));
        } else {
            document.add(new com.itextpdf.layout.element.AreaBreak());
            float freshRemaining = (pageHeight - document.getTopMargin() - bottomMargin) - sigHeight;
            if (freshRemaining > 0) {
                document.add(new Paragraph(" ").setHeight(freshRemaining).setMargin(0).setPadding(0));
            }
        }

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

    private void addInvoiceHeader(Document document, JobCard jobCard,
                                  String userName, String address, String phone,
                                  String licenseNumber, String modelCode, String modelYear, String brandNameEn,
                                  User user) throws IOException {
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
        customerTable.addCell(createCell(String.valueOf(jobCard.getId()), false));
        customerTable.addCell(createCell("Date:", true));
        customerTable.addCell(createCell(jobCard.getCreatedAt() != null ? jobCard.getCreatedAt().toString() : "", false));

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

        document.add(customerTable);
    }

    private void addNotes(User user, String userName, Table notesTable, PdfFont arabicFont, JobCardNotes note, Integer accountType) {
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
                System.out.println("Exception: " + exception.getMessage());
            }
        } else {
            notesTable.addCell(new Cell().add(message).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        }

        String createdBy = "Admin";
        String approvedByCustomerAt = "Not Yet";
        String approvedByCustomersDevice = "Not Yet";
        if (user != null) {
            if (accountType.equals(USER_TYPE)) {
                createdBy = userName;
                if (note.getCustomerMobileVersion() != null) {
                    Timestamp approvedAt = note.getApprovedByCustomerAt();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                    approvedByCustomerAt = sdf.format(approvedAt);
                    approvedByCustomersDevice = note.getCustomerMobileVersion();
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

        notesTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(approvedByCustomersDevice).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        notesTable.addCell(new Cell().add(note.getCreatedAt().toString()).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
    }

    private void addProductWithLanguage(Table productsTable, PdfFont arabicFont, JobCardProduct product,
                                        String createdBy, String approvedByCustomerAt,
                                        String productQuantity, String productPrice) {
        String productName = product.getName();
        if (productName != null && productName.matches(".*\\p{InArabic}.*")) {
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
                System.out.println("Exception: " + exception.getMessage());
            }
        } else {
            productsTable.addCell(new Cell().add(productName != null ? productName : "Product")
                    .setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5);
        }
        productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));
    }
}
