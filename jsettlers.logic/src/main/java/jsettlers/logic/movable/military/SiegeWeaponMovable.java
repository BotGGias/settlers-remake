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
package jsettlers.logic.movable.military;

import java.util.Optional;

import jsettlers.algorithms.simplebehaviortree.Node;
import jsettlers.algorithms.simplebehaviortree.Root;
import jsettlers.common.map.shapes.HexGridArea;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.EMovableAction;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.movable.MovableGrid;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.MovableManager;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.interfaces.IAttackable;
import jsettlers.logic.movable.interfaces.ISiegeAttackable;
import jsettlers.logic.movable.other.AttackableHumanMovable;
import jsettlers.logic.player.Player;

import static jsettlers.algorithms.simplebehaviortree.BehaviorTreeHelper.*;

/**
 * A siege weapon (catapult, ballista, cannon or gong). It is controlled like a soldier, shoots at enemy soldiers and military buildings from a
 * distance and reloads its ammunition from ammunition stacks next to it while it is idle.
 * <p>
 * Siege weapons can kill the defenders of a military building but can't conquer it. That has to be done by soldiers.
 */
public class SiegeWeaponMovable extends AttackableHumanMovable {
	private static final long serialVersionUID = 4270912580931675482L;

	/**
	 * Ammunition stacks in this radius are used to reload.
	 */
	public static final short RELOAD_RADIUS = 3;
	private static final int RELOAD_DURATION = 1000;
	private static final int TARGET_SCAN_INTERVAL = 2000;

	private final ESiegeWeaponType weaponType;
	private int ammo = 0;
	private int nextShotTime = 0;
	private int nextScanTime = 0;
	private boolean enemyNearby = true;
	private IAttackable target = null;

	private ShortPoint2D currentTarget = null;
	private ShortPoint2D goToTarget = null;
	private int patrolStep = -1;
	private ShortPoint2D[] patrolPoints = null;

	public SiegeWeaponMovable(AbstractMovableGrid grid, EMovableType movableType, ShortPoint2D position, Player player, Movable movable) {
		super(grid, movableType, position, player, movable);
		this.weaponType = ESiegeWeaponType.fromMovableType(movableType);

		if (movable instanceof SiegeWeaponMovable) { // e.g. the weapon changed its player
			ammo = ((SiegeWeaponMovable) movable).ammo;
		}
	}

	static {
		Root<SiegeWeaponMovable> behaviour = new Root<>(createSiegeWeaponBehaviour());

		for (EMovableType type : EMovableType.SIEGE_WEAPONS) {
			MovableManager.registerBehaviour(type, behaviour);
		}
	}

	private static Node<SiegeWeaponMovable> createSiegeWeaponBehaviour() {
		return guardSelector(
				handleFrozenEffect(),
				guard(mov -> mov.nextTarget != null,
					action(SiegeWeaponMovable::applyMoveOrder)
				),
				guard(mov -> mov.goToTarget != null,
					sequence(
						ignoreFailure(goToPos(mov -> mov.goToTarget)),
						action(mov -> {
							mov.enterFerry();
							mov.goToTarget = null;
							mov.startNextWaypoint();
						})
					)
				),
				guard(SiegeWeaponMovable::isReadyToFire,
					selector(
						sequence(
							condition(SiegeWeaponMovable::findTarget),
							fire()
						),
						action(mov -> {
							mov.target = null;
							mov.enemyNearby = false;
							mov.nextScanTime = MatchConstants.clock().getTime() + TARGET_SCAN_INTERVAL;
						})
					)
				),
				guard(mov -> !mov.hasActiveMoveOrder() && mov.patrolStep == -1 && mov.findAmmoStack().isPresent(),
					sequence(
						sleep(RELOAD_DURATION),
						action(SiegeWeaponMovable::reload)
					)
				),
				guard(mov -> mov.currentTarget != null,
					sequence(
						ignoreFailure(goToPos(mov -> mov.currentTarget)),
						action(mov -> {
							mov.currentTarget = null;
							mov.startNextWaypoint();
						})
					)
				),
				guard(mov -> mov.patrolStep != -1,
					sequence(
						ignoreFailure(goToPos(mov -> mov.patrolPoints[mov.patrolStep])),
						action(mov -> {
							mov.patrolStep = (mov.patrolStep + 1) % mov.patrolPoints.length;
						})
					)
				),
				guard(mov -> true,
					sequence(
						action(mov -> {
							if (MatchConstants.clock().getTime() >= mov.nextScanTime) {
								mov.enemyNearby = true; // scan again from time to time, e.g. for buildings that got new defenders
							}
						}),
						doingNothingAction()
					)
				)
		);
	}

