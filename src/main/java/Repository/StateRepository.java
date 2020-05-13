package Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import Entity.GupshupStateEntity;

@Repository
public interface StateRepository extends JpaRepository<GupshupStateEntity, Long> {
  GupshupStateEntity findByPhoneNo(String phoneNo);
}
