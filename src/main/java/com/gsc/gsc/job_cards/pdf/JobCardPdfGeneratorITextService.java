package com.gsc.gsc.job_cards.pdf;

import com.gsc.gsc.constants.ReturnObject;
import com.gsc.gsc.job_cards.service.serviceImplementation.JobCardService;
import com.gsc.gsc.model.*;
import com.gsc.gsc.repo.*;
import com.gsc.gsc.user.service.servicesImplementation.UserService;
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
    private BrandTextRepository brandTextRepository;
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

    public ReturnObject exportIText(String token, int jobCardId, String macAddress,Boolean includePrivateNotes) throws IOException {
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
            if (user.getName() != null)
                userName = user.getName();
            if (user.getAddress() != null)
                address = user.getAddress();
            if (user.getPhone() != null)
                phone = user.getPhone();
            Optional<Car> carOptional = carRepository.findById(jobCard.getCarId());
            if (carOptional.isPresent()) {
                Car car = carOptional.get();
                carMake = car.getLicenseNumber();
                if (car.getModelId() != null) {
                    Optional<Model> modelOptional = modelRepository.findById(car.getModelId());
                    if (modelOptional.isPresent()) {
                        Model model = modelOptional.get();
                        carModel.append(model.getCode()).append("/").append(model.getCreationYear());
                        if (model.getBrandId() != null) {
                            Optional<Brand> brandOptional = brandRepository.findById(model.getBrandId());
                            if (brandOptional.isPresent()) {
                                Brand brand = new Brand();
                                carModel.append("/").append(brand.getCode());
                                Optional<BrandText> brandTextOptional = brandTextRepository.findByBrandIdAndLangId(brand.getId(), 1);
                                System.out.println(brandTextOptional.isPresent());
                                if (brandTextOptional.isPresent()) {
                                    BrandText brandText = brandTextOptional.get();
                                    if (brandText.getName() != null) {
                                        carModel.append("/").append(brandText.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                carKilosCovered = car.getCoveredKilos();
            }

            List<JobCardNotes> jobCardNotes = jobCardNotesRepository.findAllByJobCardId(jobCardId);

            // Create a centered title
            Paragraph title = new Paragraph("GSC")
                    .setFontSize(16f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(title);

            float[] columnWidths = {1, 1}; // Two equally sized columns
            Table table = new Table(columnWidths);
            table.setWidthPercent(100);

            // Add "JOB CARD" to the first cell
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("JOB CARD").setFontSize(12f).setBold())
                    .setBorder(Border.NO_BORDER));

            // Add "JOB NO: xxx" to the second cell, aligned to the right
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph("JOB NO: " + jobCard.getCode()).setFontSize(12f))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(Border.NO_BORDER));

            document.add(table);

            document.add(new Paragraph("Name: " + userName).setFontSize(12f).setMarginTop(10f));
            document.add(new Paragraph("Address: " + address).setFontSize(12f));
            document.add(new Paragraph("Phone: " + phone).setFontSize(12f));
            document.add(new Paragraph("Car: " + carMake).setFontSize(12f));
            document.add(new Paragraph("Device Mac Address: " + macAddress).setFontSize(12f));
            document.add(new Paragraph("Car Model: " + carModel).setFontSize(12f));
            document.add(new Paragraph("Down Payment : " + downPayment).setFontSize(12f));
            if (jobCard.getIsTestDrive() == 1) {
                document.add(new Paragraph("Test Drive: Yes").setFontSize(12f));
            }
            if (jobCard.getJobCardStatusId() == COMPLETED) {
                document.add(new Paragraph("Status: Completed").setFontSize(12f));
            } else if (jobCard.getJobCardStatusId() == APPROVED) {
                document.add(new Paragraph("Status: Approved").setFontSize(12f));
            } else if (jobCard.getJobCardStatusId() == PENDING_CUSTOMER_APPROVAL) {
                document.add(new Paragraph("Status: Pending Customer Approval").setFontSize(12f));
            } else if (jobCard.getJobCardStatusId() == NEW) {
                document.add(new Paragraph("Status: New Job Card").setFontSize(12f));
            }
            document.add(new Paragraph("Kilos: " + carKilosCovered).setFontSize(12f));

            PdfFont arabicFont = createArabicFontForPdf();
            if (!jobCardNotes.isEmpty()) {
                Table notesTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});  // 5 columns
                notesTable.setWidthPercent(100);
                notesTable.addCell(new Cell().add("Message").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                notesTable.addCell(new Cell().add("Created By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                notesTable.addCell(new Cell().add("Customer Model Number").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                notesTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                notesTable.addCell(new Cell().add("Created At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));

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
                    Table productsTable = new Table(new float[]{200f, 150f, 150f, 150f, 150f});  // 3 columns
                    productsTable.addCell(new Cell().add("Product").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                    productsTable.addCell(new Cell().add("QTY").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                    productsTable.addCell(new Cell().add("PRICE").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                    productsTable.addCell(new Cell().add("Added By").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));
                    productsTable.addCell(new Cell().add("Approved By Customer At").setBold().setFontSize(14f).setBorderBottom(new SolidBorder(1)));

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
                            if (jobCardProduct.getPrice() != null)
                                productPrice = jobCardProduct.getPrice();
                            if (jobCardProduct.getQuantity() != null)
                                productQuantity = String.valueOf(jobCardProduct.getQuantity());

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
                            if (jobCardProduct.getPrice() != null)
                                productPrice = jobCardProduct.getPrice();
                            if (jobCardProduct.getQuantity() != null)
                                productQuantity = String.valueOf(jobCardProduct.getQuantity());

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
                /*            productsTable.addCell(new Cell().add(productName).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));
                        */
                        addProductWithLanguage(productsTable, arabicFont, jobCardProduct,createdBy,approvedByCustomerAt,productQuantity,productPrice);
                        }
                    }
                    document.add(productsTable);
                }
            }

            List<JobCardImages> jobCardImages = jobCardImagesRepository.findAllByJobCardId(jobCardId);

            float maxWidth = 200f; // Maximum width
            float maxHeight = 200f; // Maximum height

            if (!jobCardImages.isEmpty()) {
                document.add(new Paragraph("Job Card Images:").setFontSize(12f).setBold());
                int counter = 1;
                for (JobCardImages jobCardImage : jobCardImages) {
                    try {
                        // Fetch the image data from the Firebase Storage URL
                        URL url = new URL(jobCardImage.getUrl());
                        System.out.println("Url : " + url);
                        InputStream inputStream = url.openStream();

                        // Read the image data and determine its type
                        BufferedImage bufferedImage = ImageIO.read(inputStream);
                        if (bufferedImage == null) {
                            throw new IOException("Image format not recognized");
                        }

                        // Convert the BufferedImage to a byte array with explicit format
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        String format = "png";
                        if(String.valueOf(url).toLowerCase().contains("png")){
                            format = "png";
                        }
                        ImageIO.write(bufferedImage, format, baos); // Explicitly set the format
                        byte[] imageBytes = baos.toByteArray();

                        // Add the image to the PDF document
                        Image image = new Image(ImageDataFactory.create(imageBytes)).setAutoScale(true);

                        // Get the original dimensions of the image
                        float originalWidth = image.getImageWidth();
                        float originalHeight = image.getImageHeight();

                        if (originalWidth > maxWidth || originalHeight > maxHeight) {
                            float widthRatio = maxWidth / originalWidth;
                            float heightRatio = maxHeight / originalHeight;
                            float scalingFactor = Math.min(widthRatio, heightRatio);

                            // Apply scaling directly with float values
                            image.scaleToFit(originalWidth * scalingFactor, originalHeight * scalingFactor);
                        }

                        document.add(image);
                        document.add(new Paragraph("\n")).setWidth(200f).setHeight(200f);

                        counter++;
                    } catch (IOException e) {
                        // Log and handle exceptions for missing or invalid images
                        e.printStackTrace();
                    }
                }
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
                                if(brand.getCode() !=null ){
                                    carModel.append("/").append(brand.getCode());
                                }
                                Optional<BrandText> brandTextOptional = brandTextRepository.findByBrandIdAndLangId(brand.getId(), 1);
                                System.out.println("brand Text : " + brandTextOptional.isPresent());
                                if (brandTextOptional.isPresent()) {
                                    BrandText brandText = brandTextOptional.get();
                                    if (brandText.getName() != null) {
                                        carModel.append("/").append(brandText.getName());
                                    }
                                }
                            }
                        }
                    }
                }
                carKilosCovered = car.getCoveredKilos();
            }

            List<JobCardNotes> jobCardNotes = jobCardNotesRepository.findAllByJobCardId(jobCardId);

            // Create a centered title
