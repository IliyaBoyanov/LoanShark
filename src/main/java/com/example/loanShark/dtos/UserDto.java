package com.example.loanShark.dtos;

import com.example.loanShark.model.User;

public class UserDto {
    private Long userId;
    private String email;

    public static UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.userId = user.getId();
        userDto.email = user.getEmail();
        return userDto;
    }
}
