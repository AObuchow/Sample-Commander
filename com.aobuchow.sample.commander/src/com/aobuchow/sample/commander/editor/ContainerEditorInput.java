package com.aobuchow.sample.commander.editor;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class ContainerEditorInput extends PlatformObject
		implements IContainerEditorInput, IPersistableElement, IPathEditorInput, IURIEditorInput {

	IContainer container;

	public ContainerEditorInput(IContainer container) {
		this.container = container;
	}

	@Override
	public <T> T getAdapter(Class<T> adapterType) {
		if (IWorkbenchAdapter.class.equals(adapterType)) {
			return adapterType.cast(new IWorkbenchAdapter() {

				@Override
				public Object[] getChildren(Object o) {
					return new Object[0];
				}

				@Override
				public ImageDescriptor getImageDescriptor(Object object) {
					return ContainerEditorInput.this.getImageDescriptor();
				}

				@Override
				public String getLabel(Object o) {
					return ContainerEditorInput.this.getName();
				}

				@Override
				public Object getParent(Object o) {
					return ContainerEditorInput.this.getContainer().getParent();
				}
			});
		}

		return super.getAdapter(adapterType);
	}

	@Override
	public boolean exists() {
		return container.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
			return container.getName();	
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return container.getFullPath().makeRelative().toString();
	}

	@Override
	public void saveState(IMemento memento) {
		ContainerEditorInputFactory.saveState(memento, this);
	}

	@Override
	public String getFactoryId() {
		return ContainerEditorInputFactory.getFactoryId();
	}

	public IContainer getContainer() {
		return this.container;
	}

	// Copied from org.eclipse.ui.part.FileEditorInput.getPath()
	@Override
	public IPath getPath() {
		IPath location = container.getLocation();
		if (location != null)
			return location;
		// this is not a local file, so try to obtain a local file
		try {
			final URI locationURI = container.getLocationURI();
			if (locationURI == null)
				throw new IllegalArgumentException();
			IFileStore store = EFS.getStore(locationURI);
			// first try to obtain a local file directly fo1r this store
			java.io.File localFile = store.toLocalFile(EFS.NONE, null);
			// if no local file is available, obtain a cached file
			if (localFile == null)
				localFile = store.toLocalFile(EFS.CACHE, null);
			if (localFile == null)
				throw new IllegalArgumentException();
			return Path.fromOSString(localFile.getAbsolutePath());
		} catch (CoreException e) {
			// this can only happen if the file system is not available for this scheme
			// TODO: Don't use internal API
			IDEWorkbenchPlugin.log("Failed to obtain file store for resource", e); //$NON-NLS-1$
			throw new RuntimeException(e);
		}
	}

	@Override
	public URI getURI() {
		return container.getLocationURI();
	}

	@Override
	public IStorage getStorage() throws CoreException {
		return new ContainerStorage(container);
	}

}
