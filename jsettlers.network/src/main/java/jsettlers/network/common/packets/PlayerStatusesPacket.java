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
import java.util.Arrays;

import jsettlers.network.infrastructure.channel.packet.Packet;

/**
 * The network status of all players of a running match. It is regularly sent by the server.
 */
public class PlayerStatusesPacket extends Packet {
	private PlayerStatusPacket[] statuses;

	public PlayerStatusesPacket() {
	}

	public PlayerStatusesPacket(PlayerStatusPacket[] statuses) {
		this.statuses = statuses;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeInt(statuses.length);
		for (PlayerStatusPacket status : statuses) {
			status.serialize(dos);
		}
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		statuses = new PlayerStatusPacket[length];
		for (int i = 0; i < length; i++) {
			statuses[i] = new PlayerStatusPacket();
			statuses[i].deserialize(dis);
		}
	}

	public PlayerStatusPacket[] getStatuses() {
		return statuses;
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(statuses);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return Arrays.equals(statuses, ((PlayerStatusesPacket) obj).statuses);
	}

	@Override
	public String toString() {
		return "PlayerStatusesPacket " + Arrays.toString(statuses);
	}
}
