package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.aobuchow.sample.commander.Activator;

public class FlatDirectoryLayoutAction extends Action {

	private FileManagerEditor editor;

	public FlatDirectoryLayoutAction(FileManagerEditor editor) {
		this.editor = editor;
		this.setEnabled(true);
		this.setId("com.aobuchow.sample.commander.actions.FlatDirectory"); //$NON-NLS-1$
		this.setText(Messages.FlatDirectoryLayoutAction_Text);
		this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/view-list_32.png")); //$NON-NLS-1$
	}

	@Override
	public void run() {
		editor.toggleFlatMode();
		if (editor.isFlatMode()) {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/folder_hero_32.png")); //$NON-NLS-1$
		} else {
			this.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/view-list_32.png")); //$NON-NLS-1$
		}
		super.run();
	}

	public void setEditor(FileManagerEditor targetEditor) {
		this.editor = targetEditor;
	}

}
