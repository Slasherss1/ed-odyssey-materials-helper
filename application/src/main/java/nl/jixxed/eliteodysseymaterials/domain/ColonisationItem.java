package nl.jixxed.eliteodysseymaterials.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import nl.jixxed.eliteodysseymaterials.enums.ColonisationBuildable;
import nl.jixxed.eliteodysseymaterials.enums.ColonisationLayout;
import nl.jixxed.eliteodysseymaterials.enums.Commodity;
import nl.jixxed.eliteodysseymaterials.service.ColonisationService;
import nl.jixxed.eliteodysseymaterials.service.LocaleService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
public class ColonisationItem {

    @JsonIgnore
    public static final ApplicationState APPLICATION_STATE = ApplicationState.getInstance();
    @JsonIgnore
    public static final ColonisationItem ALL = new ColonisationItem("0", "All colonisation projects", "-1", false, null, null, new HashMap<>());
    @JsonIgnore
    private static ColonisationItem current;
    @EqualsAndHashCode.Include
    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String marketID;
    private Boolean hideFromAll = false; // Used to hide the item from the ALL list, but still show it in the current project
    private ColonisationBuildable colonisationBuildable;
    private ColonisationLayout colonisationLayout;
    private Map<Commodity, ConstructionProgress> constructionRequirements;

    public ColonisationItem(String name, String marketID, ColonisationBuildable colonisationBuildable, ColonisationLayout colonisationLayout, Map<Commodity, ConstructionProgress> constructionRequirements) {
        this.name = name;
        this.marketID = marketID;
        this.colonisationBuildable = colonisationBuildable;
        this.colonisationLayout = colonisationLayout;
        this.constructionRequirements = new HashMap<>(constructionRequirements);
    }

    public ColonisationItem(ColonisationItem other) {
        this(LocaleService.getLocalizedStringForCurrentLocale("tab.colonisation.project.name"), other.getMarketID(), other.getColonisationBuildable(), other.getColonisationLayout(), other.getConstructionRequirements());
    }


    @JsonIgnore
    public static ColonisationItem getCurrent() {
        return current;
    }

    @JsonIgnore
    public static void setCurrent(ColonisationItem current) {
        ColonisationItem.current = current;
        ColonisationItem.current.uuid = "1";
    }

    public Map<Commodity, ConstructionProgress> getConstructionRequirements() {
        if (this == ALL) {
            return APPLICATION_STATE.getPreferredCommander()
                    .map(commander -> (Map<Commodity, ConstructionProgress>) ColonisationService.getColonisationItems(commander).getAllColonisationItems().stream()
                            .filter(colonisationItem -> !colonisationItem.isAll() && !colonisationItem.isCurrent() && !colonisationItem.getHideFromAll())
                            .flatMap(colonisationItem -> colonisationItem.getConstructionRequirements().entrySet().stream())
                            .collect(HashMap<Commodity, ConstructionProgress>::new, (m, v) -> m.merge(v.getKey(), v.getValue(), ConstructionProgress::sum), HashMap::putAll))
                    .orElse(constructionRequirements);
        }
        return constructionRequirements;
    }

    public void updateAmount(Commodity commodity, ConstructionProgress value) {
        constructionRequirements.put(commodity, value);

    }

    @JsonIgnore
    public boolean isCurrent() {
        return this.uuid.equals("1");
    }

    @JsonIgnore
    public boolean isAll() {
        return this == ALL;
    }

    @Override
    public String toString() {
        if (isAll()) {
            return LocaleService.getLocalizedStringForCurrentLocale("colonisation.all.projects");
        }
        if (isCurrent()) {
            return LocaleService.getLocalizedStringForCurrentLocale("colonisation.current.project");
        }
        return name + ((colonisationBuildable == null || ColonisationBuildable.UNKNOWN.equals(colonisationBuildable)) ? "" : " - " + colonisationBuildable.toString()) + " - " + marketID;
    }
}
