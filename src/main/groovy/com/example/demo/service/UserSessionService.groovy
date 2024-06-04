package com.example.demo.service

interface UserSessionService {

    long login(long userId)

    boolean logout(long sessionId)

    boolean isValid(long sessionId)

    Long userId(long sessionId)
}
