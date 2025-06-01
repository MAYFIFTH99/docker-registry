package opensource.dockerregistry.backend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageActivityResponse {
    private String username;
    private String action; // e.g., "PUSH", "PULL", "DELETE_IMAGE_TAG"
    private LocalDateTime timestamp;
}
