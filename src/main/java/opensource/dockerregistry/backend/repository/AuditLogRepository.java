package opensource.dockerregistry.backend.repository;

import java.util.List;
import opensource.dockerregistry.backend.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByUsername(String username);

    List<AuditLogEntity> findByTarget(String target);
}