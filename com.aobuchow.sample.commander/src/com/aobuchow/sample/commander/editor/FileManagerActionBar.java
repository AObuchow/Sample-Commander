package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

public class FileManagerActionBar extends EditorActionBarContributor {

	private IEditorPart editor;
	private FlatDirectoryLayoutAction flatDirectory;
	private AutoPlayToggleAction autoPlay;

	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		this.editor = targetEditor;
		super.setActiveEditor(targetEditor);

		if (targetEditor instanceof FileManagerEditor) {
			flatDirectory.setEditor((FileManagerEditor) targetEditor);
		} 
	}

	@Override
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		FileManagerEditor ed = (FileManagerEditor) editor;
		flatDirectory = new FlatDirectoryLayoutAction(ed);
		autoPlay = new AutoPlayToggleAction();
		toolBarManager.add(flatDirectory);
		toolBarManager.add(autoPlay);
		super.contributeToToolBar(toolBarManager);
	}

}
