package ch.hevs.medgift.swarmplatform.swarmbackend;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SwarmBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwarmBackendApplication.class, args);
    }

}
