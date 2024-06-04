package com.example.demo.controller

import com.example.demo.model.dto.Response
import com.example.demo.model.dto.UserDto
import com.example.demo.service.UserService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/user')
class UserController extends AbstractController {

    private final UserService userService

    UserController(UserService userService) {
        this.userService = userService
    }

    @GetMapping
    Response<?> allUsers() {
        new Response<>(userService.all())
    }

    @PostMapping
    Response<?> registerUser(@RequestBody UserDto dto) {
        def result = userService.save(dto)
        if (result == null) {
            return new Response<>(-1, "Not register user = " + dto.name)
        }

        return new Response<>(result)
    }

    @PutMapping
    Response<?> updateUser(@RequestBody UserDto dto) {
        UserDto user = userService.update(dto)
        if (user == null) {
            return new Response<>(-1, "Not found user for id = " + dto.id)
        }

        return new Response<>(user)
    }

    @DeleteMapping('/{id}')
    deleteUser(@PathVariable("id") long id) {
        if (userService.delete(id)) {
            return new Response<>(null)
        }

        new Response<>(-1, "Not found user for id = " + id)
    }

    @GetMapping('/{id}')
    Response<?> userById(@PathVariable("id") long id) {
        def optional = userService.findById(id)
        if (optional.isPresent()) {
            return new Response<>(optional.get())
        }

        new Response<>(-1, "Not found user for id = " + id)
    }
}
