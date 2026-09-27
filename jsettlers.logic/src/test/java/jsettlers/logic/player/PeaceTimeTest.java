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
package jsettlers.logic.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.logic.constants.MatchConstants;
import jsettlers.logic.map.grid.movable.MovableGrid;
import jsettlers.network.synchronic.timer.NetworkTimer;

public class PeaceTimeTest {
	private static final int MINUTE = 60 * 1000;

	private NetworkTimer clock;
	private Player player;
	private Player enemy;

	@Before
	public void setUp() {
		clock = new NetworkTimer(true);
		MatchConstants.init(clock, 0L);
		player = new Player((byte) 0, new Team((byte) 0), (byte) 2, EPlayerType.HUMAN, ECivilisation.ROMAN);
		enemy = new Player((byte) 1, new Team((byte) 1), (byte) 2, EPlayerType.AI_HARD, ECivilisation.EGYPTIAN);
	}

	@After
	public void tearDown() {
		MatchConstants.clearState();
	}

	@Test
	public void testNoPeaceTimeByDefault() {
		assertFalse(player.isInPeaceTime());
		assertEquals(0, player.getPeaceTimeRemainingMs());
		assertFalse(MovableGrid.isPeaceTimeBetween(player, enemy));
	}

	@Test
	public void testPeaceTimeEnds() {
		player.setPeaceTimeEnd(10 * MINUTE);
		enemy.setPeaceTimeEnd(10 * MINUTE);

		clock.setTime(4 * MINUTE);
		assertTrue(player.isInPeaceTime());
		assertEquals(6 * MINUTE, player.getPeaceTimeRemainingMs());
		assertTrue(MovableGrid.isPeaceTimeBetween(player, enemy));

		clock.setTime(10 * MINUTE);
		assertFalse(player.isInPeaceTime());
		assertEquals(0, player.getPeaceTimeRemainingMs());
		assertFalse(MovableGrid.isPeaceTimeBetween(player, enemy));
	}

	@Test
	public void testPeaceTimeOfOnePlayerProtectsBoth() {
		enemy.setPeaceTimeEnd(10 * MINUTE);
		assertTrue(MovableGrid.isPeaceTimeBetween(player, enemy));
		assertTrue(MovableGrid.isPeaceTimeBetween(enemy, player));
	}

	@Test
	public void testPeaceTimeIsSerialized() throws IOException, ClassNotFoundException {
		player.setPeaceTimeEnd(20 * MINUTE);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(bytes)) {
			oos.writeObject(player);
		}
		Player read;
		try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			read = (Player) ois.readObject();
		}

		assertEquals(20 * MINUTE, read.getPeaceTimeRemainingMs());
	}

	@Test
	public void testNotifierMessages() {
		List<String> messages = new ArrayList<>();
		player.setMessenger(message -> messages.add(message.getMessageLabel()));
		player.setPeaceTimeEnd(20 * MINUTE);

		PeaceTimeNotifier notifier = new PeaceTimeNotifier(player);
		for (int time = 0; time <= 21 * MINUTE; time += 1000) {
			clock.setTime(time);
			notifier.timerEvent();
		}

		assertEquals(List.of("peace_time_active", "peace_time_ends_10min", "peace_time_ends_5min", "peace_time_ends_1min", "peace_time_over"), messages);
	}

	@Test
	public void testNotifierSkipsWarningsThatAreAlreadyDue() {
		List<String> messages = new ArrayList<>();
		player.setMessenger(message -> messages.add(message.getMessageLabel()));
		player.setPeaceTimeEnd(10 * MINUTE);

		PeaceTimeNotifier notifier = new PeaceTimeNotifier(player);
		for (int time = 0; time <= 11 * MINUTE; time += 1000) {
			clock.setTime(time);
			notifier.timerEvent();
		}

		assertEquals(List.of("peace_time_active", "peace_time_ends_5min", "peace_time_ends_1min", "peace_time_over"), messages);
	}
}
