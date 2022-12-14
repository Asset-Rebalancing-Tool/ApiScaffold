package ARApi.Scaffold.AssetFetchers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AssetFetcherManager {

    private final Set<IPublicAssetFetcher> WorkingFetchers;

    @Autowired
    public AssetFetcherManager(List<IPublicAssetFetcher> assetFetchers) {
        WorkingFetchers = new HashSet<>(assetFetchers);
    }

    public <T> T ExecuteWithFetcher (FetcherExecute<T> execute){
        while(!WorkingFetchers.isEmpty()){
            IPublicAssetFetcher fetcher = GetFetcher();
            try{
                return execute.Execute(fetcher);
            }catch (Exception e){
                e.printStackTrace();
                WorkingFetchers.remove(fetcher);
                // TODO: CREATE BUG ITEM
            }
        }
        return null;
    }

    private IPublicAssetFetcher GetFetcher(){
        // TODO: split the load somehow
        if(WorkingFetchers.isEmpty()){
            return null;
        }
        return WorkingFetchers.stream().findAny().get();
    }
}
