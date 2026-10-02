package jsettlers.common.buildings;

import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import jsettlers.common.images.ImageLink;
import jsettlers.common.images.OriginalImageLink;
import jsettlers.common.player.ECivilisation;

public class BuildingImagesTest {

	@Test
	public void testEachBuildingOfACivilisationHasItsOwnGuiIcon() {
		for (ECivilisation civilisation : ECivilisation.VALUES) {
			Map<String, EBuildingType> usedIcons = new HashMap<>();
			for (EBuildingType buildingType : EBuildingType.VALUES) {
				BuildingVariant variant = buildingType.getVariant(civilisation);
				if (variant != null) {
					EBuildingType previous = usedIcons.put(keyOf(variant.getGuiImage()), buildingType);
					assertNull(civilisation + ": " + buildingType + " uses the gui icon of " + previous, previous);
				}
			}
		}
	}

	@Test
	public void testEachBuildingOfACivilisationHasItsOwnSprite() {
		for (ECivilisation civilisation : ECivilisation.VALUES) {
			Map<String, EBuildingType> usedSprites = new HashMap<>();
			for (EBuildingType buildingType : EBuildingType.VALUES) {
				BuildingVariant variant = buildingType.getVariant(civilisation);
				if (variant != null) {
					EBuildingType previous = usedSprites.put(keyOf(variant.getImages()[0]), buildingType);
					assertNull(civilisation + ": " + buildingType + " uses the sprite of " + previous, previous);
				}
			}
		}
	}

	private static String keyOf(ImageLink link) {
		OriginalImageLink original = (OriginalImageLink) link;
		return original.getType() + ":" + original.getFile() + ":" + original.getSequence() + ":" + original.getImage();
	}
}
