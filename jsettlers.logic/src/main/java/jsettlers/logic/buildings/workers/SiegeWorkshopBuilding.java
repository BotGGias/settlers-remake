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
package jsettlers.logic.buildings.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.buildings.stacks.RelativeStack;
import jsettlers.common.map.shapes.HexGridArea;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.buildings.IBuildingsGrid;
import jsettlers.logic.buildings.stack.IRequestStack;
import jsettlers.logic.buildings.stack.RequestStack;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.military.SiegeWeaponMovable;
import jsettlers.logic.player.Player;

/**
 * A workshop that builds the siege weapon of its civilisation when the player orders one and produces ammunition for it otherwise.
 * <p>
 * The materials of the ammunition are requested all the time while the ammunition production is switched on. The materials that are only needed
 * for a weapon are requested only while a weapon is ordered, and only as many as the current weapon still needs. With the ammunition production
 * switched off, this applies to all materials of the weapon.
 */
public class SiegeWorkshopBuilding extends WorkerBuilding implements IBuilding.ISiegeWorkshop {
	private static final long serialVersionUID = 2894125640178405216L;

	public static final int MAX_ORDERED_WEAPONS = 9;
	private static final int WEAPON_SPAWN_SEARCH_RADIUS = 4;

	public enum ESiegeWorkshopJob {
		NONE,
		FETCH_WEAPON_MATERIAL,
		ASSEMBLE_WEAPON,
		PRODUCE_AMMO,
	}

	private final ESiegeWeaponType weaponType;
	/**
	 * Number of items of each material of {@link ESiegeWeaponType#getWeaponCostMaterials()} that were already brought to the anvil for the
	 * current weapon.
	 */
	private final short[] takenForWeapon;
	private int orderedWeapons = 0;
	private int assemblySteps = 0;
	/**
	 * The unbounded stacks requesting the materials of the ammunition. They are kept while the building is occupied.
	 */
	private List<RequestStack> ammoStacks = null;
	/**
	 * Stored inverted, so workshops of older savegames keep producing ammunition.
	 */
	private boolean ammoProductionStopped = false;

	public SiegeWorkshopBuilding(EBuildingType type, Player player, ShortPoint2D position, IBuildingsGrid buildingsGrid) {
		super(type, player, position, buildingsGrid);
		this.weaponType = ESiegeWeaponType.fromWorkshop(type);
		this.takenForWeapon = new short[weaponType.getWeaponCostMaterials().length];
	}

	@Override
	protected List<? extends IRequestStack> createWorkStacks() {
		if (ammoStacks == null && isAmmoProductionEnabled()) {
			ammoStacks = new ArrayList<>();
			for (RelativeStack stack : getBuildingVariant().getRequestStacks()) {
				if (weaponType.isAmmoInput(stack.getMaterialType())) {
					ammoStacks.add(new RequestStack(grid.getRequestStackGrid(), stack.calculatePoint(pos), stack.getMaterialType(), type, getPriority()));
				}
			}
		}

		List<RequestStack> newStacks = ammoStacks != null ? new ArrayList<>(ammoStacks) : new ArrayList<>();
		if (orderedWeapons > 0) {
			for (RelativeStack stack : getBuildingVariant().getRequestStacks()) {
				EMaterialType material = stack.getMaterialType();
				short stillNeeded = getStillNeededForWeapon(material);
				boolean requestedForAmmo = ammoStacks != null && weaponType.isAmmoInput(material);
				if (!requestedForAmmo && stillNeeded > 0) {
					newStacks.add(new RequestStack(grid.getRequestStackGrid(), stack.calculatePoint(pos), material, type, getPriority(), stillNeeded));
				}
			}
		}
		return newStacks;
	}

	@Override
	protected void releaseRequestStacks() {
		super.releaseRequestStacks();
		ammoStacks = null;
	}

	/**
	 * Replaces the bounded stacks of the last weapon with the ones of the next weapon.
	 */
	private void updateWeaponStacks() {
		if (!isOccupied() || isDestroyed()) {
			return;
		}

		for (IRequestStack stack : getStacks()) {
			if (ammoStacks == null || !ammoStacks.contains(stack)) {
				stack.releaseRequests();
			}
		}
		initWorkStacks();
	}

	public void orderSiegeWeapon() {
		if (isDestroyed() || orderedWeapons >= MAX_ORDERED_WEAPONS) {
			return;
		}

		orderedWeapons++;
		if (orderedWeapons == 1) {
			updateWeaponStacks();
		}
	}

	/**
	 * Switches the ammunition production on or off. While it is off, the workshop requests no materials for ammunition.
	 */
	public void setAmmoProduction(boolean enabled) {
		if (!weaponType.usesAmmo() || isAmmoProductionEnabled() == enabled) {
			return;
		}

		ammoProductionStopped = !enabled;
		if (!isOccupied() || isDestroyed()) {
			return; // the stacks are created when the worker arrives
		}

		for (IRequestStack stack : getStacks()) {
			stack.releaseRequests();
		}
		ammoStacks = null;
		initWorkStacks();
	}

	@Override
	public boolean isAmmoProductionEnabled() {
		return weaponType.usesAmmo() && !ammoProductionStopped;
	}

	public ESiegeWorkshopJob getNextJob() {
		if (orderedWeapons > 0) {
			if (areAllWeaponMaterialsTaken()) {
				return ESiegeWorkshopJob.ASSEMBLE_WEAPON;
			}
			if (findAvailableWeaponMaterial() != null) {
				return ESiegeWorkshopJob.FETCH_WEAPON_MATERIAL;
			}
		}
		if (canProduceAmmo()) {
			return ESiegeWorkshopJob.PRODUCE_AMMO;
		}
		return ESiegeWorkshopJob.NONE;
	}

