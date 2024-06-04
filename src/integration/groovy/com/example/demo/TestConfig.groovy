package com.example.demo

import com.example.demo.repository.*
import com.example.demo.service.PostService
import com.example.demo.service.UserService
import com.example.demo.service.UserSessionService
import com.example.demo.service.impl.PostServiceImpl
import com.example.demo.service.impl.UserServiceImpl
import com.example.demo.service.impl.UserSessionServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TestConfig {

    @Bean
    static UserService userService(UserRepository userRepository) {
        new UserServiceImpl(userRepository)
    }

    @Bean
    static UserSessionService userSessionService(UserSessionRepository userSessionRepository) {
        new UserSessionServiceImpl(userSessionRepository)
    }

    @Bean
    static PostService postService(UserRepository userRepository,
                                   UserSessionService userSessionService,
                                   PostRepository postRepository,
                                   PostCommentRepository postCommentRepository,
                                   PostFavoriteRepository postFavoriteRepository,
                                   PostLikeRepository postLikeRepository,
                                   SubscriptionRepository subscriptionRepository) {
        new PostServiceImpl(userRepository,
                userSessionService,
                postRepository,
                postCommentRepository,
                postFavoriteRepository,
                postLikeRepository,
                subscriptionRepository)
    }

}
