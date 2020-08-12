package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import com.aobuchow.sample.commander.Activator;

public class FileManagerActionBar extends EditorActionBarContributor {

	private IEditorPart editor;
	private FlatDirectoryLayoutAction flatDirectory;

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		this.editor = targetEditor;
		super.setActiveEditor(targetEditor);

		if (targetEditor instanceof FileManagerEditor) {
			flatDirectory.setEnabled(true);
			flatDirectory.setEditor((FileManagerEditor) targetEditor);
		} else {
			flatDirectory.setEnabled(false);
		}
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		FileManagerEditor ed = (FileManagerEditor) editor;
		flatDirectory = new FlatDirectoryLayoutAction(ed);
		toolBarManager.add(flatDirectory);
		super.contributeToToolBar(toolBarManager);
	}

}
