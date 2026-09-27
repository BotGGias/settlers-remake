/*
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
 */
package jsettlers.common.player;

import java.util.Set;
import java.util.stream.Stream;

import jsettlers.common.movable.EMovableType;

/**
 * A snapshot of the settler counts of a player, grouped like in the settler statistics.
 */
public final class SettlerStatistics {

	private static final EMovableType[] SOLDIER_TYPES = {
			EMovableType.SWORDSMAN_L1, EMovableType.SWORDSMAN_L2, EMovableType.SWORDSMAN_L3,
			EMovableType.BOWMAN_L1, EMovableType.BOWMAN_L2, EMovableType.BOWMAN_L3,
			EMovableType.PIKEMAN_L1, EMovableType.PIKEMAN_L2, EMovableType.PIKEMAN_L3,
			EMovableType.MAGE
	};

	private static final EMovableType[] GENERIC_WORKER_TYPES = {
			EMovableType.PIG_FARMER, EMovableType.DOCKWORKER,
			EMovableType.FARMER, EMovableType.LUMBERJACK,
			EMovableType.SAWMILLER, EMovableType.FISHERMAN,
			EMovableType.WATERWORKER, EMovableType.BAKER,
			EMovableType.MINER, EMovableType.SLAUGHTERER,
			EMovableType.MILLER, EMovableType.SMITH,
			EMovableType.FORESTER, EMovableType.MELTER,
			EMovableType.WINEGROWER, EMovableType.CHARCOAL_BURNER,
			EMovableType.STONECUTTER, EMovableType.BREWER,
			EMovableType.RICE_FARMER, EMovableType.DISTILLER,
			EMovableType.ALCHEMIST, EMovableType.MEAD_BREWER,
			EMovableType.POWDER_MAKER
	};

	private final int beds;
	private final int soldiers;
	private final int workers;
	private final int civilians;
	private final int bearers;
	private final int diggers;
	private final int bricklayers;
	private final int swordsmen;
	private final int bowmen;
	private final int pikemen;
	private final int mages;
	private final int geologists;
	private final int thieves;
	private final int pioneers;
	private final int donkeys;

	public SettlerStatistics(ISettlerInformation settlerInformation, int beds) {
		this.beds = beds;
		soldiers = count(settlerInformation, SOLDIER_TYPES);
		workers = count(settlerInformation, GENERIC_WORKER_TYPES);
		bearers = settlerInformation.getMovableCount(EMovableType.BEARER);
		diggers = settlerInformation.getMovableCount(EMovableType.DIGGER);
		bricklayers = settlerInformation.getMovableCount(EMovableType.BRICKLAYER);
		civilians = workers + bearers + diggers + bricklayers;
		swordsmen = count(settlerInformation, EMovableType.SWORDSMEN);
		bowmen = count(settlerInformation, EMovableType.BOWMEN);
		pikemen = count(settlerInformation, EMovableType.PIKEMEN);
		mages = settlerInformation.getMovableCount(EMovableType.MAGE);
		geologists = settlerInformation.getMovableCount(EMovableType.GEOLOGIST);
		thieves = settlerInformation.getMovableCount(EMovableType.THIEF);
		pioneers = settlerInformation.getMovableCount(EMovableType.PIONEER);
		donkeys = settlerInformation.getMovableCount(EMovableType.DONKEY);
	}

	private static int count(ISettlerInformation settlerInformation, EMovableType... movableTypes) {
		return Stream.of(movableTypes).mapToInt(settlerInformation::getMovableCount).sum();
	}

	private static int count(ISettlerInformation settlerInformation, Set<EMovableType> movableTypes) {
		return movableTypes.stream().mapToInt(settlerInformation::getMovableCount).sum();
	}

	public int getBeds() {
		return beds;
	}

	/**
	 * @return All soldiers including mages.
	 */
	public int getSoldiers() {
		return soldiers;
	}

	/**
	 * @return All workers of production buildings.
	 */
	public int getWorkers() {
		return workers;
	}

	/**
	 * @return Workers, bearers, diggers and bricklayers.
	 */
	public int getCivilians() {
		return civilians;
	}

	/**
	 * @return Civilians and soldiers.
	 */
	public int getTotal() {
		return civilians + soldiers;
	}

	public int getBearers() {
		return bearers;
	}

	public int getDiggers() {
		return diggers;
	}

	public int getBricklayers() {
		return bricklayers;
	}

	public int getSwordsmen() {
		return swordsmen;
	}

	public int getBowmen() {
		return bowmen;
	}

	public int getPikemen() {
		return pikemen;
	}

	public int getMages() {
		return mages;
	}

	public int getGeologists() {
		return geologists;
	}

	public int getThieves() {
		return thieves;
	}

	public int getPioneers() {
		return pioneers;
	}

	public int getDonkeys() {
		return donkeys;
	}
}
