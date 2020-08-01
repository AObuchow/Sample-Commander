package com.aobuchow.sample.commander;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.goxr3plus.streamplayer.stream.ThreadFactoryWithNamePrefix;
// TODO: Extend AbstractUIPlugin?
public class Activator implements BundleActivator {
	
	// The shared instance
	private static Activator plugin;

	private static BundleContext context;
	private AudioPlayer audioPlayer;
	private static ExecutorService streamPlayerExecutor;
	private static ExecutorService streamPlayerEventExecutor;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		plugin = this;
		Display.getDefault().asyncExec(() -> {
			audioPlayer = newAudioPlayer();	
			
		});
	}

	public void stop(BundleContext bundleContext) throws Exception {
		plugin = null;
		Activator.context = null;
		shutdownAudioPlayer();
	}

	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	
	private static void shutdownAudioPlayer() {
		streamPlayerExecutor.shutdown();
		streamPlayerEventExecutor.shutdown();
	}

	private static AudioPlayer newAudioPlayer() {
		Logger mutedLoger = Logger.getLogger(AudioPlayer.class.getName());
		mutedLoger.setLevel(Level.OFF);
		streamPlayerExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryWithNamePrefix("StreamPlayer"));
		streamPlayerEventExecutor = Executors
				.newSingleThreadExecutor(new ThreadFactoryWithNamePrefix("StreamPlayerEvent"));
		return new AudioPlayer(mutedLoger, streamPlayerExecutor, streamPlayerEventExecutor);
	}

}
