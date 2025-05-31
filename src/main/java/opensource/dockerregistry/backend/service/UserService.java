package opensource.dockerregistry.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class UserService {

    private final static Path HTPASSWD_PATH = Paths.get("auth", "htpasswd");

    public void addUser(String username, String password) {
        try {
            String bcryptHash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            String entry = username + ":" + bcryptHash + "\n";

            Files.write(HTPASSWD_PATH, entry.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("사용자 추가 실패", e);
        }
    }


    public void deleteUser(String username) {
        try {
            if (!Files.exists(HTPASSWD_PATH)) return;

            List<String> updatedLines = Files.readAllLines(HTPASSWD_PATH).stream()
                    .filter(line -> !line.startsWith(username + ":"))
                    .collect(Collectors.toList());

            Files.write(HTPASSWD_PATH, updatedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("사용자 삭제 실패", e);
        }
    }

    public List<String> listUsers() {
        try {
            if (!Files.exists(HTPASSWD_PATH)) return List.of();

            return Files.readAllLines(HTPASSWD_PATH).stream()
                    .map(line -> line.split(":")[0])
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("사용자 목록 불러오기 실패", e);
        }
    }
}
