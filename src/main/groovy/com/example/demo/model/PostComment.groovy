package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "post_comments")
class PostComment {

    @Id
    long id

    @Indexed
    long postId

    @Indexed
    long ownerId

    String text
}