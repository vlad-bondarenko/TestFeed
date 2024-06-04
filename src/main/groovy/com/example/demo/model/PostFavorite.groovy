package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "post_favorites")
class PostFavorite {

    @Id
    String id //postId + ":" + ownerId

    @Indexed
    long postId

    @Indexed
    long ownerId
}