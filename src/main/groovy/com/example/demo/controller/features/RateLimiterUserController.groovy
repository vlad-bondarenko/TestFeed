package com.example.demo.controller.features

import com.example.demo.BlockingRateLimiter
import com.example.demo.controller.AbstractController
import com.example.demo.model.dto.Response
import com.example.demo.service.UserService
import jakarta.annotation.PreDestroy
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping('/ratelimiter/user')
class RateLimiterUserController extends AbstractController {

    private final UserService userService

    private static final int maxRatePerSecond = 2
    private final BlockingRateLimiter blockingRateLimiter

    RateLimiterUserController(UserService userService) {
        this.userService = userService

        blockingRateLimiter = new BlockingRateLimiter(maxRatePerSecond)
    }

    @GetMapping
    Response<?> allUsers() {
        blockingRateLimiter.acquireAccess()

        return new Response<>(userService.all())
    }

    @PreDestroy
    void destroy() {
        BlockingRateLimiter.terminate()
    }
}
