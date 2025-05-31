package opensource.dockerregistry.backend.util;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class UserUtils {

    public static String extractUsername(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Basic ")) {
            try {
                String base64Credentials = header.substring("Basic ".length());
                String decoded = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
                int colonIndex = decoded.indexOf(':');
                return (colonIndex > 0) ? decoded.substring(0, colonIndex) : "";
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}
