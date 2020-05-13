package Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import Entity.GupshupMessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<GupshupMessageEntity, Long> {
  GupshupMessageEntity findByPhoneNo(String phoneNo);
}
