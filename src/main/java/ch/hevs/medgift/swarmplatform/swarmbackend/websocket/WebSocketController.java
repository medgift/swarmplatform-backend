package ch.hevs.medgift.swarmplatform.swarmbackend.websocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.LogMessage;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.messages.swarm.Task;

import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;
import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;
import ch.qos.logback.core.net.SyslogOutputStream;

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
	

	
	@MessageMapping(Defaults.LOGS_MESSAGE_MAPPING_PREFIX+"/{serviceName}")
    public void getLogs(@DestinationVariable("serviceName") String serviceName, SimpMessageHeaderAccessor headerAccessor) 
    		throws Exception {
    	
		System.out.println("get logs");
		//Non blocking
	    Future<LogPoller> poller = this.logPoll.run(serviceName, this.dockerService, this.template);
	    System.out.println("future: "+poller);
	    
	    Map<String,Object> attribs = headerAccessor.getSessionAttributes();
	    attribs.put(serviceName, poller);
	    
	    System.out.println("im here");
	    
	    headerAccessor.setSessionAttributes(attribs);

    }
	
	@MessageMapping("/logclose/{serviceName}")
    static void closePing(@DestinationVariable("serviceName") String serviceName, SimpMessageHeaderAccessor headerAccessor) {
		System.out.println("close");
        Future<LogPoller> poller = (Future<LogPoller>) headerAccessor.getSessionAttributes().get(serviceName);
        if(poller == null)
        	return;
		
		headerAccessor.getSessionAttributes().entrySet().forEach((a) -> System.out.println(a.getKey()));
        poller.cancel(true);
    }
	
	
//    	@MessageMapping(Defaults.LOGS_MESSAGE_MAPPING_PREFIX+"/{serviceName}")
//	    @SendTo(Defaults.LOGS_BROKER+"/{serviceName}")
//	    public String greeting(@DestinationVariable("serviceName") String serviceName) throws Exception {
//	    	
//		    System.out.println("sysout");
//		 
//			// Get Docker client
//	        DockerClient docker = dockerService.getDocker();
//	        
//	        //Create criteria object for finding service
//	    	Task.Criteria criteria = Task.Criteria.builder().serviceName(serviceName).build();
//	    	
//	    	//Get service tasks
//	    	List<Task> taskList = docker.listTasks(criteria);
//	    	
//	    
//	    	String serviceId = taskList.get(0).serviceId();
//	    	
//	
//	    	LogStream ls = docker.serviceLogs(serviceId, LogsParam.stdout(), LogsParam.stderr(),LogsParam.since(0));
//	    	
//	    	String log = ls.readFully();
//	    	
//	    	System.out.println(log);
//	    	
//	    	return log;
//   
//    	
//    	
//	    }
}
