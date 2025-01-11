package com.gsc.gsc.repo;

import com.gsc.gsc.model.User;
import com.gsc.gsc.user.dto.GetAllUsers;
import com.gsc.gsc.user.dto.LoginResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    User findUserById(Integer userId);
    User findByMailAndPhone(String mail,String phone);
    User findByMail(String mail);
    User findByPhone(String phone);
    Optional<User> findOptionalByPhone(String phone);
    Optional<User> findOptionalByPhoneAndMail(String phone,String mail);
    @Query("SELECT new com.gsc.gsc.user.dto.GetAllUsers(u, SUM(p.pointsNumber)) FROM User u LEFT JOIN Point p ON u.id = p.userId WHERE u.accountTypeId <> 2 GROUP BY u.id")
    List<GetAllUsers> findAllExceptAdmins();
    @Query("select new com.gsc.gsc.user.dto.LoginResponseDTO(u) from User u where u.id = ?1")
    LoginResponseDTO getLoginDTOByUserId(Integer userId);
}