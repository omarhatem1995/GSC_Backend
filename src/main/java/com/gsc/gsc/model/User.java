package com.gsc.gsc.model;

import com.gsc.gsc.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id")
    private Integer id;
    @Basic
    @Column(name = "name")
    private String name;
    @Basic
    @Column(name = "account_type_id")
    private Integer accountTypeId;
    @Basic
    @Column(name = "age")
    private String age;
    @Basic
    @Column(name = "profile_image")
    private String profileImage;
    @Basic
    @Column(name = "phone")
    private String phone;
    @Basic
    @Column(name = "password")
    private String password;
    @Basic
    @Column(name = "mail")
    private String mail;
    @Column(name = "is_verified")
    private Boolean isVerified;
    @Column(name = "is_active")
    private Integer isActive;
    @Column(name = "firebase_token")
    private String firebaseToken;
    @Basic
    @Column(name = "commercial_registry")
    private String commercialRegistry;
    @Basic
    @Column(name = "commercial_license")
    private String commercialLicense;
    @Basic
    @Column(name = "establishment_registration")
    private String establishmentRegistration;
    @Basic
    @Column(name = "tax_card")
    private String taxCard;
    @Basic
    @Column(name = "mailbox")
    private String mailbox;
    @Basic
    @Column(name = "address")
    private String address;
    @Column(name = "verification_otp")
    private String verificationOTP;
    @Basic
    @Column(name = "created_at")
    private Timestamp createdAt;
    @Basic
    @Column(name = "updated_at")
    private Timestamp updatedAt;
    @Basic
    @Column(name = "last_logout_at")
    private Timestamp lastLogoutAt;

    public User(UserDTO userDTO) {
        if(userDTO.getId()!=null)
        this.id = userDTO.getId();
        this.address = userDTO.getAddress();
        this.mail = userDTO.getMail();
        this.age = userDTO.getAge();
        this.accountTypeId = userDTO.getCustomerType();
        this.establishmentRegistration = userDTO.getEstablishmentRegistration();
        this.commercialRegistry = userDTO.getCommercialRegistry();
        this.mailbox = userDTO.getMailBox();
        this.password = userDTO.getPassword();
        this.commercialLicense = userDTO.getCommercialLicense();
        this.name = userDTO.getName();
        this.profileImage = userDTO.getProfileImage();
        this.taxCard = userDTO.getTaxCard();
        this.phone = userDTO.getPhone();
        this.verificationOTP = userDTO.getVerificationOTP();
    }
}
