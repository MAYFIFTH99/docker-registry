package opensource.dockerregistry.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opensource.dockerregistry.backend.dto.AuditLogRequestDto;
import opensource.dockerregistry.backend.entity.AuditLogEntity;
import opensource.dockerregistry.backend.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository repository;

    public void log(AuditLogRequestDto dto) {
        AuditLogEntity logEntity = AuditLogEntity.builder()
                .timestamp(LocalDateTime.now())
                .username(dto.getUsername())
                .action(dto.getAction())
                .target(dto.getTarget())
                .build();

        log.info("저장 요청: {}", logEntity);
        repository.save(logEntity);
    }

    public List<AuditLogEntity> getByUser(String username) {
        return repository.findByUsername(username);
    }

    public List<AuditLogEntity> getByImage(String image) {
        return repository.findByTarget(image);
    }
}
