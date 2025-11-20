package com.bookingcare.account.mapper;

import com.bookingcare.account.dto.UpdateProfileRequest;
import com.bookingcare.account.dto.UserDTO;
import com.bookingcare.account.entity.Gender;
import com.bookingcare.account.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class UsersMapper {

    public UserDTO toUsersDTO(Users users) {
        if (users == null) {
            return null;
        }

        return new UserDTO(
                users.getId(),
                users.getFullname(),
                users.getBirthdate(),
                users.getEmail(),
                users.getPhone(),
                users.getAddress(),
                users.getGender(),
                users.getImage(),
                users.isDeleted()
        );
    }

    public Users toUsersEntity(UpdateProfileRequest request) {
        if (request == null) {
            return null;
        }

        Users users = new Users();
        users.setGender(stringToGender(request.gender()));
        users.setFullname(request.fullname());
        users.setEmail(request.email());
        users.setPhone(request.phone());
        users.setAddress(request.address());
        users.setImage(request.image());
        users.setBirthdate(request.birthdate());
        return users;
    }

    public void updateUsersFromRequest(UpdateProfileRequest request, Users users) {
        if (request == null || users == null) {
            return;
        }

        users.setGender(stringToGender(request.gender()));
        users.setFullname(request.fullname());
        users.setEmail(request.email());
        users.setPhone(request.phone());
        users.setAddress(request.address());
        users.setImage(request.image());
        users.setBirthdate(request.birthdate());
    }

    private Gender stringToGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
