package com.example.demo.service.impl

import com.example.demo.AbstractLogging
import com.example.demo.ConcurrentLruCache
import com.example.demo.model.UserSession
import com.example.demo.repository.UserSessionRepository
import com.example.demo.service.IdGenerator
import com.example.demo.service.UserSessionService
import org.springframework.stereotype.Service

@Service
class UserSessionServiceImpl extends AbstractLogging implements UserSessionService {

    static final int CACHE_BUCKET_CAPACITY = 128
    static final int CACHE_BUCKET_SIZE = 8

    private final UserSessionRepository userSessionRepository

    private final ConcurrentLruCache<Long, Long> cache

    UserSessionServiceImpl(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository

        cache = new ConcurrentLruCache<Long, Long>(CACHE_BUCKET_CAPACITY, CACHE_BUCKET_SIZE, null) {
            @Override
            protected int getIndex(Long key) {
                return Long.hashCode(key) & mask
            }
        }
    }

    @Override
    long login(long userId) {
        def sessionId = cache.lookup(userId, this::findSessionId)
        if (sessionId != null) {
            log.warn("Duplicate session, remove old session")

            //logout duplicate session
//            logout(sessionId)
        }

        sessionId = IdGenerator.INSTANCE.id()

        def userSession = new UserSession(id: sessionId, userId: userId)
        if (userSessionRepository.save(userSession) != null) {
            cache.put(sessionId, userId)

            return sessionId
        }

        throw new RuntimeException("Not login for " + userId)
    }

    @Override
    boolean logout(long sessionId) {
        userSessionRepository.deleteById(sessionId)

        cache.remove(sessionId)

        return true
    }

    @Override
    boolean isValid(long sessionId) {
        return cache.lookup(sessionId, this::findSessionId) != null
    }

    @Override
    Long userId(long sessionId) {
        return cache.lookup(sessionId, this::findSessionId)
    }

    private Long findSessionId(long userId) {
        return userSessionRepository.findByUserId(userId).map(userSession -> userSession.id).orElse(null)
    }
}