//            Paragraph title = new Paragraph("GSC")
//                    .setFontSize(16f)
//                    .setBold()
//                    .setTextAlignment(TextAlignment.CENTER);

//            document.add(title);
//
//            float[] columnWidths = {1, 1}; // Two equally sized columns
//            Table table = new Table(columnWidths);
//            table.setWidthPercent(100);

            // Add "JOB CARD" to the first cell
//            table.addCell(new com.itextpdf.layout.element.Cell()
//                    .add(new Paragraph("JOB CARD").setFontSize(12f).setBold())
//                    .setBorder(Border.NO_BORDER));
//
//            // Add "JOB NO: xxx" to the second cell, aligned to the right
//            table.addCell(new com.itextpdf.layout.element.Cell()
//                    .add(new Paragraph("JOB NO: " + jobCard.getCode()).setFontSize(12f))
//                    .setTextAlignment(TextAlignment.RIGHT)
//                    .setBorder(Border.NO_BORDER));
//
//            document.add(table);
//
//            document.add(new Paragraph("Name: " + userName).setFontSize(12f).setMarginTop(10f));
//            document.add(new Paragraph("Address: " + address).setFontSize(12f));
//            document.add(new Paragraph("Phone: " + phone).setFontSize(12f));
//            document.add(new Paragraph("Car: " + carMake).setFontSize(12f));
//            document.add(new Paragraph("Device Mac Address: " + macAddress).setFontSize(12f));
//            document.add(new Paragraph("Car Model: " + carModel).setFontSize(12f));
//            document.add(new Paragraph("Down Payment : " + downPayment).setFontSize(12f));
//            if (jobCard.getIsTestDrive() == 1) {
//                document.add(new Paragraph("Test Drive: Yes").setFontSize(12f));
//            }
//            if (jobCard.getJobCardStatusId() == COMPLETED) {
//                document.add(new Paragraph("Status: Completed").setFontSize(12f));
//            } else if (jobCard.getJobCardStatusId() == APPROVED) {
//                document.add(new Paragraph("Status: Approved").setFontSize(12f));
//            } else if (jobCard.getJobCardStatusId() == PENDING_CUSTOMER_APPROVAL) {
//                document.add(new Paragraph("Status: Pending Customer Approval").setFontSize(12f));
//            } else if (jobCard.getJobCardStatusId() == NEW) {
//                document.add(new Paragraph("Status: New Job Card").setFontSize(12f));
//            }
//            document.add(new Paragraph("Kilos: " + carKilosCovered).setFontSize(12f));

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
                            // Assuming JobCardNote has fields: getMessage(), getCreatedBy(), getCreatedAt()
                /*            productsTable.addCell(new Cell().add(productName).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
                            productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));
                        */
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

            float maxWidth = 200f; // Maximum width
            float maxHeight = 200f; // Maximum height

            if (!jobCardImages.isEmpty()) {
                document.add(new Paragraph("Job Card Images:").setFontSize(12f).setBold());
                int counter = 1;
                for (JobCardImages jobCardImage : jobCardImages) {
                    try {
                        // Fetch the image data from the Firebase Storage URL
                        URL url = new URL(jobCardImage.getUrl());
                        System.out.println("Url : " + url);
                        InputStream inputStream = url.openStream();

                        // Read the image data and determine its type
                        BufferedImage bufferedImage = ImageIO.read(inputStream);
                        if (bufferedImage == null) {
                            throw new IOException("Image format not recognized");
                        }

                        // Convert the BufferedImage to a byte array with explicit format
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        String format = "png";
                        if(String.valueOf(url).toLowerCase().contains("png")){
                            format = "png";
                        }
                        ImageIO.write(bufferedImage, format, baos); // Explicitly set the format
                        byte[] imageBytes = baos.toByteArray();

                        // Add the image to the PDF document
                        Image image = new Image(ImageDataFactory.create(imageBytes)).setAutoScale(true);

                        // Get the original dimensions of the image
                        float originalWidth = image.getImageWidth();
                        float originalHeight = image.getImageHeight();

                        if (originalWidth > maxWidth || originalHeight > maxHeight) {
                            float widthRatio = maxWidth / originalWidth;
                            float heightRatio = maxHeight / originalHeight;
                            float scalingFactor = Math.min(widthRatio, heightRatio);

                            // Apply scaling directly with float values
                            image.scaleToFit(originalWidth * scalingFactor, originalHeight * scalingFactor);
                        }

                        document.add(image);
                        document.add(new Paragraph("\n")).setWidth(200f).setHeight(200f);

                        counter++;
                    } catch (IOException e) {
                        // Log and handle exceptions for missing or invalid images
                        e.printStackTrace();
                    }
                }
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

        notesTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(approvedByCustomersDevice).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
        notesTable.addCell(new Cell().add(note.getCreatedAt().toString()).setFontSize(12f).setBorder(new SolidBorder(1))).setPadding(5).setBackgroundColor(whiteBgColor);
    }
    private void addProductWithLanguage(Table productsTable, PdfFont arabicFont, JobCardProduct product,String createdBy,String approvedByCustomerAt,String productQuantity,String productPrice) {
        String productName = product.getName();
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
        productsTable.addCell(new Cell().add(productQuantity).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(productPrice).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(createdBy).setFontSize(12f).setBorder(new SolidBorder(1)));
        productsTable.addCell(new Cell().add(approvedByCustomerAt).setFontSize(12f).setBorder(new SolidBorder(1)));

    }
}
