package ARApi.Scaffold.Database.Entities;

import ARApi.Scaffold.Database.Entities.PublicAsset.PublicAssetPriceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PriceRecordRepository extends JpaRepository<PublicAssetPriceRecord, UUID> {
}
