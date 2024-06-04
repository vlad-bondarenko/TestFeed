package com.example.demo.controller

import com.example.demo.model.dto.FeedDto
import com.example.demo.model.dto.PostDto
import com.example.demo.model.dto.Response
import com.example.demo.service.PostService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/feed')
class FeedController extends AbstractController {

    private final PostService postService

    FeedController(PostService postService) {
        this.postService = postService
    }

    @GetMapping('/login')
    Response<?> login(@RequestParam(name = "name") String name, @RequestHeader(name = "secret") String secret) {
        try {
            return new Response<Long>(postService.login(name, secret))
        } catch (IllegalArgumentException ignored) {
            return new Response<>(-1, "Wrong secret for user = " + name)
        }
    }

    @GetMapping('/logout')
    Response<?> logout(@RequestParam(name = "sessionId") long sessionId) {
        if (postService.logout(sessionId)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not found sessionId = " + sessionId)
    }

    @PostMapping
    Response<?> savePost(@RequestBody PostDto dto) {
        return new Response<Long>(postService.savePost(dto.sessionId, dto.userId, dto.text))
    }

    @PutMapping
    Response<?> editPost(@RequestBody PostDto dto) {
        if (postService.editPost(dto.sessionId, dto.id, dto.text)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not edit post for id = " + dto.id)
    }

    @DeleteMapping
    Response<?> deletePost(@RequestBody PostDto dto) {
        if (postService.deletePost(dto.sessionId, dto.userId, dto.id)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not delete post for id = " + dto.id)
    }

    @PostMapping('/comment')
    Response<?> commentPost(@RequestBody PostDto dto) {
        if (postService.commentPost(dto.sessionId, dto.id, dto.text)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not create comment for id = " + dto.id)
    }

    @PostMapping('/favorite')
    Response<?> favoritePost(@RequestBody PostDto dto) {
        if (postService.favoritePost(dto.sessionId, dto.id)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not create favorite for id = " + dto.id)
    }

    @PostMapping('/unfavorite')
    Response<?> unfavoritePost(@RequestBody PostDto dto) {
        if (postService.unfavoritePost(dto.sessionId, dto.id)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not create favorite for id = " + dto.id)
    }

    @PostMapping('/like')
    Response<?> likePost(@RequestBody PostDto dto) {
        if (postService.likePost(dto.sessionId, dto.id)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not create favorite for id = " + dto.id)
    }

    @PostMapping('/unlike')
    Response<?> unlikePost(@RequestBody PostDto dto) {
        if (postService.unlikePost(dto.sessionId, dto.id)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not create favorite for id = " + dto.id)
    }

    @PostMapping('/subscribe')
    Response<?> subscribeUser(@RequestBody PostDto dto) {
        if (postService.subscribeUser(dto.sessionId, dto.userId)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not subscribe for user = " + dto.userId)
    }

    @PostMapping('/unsubscribe')
    Response<?> unsubscribeUser(@RequestBody PostDto dto) {
        if (postService.unsubscribeUser(dto.sessionId, dto.userId)) {
            return new Response<Boolean>(Boolean.TRUE)
        }

        return new Response<>(-1, "Not unsubscribe for user = " + dto.userId)
    }

    @GetMapping('/owner')
    Response<?> feeds(@RequestParam(name = "sessionId") long sessionId) {
        return new Response<List<FeedDto>>(postService.feeds(sessionId))
    }

    @GetMapping('/user')
    Response<?> feeds(@RequestParam(name = "sessionId") long sessionId, @RequestParam(name = "userId") long userId) {
        return new Response<List<FeedDto>>(postService.feeds(sessionId, userId))
    }
}
