package ch.hevs.medgift.swarmplatform.swarmbackend.websocket;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.spotify.docker.client.DockerClient.LogsParam;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Task;

import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;
import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;

public class LogPoller {
	
//	
//	@Autowired
	private DockerService dockerService;
	
//	@Autowired
	private SimpMessagingTemplate template;
	
	private String serviceName;
	private String clientId;

	
	private boolean doPolling = false;
	
	private String serviceId;
	
	private long lastPoll;

	
	
	public LogPoller(String serviceName, String clientId, 
			DockerService dockerService, SimpMessagingTemplate template ) throws DockerException, InterruptedException {
		this.serviceName = serviceName;
		this.clientId = clientId;
		this.dockerService = dockerService;
		this.template = template;
		this.initService();

	}
	
	
	public void start() throws InterruptedException, DockerException{
		
		
		this.doPolling = true;
		this.lastPoll = (System.currentTimeMillis());
		
		//get initial log messages
		String log = this.poll(0);
		this.sendToQueue(log);
		
		//Then do polling
		while(doPolling){
			if (Thread.interrupted()) { //cancel if thread interrupted from outside
                
                break;
            }
			
			long currentPoll = (System.currentTimeMillis());
			log = this.poll((int)(this.lastPoll/1000));
			this.lastPoll = currentPoll;
			this.sendToQueue(log);
			
				
			Thread.sleep(2000);

		}	
	}
	
	
	/*
	 * Get log message
	 */
	private String poll(int since) throws DockerException, InterruptedException{
		return this.createLogStream(since).readFully();
	}
	
	
	/*
	 * Send message to queue
	 */
	private void sendToQueue(String log){
		
		if(log == null || log.trim().equals(""))
			return;
		
		System.out.format("LOG for serviceName/clientId: %s/%s \n%s",serviceName,clientId,log);
		this.template.convertAndSend(Defaults.LOGS_BROKER+"/"+this.serviceName+"/"+this.clientId,log);
	}
	
	
	private void initService() throws DockerException, InterruptedException{
		//Get docker
		DockerClient docker = this.dockerService.getDocker();
		
		Task.Criteria criteria = Task.Criteria.builder().serviceName(serviceName).build();
    	
    	//Get service tasks
    	List<Task> taskList = docker.listTasks(criteria);
    	
    
    	//Get service id from first task (we assume there is only one task)
    	String serviceId = taskList.get(0).serviceId();
    	
    	this.serviceId = serviceId;
		
	}
	
	private LogStream createLogStream(int since) throws DockerException, InterruptedException{
				
		return  this.dockerService.getDocker()
				.serviceLogs(this.serviceId, LogsParam.stdout(),LogsParam.stderr()
				, LogsParam.since(since),LogsParam.timestamps(true));
	}
	
	
	public void cancel(){
		this.doPolling = false;
		
	}

}
