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

import jsettlers.network.NetworkConstants;

/**
 * Server address as entered by the user or passed by a launcher: {@code host} or {@code host:port} (IPv6 literals in brackets:
 * {@code [::1]:10214}). Without a port, {@link NetworkConstants.Server#SERVER_PORT} is used.
 */
public final class ServerAddress {
	public final String host;
	public final int port;

	public ServerAddress(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public static ServerAddress parse(String address) {
		String s = address == null ? "" : address.trim();
		int port = NetworkConstants.Server.SERVER_PORT;
		String host = s;
		if (s.startsWith("[")) {
			int end = s.indexOf(']');
			if (end > 0) {
				host = s.substring(1, end);
				String rest = s.substring(end + 1);
				if (rest.startsWith(":")) {
					port = parsePort(rest.substring(1), port);
				}
			}
		} else {
			int colon = s.lastIndexOf(':');
			// exactly one colon: host:port; several colons: plain IPv6 literal without port
			if (colon > 0 && s.indexOf(':') == colon) {
				host = s.substring(0, colon);
				port = parsePort(s.substring(colon + 1), port);
			}
		}
		return new ServerAddress(host, port);
	}

	private static int parsePort(String text, int fallback) {
		try {
			int p = Integer.parseInt(text.trim());
			return p > 0 && p <= 65535 ? p : fallback;
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	@Override
	public String toString() {
		return (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + ":" + port;
	}
}
