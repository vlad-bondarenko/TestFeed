package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "posts")
class Post {

    @Id
    long id

    @Indexed
    long ownerId

    @Indexed
    long userId

    String text

    @Version
    int version
}