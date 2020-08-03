package com.aobuchow.sample.commander.editor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;

public class ContainerInputFactory implements IAdapterFactory{

	@Override
	public <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
		if (IContainer.class.equals(adapterType)) {
			return adapterType.cast(((IContainerEditorInput) adaptableObject).getContainer());
		}
		if (IResource.class.equals(adapterType)) {
			return adapterType.cast(((IContainerEditorInput) adaptableObject).getContainer());
		}
		return null;
	}

	@Override
	public Class<?>[] getAdapterList() {
		return new Class[] { IFile.class, IResource.class };
	}

}
