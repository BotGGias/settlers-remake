/*******************************************************************************
 * Copyright (c) 2026
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.map.grid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.common.CommonConstants;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.stacks.RelativeStack;
import jsettlers.common.map.shapes.HexGridArea;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.integration.ai.AiTestUtils;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.buildings.military.occupying.OccupyingBuilding;
import jsettlers.logic.buildings.workers.SiegeWorkshopBuilding;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.ILogicMovable;
import jsettlers.logic.movable.military.SiegeWeaponMovable;
import jsettlers.logic.player.Player;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.replay.ReplayUtils;
import jsettlers.testutils.TestUtils;
import jsettlers.testutils.map.MapUtils;

/**
 * Tests the siege workshops and the siege weapons in a running game.
 */
public class SiegeWeaponsIT {
	private static final byte PLAYER = 0;
	private static final byte ENEMY = 1;
	private static final int STEP_MS = 200;

	static {
		CommonConstants.ENABLE_CONSOLE_LOGGING = true;
		Constants.FOG_OF_WAR_DEFAULT_ENABLED = false;

		TestUtils.setupTempResourceManager();
	}

	private IStartedGame startedGame;
	private MainGrid grid;
	private Player player;
	private Player enemy;
	private int time;

	@Before
	public void startGame() throws MapLoadException {
		MapLoader map = MapUtils.getMountainlake();
		PlayerSetting[] playerSettings = AiTestUtils.getDefaultPlayerSettings(map.getMaxPlayers());
		playerSettings[PLAYER] = new PlayerSetting(EPlayerType.HUMAN, ECivilisation.ROMAN, PLAYER);
		playerSettings[ENEMY] = new PlayerSetting(EPlayerType.HUMAN, ECivilisation.EGYPTIAN, ENEMY);

		JSettlersGame.GameRunner game = AiTestUtils.createStartingGame(playerSettings, map);
		startedGame = ReplayUtils.waitForGameStartup(game);

		MatchConstants.clock().setPausing(true); // the test thread executes the game
		time = 1000;
		MatchConstants.clock().fastForwardTo(time);

		grid = game.getMainGrid();
		player = grid.partitionsGrid.getPlayer(PLAYER);
		enemy = grid.partitionsGrid.getPlayer(ENEMY);
	}

	@After
	public void stopGame() {
		ReplayUtils.awaitShutdown(startedGame);
	}

	@Test
	public void testWorkshopProducesAmmunitionAndOrderedWeapons() {
		ShortPoint2D workshopPosition = findConstructionPosition(EBuildingType.CATAPULT_WORKSHOP, PLAYER);
		SiegeWorkshopBuilding workshop = (SiegeWorkshopBuilding) grid.constructBuildingAt(workshopPosition, EBuildingType.CATAPULT_WORKSHOP, player, true);
		Movable.createMovable(EMovableType.SIEGE_ENGINEER, player, findFreePosition(workshop.getDoor(), 1), grid.movablePathfinderGrid);

		// ammunition is produced from stone, but never more than fits on the stack
		supply(workshop, EMaterialType.STONE, 8);
		assertTrue("no ammunition produced", runUntil(() -> workshop.getAmmoStackSize() > 0, 3 * 60 * 1000));
		runFor(3 * 60 * 1000);
		assertTrue(workshop.getAmmoStackSize() <= ESiegeWeaponType.AMMO_STACK_CAPACITY);

		// an ordered weapon is built from planks and iron
		workshop.orderSiegeWeapon();
		assertEquals(1, workshop.getOrderedSiegeWeapons());
		supply(workshop, EMaterialType.PLANK, 4);
		supply(workshop, EMaterialType.IRON, 2);
		assertTrue("no catapult built", runUntil(() -> !getWeapons(EMovableType.CATAPULT).isEmpty(), 5 * 60 * 1000));
		assertEquals(0, workshop.getOrderedSiegeWeapons());

		// the new weapon loads the ammunition of the workshop
		SiegeWeaponMovable catapult = getWeapons(EMovableType.CATAPULT).get(0);
		assertTrue("catapult did not reload", runUntil(() -> catapult.getAmmo() > 0, 60 * 1000));
	}

	@Test
	public void testAmmunitionProductionCanBeSwitchedOff() {
		ShortPoint2D workshopPosition = findConstructionPosition(EBuildingType.CATAPULT_WORKSHOP, PLAYER);
		SiegeWorkshopBuilding workshop = (SiegeWorkshopBuilding) grid.constructBuildingAt(workshopPosition, EBuildingType.CATAPULT_WORKSHOP, player, true);
		Movable.createMovable(EMovableType.SIEGE_ENGINEER, player, findFreePosition(workshop.getDoor(), 1), grid.movablePathfinderGrid);
		assertTrue(workshop.isAmmoProductionEnabled());

		workshop.setAmmoProduction(false);
		assertFalse(workshop.isAmmoProductionEnabled());
		supply(workshop, EMaterialType.STONE, 4);
		runFor(2 * 60 * 1000);
		assertEquals("ammunition produced while switched off", 0, workshop.getAmmoStackSize());

		workshop.setAmmoProduction(true);
		assertTrue("no ammunition produced after switching on", runUntil(() -> workshop.getAmmoStackSize() > 0, 3 * 60 * 1000));
	}

