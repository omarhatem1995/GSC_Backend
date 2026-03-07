package com.gsc.gsc.user.dto;

import com.gsc.gsc.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllUsers {
    Integer id;
    String name;
    Integer accountTypeId;
    String mail;
    String commercialRegistry;
    String commercialLicense;
    String establishmentRegistration;
    String taxCard;
    String mailbox;
    String address;
    String phone;
    Long points;
    Integer isActive;

    public GetAllUsers(User user,Long totalPoints){
        this.id = user.getId();
        this.name = user.getName();
        this.accountTypeId = user.getAccountTypeId();
        this.mail = user.getMail();
        this.commercialLicense = user.getCommercialLicense();
        this.commercialRegistry = user.getCommercialRegistry();
        this.establishmentRegistration = user.getEstablishmentRegistration();
        this.taxCard = user.getTaxCard();
        this.mailbox = user.getMailbox();
        this.address = user.getAddress();
        this.points = totalPoints;
        this.phone = user.getPhone();
    }

}
