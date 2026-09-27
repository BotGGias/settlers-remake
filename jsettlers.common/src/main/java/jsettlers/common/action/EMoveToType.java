package jsettlers.common.action;

public enum EMoveToType {
	DEFAULT(true, true),
	FORCED(false, false),
	/**
	 * patrol (for soldiers; behaves like {@link #DEFAULT} in all other cases)
	 */
	PATROL(true, true),
	/**
	 * waypoint: behaves like {@link #DEFAULT}, but is appended to the movable's list of waypoints if it is already on its way somewhere.
	 * <p>
	 * Must stay after the other values, because the ordinal is serialized.
	 */
	WAYPOINT(true, true);
	
	public static EMoveToType[] VALUES = values();

	private final boolean attackOnTheWay;
	private final boolean workOnDestination;

	EMoveToType(boolean attackOnTheWay, boolean workOnDestination) {
		this.attackOnTheWay = attackOnTheWay;
		this.workOnDestination = workOnDestination;
	}

	public boolean isAttackOnTheWay() {
		return attackOnTheWay;
	}

	public boolean isWorkOnDestination() {
		return workOnDestination;
	}
}