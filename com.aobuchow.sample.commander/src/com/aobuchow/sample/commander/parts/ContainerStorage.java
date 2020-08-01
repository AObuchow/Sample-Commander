package com.aobuchow.sample.commander.parts;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

public class ContainerStorage implements IStorage {
	private IContainer container;
	public ContainerStorage(IContainer container) {
		this.container = container;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return container.getAdapter(adapter);
	}

	@Override
	public InputStream getContents() throws CoreException {
		return  new ByteArrayInputStream(container.getName().getBytes(Charset.forName("UTF-8")));
	}

	@Override
	public IPath getFullPath() {
		return container.getFullPath();
	}

	@Override
	public String getName() {
		return container.getName();
	}

	@Override
	public boolean isReadOnly() {
		return container.isReadOnly();
	}

}
