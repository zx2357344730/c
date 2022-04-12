package com.cresign.tools.pojo.po;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Result {
        public String clientId;
        public long timestamp;
        public SseEmitter sseEmitter;

        private Map<String, Result> sseEmitterMap = new ConcurrentHashMap<>();

        public Result(String clientId, long timestamp, SseEmitter sseEmitter) {
            this.clientId = clientId;
            this.timestamp = timestamp;
            this.sseEmitter = sseEmitter;
        }
    }