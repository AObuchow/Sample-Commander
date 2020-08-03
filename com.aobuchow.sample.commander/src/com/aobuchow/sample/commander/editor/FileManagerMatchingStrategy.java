package com.aobuchow.sample.commander.editor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;

public class FileManagerMatchingStrategy implements IEditorMatchingStrategy {

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		IContainer refParent = null;
		if (input == null) {
			return false;
		}
		String inputName = input instanceof FileEditorInput ? ((FileEditorInput) input).getFile().getParent().getName() : input.getName();

		 
		if (!editorRef.getTitle().equals(inputName)) {
			return false;
		}
		try {
			 refParent = ((IContainerEditorInput) editorRef.getEditorInput()).getContainer();
		} catch (PartInitException e) {
			e.printStackTrace();
			return false;
		}
		IContainer inputParent = null;
		if ((input instanceof IFileEditorInput)) {
			inputParent = ((IFileEditorInput) input).getFile().getParent();
		} else {
			inputParent =  ((IContainerEditorInput) input).getContainer();	
		}
		 
		
		if (refParent == null || inputParent == null) {
			return false;
		}
		
		return inputParent.equals(refParent);
	}

}
