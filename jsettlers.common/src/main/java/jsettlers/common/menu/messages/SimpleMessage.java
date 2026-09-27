/*
 * Copyright (c) 2018
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
package jsettlers.common.menu.messages;

import jsettlers.common.Color;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.position.ShortPoint2D;

import java.util.Objects;

/**
 * This is a messageLabel that states that the user was attacked by an other player.
 *
 * @author Michael Zangl
 */
public class SimpleMessage implements IMessage {
	/**
	 * Status messages are only shown for a short time.
	 */
	public static final long PLAYER_STATUS_TTL = 20000;

	private final byte sender;
	private final ShortPoint2D pos;
	private final String messageLabel;
	private final EMessageType type;
	private final Color indicatorColor;
	private final long timeToLive;
	private int age;

	/**
	 * Creates a new simple chat messageLabel.
	 *
	 * @param type
	 * 		The messageLabel type.
	 * @param messageLabel
	 * 		The messageLabel string to display.
	 * @param sender
	 * 		The sender of the messageLabel
	 * @param pos
	 * 		The position the messageLabel was sent from.
	 */
	private SimpleMessage(EMessageType type, String messageLabel, byte sender, ShortPoint2D pos) {
		this(type, messageLabel, sender, pos, null, MESSAGE_TTL);
	}

	private SimpleMessage(EMessageType type, String messageLabel, byte sender, ShortPoint2D pos, Color indicatorColor, long timeToLive) {
		this.type = type;
		this.messageLabel = messageLabel;
		this.sender = sender;
		this.pos = pos;
		this.indicatorColor = indicatorColor;
		this.timeToLive = timeToLive;
		this.age = 0;
	}

	@Override
	public EMessageType getType() {
		return type;
	}

	@Override
	public int getAge() {
		return this.age;
	}

	@Override
	public int ageBy(int interval) {
		this.age += interval;
		return this.age;
	}

	@Override
	public String getMessageLabel() {
		return messageLabel;
	}

	@Override
	public byte getSender() {
		return sender;
	}

	@Override
	public ShortPoint2D getPosition() {
		return pos;
	}

	@Override
	public long getTimeToLive() {
		return timeToLive;
	}

	@Override
	public Color getIndicatorColor() {
		return indicatorColor;
	}

	@Override
	public boolean duplicates(IMessage m) {
		if (this.type == EMessageType.PLAYER_STATUS) {
			return false; // status messages are only sent when the status changes
		}
		if (this.type == EMessageType.CHAT) {
			return false; // a player may send the same text more than once
		}
		if ((m.getSender() == this.sender)
				&& m.getMessageLabel().equals(this.messageLabel)
				&& m.getType() == this.type) {
			if ((this.type == EMessageType.ATTACKED) || (this.type == EMessageType.MINERALS)) {
				if (m.getAge() < MESSAGE_TTL / 6) {
					return this.pos.getOnGridDistTo(m.getPosition()) < MESSAGE_DIST_THRESHOLD;
				}
			} else {
				return Objects.equals(this.pos, m.getPosition());
			}
		}
		return false;
	}

	public static IMessage donkeyAttacked(byte otherplayer, ShortPoint2D pos) {
		return new SimpleMessage(EMessageType.ATTACKED, "attacked_donkey", otherplayer, pos);
	}


	/**
	 * Creates a new attacked-messageLabel.
	 *
	 * @param otherplayer
	 * 		The attacking player
	 * @param pos
	 * 		The position that player attacked on.
	 * @return THe messageLabel.
	 */
	public static IMessage attacked(byte otherplayer, ShortPoint2D pos) {
		return new SimpleMessage(EMessageType.ATTACKED, "attacked", otherplayer, pos);
	}

	/**
	 * Create a new messageLabel if a geologist found minerals.
	 *
	 * @param type
	 * 		The type of minerals.
	 * @param pos
	 * 		The position
	 * @return The messageLabel object
	 */
	public static IMessage foundMinerals(EMaterialType type, ShortPoint2D pos) {
		return new SimpleMessage(EMessageType.MINERALS, "minerals_" + type.toString(), (byte) -1, pos);
	}

	/**
	 * Create a new messageLabel that a building cannot find any more work.
	 *
	 * @param building
	 * 		The building
	 * @return THe messageLabel object
	 */
	public static IMessage cannotFindWork(IBuilding building) {
		return new SimpleMessage(EMessageType.NOTHING_FOUND_IN_SEARCH_AREA, "cannot_find_work_" + building.getBuildingVariant().getType(), (byte) -1, building.getPosition());
	}

	/**
	 *
	 * @param at
	 * 		The location where to mage was supposed to cast a spell
	 * @param messageLabel
	 * 		The translation key of the text that will be shown
	 * @return The messageLabel object
	 */
	public static IMessage castFailed(ShortPoint2D at, String messageLabel) {
		return new SimpleMessage(EMessageType.NOTHING_FOUND_IN_SEARCH_AREA, messageLabel, (byte)-1, at);
	}

	/**
	 * Creates a general information message that is not bound to a position on the map.
	 *
	 * @param messageLabel
	 * 		The translation key of the text that will be shown
	 * @return The messageLabel object
	 */
	public static IMessage info(String messageLabel) {
		return new SimpleMessage(EMessageType.INFO, messageLabel, (byte) -1, null);
	}

	/**
	 * Creates a general information message about an action of a player that is not bound to a position on the map.
	 *
	 * @param messageLabel
	 * 		The translation key of the text that will be shown after the name of the player
	 * @param player
	 * 		The player the message is about
	 * @return The messageLabel object
	 */
	public static IMessage playerInfo(String messageLabel, byte player) {
		return new SimpleMessage(EMessageType.INFO, messageLabel, player, null);
	}

	/**
	 * Creates a short message about the state of a player, e.g. a network problem.
	 *
	 * @param messageLabel
	 *            The label of the message text.
	 * @param player
	 *            The player the message is about or -1 if it is not about a specific player.
	 * @param indicatorColor
	 *            The color of the status dot shown in front of the message.
	 */
	public static IMessage playerStatus(String messageLabel, byte player, Color indicatorColor) {
		return new SimpleMessage(EMessageType.PLAYER_STATUS, messageLabel, player, null, indicatorColor, PLAYER_STATUS_TTL);
	}

	/**
	 * Creates a chat message a player sent to the other players of a multiplayer game.
	 *
	 * @param player
	 *            The player that wrote the message.
	 * @param text
	 *            The text of the message. It is shown as it is and not translated.
	 */
	public static IMessage chat(byte player, String text) {
		return new SimpleMessage(EMessageType.CHAT, text, player, null);
	}
}
