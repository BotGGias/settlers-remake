package jsettlers.common.buildings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.stream.Collectors;

import org.junit.Test;

import jsettlers.common.buildings.stacks.RelativeStack;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.selectable.ESelectionType;

public class SiegeWorkshopTest {

	@Test
	public void testEachCivilisationHasExactlyOneSiegeWeapon() {
		for (ECivilisation civilisation : ECivilisation.VALUES) {
			ESiegeWeaponType weaponType = ESiegeWeaponType.forCivilisation(civilisation);
			assertNotNull(civilisation + " has no siege weapon", weaponType);
			assertSame(civilisation, weaponType.civilisation);
		}
		assertEquals(ECivilisation.VALUES.length, ESiegeWeaponType.VALUES.length);
	}

	@Test
	public void testWorkshopsOnlyExistForTheirCivilisation() {
		for (ESiegeWeaponType weaponType : ESiegeWeaponType.VALUES) {
			for (ECivilisation civilisation : ECivilisation.VALUES) {
				BuildingVariant variant = weaponType.workshop.getVariant(civilisation);
				if (civilisation == weaponType.civilisation) {
					assertNotNull(variant);
				} else {
					assertNull(civilisation + " must not have a " + weaponType.workshop, variant);
				}
			}
		}
	}

	@Test
	public void testWorkshopStacksMatchTheRecipes() {
		for (ESiegeWeaponType weaponType : ESiegeWeaponType.VALUES) {
			BuildingVariant variant = weaponType.workshop.getVariant(weaponType.civilisation);

			assertEquals(EMovableType.SIEGE_ENGINEER, variant.getWorkerType());
			assertNotNull(variant.getAnvilPosition());
			assertNotNull(variant.getSmithDropPosition());

			EnumSet<EMaterialType> expectedRequests = EnumSet.noneOf(EMaterialType.class);
			expectedRequests.addAll(Arrays.asList(weaponType.getAmmoInputs()));
			expectedRequests.addAll(Arrays.asList(weaponType.getWeaponCostMaterials()));
			assertEquals(weaponType.toString(), expectedRequests, materialsOf(variant.getRequestStacks()));
			assertEquals("one request stack per material", expectedRequests.size(), variant.getRequestStacks().length);

			if (weaponType.usesAmmo()) {
				assertEquals(EnumSet.of(weaponType.getAmmo()), materialsOf(variant.getOfferStacks()));
			} else {
				assertEquals(0, variant.getOfferStacks().length);
			}
		}
	}

	@Test
	public void testWeaponTypesAreConsistent() {
		for (ESiegeWeaponType weaponType : ESiegeWeaponType.VALUES) {
			EMovableType movableType = weaponType.movableType;

			assertSame(weaponType, ESiegeWeaponType.fromMovableType(movableType));
			assertSame(weaponType, ESiegeWeaponType.fromWorkshop(weaponType.workshop));
			assertTrue(movableType.isSiegeWeapon());
			assertFalse("siege weapons must not occupy towers", movableType.isSoldier());
			assertTrue(movableType.isPlayerControllable());
			assertEquals(ESelectionType.SOLDIERS, movableType.getSelectionType());
			assertTrue(weaponType.minRange <= weaponType.maxRange);
			assertTrue(weaponType.getWeaponCostMaterials().length > 0);

			if (weaponType.usesAmmo()) {
				assertFalse("ammunition is not carried by bearers", weaponType.getAmmo().isDroppable());
				assertTrue(weaponType.maxAmmo > 0);
				assertTrue(weaponType.getAmmoInputs().length > 0);
			}
		}
	}

	private static EnumSet<EMaterialType> materialsOf(RelativeStack[] stacks) {
		return Arrays.stream(stacks)
				.map(RelativeStack::getMaterialType)
				.collect(Collectors.toCollection(() -> EnumSet.noneOf(EMaterialType.class)));
	}
}
