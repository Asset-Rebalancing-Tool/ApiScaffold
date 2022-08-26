package ARApi.Scaffold.Endpoints;


import ARApi.Scaffold.Database.Entities.User;
import ARApi.Scaffold.Database.Repos.*;
import ARApi.Scaffold.Endpoints.Model.*;
import ARApi.Scaffold.Endpoints.Requests.PostAssetHoldingGroupingRequest;
import ARApi.Scaffold.Endpoints.Requests.PostPrivateAssetHoldingRequest;
import ARApi.Scaffold.Endpoints.Requests.PostPublicAssetHoldingRequest;
import ARApi.Scaffold.Endpoints.Requests.PrivateCategoryRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

// TODO: rename everything to possession...
@RestController
@Component
@RequestMapping("holding_api")
public class HoldingApi {

    private final User user;

    private final PrivateCategoryRepository privateCategoryRepository;

    private final UserRepository userRepository;

    private final AssetHoldingGroupingRepository assetHoldingGroupingRepository;

    private final PublicAssetHoldingRepository publicAssetHoldingRepository;

    private final PrivateAssetHoldingRepository privateAssetHoldingRepository;

    private final PublicAssetRepository publicAssetRepository;

    @Autowired
    public HoldingApi(PrivateCategoryRepository privateCategoryRepository, UserRepository userRepository, AssetHoldingGroupingRepository assetHoldingGroupingRepository, PublicAssetHoldingRepository publicAssetHoldingRepository, PrivateAssetHoldingRepository privateAssetHoldingRepository, PublicAssetRepository publicAssetRepository) {
        this.privateCategoryRepository = privateCategoryRepository;
        this.userRepository = userRepository;
        this.assetHoldingGroupingRepository = assetHoldingGroupingRepository;
        this.publicAssetHoldingRepository = publicAssetHoldingRepository;
        this.privateAssetHoldingRepository = privateAssetHoldingRepository;
        this.publicAssetRepository = publicAssetRepository;

        // create temp user if not exists
        var users = userRepository.findAll();
        if (users.isEmpty()) {
            var user = new User();
            users.add(user);
            userRepository.saveAndFlush(user);
        }
        user = users.get(0);
    }

    @PostMapping("/category")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelPrivateCategory PostPrivateCategory(@RequestBody PrivateCategoryRequest privateCategoryRequest) {
        if (privateCategoryRequest.categoryName == null || privateCategoryRequest.categoryName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoryName is null or blank");
        }

        var privateCategory = privateCategoryRepository.save(privateCategoryRequest.toPrivateCategory(user.uuid, userRepository));

        return new ModelPrivateCategory(privateCategory);
    }

    @GetMapping("/category")
    public List<ModelPrivateCategory> GetPrivateCategories() {

        var categories = privateCategoryRepository.findByUserUuid(user.uuid);

        return categories.stream().map(ModelPrivateCategory::new).toList();
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
    @ResponseStatus(HttpStatus.CREATED)
    public ModelPublicAssetHolding PostPublicAssetHolding(@RequestBody PostPublicAssetHoldingRequest postPublicAssetHoldingRequest) {
        var uuid = UUID.fromString(postPublicAssetHoldingRequest.publicAssetUuid);
        if (publicAssetHoldingRepository.existsByPublicAssetUuid(uuid, user.uuid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Public asset holding already exists for user");
        }

        var publicAssetHolding = publicAssetHoldingRepository.save(
                postPublicAssetHoldingRequest.toPublicAssetHolding(user.uuid,
                        userRepository,
                        publicAssetHoldingRepository,
                        publicAssetRepository,
                        privateAssetHoldingRepository)
        );

        return new ModelPublicAssetHolding(publicAssetHolding);
    }

    @GetMapping("/asset_holding/public")
    public Set<ModelPublicAssetHolding> GetPublicAssetHoldings() {
        return publicAssetHoldingRepository.GetAssetsOfUser(user.uuid).stream().map(ModelPublicAssetHolding::new).collect(Collectors.toSet());
    }

    @PostMapping("/asset_holding/private")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelPrivateAssetHolding PostPrivateAssetHolding(@RequestBody PostPrivateAssetHoldingRequest postPrivateAssetHoldingRequest) {
        var privateAssetHolding = privateAssetHoldingRepository.save(postPrivateAssetHoldingRequest.toPrivateAssetHolding(user.uuid, userRepository, privateAssetHoldingRepository, publicAssetHoldingRepository));
        return new ModelPrivateAssetHolding(privateAssetHolding);
    }

    @GetMapping("/asset_holding/private")
    public Set<ModelPrivateAssetHolding> GetPrivateAssetsHoldings() {
        return privateAssetHoldingRepository.GetAssetsOfUser(user.uuid).stream().map(ModelPrivateAssetHolding::new).collect(Collectors.toSet());
    }

    @PostMapping("/asset_holding/grouping")
    @ResponseStatus(HttpStatus.CREATED)
    public ModelAssetHoldingGrouping PostAssetHoldingGrouping(@RequestBody PostAssetHoldingGroupingRequest postAssetHoldingGroupingRequest) {
        var assetGrouping = assetHoldingGroupingRepository.save(postAssetHoldingGroupingRequest.toAssetHoldingGrouping(user.uuid, userRepository, publicAssetHoldingRepository, privateAssetHoldingRepository));
        return new ModelAssetHoldingGrouping(assetGrouping);
    }

    @GetMapping("/asset_holding/grouping")
    public Set<ModelAssetHoldingGrouping> GetAssetHoldingGroupings() {
        return assetHoldingGroupingRepository.GetByUserUuid(user.uuid).stream().map(ModelAssetHoldingGrouping::new).collect(Collectors.toSet());
    }

    @DeleteMapping("/asset_holding/grouping/{groupingUuid}")
    public HttpStatus DeleteAssetHoldingGrouping(@PathVariable String groupingUuid) {
        var assetGroupingUuid = UUID.fromString(groupingUuid);
        assetHoldingGroupingRepository.deleteById(assetGroupingUuid);
        return HttpStatus.OK;
    }
}
