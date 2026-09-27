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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.Test;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.EPeaceTime;
import jsettlers.common.player.ECivilisation;
import jsettlers.logic.map.loading.EMapStartResources;

public class InitialGameStateTest {

	private static PlayerSetting[] createPlayerSettings() {
		return new PlayerSetting[] {
				new PlayerSetting(EPlayerType.HUMAN, ECivilisation.ROMAN, (byte) 0),
				new PlayerSetting(EPlayerType.AI_HARD, ECivilisation.AMAZON, (byte) 1),
				new PlayerSetting()
		};
	}

	@Test
	public void testSerializationRoundTrip() throws IOException {
		InitialGameState state = new InitialGameState((byte) 1, createPlayerSettings(), 4711L, EMapStartResources.LOW_GOODS, EPeaceTime.MIN_30);

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		state.serialize(new DataOutputStream(bytes));
		InitialGameState read = new InitialGameState(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

		assertEquals(1, read.getPlayerId());
		assertEquals(4711L, read.getRandomSeed());
		assertEquals(EMapStartResources.LOW_GOODS, read.getStartResources());
		assertEquals(EPeaceTime.MIN_30, read.getPeaceTime());
		assertEquals(3, read.getPlayerSettings().length);
		assertEquals(ECivilisation.AMAZON, read.getPlayerSettings()[1].getCivilisation());
		assertEquals(EPlayerType.AI_HARD, read.getPlayerSettings()[1].getPlayerType());
	}

	@Test
	public void testReadVersion1WithoutPeaceTime() throws IOException {
		PlayerSetting[] playerSettings = createPlayerSettings();

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bytes);
		dos.writeByte(1);
		dos.writeLong(123L);
		dos.writeByte(0);
		dos.writeByte(EMapStartResources.MEDIUM_GOODS.ordinal());
		dos.writeInt(playerSettings.length);
		for (PlayerSetting playerSetting : playerSettings) {
			playerSetting.writeTo(dos);
		}

		InitialGameState read = new InitialGameState(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray())));

		assertEquals(123L, read.getRandomSeed());
		assertEquals(EMapStartResources.MEDIUM_GOODS, read.getStartResources());
		assertEquals(EPeaceTime.WITHOUT, read.getPeaceTime());
		assertEquals(3, read.getPlayerSettings().length);
	}

	@Test
	public void testCloneKeepsSettings() {
		InitialGameState state = new InitialGameState((byte) 0, createPlayerSettings(), 42L, EMapStartResources.LOW_GOODS, EPeaceTime.MIN_60);
		InitialGameState clone = state.clone();

		assertEquals(EMapStartResources.LOW_GOODS, clone.getStartResources());
		assertEquals(EPeaceTime.MIN_60, clone.getPeaceTime());
		assertEquals(42L, clone.getRandomSeed());
		assertEquals(EPlayerType.HUMAN, clone.getPlayerSettings()[1].getPlayerType());
	}

	@Test
	public void testDefaultConstructorHasNoPeaceTime() {
		InitialGameState state = new InitialGameState((byte) 0, createPlayerSettings(), 42L);

		assertEquals(EMapStartResources.HIGH_GOODS, state.getStartResources());
		assertEquals(EPeaceTime.WITHOUT, state.getPeaceTime());
	}
}
