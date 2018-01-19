package ch.hevs.medgift.swarmplatform.swarmbackend.controllers;

import ch.hevs.medgift.swarmplatform.swarmbackend.services.DockerService;
import ch.hevs.medgift.swarmplatform.swarmbackend.utils.Defaults;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.exceptions.ServiceNotFoundException;
import com.spotify.docker.client.messages.mount.Mount;
import com.spotify.docker.client.messages.swarm.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

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
    
    
    @RequestMapping("/create-job")
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

        return ResponseEntity.ok("Service " + serviceName + " started!");
    }


//    @RequestMapping("/create-job/{image-name}")
//    public ResponseEntity createJob(@PathVariable("image-name") String imageName,
//                            @RequestParam("labels") Optional<String[]> labels,
//                            @RequestParam("dataset-mount") Optional<String> datasetMount) throws DockerException, InterruptedException {
//
//        // Get Docker client
//        DockerClient docker = dockerService.getDocker();
//        
//
//        // Print cluster info
//        System.out.println(docker.info().toString());
//
//        // Restart Policy
//        RestartPolicy restartPolicy = RestartPolicy.builder()
//                .condition(RestartPolicy.RESTART_POLICY_NONE)
//                .build();
//
//        // Mounts
//        List<Mount> mountList = new ArrayList<>();
//        if(datasetMount.isPresent()){
//            String mountPoint = datasetMount.get();
//
//            Mount mount = Mount.builder()
//                    .readOnly(true)
//                    .source(mountPoint)
//                    .target(Defaults.DATASET_MOUNT_TARGET)
//                    .build();
//
//            mountList.add(mount);
//        }
//
//
//        // Container spec
//        ContainerSpec containerSpec = ContainerSpec.builder()
//                .image(imageName)
//                .mounts(mountList)
//                .build();
//
//        // Service properties
//        ServiceMode serviceMode = ServiceMode.withReplicas(Defaults.NB_REPLICAS);
//
//        // Placement (conditions)
//        List<String> placementConstraints = new ArrayList<>(labels.get().length);
//
//        // Add label constraints (if present)
//        if (labels.isPresent() && labels.get().length > 0) {
//
//            // Translate "highcpu" to "cpu=high", etc.
//            for (String label : labels.get()) {
//                placementConstraints.add(translateLabelToKeyValue(label));
//            }
//        }
//        Placement placement = Placement.create(placementConstraints);
//
//        // Task spec
//        TaskSpec taskSpecification = TaskSpec.builder()
//                .restartPolicy(restartPolicy)
//                .containerSpec(containerSpec)
//                .placement(placement)
//                .build();
//
//        // Service name - currently the name of the image and the current time
//        String serviceName = imageName + System.currentTimeMillis();
//
//        // Define service
//        ServiceSpec serviceSpec = ServiceSpec.builder()
//                .mode(serviceMode)
//                .name(serviceName)
//                .taskTemplate(taskSpecification)
//                .build();
//
//        // Create service
//        docker.createService(serviceSpec);
//
//        return ResponseEntity.ok("Service " + serviceName + " started!");
//    }

    @RequestMapping("/delete-job/{service-name}")
    public ResponseEntity createJob(@PathVariable("service-name") String serviceName) throws DockerException, InterruptedException {

        DockerClient docker = dockerService.getDocker();

        try {
            docker.removeService(serviceName);
        } catch (ServiceNotFoundException e){
            return ResponseEntity.badRequest().body("Service " + serviceName + " not found!");
        }

        return ResponseEntity.ok("Service " + serviceName + " was deleted!");
    }

}
