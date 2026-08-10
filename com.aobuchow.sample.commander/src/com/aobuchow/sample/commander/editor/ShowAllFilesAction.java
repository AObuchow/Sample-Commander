package com.aobuchow.sample.commander.editor;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.service.prefs.BackingStoreException;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.Images;

public class ShowAllFilesAction extends Action {

	public static final String SHOW_ALL_FILES_PREFERENCE_KEY = "showAllFiles"; //$NON-NLS-1$
	private IEclipsePreferences preferences;

	public ShowAllFilesAction() {
		this.setEnabled(true);
		this.setId("com.aobuchow.sample.commander.actions.ShowAllFiles"); //$NON-NLS-1$
		this.setText("Show All Files"); //$NON-NLS-1$

		preferences = InstanceScope.INSTANCE.getNode(Activator.PREFERENCES_NODE_QUALIFER);
		if (preferences.getBoolean(SHOW_ALL_FILES_PREFERENCE_KEY, false)) {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.LIST));
		} else {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_FILE));
		}
	}

	@Override
	public void run() {
		boolean oldValue = preferences.getBoolean(SHOW_ALL_FILES_PREFERENCE_KEY, false);
		boolean newValue = !oldValue;
		preferences.putBoolean(SHOW_ALL_FILES_PREFERENCE_KEY, newValue);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
		if (newValue) {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.LIST));
		} else {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_FILE));
		}

		super.run();
	}

}
