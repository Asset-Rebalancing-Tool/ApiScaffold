/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package ARApi.Scaffold;

import ARApi.Scaffold.AssetFetchers.DbAssetFetcher;
import ARApi.Scaffold.Database.Entities.PublicAsset.PublicAsset;
import ARApi.Scaffold.Database.Entities.PublicAsset.PublicAssetPriceRecord;
import ARApi.Scaffold.Database.Entities.PublicAsset.PublicAssetRepository;
import ARApi.Scaffold.Endpoints.AssetApi;
import ARApi.Scaffold.WebDriver.IWebDriverService;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


import java.util.Set;
import java.util.UUID;

@ContextConfiguration("/test.xml")
@ExtendWith(SpringExtension.class)
class AppTest {

    @Autowired
    AssetApi stockApi;

    @Autowired
    SessionFactory dbSessionProvider;

    @Autowired
    IWebDriverService webDriverService;

    @Autowired
    DbAssetFetcher assetFetcher;

    @Autowired
    PublicAssetRepository publicAssetRepository;

    @Test void SaveAsset(){
        var session = dbSessionProvider.openSession();
        var trans = session.beginTransaction();
        var dbAsset = new PublicAsset();
        dbAsset.isin = UUID.randomUUID().toString();
        dbAsset.asset_name = UUID.randomUUID().toString();
        var dbAssetRecord = new PublicAssetPriceRecord();
        dbAssetRecord.Asset = dbAsset;
        dbAsset.AssetPriceRecords = Set.of(dbAssetRecord);
        session.persist(dbAssetRecord);
        session.persist(dbAsset);
        trans.commit();
        session.close();
    }

    @Test void TestFetcher(){
        assetFetcher.GetTargetHost();
    }

    @Test void BulkInsert(){
        var p = publicAssetRepository.GetFullAssets();

        var assets = publicAssetRepository.findAll();
        assets.forEach(as -> {
            var r = as.AssetPriceRecords.isEmpty();
            int wait = 1;
        });


    }




}
