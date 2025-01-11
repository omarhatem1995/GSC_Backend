package com.gsc.gsc.utilities;


import com.gsc.gsc.model.User;
import com.gsc.gsc.user.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")

public interface CustomMapper {
    CustomMapper INSTANCE = Mappers.getMapper(CustomMapper.class);

    User userToCustomer(LoginDTO entity);


    User userDtoToUser(UserCreateDTO userDto);

    UserCreateResponseDTO UserToUserCreateDTO(User user);
    UserDTO UserToUserDTO(User user);

    @Mapping(target = "cookieExpiry", ignore = true)
    void updateLoginResponseDTOFromUser(User user, @MappingTarget LoginResponseDTO loginResponseDTO);

//    UserSalesResultDTO UserToUserSalesResultDTO(User user);


}