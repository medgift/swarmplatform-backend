package ch.hevs.medgift.swarmplatform.swarmbackend.controllers;

import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;
import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.DockerRequestException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.*;
import com.spotify.docker.client.messages.swarm.Config.Criteria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import javax.ws.rs.NotFoundException;

import static ch.hevs.medgift.swarmplatform.swarmbackend.utils.Utils.translateLabelToKeyValue;

@RestController
@CrossOrigin
public class JobController {

    @Autowired
    DockerService dockerService;

    @RequestMapping("/")
    public String index() {
        return "Welcome to the Docker Swarm Backend!";
    }

//    @RequestMapping("/create-job")
//    public ResponseEntity createJob() {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body("Please specify the image name in the path: /create-job/NAME_OF_THE_IMAGE");
//    }
    
    
    @RequestMapping(value="/jobs" ,method = RequestMethod.POST)
    public ResponseEntity<String> createJob(@RequestParam("imageName") String imageName,
                            @RequestParam("labels") Optional<Integer[]> labels,
                            @RequestParam("datasetMount") Optional<String> datasetMount) throws DockerException, InterruptedException {

        // Get Docker client
        DockerClient docker = dockerService.getDocker();
     
        // Print cluster info
        System.out.println(docker.info().toString());

        // Restart Policy
        RestartPolicy restartPolicy = RestartPolicy.builder()
                .condition(RestartPolicy.RESTART_POLICY_NONE)
                .build();

        // Mounts
        List<Mount> mountList = new ArrayList<>();
        if(datasetMount.isPresent()){
            String mountPoint = datasetMount.get();

            Mount mount = Mount.builder()
                    .readOnly(true)
                    .source(mountPoint)
                    .target(Defaults.DATASET_MOUNT_TARGET)
                    .build();

            mountList.add(mount);
        }
        //TODO: If not present? what to do?...return error code


        // Container spec
        ContainerSpec containerSpec = ContainerSpec.builder()
                .image(imageName)
                .mounts(mountList)
                .build();

        // Service properties
        ServiceMode serviceMode = ServiceMode.withReplicas(Defaults.NB_REPLICAS);

        // Placement (conditions)
        List<String> placementConstraints = new ArrayList<>(labels.get().length);

        // Add label constraints (if present)
        if (labels.isPresent() && labels.get().length > 0) {

            // Translate "highcpu" to "cpu=high", etc.
            for (int label : labels.get()) {
                placementConstraints.add(translateLabelToKeyValue(label));

            }
        }
        Placement placement = Placement.create(placementConstraints);

        // Task spec
        TaskSpec taskSpecification = TaskSpec.builder()
                .restartPolicy(restartPolicy)
                .containerSpec(containerSpec)
                .placement(placement)
                .build();

        // Service name - currently the name of the image and the current time
        String serviceName = imageName.replace("/", "_")+ System.currentTimeMillis();

        // Define service
        ServiceSpec serviceSpec = ServiceSpec.builder()
                .mode(serviceMode)
                .name(serviceName)
                .taskTemplate(taskSpecification)
                .build();

        // Create service
        docker.createService(serviceSpec);
        
        StringBuilder jsonResponse = new StringBuilder("{\"serviceName\":");
        jsonResponse.append("\"");
        jsonResponse.append(serviceName);
        jsonResponse.append("\"}");

        return ResponseEntity.ok(jsonResponse.toString());
    }
    
    
    //TODO also return container ID of service
    //   taskList.get(0).status().containerStatus().containerId()
    @RequestMapping(value="/jobs/{serviceName}" ,method = RequestMethod.GET)
    public ResponseEntity<String> getServiceState(@PathVariable String serviceName) throws DockerException, InterruptedException{
    	
    	// Get Docker client
        DockerClient docker = dockerService.getDocker();

        //Create criteria object for finding service
    	Task.Criteria criteria = Task.Criteria.builder().serviceName(serviceName).build();
    	
    	//Get service tasks
    	List<Task> taskList = null;
    	try{
    		taskList = docker.listTasks(criteria);
		
    	}
    	catch(DockerRequestException e){
    		//Throw 404 if service not found
    		if(e.status() == 404)
    			return ResponseEntity.notFound().build();
    		    			
    		throw e;
    	}
  	
    	//If there are no tasks for this service, throw 404
    	//(Not sure if that can actually happen)
    	if(taskList.size() < 1)
    		return ResponseEntity.notFound().build();
    	
    	//We always assume that there is only 1 task per service
    	//Get the current state of the task
    	String state = taskList.get(0).status().state();


   
    	//Json response
    	StringBuilder jsonResponse = new StringBuilder("{\"state\":");
        jsonResponse.append("\"");
        jsonResponse.append(state);
        jsonResponse.append("\"}");
    	
    	return ResponseEntity.ok(jsonResponse.toString());

    }
    
    
    



    @RequestMapping(value="/jobs/{service-name}",method = RequestMethod.DELETE)
    //@ResponseBody
    public ResponseEntity<String> deleteJob(@PathVariable("service-name") String serviceName) throws DockerException, InterruptedException {

        DockerClient docker = dockerService.getDocker();

        try {
            docker.removeService(serviceName);
        } catch (ServiceNotFoundException e){
            return ResponseEntity.notFound().build();
        }

        //204 No content on success
        return ResponseEntity.noContent().build();
    }
    
    //TODO implement copy of result file to NAS or to other container
    public void copy(String containerFrom, String containerTo){
    	
    }
    
    
    
    

}