	/**
	 * @return A material the current weapon still needs and that lies on its stack, or null if there is none.
	 */
	public EMaterialType findAvailableWeaponMaterial() {
		if (orderedWeapons <= 0) {
			return null;
		}

		for (EMaterialType material : weaponType.getWeaponCostMaterials()) {
			if (getStillNeededForWeapon(material) > 0 && hasMaterialOnStack(material)) {
				return material;
			}
		}
		return null;
	}

	/**
	 * Called when the worker brought an item of the given material to the anvil.
	 */
	public void weaponMaterialTaken(EMaterialType material) {
		EMaterialType[] costMaterials = weaponType.getWeaponCostMaterials();
		for (int i = 0; i < costMaterials.length; i++) {
			if (costMaterials[i] == material) {
				takenForWeapon[i]++;
				return;
			}
		}
	}

	/**
	 * Called for each work step at the anvil. Creates the weapon after the last step.
	 */
	public void workOnWeapon() {
		if (orderedWeapons <= 0 || !areAllWeaponMaterialsTaken() || isDestroyed()) {
			return;
		}

		if (assemblySteps < ESiegeWeaponType.ASSEMBLY_STEPS) {
			assemblySteps++;
		}
		if (assemblySteps >= ESiegeWeaponType.ASSEMBLY_STEPS && finishWeapon()) {
			assemblySteps = 0;
			for (int i = 0; i < takenForWeapon.length; i++) {
				takenForWeapon[i] = 0;
			}
			orderedWeapons--;
			updateWeaponStacks();
		}
	}

	private boolean finishWeapon() {
		AbstractMovableGrid movableGrid = grid.getMovableGrid();
		// weapons with ammunition are placed next to the ammunition stack, so they can load it right away
		ShortPoint2D center = weaponType.usesAmmo() ? getAmmoStackPosition() : getWeaponDropPosition();
		int startRadius = weaponType.usesAmmo() ? 1 : 0;
		int maxRadius = weaponType.usesAmmo() ? SiegeWeaponMovable.RELOAD_RADIUS : WEAPON_SPAWN_SEARCH_RADIUS;

		Optional<ShortPoint2D> spawnPosition = HexGridArea.stream(center.x, center.y, startRadius, maxRadius)
				.filter((x, y) -> movableGrid.isFreePosition(x, y) && !movableGrid.isWater(x, y))
				.getFirst();

		if (!spawnPosition.isPresent()) {
			return false; // try again with the next work step
		}

		Movable.createMovable(weaponType.movableType, getPlayer(), spawnPosition.get(), movableGrid);
		return true;
	}

	public boolean canProduceAmmo() {
		if (!isAmmoProductionEnabled()) {
			return false;
		}

		for (EMaterialType input : weaponType.getAmmoInputs()) {
			if (getStillNeededForWeapon(input) > 0 || !hasMaterialOnStack(input)) {
				return false; // the weapon has precedence
			}
		}

		return getAmmoStackSize() < ESiegeWeaponType.AMMO_STACK_CAPACITY;
	}

	/**
	 * Puts one ammunition item on the output stack. Ammunition can't be carried, so the worker doesn't drop it.
	 *
	 * @return true if the ammunition was stored.
	 */
	public boolean storeAmmo() {
		if (!weaponType.usesAmmo() || getAmmoStackSize() >= ESiegeWeaponType.AMMO_STACK_CAPACITY) {
			return false;
		}
		return grid.getMovableGrid().dropMaterial(getAmmoStackPosition(), weaponType.getAmmo(), false, false);
	}

	@Override
	public int getAmmoStackSize() {
		if (!weaponType.usesAmmo()) {
			return 0;
		}
		return grid.getRequestStackGrid().getStackSize(getAmmoStackPosition(), weaponType.getAmmo());
	}

	public ShortPoint2D getAmmoStackPosition() {
		for (RelativeStack stack : getBuildingVariant().getOfferStacks()) {
			if (stack.getMaterialType() == weaponType.getAmmo()) {
				return stack.calculatePoint(pos);
			}
		}
		throw new AssertionError("No ammunition stack in " + getBuildingVariant());
	}

	private ShortPoint2D getWeaponDropPosition() {
		return getBuildingVariant().getSmithDropPosition().calculatePoint(pos);
	}

	private boolean areAllWeaponMaterialsTaken() {
		for (EMaterialType material : weaponType.getWeaponCostMaterials()) {
			if (getStillNeededForWeapon(material) > 0) {
				return false;
			}
		}
		return true;
	}

	private short getStillNeededForWeapon(EMaterialType material) {
		if (orderedWeapons <= 0) {
			return 0;
		}

		EMaterialType[] costMaterials = weaponType.getWeaponCostMaterials();
		for (int i = 0; i < costMaterials.length; i++) {
			if (costMaterials[i] == material) {
				return (short) (weaponType.getWeaponCost(material) - takenForWeapon[i]);
			}
		}
		return 0;
	}

	private boolean hasMaterialOnStack(EMaterialType material) {
		for (IRequestStack stack : getStacks()) {
			if (stack.getMaterialType() == material && stack.hasMaterial()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public ESiegeWeaponType getSiegeWeaponType() {
		return weaponType;
	}

	@Override
	public int getOrderedSiegeWeapons() {
		return orderedWeapons;
	}
}
