package com.example.demo.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "subscriptions")
class Subscription {

    @Id
    String id //ownerId + ":" + userId

    @Indexed
    long ownerId

    @Indexed
    long userId

    @Version
    int version
}