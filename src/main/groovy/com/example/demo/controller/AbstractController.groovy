package com.example.demo.controller

import com.example.demo.AbstractLogging
import com.example.demo.model.dto.Response
import org.springframework.web.bind.annotation.ExceptionHandler

abstract class AbstractController extends AbstractLogging {

    @ExceptionHandler(Exception)
    Response<?> handleException(Exception e) {
        log.error(e.toString(), e)

        return new Response<>(-1, e.toString())
    }
}
