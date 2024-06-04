package com.example.demo.service

import com.example.demo.model.User
import com.example.demo.model.dto.UserDto

interface UserService {

    List<UserDto> all()

    Optional<UserDto> findById(long id)

    UserDto save(UserDto user)

    UserDto update(UserDto user)

    boolean delete(long id)

    default User toUser(UserDto dto) {
        new User(id: dto.id, name: dto.name, desc: dto.desc)
    }

}
