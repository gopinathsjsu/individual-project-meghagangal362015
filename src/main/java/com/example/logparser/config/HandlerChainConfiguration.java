package com.example.logparser.config;

import com.example.logparser.handler.ApmLogHandler;
import com.example.logparser.handler.ApplicationLogHandler;
import com.example.logparser.handler.LogHandler;
import com.example.logparser.handler.RequestLogHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HandlerChainConfiguration {
    @Bean
    LogHandler rootHandler() {
        LogHandler apm = new ApmLogHandler();
        LogHandler app = new ApplicationLogHandler();
        LogHandler request = new RequestLogHandler();

        apm.setNext(app);
        app.setNext(request);

        return apm;
    }
}
