package opensource.dockerregistry.backend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserActivityResponse {
    private String image;
    private String action; // e.g., "PUSH", "PULL", "VIEW_TAGS"
    private LocalDateTime timestamp;
}
