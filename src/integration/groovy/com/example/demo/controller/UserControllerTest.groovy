package com.example.demo.controller

import com.example.demo.TestConfig
import com.example.demo.model.User
import com.example.demo.model.dto.Response
import com.example.demo.model.dto.UserDto
import com.example.demo.repository.UserRepository
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

import static org.junit.jupiter.api.Assertions.*

@ActiveProfiles("integration_test")
@AutoConfigureMockMvc
@WebMvcTest
@Import(TestConfig)
@AutoConfigureDataMongo()
class UserControllerTest extends Specification {

    @Autowired
    MockMvc mvc

    @Autowired
    UserRepository userRepository

    @Autowired
    ObjectMapper objectMapper

    void setup() {
        userRepository.deleteAll()
    }

    void cleanup() {
    }

    def "All users"() {
        given:
        def user1 = new User(id: IdGenerator.INSTANCE.id(), name: "user1", desc: "desc1")
        def user2 = new User(id: IdGenerator.INSTANCE.id(), name: "user2", desc: "desc2")
        userRepository.save(user1)
        userRepository.save(user2)

        expect: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.get("/user"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<UserDto[]>>() {})

        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        assertArrayEquals(new UserDto[]{UserDto.of(user1), UserDto.of(user2)}, responseObj.data)
    }

    def "Register user"() {
        given:
        def user1 = new User(name: "user1", desc: "desc1")
        def user2 = new User(name: "user2", desc: "desc2")

        when: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(UserDto.of(user1))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj1 = objectMapper.readValue(response, new TypeReference<Response<UserDto>>() {})
        assertEquals(0, responseObj1.code)
        assertNull(responseObj1.reason)
        assertNotNull(responseObj1.data)
        assertEquals(responseObj1.data, UserDto.of(user1))

        response = mvc.perform(MockMvcRequestBuilders.post("/user")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(UserDto.of(user2))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj2 = objectMapper.readValue(response, new TypeReference<Response<UserDto>>() {})
        assertEquals(0, responseObj2.code)
        assertNull(responseObj2.reason)
        assertNotNull(responseObj2.data)

        assertEquals(responseObj2.data, UserDto.of(user2))

        then: "Find user with repository"
        def actualUser1 = userRepository.findById(responseObj1.data.id)
        assertEquals(user1, actualUser1.orElse(null))

        def actualUser2 = userRepository.findById(responseObj2.data.id)
        assertEquals(user2, actualUser2.orElse(null))
    }

    def "Update not exists user"() {
        given:
        def user = new User(id: Long.MIN_VALUE, name: "user", desc: "desc")
        assertTrue(userRepository.findById(user.id).isEmpty())

        expect: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.put("/user")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(UserDto.of(user))))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, Response)

        assertEquals(-1, responseObj.code)
        assertFalse(responseObj.reason.isBlank())
        assertNull(responseObj.data)
    }

    def "Update exists user"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        assertTrue(userRepository.save(user) != null)

        def dto = UserDto.of(user)
        dto.name = dto.name + "_edit"
        dto.desc = dto.desc + "_edit"

        when: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.put("/user")
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<UserDto>>() {})
        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)
        assertEquals(responseObj.data, dto)

        then: "Find user with repository"
        def actualUser = userRepository.findById(dto.id)
        assertEquals(dto, actualUser.map(v -> UserDto.of(v)).orElse(null))
    }

    def "Delete not exists user"() {
        given:
        def user = new User(id: Long.MIN_VALUE, name: "user", desc: "desc")
        assertTrue(userRepository.findById(user.id).isEmpty())

        expect: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.delete("/user/" + user.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, Response)

        assertEquals(-1, responseObj.code)
        assertFalse(responseObj.reason.isBlank())
        assertNull(responseObj.data)
    }

    def "Delete exists user"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        assertTrue(userRepository.save(user) != null)

        when: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.delete("/user/" + user.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<UserDto>>() {})
        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNull(responseObj.data)

        then: "Find user with repository"
        assertTrue(userRepository.findById(user.id).isEmpty())
    }

    def "Find not exists user"() {
        given:
        def user = new User(id: Long.MIN_VALUE, name: "user", desc: "desc")
        assertTrue(userRepository.findById(user.id).isEmpty())

        expect: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.get("/user/" + user.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, Response)

        assertEquals(-1, responseObj.code)
        assertFalse(responseObj.reason.isBlank())
        assertNull(responseObj.data)
    }

    def "Find exists user"() {
        given:
        def user = new User(id: IdGenerator.INSTANCE.id(), name: "user", desc: "desc")
        assertTrue(userRepository.save(user) != null)

        when: "Status is 200 and the response"
        def response = mvc.perform(MockMvcRequestBuilders.get("/user/" + user.id))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .response.contentAsString
        def responseObj = objectMapper.readValue(response, new TypeReference<Response<UserDto>>() {})
        assertEquals(0, responseObj.code)
        assertNull(responseObj.reason)
        assertNotNull(responseObj.data)

        then: "Find user with repository"
        assertEquals(user, userRepository.findById(user.id).get())
    }
}
