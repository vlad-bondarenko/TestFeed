package com.example.demo.controller

import com.example.demo.TestConfig
import com.example.demo.model.Post
import com.example.demo.model.User
import com.example.demo.model.UserSession
import com.example.demo.model.dto.FeedDto
import com.example.demo.model.dto.PostDto
import com.example.demo.model.dto.Response
import com.example.demo.repository.PostRepository
import com.example.demo.repository.UserRepository
import com.example.demo.repository.UserSessionRepository
import com.example.demo.service.IdGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import spock.lang.Specification

import java.util.concurrent.CompletableFuture

import static org.junit.jupiter.api.Assertions.*

@ActiveProfiles("integration_test")
@AutoConfigureMockMvc
@WebMvcTest
@Import(TestConfig)
@AutoConfigureDataMongo()
class FeedControllerTest extends Specification {

    @Autowired
    MockMvc mvc

    @Autowired
    UserRepository userRepository

    @Autowired
    UserSessionRepository userSessionRepository

    @Autowired
    PostRepository postRepository

    @Autowired
    ObjectMapper objectMapper

    void setup() {
        userRepository.deleteAll()
    }

    void cleanup() {
    }

    def "Login"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        when: "Status is 200 and the response"
        def response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        then: "Find with repository"
        def userSessions = userSessionRepository.findAll()

