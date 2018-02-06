package ch.hevs.medgift.swarmplatform.swarmbackend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer{
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(Defaults.SOCKET_ENDPOINT)
                .setAllowedOrigins("*");
        
        //In case browser does not suppoert websockets, emulate with SockJS
        registry.addEndpoint(Defaults.SOCKET_ENDPOINT).setAllowedOrigins("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
    	registry.enableSimpleBroker(Defaults.LOGS_BROKER);
        registry.setApplicationDestinationPrefixes(Defaults.LOGS_BROKER_APP_DEST_PREFIX);
        
    }
    
    
    
    
}