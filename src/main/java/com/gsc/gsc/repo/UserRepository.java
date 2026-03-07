package com.gsc.gsc.repo;

import com.gsc.gsc.model.User;
import com.gsc.gsc.user.dto.GetAllUsers;
import com.gsc.gsc.user.dto.LoginResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserById(Integer userId);

    User findByMailAndPhone(String mail, String phone);

    User findByMail(String mail);

    User findByPhone(String phone);

    Optional<User> findOptionalByPhone(String phone);

    Optional<User> findOptionalByPhoneAndMail(String phone, String mail);

    @Query("SELECT new com.gsc.gsc.user.dto.GetAllUsers(u, SUM(p.pointsNumber)) FROM User u LEFT JOIN Point p ON u.id = p.userId WHERE u.accountTypeId <> 2 GROUP BY u.id")
    List<GetAllUsers> findAllExceptAdmins();

    @Query("select new com.gsc.gsc.user.dto.LoginResponseDTO(u) from User u where u.id = ?1")
    LoginResponseDTO getLoginDTOByUserId(Integer userId);


    @Query("SELECT u FROM User u " +
            "WHERE ( " +
            "    (:accountTypeId IS NULL AND u.accountTypeId <> 3) OR " + // no filter → exclude admin
            "    (:accountTypeId = 1 AND u.accountTypeId = 1) OR " +      // type 1
            "    (:accountTypeId = 2 AND u.accountTypeId = 2) OR " +      // type 2
            "    (:accountTypeId = 3 AND u.accountTypeId = 3) " +         // admins
            ") " +
            "AND (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%'))")
    Page<User> findAllWithFilters(
            @Param("accountTypeId") Integer accountTypeId,
            @Param("name") String name,
            @Param("phone") String phone,
            Pageable pageable
    );


}