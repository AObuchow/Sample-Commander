package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;

public class ContainerLinkHelper implements ILinkHelper {

	@Override
	public IStructuredSelection findSelection(IEditorInput anInput) {
		if (anInput instanceof IContainerEditorInput) {
			IContainerEditorInput input = (IContainerEditorInput) anInput;
			return new StructuredSelection(input.getContainer());
		}
		
		return null;
	}

	@Override
	public void activateEditor(IWorkbenchPage aPage, IStructuredSelection aSelection) {
		IEditorPart editor = aPage.getActiveEditor();
		if (editor != null && editor instanceof FileManagerEditor) {
			((FileManagerEditor) editor).setSelection(aSelection);
		}
	}

}
