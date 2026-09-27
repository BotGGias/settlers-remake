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
import java.util.List;

import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IMultiplayerListener;
import jsettlers.common.menu.IMultiplayerPlayer;
import jsettlers.common.menu.IMultiplayerSlot;
import jsettlers.common.menu.IStartingGame;
import jsettlers.common.utils.collections.ChangingList;
import jsettlers.common.utils.collections.IChangingListListener;

/**
 * Drives the join phase of a match without the setup screen, following the launcher's {@link SuSlotPlan} (contract v2). Everyone
 * sets itself ready. The host limits the slot count, waits until all planned players have joined, sets type, civilisation, team and
 * position of every slot and starts the match once the server reports exactly that setup and all players are ready.
 * <p>
 * Callbacks arrive on the network thread.
 */
public class SuMatchDirector implements IMultiplayerListener, IChangingListListener<IMultiplayerSlot> {

	public interface Listener {
		/** Planned players connected and ready so far. */
		void progress(int joined, int ready, int total);

		void starting(IStartingGame game);

		/** Technical reason (English); the match cannot start as planned. */
		void failed(String reason);
	}

	private final IJoinPhaseMultiplayerGameConnector connector;
	private final SuSlotPlan plan;
	private final boolean host;
	private final Listener listener;

	/** Host: join order the slots were configured for; null = not configured yet. */
	private List<String> configuredOrder;
	private boolean finished;

	public SuMatchDirector(IJoinPhaseMultiplayerGameConnector connector, SuSlotPlan plan, boolean host, Listener listener) {
		this.connector = connector;
		this.plan = plan;
		this.host = host;
		this.listener = listener;
	}

	public synchronized void start() {
		connector.setMultiplayerListener(this);
		connector.getSlots().setListener(this);
		if (host) {
			connector.setPlayerCount(plan.size());
		}
		connector.setReady(true);
		update();
	}

	/** Stops reacting (the caller aborts the match if needed). */
	public synchronized void stop() {
		finished = true;
		connector.getSlots().removeListener(this);
	}

	@Override
	public void listChanged(ChangingList<? extends IMultiplayerSlot> list) {
		update();
	}

	@Override
	public synchronized void gameIsStarting(IStartingGame game) {
		if (finished) {
			return;
		}
		stop();
		listener.starting(game);
	}

	@Override
	public synchronized void gameAborted() {
		fail("match aborted");
	}

	private synchronized void update() {
		if (finished) {
			return;
		}
		List<IMultiplayerPlayer> players = new ArrayList<>(connector.getPlayers().getItems());
		List<String> ids = new ArrayList<>();
		int joined = 0;
		int ready = 0;
		for (IMultiplayerPlayer player : players) {
			ids.add(player.getId());
			if (plan.findHuman(player.getId()) != null) {
				joined++;
				if (player.isReady()) {
					ready++;
				}
			}
		}
		listener.progress(joined, ready, plan.getHumanCount());
		if (!host) {
			return;
		}

		SuSlotPlan.Slot[] assigned;
		try {
			assigned = plan.assign(ids);
		} catch (IllegalStateException e) {
			fail(e.getMessage());
			return;
		}
		if (assigned == null) {
			configuredOrder = null; // someone left: configure again once all are back
			return;
		}
		if (!ids.equals(configuredOrder)) {
			configuredOrder = ids; // before sending: updates may arrive while configuring
			configure(assigned);
			return;
		}
		if (ready == assigned.length - countAi(assigned) && matches(assigned, connector.getSlots().getItems())) {
			connector.startGame();
		}
	}

	private void configure(SuSlotPlan.Slot[] assigned) {
		// positions are swapped by the server; the targets are distinct, so every slot keeps the one set for it
		for (int i = 0; i < assigned.length; i++) {
			SuSlotPlan.Slot slot = assigned[i];
			byte index = (byte) i;
			connector.setType(index, slot.type);
			connector.setCivilisation(index, slot.civilisation);
			connector.setTeam(index, slot.team);
			connector.setPosition(index, slot.position);
		}
	}

	static boolean matches(SuSlotPlan.Slot[] assigned, List<? extends IMultiplayerSlot> slots) {
		if (slots.size() != assigned.length) {
			return false;
		}
		for (int i = 0; i < assigned.length; i++) {
			SuSlotPlan.Slot want = assigned[i];
			IMultiplayerSlot have = slots.get(i);
			if (have.getType() != want.type || have.getCivilisation() != want.civilisation || have.getTeam() != want.team
					|| have.getPosition() != want.position) {
				return false;
			}
			if (want.isHuman() && (have.getPlayer() == null || !want.playerId.equals(have.getPlayer().getId()))) {
				return false;
			}
		}
		return true;
	}

	private static int countAi(SuSlotPlan.Slot[] assigned) {
		int n = 0;
		for (SuSlotPlan.Slot slot : assigned) {
			if (!slot.isHuman()) {
				n++;
			}
		}
		return n;
	}

	private void fail(String reason) {
		if (finished) {
			return;
		}
		stop();
		listener.failed(reason);
	}
}
