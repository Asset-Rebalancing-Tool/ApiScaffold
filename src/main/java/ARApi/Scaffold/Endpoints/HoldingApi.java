package ARApi.Scaffold.Endpoints;


import ARApi.Scaffold.Database.Entities.User;
import ARApi.Scaffold.Database.Repos.*;
import ARApi.Scaffold.Endpoints.Model.*;
import ARApi.Scaffold.Endpoints.Requests.PostAssetHoldingGroupRequest;
import ARApi.Scaffold.Endpoints.Requests.PostPrivateAssetHoldingRequest;
import ARApi.Scaffold.Endpoints.Requests.PostPublicAssetHoldingRequest;
import ARApi.Scaffold.Endpoints.Requests.PrivateCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

// TODO: rename everything to possession...
@RestController
@Component
@RequestMapping("holding_api")

public class HoldingApi {
    
    private final PrivateCategoryRepository privateCategoryRepository;

    private final UserRepository userRepository;

    private final AssetHoldingGroupRepository assetHoldingGroupRepository;

    private final PublicAssetHoldingRepository publicAssetHoldingRepository;

    private final PrivateAssetHoldingRepository privateAssetHoldingRepository;

    private final PublicAssetRepository publicAssetRepository;

    @Autowired
    public HoldingApi(PrivateCategoryRepository privateCategoryRepository, UserRepository userRepository, AssetHoldingGroupRepository assetHoldingGroupRepository, PublicAssetHoldingRepository publicAssetHoldingRepository, PrivateAssetHoldingRepository privateAssetHoldingRepository, PublicAssetRepository publicAssetRepository) {
        this.privateCategoryRepository = privateCategoryRepository;
        this.userRepository = userRepository;
        this.assetHoldingGroupRepository = assetHoldingGroupRepository;
        this.publicAssetHoldingRepository = publicAssetHoldingRepository;
        this.privateAssetHoldingRepository = privateAssetHoldingRepository;
        this.publicAssetRepository = publicAssetRepository;
    }  
    
    public UUID getUserUuid(){
        var auth = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return auth.uuid;
    }

    @PostMapping("/category")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ModelPrivateCategory> PostPrivateCategory(@RequestBody PrivateCategoryRequest privateCategoryRequest) {
        if (privateCategoryRequest.categoryName == null || privateCategoryRequest.categoryName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryName is null or blank");
        }

        var privateCategory = privateCategoryRepository.saveAndFlush(privateCategoryRequest.toPrivateCategory(getUserUuid(), userRepository));

        return ResponseEntity.status(HttpStatus.CREATED).body(new ModelPrivateCategory(privateCategory));
    }

    @GetMapping("/category")
    public ModelPrivateCategory[] GetPrivateCategories() {

        var categories = privateCategoryRepository.findByUserUuid(getUserUuid());
        var arr = categories.stream().map(ModelPrivateCategory::new).toArray(ModelPrivateCategory[]::new);

        return arr;
    }

    @DeleteMapping("/category/{uuidStr}")
    public HttpStatus DeletePrivateCategory(@PathVariable String uuidStr) {
        var uuid = UUID.fromString(uuidStr);
        if (!privateCategoryRepository.existsById(uuid)) {
            return HttpStatus.NOT_FOUND;
        }

        privateCategoryRepository.deleteById(uuid);
        return HttpStatus.OK;
    }

    @PostMapping("/asset_holding/public")
    public ResponseEntity<ModelPublicAssetHolding> PostPublicAssetHolding(@RequestBody PostPublicAssetHoldingRequest postPublicAssetHoldingRequest) {
        var uuid = UUID.fromString(postPublicAssetHoldingRequest.publicAssetUuid);
        if (publicAssetHoldingRepository.existsByPublicAssetUuid(uuid, getUserUuid())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "HOLDING_FOR_ASSET_EXISTS");
        }

        var publicAssetHolding = publicAssetHoldingRepository.save(
                postPublicAssetHoldingRequest.toPublicAssetHolding(getUserUuid(),
                        userRepository,
                        publicAssetHoldingRepository,
                        publicAssetRepository,
                        privateAssetHoldingRepository)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(new ModelPublicAssetHolding(publicAssetHolding));
    }

    @GetMapping("/asset_holding/public")
    public ModelPublicAssetHolding[] GetPublicAssetHoldings() {
        var holdings = publicAssetHoldingRepository.GetAssetsOfUser(getUserUuid()).stream().map(ModelPublicAssetHolding::new).toArray(ModelPublicAssetHolding[]::new);
        return holdings;
    }

    @DeleteMapping("/asset_holding/public/{holdingUuid}")
    public HttpStatus DeletePublicAssetHolding(@PathVariable String holdingUuid) {
        publicAssetHoldingRepository.deleteById(UUID.fromString(holdingUuid));
        return HttpStatus.OK;
    }

    @PostMapping("/asset_holding/private")
    public ResponseEntity<ModelPrivateAssetHolding> PostPrivateAssetHolding(@RequestBody PostPrivateAssetHoldingRequest postPrivateAssetHoldingRequest) {
        var privateAssetHolding = privateAssetHoldingRepository.save(postPrivateAssetHoldingRequest.toPrivateAssetHolding(getUserUuid(), userRepository, privateAssetHoldingRepository, publicAssetHoldingRepository));
        return ResponseEntity.status(HttpStatus.CREATED).body(new ModelPrivateAssetHolding(privateAssetHolding));
    }

    @GetMapping("/asset_holding/private")
    public ModelPrivateAssetHolding[] GetPrivateAssetHoldings() {
        return privateAssetHoldingRepository.GetAssetsOfUser(getUserUuid()).stream().map(ModelPrivateAssetHolding::new).toArray(ModelPrivateAssetHolding[]::new);
    }

    @DeleteMapping("/asset_holding/private/{holdingUuid}")
    public HttpStatus DeletePrivateAssetHolding(@PathVariable String holdingUuid){
        privateAssetHoldingRepository.deleteById(UUID.fromString(holdingUuid));
        return HttpStatus.OK;
    }

    @PostMapping("/asset_holding/group")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelAssetHoldingGroup PostAssetHoldingGroup(@RequestBody PostAssetHoldingGroupRequest postAssetHoldingGroupRequest) {
        var assetGrouping = assetHoldingGroupRepository.save(postAssetHoldingGroupRequest.toAssetHoldingGrouping(getUserUuid(), userRepository, publicAssetHoldingRepository, privateAssetHoldingRepository));
        return new ModelAssetHoldingGroup(assetGrouping);
    }

    @GetMapping("/asset_holding/group")
    public ModelAssetHoldingGroup[] GetAssetHoldingGroups() {
        return assetHoldingGroupRepository.GetByUserUuid(getUserUuid()).stream().map(ModelAssetHoldingGroup::new).toArray(ModelAssetHoldingGroup[]::new);
    }

    @DeleteMapping("/asset_holding/group/{groupUuid}")
    public HttpStatus DeleteAssetHoldingGroup(@PathVariable String groupUuid) {
        var assetGroupingUuid = UUID.fromString(groupUuid);
        assetHoldingGroupRepository.deleteById(assetGroupingUuid);
        return HttpStatus.OK;
    }
}
