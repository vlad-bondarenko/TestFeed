package com.example.demo.repository

import com.example.demo.model.PostLike
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PostLikeRepository extends MongoRepository<PostLike, String> {

    List<PostLike> findByPostId(long postId)
}
