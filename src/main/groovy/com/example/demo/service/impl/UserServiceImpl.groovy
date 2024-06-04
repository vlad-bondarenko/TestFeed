package com.example.demo.service.impl

import com.example.demo.model.User
import com.example.demo.model.dto.UserDto
import com.example.demo.repository.UserRepository
import com.example.demo.service.IdGenerator
import com.example.demo.service.UserService
import org.springframework.stereotype.Service

@Service
class UserServiceImpl implements UserService {

    private final UserRepository userRepository

    UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository
    }

    @Override
    List<UserDto> all() {
        def users = userRepository.findAll() as List<User>
        if (users == null || users.is()) {
            return Collections.EMPTY_LIST
        }

        def result = new ArrayList(users.size())
        for (final def user in users) {
            result.add(UserDto.of(user))
        }

        return result
    }

    @Override
    Optional<UserDto> findById(long id) {
        return userRepository.findById(id).map(user -> UserDto.of(user))
    }

    @Override
    UserDto save(UserDto dto) {
        if (userRepository.findByName(dto.name).isPresent()) {
            return null
        }

        def user = toUser(dto)
        user.id = IdGenerator.INSTANCE.id()

        return UserDto.of(userRepository.save(user))
    }

    @Override
    UserDto update(UserDto dto) {
        if (dto.id == 0) {
            return null
        }

        def optional = userRepository.findById(dto.id)
        if (optional.isEmpty()) {
            return null
        }

        return UserDto.of(userRepository.save(toUser(dto)))
    }

    @Override
    boolean delete(long id) {
        def optional = userRepository.findById(id)
        if (optional.isEmpty()) {
            return false
        }

        userRepository.deleteById(id)

        return true
    }
}
