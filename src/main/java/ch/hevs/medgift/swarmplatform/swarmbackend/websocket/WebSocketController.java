package ch.hevs.medgift.swarmplatform.swarmbackend.websocket;


import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;
import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;


@Controller
@EnableAsync
public class WebSocketController {
	
	private final SimpMessagingTemplate template;
	
    @Autowired
    DockerService dockerService;
    
    @Autowired
    ThreadedLogPoll logPoll;

	@Autowired
	public WebSocketController(SimpMessagingTemplate template) {
		this.template = template;
	}
	

	
	@MessageMapping(Defaults.LOGS_MESSAGE_MAPPING_PREFIX+"/{serviceName}/{clientId}")
    public void getLogs(@DestinationVariable("serviceName") String serviceName, 
    		@DestinationVariable("clientId") String clientId, 
    		SimpMessageHeaderAccessor headerAccessor) 
    		throws Exception {
    	
		System.out.format("logs acticated for serviceName/clientId: %s/%s\n",serviceName,clientId);
		//Non blocking
	    Future<LogPoller> poller = this.logPoll.run(serviceName, clientId, this.dockerService, this.template);
	    
	    Map<String,Object> attribs = headerAccessor.getSessionAttributes();
	    attribs.put(serviceName+"_"+clientId, poller);
	    	    
	    //TODO put into redis key/value store
	    headerAccessor.setSessionAttributes(attribs);

    }
	
	
	@MessageMapping("/logclose/{serviceName}/{clientId}")
    static void closeLog(@DestinationVariable("serviceName") String serviceName, 
    		@DestinationVariable("clientId") String clientId,
    		SimpMessageHeaderAccessor headerAccessor) {
		System.out.format("close logs for serviceName/clientId: %s/%s\n",serviceName,clientId);
        @SuppressWarnings("unchecked")
		Future<LogPoller> poller = (Future<LogPoller>) headerAccessor.getSessionAttributes().get(serviceName+"_"+clientId);
        if(poller == null)
        	return;

        poller.cancel(true);
    }
	
	
//    
}
