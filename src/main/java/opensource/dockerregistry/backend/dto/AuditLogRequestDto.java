package opensource.dockerregistry.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditLogRequestDto {
    private String username;
    private String action;
    private String target;
}
