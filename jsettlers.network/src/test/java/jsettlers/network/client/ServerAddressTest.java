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
package jsettlers.network.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import jsettlers.network.NetworkConstants;

public class ServerAddressTest {
	private static final int DEFAULT = NetworkConstants.Server.SERVER_PORT;

	private static void check(String input, String host, int port) {
		ServerAddress a = ServerAddress.parse(input);
		assertEquals(input, host, a.host);
		assertEquals(input, port, a.port);
	}

	@Test
	public void hostWithoutPortUsesDefault() {
		check("127.0.0.1", "127.0.0.1", DEFAULT);
		check(" localhost ", "localhost", DEFAULT);
		check("217.160.141.89", "217.160.141.89", DEFAULT);
	}

	@Test
	public void hostWithPort() {
		check("127.0.0.1:47123", "127.0.0.1", 47123);
		check("example.org:10214", "example.org", 10214);
	}

	@Test
	public void invalidPortFallsBackToDefault() {
		check("127.0.0.1:abc", "127.0.0.1", DEFAULT);
		check("127.0.0.1:0", "127.0.0.1", DEFAULT);
		check("127.0.0.1:70000", "127.0.0.1", DEFAULT);
	}

	@Test
	public void ipv6() {
		check("::1", "::1", DEFAULT);
		check("[::1]:47123", "::1", 47123);
		check("[fe80::1]", "fe80::1", DEFAULT);
		assertEquals("[::1]:47123", ServerAddress.parse("[::1]:47123").toString());
		assertEquals("127.0.0.1:10214", ServerAddress.parse("127.0.0.1").toString());
	}
}
