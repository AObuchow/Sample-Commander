package com.aobuchow.sample.commander.parts;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

public class ContainerEditorInputFactory implements IElementFactory {
	/**
	 * Factory id. The workbench plug-in registers a factory by this name
	 * with the "org.eclipse.ui.elementFactories" extension point.
	 */
	private static final String ID_FACTORY = "com.aobuchow.sample.commander.parts.ContainerEditorInputFactory"; //$NON-NLS-1$

	/**
	 * Tag for the IFile.fullPath of the file resource.
	 */
	private static final String TAG_PATH = "path"; //$NON-NLS-1$

	/**
	 * Creates a new factory.
	 */
	public ContainerEditorInputFactory() {
	}

	@Override
	public IAdaptable createElement(IMemento memento) {
		String fileName = memento.getString(TAG_PATH);
		if (fileName == null) {
			return null;
		}

		IPath relativePath = new Path(fileName).makeRelative();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(relativePath.lastSegment());
		if (project != null && project.exists()) {
			return new ContainerEditorInput(project);
		} else {
			IContainer file = ResourcesPlugin.getWorkspace().getRoot().getFolder(relativePath);
			if (file != null) {
				return new ContainerEditorInput(file);
			}
		}

		return null;
	}

	/**
	 * Returns the element factory id for this class.
	 *
	 * @return the element factory id
	 */
	public static String getFactoryId() {
		return ID_FACTORY;
	}

	/**
	 * Saves the state of the given file editor input into the given memento.
	 *
	 * @param memento the storage area for element state
	 * @param input the file editor input
	 */
	public static void saveState(IMemento memento, ContainerEditorInput input) {
		IContainer file = input.getContainer();
		memento.putString(TAG_PATH, file.getFullPath().toString());
	}
}
