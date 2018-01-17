package ch.hevs.medgift.swarmplatform.swarmbackend.services;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.stereotype.Service;

@Service
public class DockerService {

    private DockerClient docker;

    public DockerService(){
        try {
            docker = DefaultDockerClient.fromEnv().build();
            System.out.println(docker.info().toString());
        } catch (DockerCertificateException e) {
            e.printStackTrace();
        } catch (DockerException e ){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
    }

    public DockerClient getDocker(){
        return docker;
    }
}
