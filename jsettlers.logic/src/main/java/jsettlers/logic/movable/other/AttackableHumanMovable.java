package jsettlers.logic.movable.other;

import java.util.ArrayDeque;

import jsettlers.common.action.EMoveToType;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.constants.Constants;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.movable.interfaces.AbstractMovableGrid;
import jsettlers.logic.movable.interfaces.IAttackableHumanMovable;
import jsettlers.logic.movable.interfaces.IFerryMovable;
import jsettlers.logic.movable.interfaces.IHealerMovable;
import jsettlers.logic.player.Player;

public class AttackableHumanMovable extends AttackableMovable implements IAttackableHumanMovable {

	private static final long serialVersionUID = 6890695823402563L;
	protected EMoveToType nextMoveToType;
	protected ShortPoint2D nextTarget = null;
	protected boolean goingToHealer = false;

	/**
	 * Targets that are visited one after another once the current move order is done. Might be null (e.g. in old savegames).
	 */
	private ArrayDeque<ShortPoint2D> waypoints = null;

	// the following data only for ship passengers
	protected IFerryMovable ferryToEnter = null;

	public AttackableHumanMovable(AbstractMovableGrid grid, EMovableType movableType, ShortPoint2D position, Player player, Movable movable) {
		super(grid, movableType, position, player, movable);
	}

	@Override
	public void leaveFerryAt(ShortPoint2D position) {
		this.position = position;
		setState(Movable.EMovableState.ACTIVE);

		grid.enterPosition(position, this, true);
	}

	@Override
	public void moveTo(ShortPoint2D targetPosition, EMoveToType moveToType) {
		if(!playerControlled) return;

		if(moveToType == EMoveToType.WAYPOINT) {
			if(hasActiveMoveOrder()) {
				if(waypoints == null) waypoints = new ArrayDeque<>();
				waypoints.add(targetPosition);
				return;
			}
			moveToType = EMoveToType.DEFAULT;
		}

		clearWaypoints();
		nextTarget = targetPosition;
		nextMoveToType = moveToType;
		goingToHealer = false;
	}

	/**
	 * @return true if this movable is currently executing (or about to execute) a move order that new waypoints should be queued behind.
	 */
	protected boolean hasActiveMoveOrder() {
		return nextTarget != null;
	}

	/**
	 * Schedules the next waypoint as new move order, if there is one.
	 *
	 * @return true if a waypoint was scheduled.
	 */
	protected boolean startNextWaypoint() {
		if(waypoints == null || waypoints.isEmpty()) return false;

		nextTarget = waypoints.poll();
		nextMoveToType = EMoveToType.DEFAULT;
		return true;
	}

	private void clearWaypoints() {
		waypoints = null;
	}

	@Override
	public void stopOrStartWorking(boolean stop) {
		if(!playerControlled) return;

		clearWaypoints();
		nextTarget = position;
		nextMoveToType = stop? EMoveToType.FORCED : EMoveToType.DEFAULT;
		goingToHealer = false;
	}

	@Override
	public void moveToFerry(IFerryMovable ferry, ShortPoint2D entrancePosition) {
		if(!playerControlled) return;

		clearWaypoints();
		ferryToEnter = ferry;
		nextTarget = entrancePosition;
		nextMoveToType = EMoveToType.FORCED;
		goingToHealer = false;
	}

	@Override
	public void heal() {
		health = getMovableType().getHealth();
	}

	@Override
	public boolean isGoingToTreatment() {
		return goingToHealer;
	}

	@Override
	public boolean needsTreatment() {
		if(health == getMovableType().getHealth()) return false;
		if(!playerControlled) return false;
		return true;
	}

	@Override
	public boolean pingWounded(IHealerMovable healer) {
		if(!needsTreatment() || isGoingToTreatment()) return false;

		clearWaypoints();
		nextTarget = healer.getHealSpot();
		nextMoveToType = EMoveToType.FORCED;
		goingToHealer = true;
		return true;
	}

	@Override
	public void defectTo(Player player) {
		Movable.createMovable(getMovableType(), player, position, grid, this);
	}

	@Override
	public ShortPoint2D getFoWPosition() {
		if(isOnFerry()) return null;

		return position;
	}

	protected void enterFerry() {
		if(ferryToEnter == null) return;

		int distanceToFerry = position.getOnGridDistTo(ferryToEnter.getPosition());
		if(distanceToFerry <= Constants.MAX_FERRY_ENTRANCE_DISTANCE) {
			if (ferryToEnter.addPassenger(this)) {
				grid.leavePosition(position, this);
				setState(EMovableState.ON_FERRY);
			}
		}
		ferryToEnter = null;
	}
}
