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

    @GetMapping
    public List<AuditLogEntity> getAudit(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String image
    ) {
        if (user != null) return auditLogService.getByUser(user);
        if (image != null) return auditLogService.getByImage(image);
        return List.of(); // 빈 리스트 반환
    }
}
