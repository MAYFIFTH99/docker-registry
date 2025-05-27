package opensource.dockerregistry.backend.service;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.client.DockerRegistryClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RegistryService {

    private final DockerRegistryClient registryClient;

    public Mono<Boolean> isRegistryAlive() {
        return registryClient.ping();
    }

    public Mono<String> fetchManifest(String imageName, String tag) {
        return registryClient.getImageManifest(imageName, tag);
    }

    public Mono<byte[]> fetchBlob(String imageName, String digest) {
        return registryClient.downloadBlob(imageName, digest);
    }
}
