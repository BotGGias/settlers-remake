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
package jsettlers.main.su;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;

/**
 * Slot setup the Settlers United launcher decided in its lobby (contract v2, extra {@code su.slots}). Text form, one entry per
 * slot, separated by {@code ;}; fields {@code key=value} separated by {@code ,}:
 *
 * <pre>
 * player=&lt;playerId&gt;,civ=0,team=0,pos=0;ai=AI_EASY,civ=3,team=1,pos=2
 * </pre>
 *
 * {@code player} or {@code ai} (one of the AI {@link EPlayerType}s), {@code civ} = {@link ECivilisation} ordinal, {@code team} 0..n-1,
 * {@code pos} = start position of the map (distinct). Player ids must not contain {@code , ; =}.
 */
public final class SuSlotPlan {
	public static final int MAX_SLOTS = 32;

	public static final class Slot {
		/** Human player (launcher player id) or null for an AI slot. */
		public final String playerId;
		public final EPlayerType type;
		public final ECivilisation civilisation;
		public final byte team;
		public final byte position;

		Slot(String playerId, EPlayerType type, ECivilisation civilisation, byte team, byte position) {
			this.playerId = playerId;
			this.type = type;
			this.civilisation = civilisation;
			this.team = team;
			this.position = position;
		}

		public boolean isHuman() {
			return playerId != null;
		}

		@Override
		public String toString() {
			return (isHuman() ? "player=" + playerId : "ai=" + type) + ",civ=" + civilisation.ordinal + ",team=" + team + ",pos=" + position;
		}
	}

	private final List<Slot> slots;

	private SuSlotPlan(List<Slot> slots) {
		this.slots = Collections.unmodifiableList(slots);
	}

	public List<Slot> getSlots() {
		return slots;
	}

	public int size() {
		return slots.size();
	}

	public int getHumanCount() {
		return getHumanIds().size();
	}

	public List<String> getHumanIds() {
		List<String> ids = new ArrayList<>();
		for (Slot slot : slots) {
			if (slot.isHuman()) {
				ids.add(slot.playerId);
			}
		}
		return ids;
	}

	public Slot findHuman(String playerId) {
		for (Slot slot : slots) {
			if (slot.isHuman() && slot.playerId.equals(playerId)) {
				return slot;
			}
		}
		return null;
	}

	/** Does the plan fit a map with this many start positions? Returns the reason if not, else null. */
	public String checkMap(int mapMaxPlayers) {
		if (slots.size() > mapMaxPlayers) {
			return slots.size() + " slots, map has " + mapMaxPlayers;
		}
		for (Slot slot : slots) {
			if (slot.position >= mapMaxPlayers) {
				return "position " + slot.position + ", map has " + mapMaxPlayers;
			}
		}
		return null;
	}

	/**
	 * JSettlers gives slot i to the i-th joined player; the remaining slots are AI slots. Returns the plan entry per slot index, or
	 * null while not all planned players have joined.
	 *
	 * @throws IllegalStateException
	 *             if a player joined that is not part of the plan
	 */
	public Slot[] assign(List<String> joinedPlayerIds) {
		for (String id : joinedPlayerIds) {
			if (findHuman(id) == null) {
				throw new IllegalStateException("unexpected player " + id);
			}
		}
		if (joinedPlayerIds.size() != getHumanCount()) {
			return null;
		}
		Slot[] result = new Slot[slots.size()];
		int i = 0;
		for (String id : joinedPlayerIds) {
			result[i++] = findHuman(id);
		}
		for (Slot slot : slots) {
			if (!slot.isHuman()) {
				result[i++] = slot;
			}
		}
		return result;
	}

	/** @throws IllegalArgumentException with a short technical reason */
	public static SuSlotPlan parse(String spec) {
		if (spec == null || spec.trim().isEmpty()) {
			throw new IllegalArgumentException("no slots");
		}
		List<Slot> slots = new ArrayList<>();
		for (String entry : spec.split(";")) {
			if (!entry.trim().isEmpty()) {
				slots.add(parseSlot(entry.trim()));
			}
		}
		if (slots.isEmpty() || slots.size() > MAX_SLOTS) {
			throw new IllegalArgumentException("invalid slot count " + slots.size());
		}
		Set<String> ids = new HashSet<>();
		Set<Byte> positions = new HashSet<>();
		for (Slot slot : slots) {
			if (slot.isHuman() && !ids.add(slot.playerId)) {
				throw new IllegalArgumentException("duplicate player " + slot.playerId);
			}
			if (!positions.add(slot.position)) {
				throw new IllegalArgumentException("duplicate position " + slot.position);
			}
			if (slot.team >= slots.size()) {
				throw new IllegalArgumentException("team " + slot.team + " >= slot count");
			}
		}
		if (ids.isEmpty()) {
			throw new IllegalArgumentException("no human player");
		}
		return new SuSlotPlan(slots);
	}

	private static Slot parseSlot(String entry) {
		String playerId = null;
		EPlayerType type = null;
		Integer civ = null, team = null, pos = null;
		for (String field : entry.split(",")) {
			int eq = field.indexOf('=');
			if (eq <= 0) {
				throw new IllegalArgumentException("invalid slot field '" + field + "'");
			}
			String key = field.substring(0, eq).trim();
			String value = field.substring(eq + 1).trim();
			switch (key) {
			case "player":
				if (value.isEmpty()) {
					throw new IllegalArgumentException("empty player id");
				}
				playerId = value;
				type = EPlayerType.HUMAN;
				break;
			case "ai":
				type = aiType(value);
				break;
			case "civ":
				civ = number(key, value, ECivilisation.VALUES.length - 1);
				break;
			case "team":
				team = number(key, value, MAX_SLOTS - 1);
				break;
			case "pos":
				pos = number(key, value, MAX_SLOTS - 1);
				break;
			default:
				throw new IllegalArgumentException("unknown slot field '" + key + "'");
			}
		}
		if (type == null || civ == null || team == null || pos == null) {
			throw new IllegalArgumentException("incomplete slot '" + entry + "'");
		}
		if (playerId != null && type != EPlayerType.HUMAN) {
			throw new IllegalArgumentException("slot is both player and ai '" + entry + "'");
		}
		return new Slot(playerId, type, ECivilisation.VALUES[civ], (byte) (int) team, (byte) (int) pos);
	}

	private static EPlayerType aiType(String value) {
		try {
			EPlayerType type = EPlayerType.valueOf(value);
			if (type.isAi()) {
				return type;
			}
		} catch (IllegalArgumentException e) {
			// below
		}
		throw new IllegalArgumentException("invalid ai type '" + value + "'");
	}

	private static int number(String key, String value, int max) {
		try {
			int n = Integer.parseInt(value);
			if (n >= 0 && n <= max) {
				return n;
			}
		} catch (NumberFormatException e) {
			// below
		}
		throw new IllegalArgumentException("invalid " + key + " '" + value + "'");
	}
}
