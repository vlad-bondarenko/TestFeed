package com.example.demo.repository

import com.example.demo.model.PostComment
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PostCommentRepository extends CrudRepository<PostComment, Long> {

    List<PostComment> findByPostIdAndOwnerId(long postId, long ownerId)

    List<PostComment> findByPostIdAndOwnerIdIn(long postId, Iterable<Long> ownerIds)
}
