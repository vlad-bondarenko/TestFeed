package com.example.demo.model.dto

class FeedDto {

    long id

    String text

    Map<Long, String> userComments

    Set<Long> userLikes
}