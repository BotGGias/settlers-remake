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
package jsettlers.network.server.lockstep;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jsettlers.network.NetworkConstants;
import jsettlers.network.server.match.lockstep.TaskCollectingListener;
import jsettlers.network.server.match.lockstep.TaskSendingTimerTask;

/**
 * The server releases locksteps only up to the slowest player of the match plus the lead: a player whose connection is
 * interrupted holds the game for everybody, a player that left does not.
 */
public class TaskSendingTimerTaskTest {
	private static final int LEAD = NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS;

	@Test
	public void testLockstepFollowsTheSlowestPlayer() {
		TaskSendingTimerTask task = new TaskSendingTimerTask(null, new TaskCollectingListener(), null);
		Object host = "host";
		Object member = "member";
		task.addPlayer(host);
		task.addPlayer(member);
		assertEquals(LEAD, task.getCurrentLockstepMax());

		// The host runs ahead, the member does not answer (connection interrupted): nothing more is released.
		task.receivedLockstepAcknowledge(host, 100);
		assertEquals(LEAD, task.getCurrentLockstepMax());

		// The member is back and catches up: the game continues for both.
		task.receivedLockstepAcknowledge(member, 60);
		assertEquals(60 + LEAD, task.getCurrentLockstepMax());
		task.receivedLockstepAcknowledge(member, 100);
		assertEquals(100 + LEAD, task.getCurrentLockstepMax());

		// An old acknowledgement does not take anything back.
		task.receivedLockstepAcknowledge(member, 50);
		assertEquals(100 + LEAD, task.getCurrentLockstepMax());

		// The member left: the host plays on alone.
		task.receivedLockstepAcknowledge(host, 150);
		assertEquals(100 + LEAD, task.getCurrentLockstepMax());
		task.removePlayer(member);
		assertEquals(150 + LEAD, task.getCurrentLockstepMax());

		// Acknowledgements of unknown senders (not in the match) do not count.
		task.receivedLockstepAcknowledge("observer", 1000);
		assertEquals(150 + LEAD, task.getCurrentLockstepMax());
	}
}
