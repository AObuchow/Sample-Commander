package com.aobuchow.sample.commander.editor;


import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.resources.ProjectExplorer;

public class ContainerEditorInputTester extends PropertyTester {
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof ProjectExplorer)) {
			return false;
		}
		ProjectExplorer editor = (ProjectExplorer) receiver;
		IStructuredSelection selection = editor.getCommonViewer().getStructuredSelection();
		IResource input = Adapters.adapt(selection.getFirstElement(), IResource.class);
		
		if (input instanceof IFolder) {
			return Adapters.adapt(input, IFolder.class) != null;
		}
		
		if (input instanceof IFile) {
			return Adapters.adapt(((IFile)input).getParent(), IFolder.class) != null;
		}

		if (input == null) {
			return false;
		}
		return input != null;
	}

}
