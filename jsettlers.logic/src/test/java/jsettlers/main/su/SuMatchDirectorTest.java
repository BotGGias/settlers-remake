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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.EPeaceTime;
import jsettlers.common.menu.IChatMessageListener;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IMultiplayerListener;
import jsettlers.common.menu.IMultiplayerPlayer;
import jsettlers.common.menu.IMultiplayerSlot;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.player.ECivilisation;
import jsettlers.common.utils.collections.ChangingList;

public class SuMatchDirectorTest {

	@Test
	public void parsesPlan() {
		SuSlotPlan plan = SuSlotPlan.parse(" player=A,civ=0,team=0,pos=2; ai=AI_EASY,civ=3,team=1,pos=0 ;player=B,civ=1,team=1,pos=1;");
		assertEquals(3, plan.size());
		assertEquals(Arrays.asList("A", "B"), plan.getHumanIds());
		SuSlotPlan.Slot ai = plan.getSlots().get(1);
		assertEquals(EPlayerType.AI_EASY, ai.type);
		assertEquals(ECivilisation.AMAZON, ai.civilisation);
		assertEquals(0, ai.position);
		assertEquals(EPlayerType.HUMAN, plan.getSlots().get(0).type);
		assertNull(plan.checkMap(3));
		assertTrue(plan.checkMap(2) != null);
	}

	@Test
	public void rejectsInvalidPlans() {
		String[] invalid = {
				"",
				"ai=AI_EASY,civ=0,team=0,pos=0", // no human
				"player=A,civ=0,team=0", // incomplete
				"player=A,civ=4,team=0,pos=0", // civ
				"player=A,civ=0,team=0,pos=0;player=A,civ=0,team=0,pos=1", // duplicate id
				"player=A,civ=0,team=0,pos=0;player=B,civ=0,team=0,pos=0", // duplicate position
				"player=A,civ=0,team=2,pos=0;player=B,civ=0,team=0,pos=1", // team >= count
				"player=A,civ=0,team=0,pos=0;ai=HUMAN,civ=0,team=0,pos=1", // ai type
				"player=A,civ=0,team=0,pos=0,peace=1", // unknown field
				"player=A,ai=AI_EASY,civ=0,team=0,pos=0", // both
		};
		for (String spec : invalid) {
			try {
				SuSlotPlan.parse(spec);
				fail("accepted " + spec);
			} catch (IllegalArgumentException expected) {
				// ok
			}
		}
	}

	@Test
	public void assignsHumansInJoinOrderAndAiAfter() {
		SuSlotPlan plan = SuSlotPlan.parse("player=A,civ=0,team=0,pos=0;ai=AI_HARD,civ=1,team=1,pos=1;player=B,civ=2,team=0,pos=2");
		assertNull(plan.assign(Arrays.asList("B")));
		SuSlotPlan.Slot[] slots = plan.assign(Arrays.asList("B", "A"));
		assertEquals("B", slots[0].playerId);
		assertEquals("A", slots[1].playerId);
		assertEquals(EPlayerType.AI_HARD, slots[2].type);
		try {
			plan.assign(Arrays.asList("A", "X"));
			fail("unexpected player accepted");
		} catch (IllegalStateException expected) {
			// ok
		}
	}

	@Test
	public void hostConfiguresSlotsAndStartsWhenAllReady() {
		FakeMatch match = new FakeMatch(4);
		match.join("A");
		SuSlotPlan plan = SuSlotPlan.parse("player=A,civ=1,team=0,pos=3;ai=AI_VERY_EASY,civ=3,team=1,pos=0;player=B,civ=2,team=1,pos=1");
		Recorder rec = new Recorder();
		SuMatchDirector director = new SuMatchDirector(match, plan, true, rec);
		director.start();

		assertEquals(3, match.slotCount);
		assertEquals(0, match.startRequests);
		assertEquals("1/1/2", rec.last);

		match.join("B");
		assertEquals("2/1/2", rec.last); // B joined, not ready yet
		assertEquals(0, match.startRequests);

		match.setReady("B");
		assertEquals("2/2/2", rec.last);
		assertEquals(1, match.startRequests);

		List<IMultiplayerSlot> slots = match.getSlots().getItems();
		check(slots.get(0), "A", EPlayerType.HUMAN, ECivilisation.EGYPTIAN, 0, 3);
		check(slots.get(1), "B", EPlayerType.HUMAN, ECivilisation.ASIAN, 1, 1);
		check(slots.get(2), null, EPlayerType.AI_VERY_EASY, ECivilisation.AMAZON, 1, 0);

		// MATCH_STARTED updates the lists once more: no second start.
		match.publish();
		assertEquals(1, match.startRequests);

		director.gameIsStarting(null);
		assertTrue(rec.started);
		assertNull(rec.failure);
	}

	@Test
	public void hostFailsOnUnexpectedPlayer() {
		FakeMatch match = new FakeMatch(4);
		match.join("A");
		Recorder rec = new Recorder();
		new SuMatchDirector(match, SuSlotPlan.parse("player=A,civ=0,team=0,pos=0;player=B,civ=0,team=1,pos=1"), true, rec).start();
		match.join("X");
		assertEquals("unexpected player X", rec.failure);
	}

	@Test
	public void memberOnlySetsReady() {
		FakeMatch match = new FakeMatch(4);
		match.join("A");
		match.join("B");
		match.me = "B";
		Recorder rec = new Recorder();
		new SuMatchDirector(match, SuSlotPlan.parse("player=A,civ=0,team=0,pos=0;player=B,civ=0,team=1,pos=1"), false, rec).start();
		assertEquals("2/1/2", rec.last);
		assertEquals(4, match.slotCount);
		assertEquals(0, match.commands);
	}

