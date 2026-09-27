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
package jsettlers.logic.movable.interfaces;

import jsettlers.common.player.IPlayer;
import jsettlers.common.position.ShortPoint2D;

/**
 * An {@link IAttackable} that can be damaged by siege weapons in a different way than by soldiers. Siege weapons can weaken a military
 * building but never conquer it.
 */
public interface ISiegeAttackable extends IAttackable {

	/**
	 * @return true if a siege hit still has an effect.
	 */
	boolean canReceiveSiegeDamage();

	/**
	 * Hits this attackable with a siege weapon.
	 *
	 * @param strength
	 *            The strength of the hit.
	 * @param attackerPos
	 *            The position of the siege weapon.
	 * @param attackingPlayer
	 *            The player of the siege weapon.
	 */
	void receiveSiegeHit(float strength, ShortPoint2D attackerPos, IPlayer attackingPlayer);
}
