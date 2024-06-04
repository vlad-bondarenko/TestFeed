package com.example.demo.service


import com.example.demo.model.dto.FeedDto

interface PostService {

    long login(String name, String secret)

    boolean logout(long sessionId)

    boolean isValid(long sessionId)


    long savePost(long sessionId, long userId, String text)

    boolean editPost(long sessionId, long id, String text)

    boolean deletePost(long sessionId, long userId, long id)

    boolean commentPost(long sessionId, long id, String comment)

    boolean favoritePost(long sessionId, long id)

    boolean unfavoritePost(long sessionId, long id)

    boolean likePost(long sessionId, long id)

    boolean unlikePost(long sessionId, long id)


    boolean subscribeUser(long sessionId, long userId)

    boolean unsubscribeUser(long sessionId, long userId)


    List<FeedDto> feeds(long sessionId)

    List<FeedDto> feeds(long sessionId, long userId)

/*
    default Post toPost(PostDto dto){
        new Post(id: dto.id, userId: dto.userId, text: dto.text)
    }
*/
}
