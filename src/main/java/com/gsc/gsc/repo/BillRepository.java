package com.gsc.gsc.repo;

import com.gsc.gsc.bill.dto.BillProductsDTO;
import com.gsc.gsc.bill.dto.GetBillsDTO;
import com.gsc.gsc.model.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Integer> {
    Optional<List<Bill>> findAllByUserId(Integer userId);
    List<Bill> findAllByReferenceNumber(String referenceNumber);
    Optional<Bill> findTopByOrderByIdDesc();
    Optional<Bill> findTopByReferenceNumberStartingWithOrderByReferenceNumberDesc(String prefix);
    @Query(
            "SELECT new com.gsc.gsc.bill.dto.GetBillsDTO(" +
                    "b.id, " +
                    "b.referenceNumber, " +
                    "b.userId, " +
                    "bs.code, " +
                    "bs.code, " +
                    "b.createdBy, " +
                    "b.total, " +
                    "b.discount, " +
                    "b.createdAt, " +
                    "b.adminNotes, " +
                    "b.customerNotes, " +
                    "c.licenseNumber, " +
                    "c.licenseNumber, " +
                    "bt.code, " +
                    "bt.code, " +
                    "bt.code," +
                    "u.name," +
                    "u.accountTypeId, " +
                    "b.finalTotalPrice" +
                    ") " +
                    "FROM Bill b " +
                    "LEFT JOIN User u ON b.userId = u.id " +
                    "LEFT JOIN Car c ON b.carId = c.id " +
                    "LEFT JOIN BillType bt ON b.billTypeId = bt.id " +
                    "LEFT JOIN CBillStatus bs ON b.statusId = bs.id " +
                    "WHERE (:userId IS NULL OR b.userId = :userId) " +
                    "AND (:search IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
                    "AND (:billNumber IS NULL OR b.referenceNumber LIKE CONCAT('%', :billNumber, '%')) " +
                    "AND (:fromDate IS NULL OR b.createdAt >= :fromDate) " +
                    "AND (:toDate IS NULL OR b.createdAt <= :toDate) " +
                    "ORDER BY b.createdAt DESC"  // <-- newest first
    )
    Page<GetBillsDTO> findByFilters(
            @Param("userId") Integer userId,
            @Param("search") String search,
            @Param("billNumber") String billNumber,
            @Param("fromDate") Timestamp fromDate,
            @Param("toDate") Timestamp toDate,
            Pageable pageable
    );
    @Query(value = "SELECT NEW com.gsc.gsc.bill.dto.GetBillsDTO(" +
            "b.id, b.referenceNumber, b.userId, " +
            "bst.name, bs.code, b.createdBy, b.total, b.discount, b.createdAt, " +
            "b.adminNotes, b.customerNotes, c.licenseNumber, " +
            "m.code, btt.name, bt.code, btt.description) " +
            "FROM Bill b " +
            "LEFT JOIN Car c ON b.carId = c.id " +
            "LEFT JOIN Model m ON c.modelId = m.id " +
            "LEFT JOIN CBillStatus bs ON b.statusId = bs.id " +
            "LEFT JOIN BillType bt ON bt.id = b.billTypeId " +
            "LEFT JOIN BillTypeText btt ON btt.billTypeId = bt.id " +
            "LEFT JOIN CBillsStatusText bst ON bs.id = bst.billStatusId " +
            "LEFT JOIN Lang l ON l.id = bst.langId " +
            "WHERE b.userId = :userId AND bst.langId = :langId AND btt.langId = :langId " +
            "ORDER BY b.createdAt DESC")
    Page<GetBillsDTO> findAllByUserIdAndLangId(@Param("userId") Integer userId,
                                               @Param("langId") Integer langId,
                                               Pageable pageable);
    @Query("SELECT NEW com.gsc.gsc.bill.dto.GetBillsDTO(b.id, b.referenceNumber, b.userId, " + "bst.name, bs.code, b.createdBy, b.total, b.discount, b.createdAt, " + "b.adminNotes, b.customerNotes, c.licenseNumber, " + "m.code, btt.name, bt.code, btt.description) " + "FROM Bill b " + "LEFT JOIN Car c ON b.carId = c.id " + "LEFT JOIN Model m ON c.modelId = m.id " + "LEFT JOIN CBillStatus bs ON b.statusId = bs.id " + "LEFT JOIN BillType bt ON bt.id = b.billTypeId " + "LEFT JOIN BillTypeText btt ON btt.billTypeId = bt.id " + "LEFT JOIN CBillsStatusText bst ON bs.id = bst.billStatusId " + "LEFT JOIN Lang l ON l.id = bst.langId " + "WHERE b.userId = :userId AND bst.langId = :langId AND btt.langId = :langId ORDER BY b.createdAt DESC")
    Optional<List<GetBillsDTO>> findAllByUserIdAndLangId(Integer userId, Integer langId);

    @Query(value = "SELECT NEW com.gsc.gsc.bill.dto.GetBillsDTO(" +
            "b.id, b.referenceNumber, b.userId, " +
            "bst.name, bs.code, b.createdBy, b.total, b.discount, b.createdAt, " +
            "b.adminNotes, b.customerNotes, c.licenseNumber, " +
            "m.code, btt.name, bt.code, btt.description) " +
            "FROM Bill b " +
            "LEFT JOIN User u ON u.id = b.userId " +
            "LEFT JOIN Car c ON b.carId = c.id " +
            "LEFT JOIN Model m ON c.modelId = m.id " +
            "LEFT JOIN CBillStatus bs ON b.statusId = bs.id " +
            "LEFT JOIN BillType bt ON bt.id = b.billTypeId " +
            "LEFT JOIN BillTypeText btt ON btt.billTypeId = bt.id " +
            "LEFT JOIN CBillsStatusText bst ON bs.id = bst.billStatusId " +
            "LEFT JOIN Lang l ON l.id = bst.langId " +
            "WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "AND bst.langId = :langId AND btt.langId = :langId " +
            "ORDER BY b.createdAt DESC")
    Page<GetBillsDTO> findAllByCustomerNameContainingIgnoreCaseAndLangId(@Param("search") String search,
                                                                         @Param("langId") Integer langId,
                                                                         Pageable pageable);

    @Query("SELECT NEW com.gsc.gsc.bill.dto.GetBillsDTO(b.id, b.referenceNumber, b.userId, " +
            "bst.name, bs.code, b.createdBy, b.total, b.discount, b.createdAt, " +
            "b.adminNotes, b.customerNotes, c.licenseNumber, " +
            "m.code, btt.name, bt.code, btt.description) " +
            "FROM Bill b " +
            "LEFT JOIN Car c on b.carId = c.id " +
            "LEFT JOIN Model m on c.modelId = m.id " +
            "JOIN CBillStatus bs on b.statusId = bs.id " +
            "JOIN BillType bt on bt.id = b.billTypeId " +
            "JOIN BillTypeText btt on btt.billTypeId = bt.id " +
            "JOIN CBillsStatusText bst on bs.id = bst.billStatusId " +
            "JOIN Lang l on l.id = bst.langId " +
            "WHERE bst.langId = :langId AND btt.langId = :langId ORDER BY b.createdAt DESC")
    Page<GetBillsDTO> findAllByLangId(Integer langId, Pageable pageable);
    @Query("SELECT NEW com.gsc.gsc.bill.dto.GetBillsDTO(b.id, b.referenceNumber, b.userId, " +
            "bst.name, bs.code, b.createdBy, b.total, b.discount, b.createdAt, " +
            "b.adminNotes, b.customerNotes, c.licenseNumber, " +
            "m.code, btt.name, bt.code, btt.description) " +
            "FROM Bill b " +
            "LEFT JOIN Car c on b.carId = c.id " +
            "LEFT JOIN Model m on c.modelId = m.id " +
            "JOIN CBillStatus bs on b.statusId = bs.id " +
            "JOIN BillType bt on bt.id = b.billTypeId " +
            "JOIN BillTypeText btt on btt.billTypeId = bt.id " +
            "JOIN CBillsStatusText bst on bs.id = bst.billStatusId " +
            "JOIN Lang l on l.id = bst.langId " +
            "WHERE bst.langId = :langId AND btt.langId = :langId AND b.userId =:userId ORDER BY b.createdAt DESC")
    Page<GetBillsDTO> findAllByLangIdAndUserId(Integer langId, Integer userId ,Pageable pageable);


    Page<Bill> findAll(Pageable pageable);

    @Query("SELECT COUNT(b) FROM Bill b")
    long countOfAllBills();


}