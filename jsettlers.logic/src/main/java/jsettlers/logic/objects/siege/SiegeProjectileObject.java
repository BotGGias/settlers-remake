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
package jsettlers.logic.objects.siege;

import jsettlers.common.mapobject.EMapObjectType;
import jsettlers.common.mapobject.ISiegeProjectileMapObject;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.ESiegeWeaponType;
import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.utils.MathUtils;
import jsettlers.logic.objects.ProgressingSoundableObject;

/**
 * A projectile of a siege weapon (catapult boulder, ballista bolt or cannon ball). It lies on its target position and damages the area around
 * it when it arrives.
 */
public final class SiegeProjectileObject extends ProgressingSoundableObject implements ISiegeProjectileMapObject {
	private static final long serialVersionUID = -5063934183421735630L;

	private static final float MIN_FLIGHT_DURATION = 0.1f;

	private final ISiegeProjectileGrid grid;
	private final short sourceX;
	private final short sourceY;
	private final IPlayer shooterPlayer;
	private final ESiegeWeaponType weaponType;
	private final float hitStrength;

	public SiegeProjectileObject(ISiegeProjectileGrid grid, ShortPoint2D targetPos, ShortPoint2D shooterPos, IPlayer shooterPlayer,
			ESiegeWeaponType weaponType, float hitStrength) {
		super(targetPos);

		this.grid = grid;
		this.sourceX = shooterPos.x;
		this.sourceY = shooterPos.y;
		this.shooterPlayer = shooterPlayer;
		this.weaponType = weaponType;
		this.hitStrength = hitStrength;

		float flightDuration = (float) (weaponType.projectileSecondsPerTile * MathUtils.hypot(shooterPos.x - targetPos.x, shooterPos.y - targetPos.y));
		super.setDuration(Math.max(MIN_FLIGHT_DURATION, flightDuration));
	}

	@Override
	public EMapObjectType getObjectType() {
		return EMapObjectType.SIEGE_PROJECTILE;
	}

	@Override
	public boolean cutOff() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean canBeCut() {
		return false;
	}

	@Override
	public boolean isBlocking() {
		return false;
	}

	@Override
	protected void changeState() {
		grid.hitWithSiegeProjectile(this);
	}

	@Override
	public EDirection getDirection() {
		return EDirection.getApproxDirection(getSourceX(), getSourceY(), getTargetX(), getTargetY());
	}

	@Override
	public short getSourceX() {
		return sourceX;
	}

	@Override
	public short getSourceY() {
		return sourceY;
	}

	@Override
	public short getTargetX() {
		return super.getX();
	}

	@Override
	public short getTargetY() {
		return super.getY();
	}

	@Override
	public ESiegeWeaponType getWeaponType() {
		return weaponType;
	}

	public ShortPoint2D getSourcePos() {
		return new ShortPoint2D(sourceX, sourceY);
	}

	public ShortPoint2D getTargetPos() {
		return new ShortPoint2D(getTargetX(), getTargetY());
	}

	public IPlayer getShooterPlayer() {
		return shooterPlayer;
	}

	public float getHitStrength() {
		return hitStrength;
	}
}
