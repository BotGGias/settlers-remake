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
package jsettlers.graphics.sound;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.After;
import org.junit.Test;

import go.graphics.sound.ISoundDataRetriever;
import go.graphics.sound.SoundHandle;
import go.graphics.sound.SoundPlayer;
import jsettlers.common.CommonConstants;
import jsettlers.common.player.ECivilisation;

public class MusicManagerTest {
	private final FakeSoundPlayer soundPlayer = new FakeSoundPlayer();
	private File musicFolder;

	@After
	public void tearDown() {
		MusicManager.setLookupPath(null);
		CommonConstants.MUSIC_VOLUME = () -> 1f;
		if (musicFolder != null) {
			for (File file : musicFolder.listFiles()) {
				file.delete();
			}
			musicFolder.delete();
		}
	}

	@Test
	public void testVolumeIsLimited() {
		CommonConstants.MUSIC_VOLUME = () -> 3f;
		MusicManager musicManager = new MusicManager(soundPlayer, ECivilisation.ROMAN);
		assertEquals(1f, musicManager.getMusicVolume(), 0.0001f);

		musicManager.setMusicVolume(0.4f, false);
		assertEquals(0.4f, musicManager.getMusicVolume(), 0.0001f);

		musicManager.setMusicVolume(-1f, true);
		assertEquals(0f, musicManager.getMusicVolume(), 0.0001f);

		musicManager.setMusicVolume(0.05f, true);
		assertEquals(0.05f, musicManager.getMusicVolume(), 0.0001f);

		musicManager.setMusicVolume(2f, false);
		assertEquals(1f, musicManager.getMusicVolume(), 0.0001f);
	}

	@Test(timeout = 5000)
	public void testStartAndStopWithoutMusic() {
		MusicManager musicManager = new MusicManager(soundPlayer, ECivilisation.ROMAN);

		musicManager.startMusic();
		assertTrue(musicManager.isRunning());
		musicManager.stopMusic();
		assertFalse(musicManager.isRunning());

		musicManager.startMusic();
		assertTrue(musicManager.isRunning());
		musicManager.stopMusic();
		assertFalse(musicManager.isRunning());
	}

	@Test(timeout = 5000)
	public void testRestartPlaysOnlyOneTrack() throws IOException, InterruptedException {
		MusicManager musicManager = new MusicManager(soundPlayer, ECivilisation.ROMAN);

		// stopping the music without music files must not leave permits that end the next track immediately
		for (int i = 0; i < 3; i++) {
			musicManager.startMusic();
			musicManager.stopMusic();
		}

		musicFolder = Files.createTempDirectory("music").toFile();
		new File(musicFolder, "ROMAN.1.mp3").createNewFile();
		new File(musicFolder, "ROMAN.2.mp3").createNewFile();
		MusicManager.setLookupPath(musicFolder);

		musicManager.startMusic();
		Thread.sleep(300);
		assertEquals(1, soundPlayer.startedTracks.size());

		musicManager.stopMusic();
		assertEquals(1, soundPlayer.pausedTracks.size());

		musicManager.startMusic();
		Thread.sleep(300);
		assertEquals(2, soundPlayer.startedTracks.size());
		musicManager.stopMusic();
		assertFalse(musicManager.isRunning());
	}

	private static class FakeSoundPlayer implements SoundPlayer {
		private final List<SoundHandle> startedTracks = new CopyOnWriteArrayList<>();
		private final List<SoundHandle> pausedTracks = new CopyOnWriteArrayList<>();

		@Override
		public void playSound(int soundStart, float leftVolume, float rightVolume) {
		}

		@Override
		public void setSoundDataRetriever(ISoundDataRetriever soundDataRetriever) {
		}

		@Override
		public SoundHandle openSound(File musicFile) {
			return new SoundHandle() {
				@Override
				public void start() {
					startedTracks.add(this);
				}

				@Override
				public void pause() {
					pausedTracks.add(this);
				}

				@Override
				public void stop() {
				}

				@Override
				public void dismiss() {
				}

				@Override
				public void setVolume(float volume) {
				}

				@Override
				public int getPlaybackDuration() {
					return 60000;
				}
			};
		}
	}
}
