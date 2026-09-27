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
package jsettlers.graphics.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import jsettlers.common.menu.messages.IMessage;
import jsettlers.common.menu.messages.SimpleMessage;
import jsettlers.common.statistics.IGameTimeProvider;

public class MessengerTest {
	private final Messenger messenger = new Messenger(IGameTimeProvider.DUMMY_IMPLEMENTATION);

	@Test
	public void testSameChatMessageIsShownTwice() {
		assertTrue(messenger.addMessage(SimpleMessage.chat((byte) 1, "hello")));
		assertTrue(messenger.addMessage(SimpleMessage.chat((byte) 1, "hello")));

		assertEquals(2, messenger.getMessages().length);
		assertEquals(2, messenger.getChatHistory().length);
	}

	@Test
	public void testChatHistoryOnlyContainsChatMessages() {
		IMessage chat = SimpleMessage.chat((byte) 0, "gg");
		messenger.addMessage(SimpleMessage.info("some_label"));
		messenger.addMessage(chat);

		assertArrayEquals(new IMessage[] { chat }, messenger.getChatHistory());
	}

	@Test
	public void testChatHistoryKeepsTheLatestMessagesInOrder() {
		int count = Messenger.MAX_CHAT_HISTORY + 5;
		for (int i = 0; i < count; i++) {
			messenger.addMessage(SimpleMessage.chat((byte) 0, "message " + i));
		}

		IMessage[] history = messenger.getChatHistory();
		assertEquals(Messenger.MAX_CHAT_HISTORY, history.length);
		assertEquals("message 5", history[0].getMessageLabel());
		assertEquals("message " + (count - 1), history[history.length - 1].getMessageLabel());
		assertEquals(IMessage.MAX_MESSAGES, messenger.getMessages().length);
	}
}
