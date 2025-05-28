package opensource.dockerregistry.backend.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HtpasswdManager {

    private static final String HTPASSWD_FILE = "/auth/htpasswd"; // docker-compose 기준

    public List<String> listUsers() throws IOException {
        return Files.lines(Paths.get(HTPASSWD_FILE))
                .map(line -> line.split(":")[0])
                .collect(Collectors.toList());
    }

    public boolean userExists(String username) throws IOException {
        return listUsers().contains(username);
    }

    public void addUser(String username, String plainPassword) throws IOException {
        if (userExists(username)) throw new IllegalArgumentException("User already exists");

        String hashed = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        String newLine = username + ":" + hashed;

        Files.write(Paths.get(HTPASSWD_FILE), (newLine + "\n").getBytes(), StandardOpenOption.APPEND);
    }

    public void deleteUser(String username) throws IOException {
        List<String> updated = Files.lines(Paths.get(HTPASSWD_FILE))
                .filter(line -> !line.startsWith(username + ":"))
                .collect(Collectors.toList());

        Files.write(Paths.get(HTPASSWD_FILE), updated, StandardOpenOption.TRUNCATE_EXISTING);
    }
}