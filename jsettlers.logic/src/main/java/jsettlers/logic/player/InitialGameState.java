package jsettlers.logic.player;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.EPeaceTime;
import jsettlers.logic.map.loading.EMapStartResources;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

public class InitialGameState implements Cloneable, Serializable {

	private final byte playerId;
	private final PlayerSetting[] playerSettings;
	private final long randomSeed;
	private final EMapStartResources startResources;
	private final EPeaceTime peaceTime;

	/**
	 * Version 2 added the peace time.
	 */
	private static final byte VERSION = 2;

	public InitialGameState(byte playerId, PlayerSetting[] playerSettings, long randomSeed, EMapStartResources startResources, EPeaceTime peaceTime) {
		this.playerId = playerId;
		this.playerSettings = playerSettings;
		this.randomSeed = randomSeed;
		this.startResources = startResources;
		this.peaceTime = peaceTime;
	}

	public InitialGameState(byte playerId, PlayerSetting[] playerSettings, long randomSeed, EMapStartResources startResources) {
		this(playerId, playerSettings, randomSeed, startResources, EPeaceTime.WITHOUT);
	}

	public InitialGameState(byte playerId, PlayerSetting[] playerSettings, long randomSeed) {
		this(playerId, playerSettings, randomSeed, EMapStartResources.HIGH_GOODS);
	}

	public InitialGameState(DataInputStream dis) throws IOException {
		byte readVersion = dis.readByte();
		if(readVersion > VERSION) throw new IllegalStateException("replay version is more recent than this build (" + readVersion + ">" + VERSION + ")");

		randomSeed = dis.readLong();
		playerId = dis.readByte();
		startResources = EMapStartResources.values()[dis.readByte()];

		playerSettings = new PlayerSetting[dis.readInt()];
		for (int i = 0; i < playerSettings.length; i++) {
			playerSettings[i] = PlayerSetting.readFromStream(dis);
		}

		if (readVersion >= 2) {
			peaceTime = EPeaceTime.fromMinutes(dis.readInt());
		} else {
			peaceTime = EPeaceTime.WITHOUT;
		}
	}

	public byte getPlayerId() {
		return playerId;
	}

	public PlayerSetting[] getPlayerSettings() {
		return playerSettings;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public EMapStartResources getStartResources() {
		return startResources;
	}

	public EPeaceTime getPeaceTime() {
		return peaceTime != null ? peaceTime : EPeaceTime.WITHOUT;
	}

	public PlayerSetting[] getReplayablePlayerSettings() {
		PlayerSetting[] playerSettings = new PlayerSetting[this.playerSettings.length];
		for (int i = 0; i < playerSettings.length; i++) {
			PlayerSetting originalSetting = this.playerSettings[i];
			playerSettings[i] = new PlayerSetting(originalSetting.isAvailable(), EPlayerType.HUMAN, originalSetting.getCivilisation(), originalSetting.getTeamId());
		}
		return playerSettings;
	}

	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeByte(VERSION);
		dos.writeLong(randomSeed);
		dos.writeByte(playerId);
		dos.writeByte(startResources.ordinal());

		dos.writeInt(playerSettings.length);
		for (PlayerSetting playerSetting : playerSettings) {
			playerSetting.writeTo(dos);
		}

		dos.writeInt(getPeaceTime().minutes);
	}

	@Override
	public InitialGameState clone() {
		return new InitialGameState(playerId, getReplayablePlayerSettings(), randomSeed, startResources, getPeaceTime());
	}

	@Override
	public String toString() {
		return "InitialGameState{" +
				"playerId=" + playerId +
				", playerSettings=" + Arrays.toString(playerSettings) +
				", randomSeed=" + randomSeed +
				", startResources=" + startResources +
				", peaceTime=" + peaceTime +
				'}';
	}
}
