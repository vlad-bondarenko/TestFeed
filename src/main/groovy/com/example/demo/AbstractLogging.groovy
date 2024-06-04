package com.example.demo

import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class AbstractLogging {

    protected final Logger log = LoggerFactory.getLogger(getClass())
}