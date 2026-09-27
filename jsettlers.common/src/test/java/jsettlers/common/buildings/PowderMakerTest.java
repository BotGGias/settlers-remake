package jsettlers.common.buildings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.junit.Test;

import jsettlers.common.buildings.stacks.RelativeStack;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.player.ECivilisation;

public class PowderMakerTest {

	@Test
	public void testOnlyAsiansHavePowderMaker() {
		for (ECivilisation civilisation : ECivilisation.VALUES) {
			BuildingVariant variant = EBuildingType.POWDER_MAKER.getVariant(civilisation);
			if (civilisation == ECivilisation.ASIAN) {
				assertNotNull(variant);
			} else {
				assertNull(civilisation + " must not have a powder maker", variant);
			}
		}
	}

	@Test
	public void testPowderMakerTurnsSulfurAndCoalIntoGunPowder() {
		BuildingVariant variant = EBuildingType.POWDER_MAKER.getVariant(ECivilisation.ASIAN);

		assertEquals(EMovableType.POWDER_MAKER, variant.getWorkerType());
		assertEquals(EnumSet.of(EMaterialType.SULFUR, EMaterialType.COAL), materialsOf(variant.getRequestStacks()));
		assertEquals(EnumSet.of(EMaterialType.GUN_POWDER), materialsOf(variant.getOfferStacks()));
		assertNotNull(variant.getOvenPosition());
	}

	private static EnumSet<EMaterialType> materialsOf(RelativeStack[] stacks) {
		return Arrays.stream(stacks)
				.map(RelativeStack::getMaterialType)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(EMaterialType.class)));
	}
}
