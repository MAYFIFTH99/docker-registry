package opensource.dockerregistry.backend.aop;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opensource.dockerregistry.backend.dto.AuditLogRequestDto;
import opensource.dockerregistry.backend.dto.DeleteTagsResponse;
import opensource.dockerregistry.backend.service.AuditLogService;
import opensource.dockerregistry.backend.util.UserUtils;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @Pointcut("execution(* opensource.dockerregistry.backend.service.ImageService.deleteTag(..)) && args(name, tag)")
    public void deleteImage(String name, String tag) {}

    @Pointcut("execution(* opensource.dockerregistry.backend.service.ImageService.deleteAllTagsForImage(..)) && args(name)")
    public void deleteAllTags(String name) {}

    @Pointcut("execution(* opensource.dockerregistry.backend.service.ImageService.listTags(..)) && args(name)")
    public void viewTags(String name) {}

    @Pointcut("execution(* opensource.dockerregistry.backend.service.ImageService.fetchAllImages(..)) && args(filterOpt)")
    public void viewAllImages(Optional<String> filterOpt) {}

    @AfterReturning(pointcut = "deleteImage(name, tag)", returning = "response")
    public void afterDeleteImage(String name, String tag, ResponseEntity<Void> response) {
        log.info("🔥 AOP 동작 확인: {}:{}", name, tag);
        if (response.getStatusCode().is2xxSuccessful()) {
            String target = name + ":" + tag;
            String username = getCurrentUsername();
            auditLogService.log(new AuditLogRequestDto(username, "DELETE_IMAGE_TAG", target));
        }
    }

    @AfterReturning(pointcut = "deleteAllTags(name)", returning = "response", argNames = "name,response")
    public void afterDeleteAllTags(String name, ResponseEntity<DeleteTagsResponse> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            String username = getCurrentUsername();
            auditLogService.log(new AuditLogRequestDto(username, "DELETE_IMAGE_ALL_TAGS", name));
        }
    }


    @AfterReturning(pointcut = "viewTags(name)", returning = "response", argNames = "name,response")
    public void afterViewTags(String name, ResponseEntity<String> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            String username = getCurrentUsername();
            auditLogService.log(new AuditLogRequestDto(username, "VIEW_TAGS", name));
        }
    }

    @AfterReturning(pointcut = "viewAllImages(filterOpt)", returning = "images", argNames = "filterOpt,images")
    public void afterViewAllImages(Optional<String> filterOpt, List<String> images) {
        String target = filterOpt.map(f -> "filter=" + f).orElse("all");
        String username = getCurrentUsername();
        auditLogService.log(new AuditLogRequestDto(username, "VIEW_ALL_IMAGES", target));
    }

    private String getCurrentUsername() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) return "anonymous";
        HttpServletRequest request = attr.getRequest();
        String s = UserUtils.extractUsername(request);
        return s != null ? s : "anonymous";
    }
}