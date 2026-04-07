package com.thechakra.backend.dto;

import com.thechakra.backend.entity.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-07T19:00:55+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        UserDto.UserDtoBuilder userDto = UserDto.builder();

        userDto.id( user.getId() );
        userDto.name( user.getName() );
        userDto.email( user.getEmail() );
        userDto.phoneNumber( user.getPhoneNumber() );
        userDto.role( user.getRole() );
        userDto.chakraAlignment( user.getChakraAlignment() );
        userDto.assignedAdminId( user.getAssignedAdminId() );
        userDto.assignedAdminName( user.getAssignedAdminName() );

        return userDto.build();
    }

    @Override
    public User toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( userDto.getId() );
        user.name( userDto.getName() );
        user.email( userDto.getEmail() );
        user.phoneNumber( userDto.getPhoneNumber() );
        user.role( userDto.getRole() );
        user.chakraAlignment( userDto.getChakraAlignment() );
        user.assignedAdminId( userDto.getAssignedAdminId() );
        user.assignedAdminName( userDto.getAssignedAdminName() );

        return user.build();
    }
}
