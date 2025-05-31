package opensource.dockerregistry.backend.controller;

import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.service.AuditLogService;
import opensource.dockerregistry.backend.entity.AuditLogEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/user")
    public List<AuditLogEntity> getByUser(@RequestParam String username) {
        return auditLogService.getByUser(username);
    }

    @GetMapping("/image")
    public List<AuditLogEntity> getByImage(@RequestParam String image) {
        return auditLogService.getByImage(image);
    }
}
