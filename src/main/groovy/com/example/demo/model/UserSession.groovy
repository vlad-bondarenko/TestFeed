package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_sessions")
class UserSession {

    @Id
    long id

    long userId
}
