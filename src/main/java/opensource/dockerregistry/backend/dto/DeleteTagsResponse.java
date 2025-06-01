package opensource.dockerregistry.backend.dto;

import java.util.List;
import lombok.Data;

@Data
public class DeleteTagsResponse {
    private String image;
    private List<String> deletedTags;
    private int count;
}
