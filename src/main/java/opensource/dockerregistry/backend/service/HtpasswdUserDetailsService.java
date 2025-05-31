package opensource.dockerregistry.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class HtpasswdUserDetailsService implements UserDetailsService {

    private static final Path HTPASSWD_PATH = Paths.get("auth", "htpasswd");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            List<String> lines = Files.readAllLines(HTPASSWD_PATH);
            for (String line : lines) {
                if (line.startsWith(username + ":")) {
                    String hashedPassword = line.split(":", 2)[1];
                    return User.builder()
                            .username(username)
                            .password(hashedPassword)
                            .roles("USER")
                            .build();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("htpasswd 읽기 실패", e);
        }
        throw new UsernameNotFoundException("사용자 '" + username + "' 없음");
    }
}
