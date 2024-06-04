package com.example.demo.repository

import com.example.demo.model.Subscription
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository extends CrudRepository<Subscription, String> {

    List<Subscription> findByOwnerId(long ownerId)
}
