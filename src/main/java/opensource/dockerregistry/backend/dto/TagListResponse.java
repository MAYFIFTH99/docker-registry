package opensource.dockerregistry.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class TagListResponse {
    private String name;
    private List<String> tags;
}
