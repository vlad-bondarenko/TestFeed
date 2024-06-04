package com.example.demo.service.impl

import com.example.demo.AbstractLogging
import com.example.demo.OptimisticLockingFailureExceptionRetryHandler
import com.example.demo.SessionExpiredException
import com.example.demo.model.*
import com.example.demo.model.dto.FeedDto
import com.example.demo.repository.*
import com.example.demo.service.IdGenerator
import com.example.demo.service.PostService
import com.example.demo.service.UserSessionService
import org.springframework.stereotype.Service

import java.util.concurrent.TimeUnit

@Service
class PostServiceImpl extends AbstractLogging implements PostService {

    final UserRepository userRepository

    final UserSessionService userSessionService

    final PostRepository postRepository

    final PostCommentRepository postCommentRepository

    final PostFavoriteRepository postFavoriteRepository

    final PostLikeRepository postLikeRepository

    final SubscriptionRepository subscriptionRepository

    final OptimisticLockingFailureExceptionRetryHandler optimisticLockingFailureExceptionRetryHandler

    PostServiceImpl(UserRepository userRepository,
                    UserSessionService userSessionService,
                    PostRepository postRepository,
                    PostCommentRepository postCommentRepository,
                    PostFavoriteRepository postFavoriteRepository,
                    PostLikeRepository postLikeRepository,
                    SubscriptionRepository subscriptionRepository) {
        this.userRepository = userRepository
        this.userSessionService = userSessionService
        this.postRepository = postRepository
        this.postCommentRepository = postCommentRepository
        this.postFavoriteRepository = postFavoriteRepository
        this.postLikeRepository = postLikeRepository
        this.subscriptionRepository = subscriptionRepository

        optimisticLockingFailureExceptionRetryHandler = new OptimisticLockingFailureExceptionRetryHandler(log)
    }

    @Override
    long login(String name, String secret) {
        Optional<User> userOptional
        if (name == null || name.isBlank() || secret == null || secret.isBlank() || (userOptional = userRepository.findByName(name)).isEmpty()) {
            throw new IllegalArgumentException()
        }

        return userSessionService.login(userOptional.get().id)
    }

    @Override
    boolean logout(long sessionId) {
        return userSessionService.logout(sessionId)
    }

    @Override
    boolean isValid(long sessionId) {
        return userSessionService.isValid(sessionId)
    }

    @Override
    long savePost(long sessionId, long userId, String text) {
        def currentUserId = currentUserId(sessionId)

        def id = IdGenerator.INSTANCE.id()
        postRepository.save(new Post(id: id, ownerId: currentUserId, userId: userId, text: text))

        return id
    }

    @Override
    boolean editPost(long sessionId, long id, String text) {
        optimisticLockingFailureExceptionRetryHandler.retry(() -> editPostUnsafe(sessionId, id, text))
    }

    private boolean editPostUnsafe(long sessionId, long id, String text) {
        def currentUserId = currentUserId(sessionId)

        def optional = postRepository.findByIdAndOwnerId(id, currentUserId)

        if (optional.isEmpty()) {
            return false
        }

        def post = optional.get()
        post.text = text

        TimeUnit.MILLISECONDS.sleep(200)//For test optimisticLockingFailureException

        postRepository.save(post)

        return true
    }

    @Override
    boolean deletePost(long sessionId, long userId, long id) {
        def currentUserId = currentUserId(sessionId)

        def optional = postRepository.findByIdAndOwnerId(id, currentUserId)

        if (optional.isEmpty()) {
            return false
        }

        postRepository.deleteById(id)

        return true
    }

    @Override
    boolean commentPost(long sessionId, long id, String comment) {
        def currentUserId = currentUserId(sessionId)

        postCommentRepository.save(new PostComment(id: IdGenerator.INSTANCE.id(), postId: id, ownerId: currentUserId, text: comment))

        return true
    }

    @Override
    boolean favoritePost(long sessionId, long id) {
        def currentUserId = currentUserId(sessionId)

        def postFavoriteId = id + ":" + currentUserId
        def optional = postFavoriteRepository.findById(postFavoriteId)

        if (!optional.isEmpty()) {
            return false
        }

        postFavoriteRepository.save(new PostFavorite(id: postFavoriteId, ownerId: currentUserId, postId: id))

        return true
    }

    @Override
    boolean unfavoritePost(long sessionId, long id) {
        def currentUserId = currentUserId(sessionId)

        def postFavoriteId = id + ":" + currentUserId
        def optional = postFavoriteRepository.findById(postFavoriteId)

        if (optional.isEmpty()) {
            return false
        }

        postFavoriteRepository.deleteById(postFavoriteId)

        return true
    }

