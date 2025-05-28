package opensource.dockerregistry.backend.service;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.util.HtpasswdManager;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final HtpasswdManager htpasswdManager;

    public List<String> getAllUsers() throws IOException {
        return htpasswdManager.listUsers();
    }

    public void createUser(String username, String password) throws IOException {
        htpasswdManager.addUser(username, password);
    }

    public void deleteUser(String username) throws IOException {
        htpasswdManager.deleteUser(username);
    }
}