	@Test
	public void testSiegeWeaponsWeakenButNeverConquerTowers() {
		OccupyingBuilding tower = Building.getAllBuildings().stream()
				.filter(building -> building instanceof OccupyingBuilding && building.getPlayer() == enemy)
				.map(building -> (OccupyingBuilding) building)
				.findFirst().orElseThrow(() -> new AssertionError("enemy has no tower"));
		assertTrue("the soldiers did not enter the tower", runUntil(() -> tower.getOccupiers().size() > 0, 60 * 1000));
		ShortPoint2D door = tower.getDoor();

		// a catapult in range shoots at the tower
		SiegeWeaponMovable catapult = (SiegeWeaponMovable) Movable.createMovable(EMovableType.CATAPULT, player, findFreePosition(door, 15), grid.movablePathfinderGrid);
		catapult.setAmmo(6);
		assertTrue("catapult did not shoot at the tower", runUntil(() -> catapult.getAmmo() < 6, 60 * 1000));

		// siege hits kill the defenders but don't conquer the tower
		for (int i = 0; i < 100 && tower.canReceiveSiegeDamage(); i++) {
			tower.receiveSiegeHit(1000, catapult.getPosition(), player);
			runFor(STEP_MS);
		}
		assertFalse(tower.canReceiveSiegeDamage());
		assertTrue(tower.getOccupiers().isEmpty());
		assertEquals(enemy, tower.getPlayer());
		assertFalse(tower.isDestroyed());

		// soldiers can still take the empty tower
		for (int i = 0; i < 3; i++) {
			Movable.createMovable(EMovableType.SWORDSMAN_L3, player, findFreePosition(door, 2), grid.movablePathfinderGrid);
		}
		assertTrue("tower was not conquered by soldiers", runUntil(() -> tower.getPlayer() == player, 2 * 60 * 1000));
	}

	@Test
	public void testGongDamagesEnemiesAroundIt() {
		ShortPoint2D gongPosition = findFreePosition(findConstructionPosition(EBuildingType.CATAPULT_WORKSHOP, PLAYER), 0);
		Movable gong = Movable.createMovable(EMovableType.GONG, player, gongPosition, grid.movablePathfinderGrid);
		Movable swordsman = Movable.createMovable(EMovableType.SWORDSMAN_L1, enemy, findFreePosition(gongPosition, 3), grid.movablePathfinderGrid);

		assertTrue("gong did not damage the enemy", runUntil(() -> !swordsman.isAlive() || swordsman.getHealth() < EMovableType.SWORDSMAN_L1.getHealth(), 30 * 1000));
		assertTrue(gong.getHealth() > 0);
	}

	private List<SiegeWeaponMovable> getWeapons(EMovableType type) {
		return MovableManager.getAllMovables().stream()
				.filter(movable -> movable.isAlive() && movable.getPlayer() == player && movable.getMovableType() == type)
				.map(movable -> (SiegeWeaponMovable) movable)
				.collect(Collectors.toList());
	}

	private void supply(SiegeWorkshopBuilding workshop, EMaterialType material, int count) {
		for (RelativeStack stack : workshop.getBuildingVariant().getRequestStacks()) {
			if (stack.getMaterialType() == material) {
				for (int i = 0; i < count; i++) {
					grid.movablePathfinderGrid.dropMaterial(stack.calculatePoint(workshop.getPosition()), material, false, false);
				}
				return;
			}
		}
		throw new AssertionError("no stack for " + material);
	}

	private ShortPoint2D findConstructionPosition(EBuildingType type, byte playerId) {
		ShortPoint2D center = Building.getAllBuildings().stream()
				.filter(building -> building.getPlayer().getPlayerId() == playerId)
				.findFirst().get().getPosition();

		Optional<ShortPoint2D> position = HexGridArea.stream(center.x, center.y, 8, 40)
				.filterBounds(grid.width, grid.height)
				.filter((x, y) -> grid.constructionMarksGrid.canConstructAt(x, y, type, playerId))
				.getFirst();
		assertTrue("no construction position for " + type, position.isPresent());
		return position.get();
	}

	private ShortPoint2D findFreePosition(ShortPoint2D center, int minRadius) {
		Optional<ShortPoint2D> position = HexGridArea.stream(center.x, center.y, minRadius, minRadius + 10)
				.filterBounds(grid.width, grid.height)
				.filter((x, y) -> grid.movablePathfinderGrid.isFreePosition(x, y) && !grid.movablePathfinderGrid.isWater(x, y)
						&& !grid.flagsGrid.isProtected(x, y))
				.getFirst();
		assertNotNull(position.orElse(null));
		return position.get();
	}

	private boolean runUntil(BooleanSupplier condition, int timeoutMs) {
		for (int passed = 0; passed < timeoutMs; passed += STEP_MS) {
			runFor(STEP_MS);
			if (condition.getAsBoolean()) {
				return true;
			}
		}
		return false;
	}

	private void runFor(int ms) {
		time += ms;
		MatchConstants.clock().fastForwardTo(time);
	}
}
