package opensource.dockerregistry.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import opensource.dockerregistry.backend.entity.AuditLogEntity;
import opensource.dockerregistry.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    public void log(String username, String action, String target) {
        AuditLogEntity log = AuditLogEntity.builder()
                .timestamp(LocalDateTime.now())
                .username(username)
                .action(action)
                .target(target)
                .build();
        System.out.println("🔥 저장 요청: " + log);  // ← 로그 찍어보기

        repository.save(log);
    }

    public List<AuditLogEntity> getByUser(String username) {
        return repository.findByUsername(username);
    }

    public List<AuditLogEntity> getByImage(String image) {
        return repository.findByTarget(image);
    }
}
