package com.example.demo.model.dto

class Response<T> {

    int code
    T data
    String reason

    Response() {
    }

    Response(T data) {
        this.data = data
    }

    Response(int code, String reason) {
        this.code = code
        this.reason = reason
    }
}
