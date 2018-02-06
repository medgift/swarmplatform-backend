package ch.hevs.medgift.swarmplatform.swarmbackend.websocket;

import java.util.concurrent.Future;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.spotify.docker.client.exceptions.DockerException;

import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;

@Service
public class ThreadedLogPoll {

    @Async
    Future<LogPoller> run(String serviceName, DockerService dockerService, SimpMessagingTemplate template) 
    		throws DockerException, InterruptedException {
    	
    	System.out.println("run "+serviceName);
    	LogPoller p = new LogPoller(serviceName, dockerService, template);
    	
    	p.start();



        return new AsyncResult<LogPoller>(p);
    }

}