        def sessionId = 0
        for (final def userSession in userSessions) {
            if (userSession.userId == user.id) {
                sessionId = userSession.id

                break
            }
        }
        assertEquals(sessionId, responseObj.data)
    }

    def "Logout"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def userSession = new UserSession(id: IdGenerator.INSTANCE.id(), userId: user.id)
        userSessionRepository.save(userSession)

        when: "Status is 200 and the response"
        def response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/logout")
                        .param("sessionId", "" + userSession.id)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<Boolean>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)
        assertTrue(responseObj.data)

        then: "Find with repository"
        assertTrue(userSessionRepository.findById(userSession.id).isEmpty())
    }

    def "SavePost"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def destUser = new User(id: IdGenerator.INSTANCE.id(), name: "destUser", desc: "desc")
        userRepository.save(destUser)

        //login
        def response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        def sessionId = responseObj.data

        def dto = new PostDto(sessionId: sessionId, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())

        when: "Status is 200 and the response"
        //save post
        response = mvc.perform(MockMvcRequestBuilders.post("/feed")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})
        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        def postId = responseObj.data

        then: "Find with repository"
        def optional = postRepository.findById(postId)

        assertTrue(optional.isPresent())

        def post = optional.get()
        assertEquals(dto.userId, post.userId)
        assertEquals(dto.text, post.text)

        assertEquals(user.id, post.ownerId)
    }

    def "EditPost"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def destUser = new User(id: IdGenerator.INSTANCE.id(), name: "destUser", desc: "desc")
        userRepository.save(destUser)

        def post = new Post(id: IdGenerator.INSTANCE.id(), ownerId: user.id, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())
        postRepository.save(post)

        //login
        def response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        def sessionId = responseObj.data

        def dto = new PostDto(sessionId: sessionId, id: post.id, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())

        when: "Status is 200 and the response"
        //edit post
        response = mvc.perform(MockMvcRequestBuilders.put("/feed")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        responseObj = objectMapper.readValue(response, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)
        assertTrue(responseObj.data)

        then: "Find with repository"
        def optional = postRepository.findById(dto.id)

        assertTrue(optional.isPresent())

        def newPost = optional.get()
        assertEquals(dto.userId, newPost.userId)
        assertEquals(dto.text, newPost.text)

        assertEquals(user.id, newPost.ownerId)
    }

    def "EditPost multiple"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def destUser = new User(id: IdGenerator.INSTANCE.id(), name: "destUser", desc: "desc")
        userRepository.save(destUser)

        def post = new Post(id: IdGenerator.INSTANCE.id(), ownerId: user.id, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())
        postRepository.save(post)

        //login1
        def response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        def sessionId1 = responseObj.data

        def dto1 = new PostDto(sessionId: sessionId1, id: post.id, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())

        //login1
        response = mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        responseObj = objectMapper.readValue(response, new TypeReference<Response<Long>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        def sessionId2 = responseObj.data

        def dto2 = new PostDto(sessionId: sessionId2, id: post.id, userId: destUser.id, text: "text" + IdGenerator.INSTANCE.id())

        assertFalse(sessionId1 == sessionId2)
        assertFalse(dto1.text.equals(dto2.text))

        when: "Status is 200 and the response"
        //edit post
        CompletableFuture.allOf(
                CompletableFuture.supplyAsync {
                    def response1 = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.put("/feed")
                            .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto1)))
                            .andExpect(MockMvcResultMatchers.status().isOk())
                            .andReturn()
                            .response.contentAsString, new TypeReference<Response<Boolean>>() {})
                    assertEquals(0, response1.code)
                    assertNull(response1.reason)
                    assertNotNull(response1.data)
                    assertTrue(response1.data)
                }, CompletableFuture.supplyAsync {
            def response2 = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.put("/feed")
                    .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto2)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .response.contentAsString, new TypeReference<Response<Boolean>>() {})
            assertEquals(0, response2.code)
            assertNull(response2.reason)
            assertNotNull(response2.data)
            assertTrue(response2.data)
        }).get()

        then: "Find with repository"
        def optional = postRepository.findById(post.id)

        assertTrue(optional.isPresent())

        def newPost = optional.get()
        assertEquals(dto1.userId, newPost.userId)
        assertEquals(dto1.userId, dto2.userId)

        assertTrue(dto1.text.equals(newPost.text) || dto2.text.equals(newPost.text))

        assertEquals(user.id, newPost.ownerId)
    }

    def "DeletePost"() {
    }

    def "CommentPost"() {
    }

    def "FavoritePost"() {
    }

    def "UnfavoritePost"() {
    }

    def "LikePost"() {
    }

    def "UnlikePost"() {
    }

    def "SubscribeUser"() {
    }

    def "UnsubscribeUser"() {
    }

    def "DeleteUser"() {
    }

    def "UserById"() {
    }

    def "Feeds"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def destUser = new User(id: IdGenerator.INSTANCE.id(), name: "destUser", desc: "desc")
        userRepository.save(destUser)

        def otherUser = new User(id: IdGenerator.INSTANCE.id(), name: "otherUser", desc: "desc")
        userRepository.save(otherUser)


        //login all users
        //login user
        def response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def userSessionId = response.data


        //login destUser
        response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", destUser.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def destUserSessionId = response.data


        //login otherUser
        response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", otherUser.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def otherUserSessionId = response.data


        //user subscribed to destUser
        def subscribeDto = new PostDto(sessionId: userSessionId, userId: destUser.id)
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/subscribe")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(subscribeDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        //user created post
        def postDto = new PostDto(sessionId: userSessionId, userId: user.id, text: "text" + IdGenerator.INSTANCE.id())
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(postDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def postId = response.data


        //destUser commented post of user
        def commentDto = new PostDto(sessionId: destUserSessionId, userId: user.id, id: postId, text: "text" + IdGenerator.INSTANCE.id())
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/comment")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        //destUser liked post of user
        def likeDto = new PostDto(sessionId: destUserSessionId, id: postId)
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/like")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        when: "Status is 200 and the response"
        //feed
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.get("/feed/owner")
                .param("sessionId", "" + userSessionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<List<FeedDto>>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        then: "Analyze feed"
        def feeds = response.data
        assertEquals(1, feeds.size())

        def feedDto = feeds.get(0)
        assertEquals(postId, feedDto.id)
        assertEquals(postDto.text, feedDto.text)

        def userComments = feedDto.userComments
        assertNotNull(userComments)
        assertEquals(1, userComments.size())
        def userIdComment = userComments.keySet().iterator().next()
        assertEquals(destUser.id, userIdComment)
        assertEquals(commentDto.text, userComments.get(userIdComment))

        def userLikes = feedDto.userLikes
        assertNotNull(userLikes)
        assertEquals(1, userLikes.size())
        assertEquals(destUser.id, userLikes.iterator().next())


        //feed for not subscribed user
        def responseOther = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.get("/feed/owner")
                .param("sessionId", "" + otherUserSessionId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<List<FeedDto>>>() {})
        assertEquals(0, responseOther.code)
        assertNull(responseOther.reason)
        assertNotNull(responseOther.data)

        assertEquals(0, responseOther.data.size())
    }

    def "Feeds user"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        userRepository.save(user)

        def destUser = new User(id: IdGenerator.INSTANCE.id(), name: "destUser", desc: "desc")
        userRepository.save(destUser)

        def otherUser = new User(id: IdGenerator.INSTANCE.id(), name: "otherUser", desc: "desc")
        userRepository.save(otherUser)


        //login all users
        //login user
        def response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", user.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def userSessionId = response.data


        //login destUser
        response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", destUser.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def destUserSessionId = response.data


        //login otherUser
        response = objectMapper.readValue(mvc.perform(
                MockMvcRequestBuilders.get("/feed/login")
                        .param("name", otherUser.name)
                        .header("secret", IdGenerator.INSTANCE.id())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})

        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def otherUserSessionId = response.data


        //user subscribed to destUser
        def subscribeDto = new PostDto(sessionId: userSessionId, userId: destUser.id)
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/subscribe")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(subscribeDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        //user created post
        def postDto = new PostDto(sessionId: userSessionId, userId: user.id, text: "text" + IdGenerator.INSTANCE.id())
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(postDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Long>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        def postId = response.data


        //destUser commented post of user
        def commentDto = new PostDto(sessionId: destUserSessionId, userId: user.id, id: postId, text: "text" + IdGenerator.INSTANCE.id())
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/comment")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        //destUser liked post of user
        def likeDto = new PostDto(sessionId: destUserSessionId, id: postId)
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.post("/feed/like")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(likeDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<Boolean>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)
        assertTrue(response.data)


        when: "Status is 200 and the response"
        //feed
        response = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.get("/feed/user")
                .param("sessionId", "" + destUserSessionId)
                .param("userId", "" + user.id)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<List<FeedDto>>>() {})
        assertEquals(0, response.code)
        assertNull(response.reason)
        assertNotNull(response.data)

        then: "Analyze feed"
        def feeds = response.data
        assertEquals(1, feeds.size())

        def feedDto = feeds.get(0)
        assertEquals(postId, feedDto.id)
        assertEquals(postDto.text, feedDto.text)

        def userComments = feedDto.userComments
        assertNotNull(userComments)
        assertEquals(1, userComments.size())
        def userIdComment = userComments.keySet().iterator().next()
        assertEquals(destUser.id, userIdComment)
        assertEquals(commentDto.text, userComments.get(userIdComment))

        def userLikes = feedDto.userLikes
        assertNotNull(userLikes)
        assertEquals(1, userLikes.size())
        assertEquals(destUser.id, userLikes.iterator().next())


        //feed for not subscribed user
        def responseOther = objectMapper.readValue(mvc.perform(MockMvcRequestBuilders.get("/feed/user")
                .param("sessionId", "" + otherUserSessionId)
                .param("userId", "" + otherUserSessionId)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString, new TypeReference<Response<List<FeedDto>>>() {})
        assertEquals(0, responseOther.code)
        assertNull(responseOther.reason)
        assertNotNull(responseOther.data)

        assertEquals(0, responseOther.data.size())
    }
}
