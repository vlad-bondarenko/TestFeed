package com.example.demo.repository

import com.example.demo.model.UserSession
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserSessionRepository extends CrudRepository<UserSession, Long> {

    Optional<UserSession> findByUserId(long userId)
}
