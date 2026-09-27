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
package jsettlers.network.common.packets;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;

/**
 * The network status of one player of a running match, as seen by the server.
 */
public class PlayerStatusPacket extends Packet {
	private byte inGamePlayerId;
	private String name;
	private boolean connected;
	private int pingMs;
	private int gameTime;
	private int millisSinceLastSync;

	public PlayerStatusPacket() {
	}

	public PlayerStatusPacket(byte inGamePlayerId, String name, boolean connected, int pingMs, int gameTime, int millisSinceLastSync) {
		this.inGamePlayerId = inGamePlayerId;
		this.name = name;
		this.connected = connected;
		this.pingMs = pingMs;
		this.gameTime = gameTime;
		this.millisSinceLastSync = millisSinceLastSync;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeByte(inGamePlayerId);
		dos.writeUTF(name);
		dos.writeBoolean(connected);
		dos.writeInt(pingMs);
		dos.writeInt(gameTime);
		dos.writeInt(millisSinceLastSync);
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		inGamePlayerId = dis.readByte();
		name = dis.readUTF();
		connected = dis.readBoolean();
		pingMs = dis.readInt();
		gameTime = dis.readInt();
		millisSinceLastSync = dis.readInt();
	}

	/**
	 * @return The id of the player in the game (the position of his slot).
	 */
	public byte getInGamePlayerId() {
		return inGamePlayerId;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return false if the player has left the match.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @return The round trip time between the server and this player.
	 */
	public int getPingMs() {
		return pingMs;
	}

	/**
	 * @return The game time this player reported last.
	 */
	public int getGameTime() {
		return gameTime;
	}

	/**
	 * @return The milliseconds since the server received the last time synchronization of this player.
	 */
	public int getMillisSinceLastSync() {
		return millisSinceLastSync;
	}

	@Override
	public int hashCode() {
		return Objects.hash(inGamePlayerId, name, connected, pingMs, gameTime, millisSinceLastSync);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		PlayerStatusPacket other = (PlayerStatusPacket) obj;
		return inGamePlayerId == other.inGamePlayerId && Objects.equals(name, other.name) && connected == other.connected && pingMs == other.pingMs
				&& gameTime == other.gameTime && millisSinceLastSync == other.millisSinceLastSync;
	}

	@Override
	public String toString() {
		return "PlayerStatusPacket [inGamePlayerId=" + inGamePlayerId + ", name=" + name + ", connected=" + connected + ", pingMs=" + pingMs
				+ ", gameTime=" + gameTime + ", millisSinceLastSync=" + millisSinceLastSync + "]";
	}
}
