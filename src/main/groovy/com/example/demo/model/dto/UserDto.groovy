package com.example.demo.model.dto

import com.example.demo.model.User

class UserDto {

    Long id

    String name

    String desc

    static UserDto of(User user) {
        new UserDto(id: user.id, name: user.name, desc: user.desc)
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (o == null || getClass() != o.class) return false

        UserDto user = (UserDto) o

        if (desc != user.desc) return false
        if (name != user.name) return false

        return true
    }

    int hashCode() {
        int result
        result = (name != null ? name.hashCode() : 0)
        result = 31 * result + (desc != null ? desc.hashCode() : 0)
        return result
    }
}
