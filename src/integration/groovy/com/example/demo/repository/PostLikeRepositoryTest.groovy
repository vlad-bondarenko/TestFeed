package com.example.demo.repository

import com.example.demo.model.PostLike
import com.example.demo.service.IdGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import spock.lang.Specification

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

@DataMongoTest
@AutoConfigureDataMongo
class PostLikeRepositoryTest extends Specification {

    @Autowired
    PostLikeRepository repository

    void setup() {
        repository.deleteAll()
    }

    void cleanup() {
    }

    def "FindByPostId"() {
        given:
        long ownerId = IdGenerator.INSTANCE.id()
        long postId = IdGenerator.INSTANCE.id()
        String id = postId + ":" + ownerId
        def entity = new PostLike(id: id, ownerId: ownerId, postId: postId)
        repository.save(entity)

        when:
        def list = repository.findByPostId(postId)

        then:
        assertNotNull(list)
        assertEquals(1, list.size())
        assertEquals(entity, list.get(0))
    }
}