	private static Node<SiegeWeaponMovable> fire() {
		return sequence(
				condition(SiegeWeaponMovable::isTargetValid),
				action(mov -> {
					mov.setDirection(EDirection.getApproxDirection(mov.position, mov.target.getPosition()));
				}),
				playAction(EMovableAction.ACTION1, mov -> mov.weaponType.fireDurationMs),
				condition(SiegeWeaponMovable::isTargetValid),
				action(SiegeWeaponMovable::shoot)
		);
	}

	private void applyMoveOrder() {
		abortGoTo();

		switch (nextMoveToType) {
		default:
		case DEFAULT:
			currentTarget = nextTarget;
			break;
		case FORCED:
			goToTarget = nextTarget;
			break;
		case PATROL:
			patrolPoints = new ShortPoint2D[] { position, nextTarget };
			patrolStep = 0;
			break;
		}

		nextTarget = null;
	}

	private void abortGoTo() {
		currentTarget = null;
		goToTarget = null;
		patrolStep = -1;
		patrolPoints = null;
	}

	private boolean isReadyToFire() {
		return enemyNearby && hasAmmo() && MatchConstants.clock().getTime() >= nextShotTime;
	}

	private boolean findTarget() {
		target = grid.getSiegeTarget(position, player, weaponType.minRange, weaponType.maxRange);
		return target != null;
	}

	private boolean isTargetValid() {
		if (target == null || !target.isAlive() || !MovableGrid.isEnemy(player, target)) {
			return false;
		}
		if (target instanceof ISiegeAttackable && !((ISiegeAttackable) target).canReceiveSiegeDamage()) {
			return false;
		}

		int distance = position.getOnGridDistTo(target.getPosition());
		return weaponType.minRange <= distance && distance <= weaponType.maxRange;
	}

	private void shoot() {
		float strength = weaponType.damage * player.getCombatStrengthInformation().getCombatStrength(isOnOwnGround());

		if (weaponType.usesAmmo()) {
			ammo--;
			grid.addSiegeProjectile(position, player, weaponType, strength, target.getPosition());
		} else {
			grid.applySiegeDamage(position, weaponType.splashRadius, strength, player, position);
		}

		nextShotTime = MatchConstants.clock().getTime() + weaponType.cooldownMs;
	}

	private boolean hasAmmo() {
		return !weaponType.usesAmmo() || ammo > 0;
	}

	/**
	 * @return A position next to this weapon with an ammunition stack it may use or an empty {@link Optional} if it can't reload now.
	 */
	private Optional<ShortPoint2D> findAmmoStack() {
		if (!weaponType.usesAmmo() || ammo >= weaponType.maxAmmo) {
			return Optional.empty();
		}

		return HexGridArea.stream(position.x, position.y, 0, RELOAD_RADIUS)
				.filterBounds(grid.getWidth(), grid.getHeight())
				.filter((x, y) -> {
					ShortPoint2D stackPosition = new ShortPoint2D(x, y);
					Player owner = grid.getPlayerAt(stackPosition);
					return (owner == null || owner.hasSameTeam(player)) && grid.canTakeMaterial(stackPosition, weaponType.getAmmo());
				})
				.getFirst();
	}

	private void reload() {
		findAmmoStack().ifPresent(stackPosition -> {
			if (grid.takeMaterial(stackPosition, weaponType.getAmmo())) {
				ammo++;
			}
		});
	}

	@Override
	protected boolean hasActiveMoveOrder() {
		return super.hasActiveMoveOrder() || currentTarget != null || goToTarget != null;
	}

	@Override
	public void informAboutAttackable(IAttackable other) {
		enemyNearby = true;
	}

	@Override
	public void receiveHit(float hitStrength, ShortPoint2D attackerPos, IPlayer attackingPlayer) {
		super.receiveHit(hitStrength, attackerPos, attackingPlayer);
		enemyNearby = true;
	}

	@Override
	public boolean needsTreatment() {
		return false; // healers can't repair war machines
	}

	public ESiegeWeaponType getWeaponType() {
		return weaponType;
	}

	public int getAmmo() {
		return ammo;
	}

	/**
	 * Sets the ammunition directly, e.g. for weapons placed by a map.
	 */
	public void setAmmo(int ammo) {
		this.ammo = Math.max(0, Math.min(ammo, weaponType.maxAmmo));
	}
}
