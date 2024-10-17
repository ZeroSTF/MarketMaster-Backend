package tn.zeros.marketmaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.rmi.registry.Registry;

@Configuration
@EnableWebSocketMessageBroker
@EnableWebSocket


public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Value("${frontend.origin}")
    private String frontendOrigin;

   @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/market").setAllowedOrigins(this.frontendOrigin).withSockJS().setClientLibraryUrl("/sockjs");;
    }
    @Override
    public void configureMessageBroker (MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}