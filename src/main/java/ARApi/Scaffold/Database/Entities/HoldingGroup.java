package ARApi.Scaffold.Database.Entities;

import ARApi.Scaffold.Database.Entities.PrivateAsset.PrivateHolding;
import ARApi.Scaffold.Database.Entities.PublicAsset.PublicHolding;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class HoldingGroup extends BaseUserEntity {

    @OneToMany(cascade = CascadeType.PERSIST)
    public List<PublicHolding> publicHoldings;

    @OneToMany(cascade = CascadeType.PERSIST)
    public List<PrivateHolding> privateHoldings;

    public double target_percentage;

    public String group_name;

    public boolean InternalPercentagesMatch(){
        double percentageCounter = 0;
        percentageCounter+= publicHoldings.stream().mapToDouble(value -> value.target_percentage).sum();
        return percentageCounter == 100d;
    }

    public boolean ContainsHolding(UUID holdingUuid){
        return publicHoldings.stream().anyMatch(holding -> holding.uuid.equals(holdingUuid)) ||
                privateHoldings.stream().anyMatch(holding -> holding.uuid.equals(holdingUuid));
    }
}
