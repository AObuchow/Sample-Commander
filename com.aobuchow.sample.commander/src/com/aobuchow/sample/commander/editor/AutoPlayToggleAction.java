package com.aobuchow.sample.commander.editor;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.Images;

public class AutoPlayToggleAction extends Action {

	public static final String AUTO_PLAY_PREFERENCE_KEY = "autoPlay"; //$NON-NLS-1$
	private IEclipsePreferences preferences;

	public AutoPlayToggleAction() {
		this.setEnabled(true);
		this.setId("com.aobuchow.sample.commander.actions.AutoPlayToggle"); //$NON-NLS-1$
		this.setText(Messages.AutoPlayToggleAction_Text);

		preferences = InstanceScope.INSTANCE.getNode(Activator.PREFERENCES_NODE_QUALIFER);
		if (preferences.getBoolean(AUTO_PLAY_PREFERENCE_KEY, true)) {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_PLAY));
		} else {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_MUTE));
		}
	}

	@Override
	public void run() {
		boolean oldValue = preferences.getBoolean(AUTO_PLAY_PREFERENCE_KEY, true);
		boolean newValue = !oldValue;
		preferences.putBoolean(AUTO_PLAY_PREFERENCE_KEY, newValue);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		if (newValue == true) {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_PLAY));
		} else {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_MUTE));
		}

		super.run();
	}

}