    @Override
    boolean likePost(long sessionId, long id) {
        def currentUserId = currentUserId(sessionId)

        def postLikeId = id + ":" + currentUserId
        def optional = postLikeRepository.findById(postLikeId)

        if (!optional.isEmpty()) {
            return false
        }

        postLikeRepository.save(new PostLike(id: postLikeId, ownerId: currentUserId, postId: id))

        return true
    }

    @Override
    boolean unlikePost(long sessionId, long id) {
        def currentUserId = currentUserId(sessionId)

        def postLikeId = id + ":" + currentUserId
        def optional = postLikeRepository.findById(postLikeId)

        if (optional.isEmpty()) {
            return false
        }

        postLikeRepository.deleteById(postLikeId)

        return true
    }

    @Override
    boolean subscribeUser(long sessionId, long userId) {
        def currentUserId = currentUserId(sessionId)

        subscriptionRepository.save(new Subscription(id: currentUserId + ":" + userId, ownerId: currentUserId, userId: userId))

        return true
    }

    @Override
    boolean unsubscribeUser(long sessionId, long userId) {
        def currentUserId = currentUserId(sessionId)

        def id = currentUserId + ":" + userId

        def optional = subscriptionRepository.findById(id)

        if (optional.isEmpty()) {
            return false
        }

        subscriptionRepository.deleteById(id)

        return true
    }

    @Override
    List<FeedDto> feeds(long sessionId) {
        def currentUserId = currentUserId(sessionId)

        def posts = postRepository.findByUserId(currentUserId)
        if (posts == null || posts.isEmpty()) {
            return Collections.EMPTY_LIST
        }

        Set<Long> userFilter = null
        def subscriptions = subscriptionRepository.findByOwnerId(currentUserId)
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (final def subscription in subscriptions) {
                if (userFilter == null) {
                    userFilter = new HashSet<>(subscriptions.size())
                }

                userFilter.add(subscription.userId)
            }
        }

        List<FeedDto> feeds = new ArrayList<>(posts.size())
        for (final def post in posts) {
            def feed = new FeedDto()
            feed.id = post.id
            feed.text = post.text

            if (userFilter != null && !userFilter.isEmpty()) {
                def comments = postCommentRepository.findByPostIdAndOwnerIdIn(post.id, userFilter)
                if (comments != null && !comments.isEmpty()) {
                    Map<Long, String> userComments = new HashMap<>(comments.size())

                    for (final def comment in comments) {
                        userComments.put(comment.ownerId, comment.text)
                    }

                    feed.userComments = userComments
                }
            }

            def likes = postLikeRepository.findByPostId(post.id)
            if (likes != null && !likes.isEmpty()) {
                Set<Long> userLikes = new HashSet<>(likes.size())

                for (final def like in likes) {
                    userLikes.add(like.ownerId)
                }

                feed.userLikes = userLikes
            }

            feeds.add(feed)
        }

        return feeds
    }

    @Override
    List<FeedDto> feeds(long sessionId, long userId) {
        def currentUserId = currentUserId(sessionId)

        def posts = postRepository.findByOwnerId(userId)
        if (posts == null || posts.isEmpty()) {
            return Collections.EMPTY_LIST
        }

        List<FeedDto> feeds = new ArrayList<>(posts.size())
        for (final def post in posts) {
            FeedDto feed = new FeedDto()
            feed.id = post.id
            feed.text = post.text

            List<PostComment> comments = postCommentRepository.findByPostIdAndOwnerId(post.id, currentUserId)
            if (comments != null && !comments.isEmpty()) {
                Map<Long, String> userComments = new HashMap<>(comments.size())

                for (final def comment in comments) {
                    userComments.put(comment.ownerId, comment.text)
                }

                feed.userComments = userComments
            }

            List<PostLike> likes = postLikeRepository.findByPostId(post.id)
            if (likes != null && !likes.isEmpty()) {
                Set<Long> userLikes = new HashSet<>(likes.size())

                for (final def like in likes) {
                    userLikes.add(like.ownerId)
                }

                feed.userLikes = userLikes
            }

            feeds.add(feed)
        }

        return feeds
    }

    private long currentUserId(long sessionId) {
        def userId = userSessionService.userId(sessionId)
        if (userId == null) {
            throw SessionExpiredException.INSTANCE
        }

        return userId
    }
}
