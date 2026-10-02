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

package jsettlers.main.android.su;

/**
 * State of one launcher start: where to connect, who we are and which match to open or join. Lives in the application until
 * the player returns to the main menu root or the game ends; nothing of it is persisted.
 */
public class SuSession {
	public final SuLaunch launch;
	/** Join: match name to join automatically once it shows up; null once joined or given up. */
	private String pendingJoin;
	/** Join: give up waiting at this uptime (ms). */
	private final long joinDeadline;

	public SuSession(SuLaunch launch, long nowMs) {
		this.launch = launch;
		this.pendingJoin = launch.role == SuLaunch.Role.JOIN ? launch.matchName : null;
		this.joinDeadline = nowMs + launch.waitSecs * 1000L;
	}

	public synchronized String getPendingJoin() {
		return pendingJoin;
	}

	/** Takes the pending join (only once). */
	public synchronized boolean takePendingJoin(String matchName) {
		if (pendingJoin != null && pendingJoin.equals(matchName)) {
			pendingJoin = null;
			return true;
		}
		return false;
	}

	/** Stops waiting (timeout, missing map); returns whether it was still waiting. */
	public synchronized boolean cancelPendingJoin() {
		boolean was = pendingJoin != null;
		pendingJoin = null;
		return was;
	}

	public long getJoinDeadline() {
		return joinDeadline;
	}
}
