package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "post_likes")
class PostLike {

    @Id
    String id //postId + ":" + ownerId

    @Indexed
    long postId

    @Indexed
    long ownerId

    boolean equals(o) {
        if (this.is(o)) return true
        if (o == null || getClass() != o.class) return false

        PostLike postLike = (PostLike) o

        if (ownerId != postLike.ownerId) return false
        if (postId != postLike.postId) return false

        return true
    }

    int hashCode() {
        int result
        result = (int) (postId ^ (postId >>> 32))
        result = 31 * result + (int) (ownerId ^ (ownerId >>> 32))
        return result
    }
}