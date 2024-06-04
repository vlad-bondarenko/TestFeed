package com.example.demo.repository

import com.example.demo.model.Post
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository extends CrudRepository<Post, Long> {

    Optional<Post> findByIdAndOwnerId(long id, long ownerId)

    List<Post> findByOwnerId(long ownerId)

    List<Post> findByUserId(long userId)
}
