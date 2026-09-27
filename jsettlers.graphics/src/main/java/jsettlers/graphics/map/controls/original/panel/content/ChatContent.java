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
package jsettlers.graphics.map.controls.original.panel.content;

import go.graphics.GLDrawContext;
import go.graphics.text.EFontSize;
import jsettlers.common.action.Action;
import jsettlers.common.menu.IStartedGame;
import jsettlers.common.menu.messages.IMessage;
import jsettlers.graphics.action.ActionFireable;
import jsettlers.graphics.action.ExecutableAction;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.localization.PlayerStatusTexts;
import jsettlers.graphics.map.MapContent;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.graphics.ui.Label;
import jsettlers.graphics.ui.Label.EHorizontalAlignment;
import jsettlers.graphics.ui.LabeledButton;
import jsettlers.graphics.ui.UIPanel;

/**
 * Shows the latest chat messages of a multiplayer game and lets the player write a new one.
 */
public class ChatContent extends AbstractContentProvider {
	private static final int MAX_ROWS = 6;
	private static final int MAX_TEXT_LENGTH = 70;
	private static final long UPDATE_INTERVAL_MS = 500;

	private static final float ROWS_TOP = .9f;
	private static final float ROWS_BOTTOM = .24f;
	private static final float ROW_HEIGHT = (ROWS_TOP - ROWS_BOTTOM) / MAX_ROWS;

	private final IStartedGame game;
	private final MapContent mapContent;
	private final Runnable onHiding;
	private final UIPanel panel;
	private final Label[] names = new Label[MAX_ROWS];
	private final Label[] texts = new Label[MAX_ROWS];
	private final Label emptyLabel;
	private long lastUpdate = 0;

	/**
	 * Creates a new chat panel.
	 *
	 * @param game
	 *            The game the chat belongs to.
	 * @param mapContent
	 *            The map that shows the chat messages and asks for new ones.
	 * @param backAction
	 *            The action of the back button.
	 * @param onHiding
	 *            Is called when this content is replaced by another one.
	 */
	public ChatContent(IStartedGame game, MapContent mapContent, Action backAction, Runnable onHiding) {
		this.game = game;
		this.mapContent = mapContent;
		this.onHiding = onHiding;
		this.panel = new UIPanel() {
			@Override
			public void drawAt(GLDrawContext gl) {
				updateIfNeeded();
				super.drawAt(gl);
			}
		};

		panel.addChild(new Label(Labels.getString("chat_title"), EFontSize.HEADLINE), .05f, .92f, .95f, 1f);

		for (int i = 0; i < MAX_ROWS; i++) {
			float top = ROWS_TOP - i * ROW_HEIGHT;
			float middle = top - ROW_HEIGHT * .35f;
			float bottom = top - ROW_HEIGHT;

			names[i] = new Label("", EFontSize.NORMAL, EHorizontalAlignment.LEFT);
			texts[i] = new Label("", EFontSize.SMALL, EHorizontalAlignment.LEFT);

			panel.addChild(names[i], .05f, middle, .95f, top);
			panel.addChild(texts[i], .05f, bottom, .95f, middle);
		}

		boolean chatAvailable = mapContent.isChatInputAvailable();
		emptyLabel = new Label(Labels.getString(chatAvailable ? "chat_no_messages" : "chat_not_available"), EFontSize.NORMAL);
		panel.addChild(emptyLabel, .1f, .5f, .9f, .8f);

		if (chatAvailable) {
			panel.addChild(new LabeledButton(Labels.getString("chat_write_message"), new ExecutableAction() {
				@Override
				public void execute() {
					mapContent.requestChatInput();
				}
			}), .1f, .13f, .9f, .22f);
		}
		panel.addChild(new LabeledButton(Labels.getString("players-back"), backAction), .1f, .02f, .9f, .11f);
	}

	private void updateIfNeeded() {
		long now = System.currentTimeMillis();
		if (now - lastUpdate < UPDATE_INTERVAL_MS) {
			return;
		}
		lastUpdate = now;

		IMessage[] history = mapContent.getChatHistory();
		int first = Math.max(0, history.length - MAX_ROWS);
		for (int i = 0; i < MAX_ROWS; i++) {
			int index = first + i;
			if (index < history.length) {
				IMessage message = history[index];
				names[i].setText(PlayerStatusTexts.getName(game, message.getSender()) + ":");
				names[i].setTextColor(MapDrawContext.getPlayerColor(message.getSender()));
				texts[i].setText(shorten(message.getMessageLabel()));
			} else {
				names[i].setText("");
				texts[i].setText("");
			}
		}
		emptyLabel.setText(history.length == 0 ? Labels.getString(mapContent.isChatInputAvailable() ? "chat_no_messages" : "chat_not_available") : "");
	}

	private static String shorten(String text) {
		if (text.length() <= MAX_TEXT_LENGTH) {
			return text;
		}
		return text.substring(0, MAX_TEXT_LENGTH - 3) + "...";
	}

	@Override
	public void contentShowing(ActionFireable actionFireable) {
		lastUpdate = 0;
	}

	@Override
	public void contentHiding(ActionFireable actionFireable, AbstractContentProvider nextContent) {
		onHiding.run();
	}

	@Override
	public UIPanel getPanel() {
		return panel;
	}

	@Override
	public ESecondaryTabType getTabs() {
		return null;
	}
}
