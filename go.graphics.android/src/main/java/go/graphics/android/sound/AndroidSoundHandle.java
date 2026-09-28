package go.graphics.android.sound;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;

import java.io.File;

import go.graphics.sound.SoundHandle;
import java.io.IOException;

public class AndroidSoundHandle implements SoundHandle {

	private volatile MediaPlayer player;
	private final File source;
	private final int length;
	private volatile float volume = 1;

	public AndroidSoundHandle(File source) {
		this.source = source;

		MediaMetadataRetriever mdr  = new MediaMetadataRetriever();
		mdr.setDataSource(source.getPath());
		String lengthString = mdr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
		mdr.release();

		// something went wrong
		if(lengthString == null) {
			length = -1;
			return;
		}

		length = Integer.parseInt(lengthString);
	}

	private void create() {
		try {
			if(player != null) return;

			player = new MediaPlayer();
			player.setDataSource(source.toString());
			player.prepare();
		} catch (IOException e) {
			e.printStackTrace();
			player = null;
		}
	}

	private void release() {
		MediaPlayer currentPlayer = player;
		if(currentPlayer == null) return;

		player = null;
		currentPlayer.release();
	}

	@Override
	public void start() {
		create();
		MediaPlayer currentPlayer = player;
		if (currentPlayer == null) {
			return; // the file could not be opened
		}
		currentPlayer.start();
		setVolume(volume);
	}

	/**
	 * Can be called from another thread than {@link #start()}, e.g. while the track is being started.
	 */
	@Override
	public void pause() {
		MediaPlayer currentPlayer = player;
		if (currentPlayer == null) {
			return;
		}
		try {
			currentPlayer.pause();
		} catch (IllegalStateException e) {
			// the player is not started yet or already released
		}
	}

	@Override
	public void stop() {
		MediaPlayer currentPlayer = player;
		if (currentPlayer != null) {
			try {
				currentPlayer.stop();
			} catch (IllegalStateException e) {
				// the player is already released
			}
		}
		release();
	}

	@Override
	public void dismiss() {
		release();
	}

	@Override
	public void setVolume(float volume) {
		this.volume = volume;

		MediaPlayer currentPlayer = player;
		if(currentPlayer != null) {
			float perceivedVolume = AndroidSoundPlayer.toPerceivedVolume(volume);
			try {
				currentPlayer.setVolume(perceivedVolume, perceivedVolume);
			} catch (IllegalStateException e) {
				// the player is already released
			}
		}
	}

	@Override
	public int getPlaybackDuration() {
		return length;
	}
}