	private static void check(IMultiplayerSlot slot, String player, EPlayerType type, ECivilisation civ, int team, int position) {
		assertEquals(player, slot.getPlayer() == null ? null : slot.getPlayer().getId());
		assertEquals(type, slot.getType());
		assertEquals(civ, slot.getCivilisation());
		assertEquals(team, slot.getTeam());
		assertEquals(position, slot.getPosition());
	}

	private static class Recorder implements SuMatchDirector.Listener {
		String last;
		boolean started;
		String failure;

		@Override
		public void progress(int joined, int ready, int total) {
			last = joined + "/" + ready + "/" + total;
		}

		@Override
		public void starting(IStartingGame game) {
			started = true;
		}

		@Override
		public void failed(String reason) {
			failure = reason;
		}
	}

	/** Mimics the server match (slot defaults, position swap) and the client's slot view (slot i = i-th player). */
	private static class FakeMatch implements IJoinPhaseMultiplayerGameConnector {
		final int maxPlayers;
		final List<String> players = new ArrayList<>();
		final List<String> ready = new ArrayList<>();
		final int[][] slots; // type, civ, team, position; -1 = default
		final ChangingList<IMultiplayerPlayer> playerList = new ChangingList<>();
		final ChangingList<IMultiplayerSlot> slotList = new ChangingList<>();
		int slotCount;
		int startRequests;
		int commands;
		String me = "A";

		FakeMatch(int maxPlayers) {
			this.maxPlayers = maxPlayers;
			this.slotCount = maxPlayers;
			slots = new int[maxPlayers][];
			for (int i = 0; i < maxPlayers; i++) {
				slots[i] = new int[] { -1, -1, i, i };
			}
		}

		void join(String id) {
			slots[players.size()][0] = -1;
			players.add(id);
			publish();
		}

		void setReady(String id) {
			ready.add(id);
			publish();
		}

		void publish() {
			List<IMultiplayerPlayer> ps = new ArrayList<>();
			for (String id : players) {
				ps.add(player(id, ready.contains(id)));
			}
			playerList.setList(ps);
			List<IMultiplayerSlot> ss = new ArrayList<>();
			for (int i = 0; i < slotCount; i++) {
				ss.add(slot(i < ps.size() ? ps.get(i) : null, slots[i]));
			}
			slotList.setList(ss);
		}

		@Override
		public void setReady(boolean r) {
			if (r && !ready.contains(me)) {
				setReady(me);
			}
		}

		@Override
		public void setType(byte slot, EPlayerType playerType) {
			commands++;
			slots[slot][0] = playerType.ordinal();
			publish();
		}

		@Override
		public void setCivilisation(byte slot, ECivilisation civilisation) {
			commands++;
			slots[slot][1] = civilisation.ordinal;
			publish();
		}

		@Override
		public void setTeam(byte slot, byte team) {
			commands++;
			slots[slot][2] = team % slotCount;
			publish();
		}

		@Override
		public void setPosition(byte slot, byte position) {
			commands++;
			int old = slots[slot][3];
			int now = position % maxPlayers;
			slots[slot][3] = now;
			for (int i = 0; i < maxPlayers; i++) {
				if (i != slot && slots[i][3] == now) {
					slots[i][3] = old;
				}
			}
			publish();
		}

		@Override
		public void setPlayerCount(int playerCount) {
			slotCount = playerCount;
			publish();
		}

		@Override
		public boolean startGame() {
			startRequests++;
			return true;
		}

		@Override
		public ChangingList<IMultiplayerPlayer> getPlayers() {
			return playerList;
		}

		@Override
		public ChangingList<IMultiplayerSlot> getSlots() {
			return slotList;
		}

		@Override
		public void setMultiplayerListener(IMultiplayerListener listener) {
		}

		@Override
		public void setChatListener(IChatMessageListener chatMessageListener) {
		}

		@Override
		public void sendChatMessage(String chatMessage) {
		}

		@Override
		public void setStartResources(int startResourcesValue) {
		}

		@Override
		public int getStartResourcesValue() {
			return 0;
		}

		@Override
		public void setPeaceTime(EPeaceTime peaceTime) {
		}

		@Override
		public EPeaceTime getPeaceTime() {
			return EPeaceTime.WITHOUT;
		}

		@Override
		public void abort() {
		}

		static IMultiplayerPlayer player(String id, boolean ready) {
			return new IMultiplayerPlayer() {
				@Override
				public String getId() {
					return id;
				}

				@Override
				public String getName() {
					return id;
				}

				@Override
				public boolean isReady() {
					return ready;
				}
			};
		}

		static IMultiplayerSlot slot(IMultiplayerPlayer player, int[] s) {
			return new IMultiplayerSlot() {
				@Override
				public IMultiplayerPlayer getPlayer() {
					return player;
				}

				@Override
				public EPlayerType getType() {
					return s[0] < 0 ? (player != null ? EPlayerType.HUMAN : EPlayerType.AI_VERY_HARD) : EPlayerType.VALUES[s[0]];
				}

				@Override
				public ECivilisation getCivilisation() {
					return s[1] < 0 ? ECivilisation.ROMAN : ECivilisation.VALUES[s[1]];
				}

				@Override
				public byte getTeam() {
					return (byte) s[2];
				}

				@Override
				public byte getPosition() {
					return (byte) s[3];
				}
			};
		}
	}
}
