package com.example.demo.controller.features

import com.example.demo.controller.AbstractController
import com.example.demo.model.dto.Response
import com.example.demo.model.dto.UserDto
import com.example.demo.service.UserService
import jakarta.annotation.PreDestroy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import java.util.concurrent.*

@RestController
@RequestMapping('/timeout/user')
class TimeoutUserController extends AbstractController {

    private final UserService userService

    private final long timeout = TimeUnit.SECONDS.toMillis(2)

    private final ExecutorService executorService = Executors.newCachedThreadPool()

    TimeoutUserController(UserService userService) {
        this.userService = userService
    }

    @GetMapping
    Response<?> allUsers() {
        Future<List<UserDto>> future = executorService.submit((() -> userService.all()) as Callable<List<UserDto>>)

        try {
            return new Response<>(future.get(timeout, TimeUnit.MILLISECONDS))
        } catch (TimeoutException ignored) {
        }

        return new Response<>(-1, "Operation timeout")
    }

    @PreDestroy
    void destroy() {
        executorService.shutdown()
    }
}
