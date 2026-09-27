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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.common.menu;

import java.util.Collections;
import java.util.List;

/**
 * The network condition of a running multiplayer game in one sentence, for a prominent notice on top of the map: the game
 * runs normally, it is interrupted (the game waits for players or the connection to the server is gone for the moment and is
 * expected to come back) or the connection is lost for good.
 */
public final class ConnectionNotice {
	public enum Type {
		/** The game runs normally (or it is no multiplayer game). */
		NONE,
		/** The game waits: connection problems or players that do not respond. It continues as soon as they are back. */
		INTERRUPTED,
		/** The connection to the game is closed; it will not come back. */
		LOST
	}

	public static final ConnectionNotice NONE = new ConnectionNotice(Type.NONE, Collections.emptyList());

	private final Type type;
	private final List<String> waitingFor;

	public ConnectionNotice(Type type, List<String> waitingFor) {
		this.type = type;
		this.waitingFor = Collections.unmodifiableList(waitingFor);
	}

	public Type getType() {
		return type;
	}

	/**
	 * @return The names of the players the game waits for (empty if unknown, e.g. the own connection is interrupted).
	 */
	public List<String> getWaitingFor() {
		return waitingFor;
	}

	@Override
	public String toString() {
		return type + (waitingFor.isEmpty() ? "" : " " + waitingFor);
	}
}
