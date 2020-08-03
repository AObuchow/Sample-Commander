package com.aobuchow.sample.commander;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.goxr3plus.streamplayer.stream.ThreadFactoryWithNamePrefix;
// TODO: Extend AbstractUIPlugin?
public class Activator extends AbstractUIPlugin {
	
	// The shared instance
	private static Activator plugin;

	private static BundleContext context;
	private AudioPlayer audioPlayer;
	private static ExecutorService streamPlayerExecutor;
	private static ExecutorService streamPlayerEventExecutor;
	private FileClipBoard clipboard;
	
	public static final String PLUGIN_ID = "com.aobuchow.sample.commander";

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
	
	// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=265741, don't call in start method
	// Also don't dispose the clipboard for this reason..
	public void initClipBoard(Display display) {
		if (clipboard == null) {
			clipboard = new FileClipBoard(display);	
		}
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

	public FileClipBoard getClipboard() {
		return clipboard;
	}

}
