package com.example.demo.repository


import com.example.demo.model.PostFavorite
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PostFavoriteRepository extends CrudRepository<PostFavorite, String> {
}
