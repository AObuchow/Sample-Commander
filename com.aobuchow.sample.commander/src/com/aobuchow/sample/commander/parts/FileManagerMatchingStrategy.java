package com.aobuchow.sample.commander.parts;

import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;

public class FileManagerMatchingStrategy implements IEditorMatchingStrategy {

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		IContainer refParent = null;
		try {
			 refParent = ((IFileEditorInput) editorRef.getEditorInput()).getFile().getParent();
		} catch (PartInitException e) {
			e.printStackTrace();
			return false;
		}
		IContainer inputParent =  ((IFileEditorInput) input).getFile().getParent();
		
		if (refParent == null || inputParent == null) {
			return false;
		}
		
		return inputParent.equals(refParent);
	}

}
