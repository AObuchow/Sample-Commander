package com.aobuchow.sample.commander;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import com.aobuchow.sample.commander.resources.AudioFile;
import com.goxr3plus.streamplayer.stream.StreamPlayer;
import com.goxr3plus.streamplayer.stream.StreamPlayerException;

public class AudioPlayer extends StreamPlayer {

	public AudioPlayer(Logger logger, ExecutorService streamPlayerExecutorService, ExecutorService eventsExecutorService) {
		super(logger, streamPlayerExecutorService, eventsExecutorService);
	}
	public AudioPlayer() {
		super();
	}

	public void play(AudioFile audioFile) {
				if (canPlay(audioFile)) {
					if (isPausedOrPlaying()) {
						stop();
					}
					try {
						open(audioFile.getLocation().toFile());
						play();
					} catch (StreamPlayerException e) {
						 e.printStackTrace();
					}
				}
			
	}

	public static boolean canPlay(AudioFile file) {
		// TODO: Don't use internal API?
		
		if (!(file.exists())) {
			return false;
		}
		String fileExtension = file.getFileExtension();
		return fileExtension.equals("mp3") || fileExtension.equals("wav") || fileExtension.equals("flac");
	}

	public void handleResume() {
		if (isPlaying()) {
			pause();
		} else {
			resume();
		}
	}

	public void restartAudio() {
		try {
			this.seekTo(0);
		} catch (StreamPlayerException e) {
			e.printStackTrace();
		}
	}


}