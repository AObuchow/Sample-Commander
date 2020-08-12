package com.aobuchow.sample.commander.editor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;

import com.aobuchow.sample.commander.AudioPlayer;
import com.aobuchow.sample.commander.resources.AudioFile;

public class FileManagerContentProvider implements IStructuredContentProvider {
	boolean flatten;
	public FileManagerContentProvider(boolean flatten) {
		this.flatten = flatten;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		try {
			return createModel((IResource) inputElement, flatten);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return null;
	}

	// TODO: Refactor this
	private Object[] createModel(IResource o, boolean flatten) throws CoreException {
		Collection<Object> filesInSelectedDir = new ArrayList<Object>();
		IFolder parentFolder = null;
		if (!o.isAccessible() && !(o instanceof IProject)) {
			return null;
		}

		if (o instanceof IProject) {
			IProject project = Adapters.adapt(o, IProject.class);
			filesInSelectedDir = Arrays.asList(project.members()).stream()
					.map(FileManagerContentProvider::resourceConverter).collect(Collectors.toList());
		} else if (o instanceof IFolder) {
			parentFolder = Adapters.adapt(o, IFolder.class);

			filesInSelectedDir = Arrays.asList(parentFolder.members()).stream()
					.map(FileManagerContentProvider::resourceConverter).collect(Collectors.toList());

		} else if (o instanceof IFile) {
			parentFolder = Adapters.adapt(o.getParent(), IFolder.class);
			if (parentFolder == null) {
				IProject project = Adapters.adapt(o.getProject(), IProject.class);
				filesInSelectedDir = Arrays.asList(project.members()).stream()
						.map(FileManagerContentProvider::resourceConverter).collect(Collectors.toList());
			} else {
				filesInSelectedDir = Arrays.asList(parentFolder.members()).stream()
						.map(FileManagerContentProvider::resourceConverter).collect(Collectors.toList());
			}
		}

		if (flatten) {
			ArrayDeque<IContainer> containersToFlatten = new ArrayDeque<>();
			for (Object obj : filesInSelectedDir) {
				if (obj instanceof IContainer) {
					containersToFlatten.add((IContainer) obj);
				}
			}
			filesInSelectedDir.removeAll(containersToFlatten);
			while (!containersToFlatten.isEmpty()) {
				IContainer container = containersToFlatten.pop();
				for (IResource child : container.members()) {
					if (child instanceof IContainer) {
						containersToFlatten.push((IContainer) child);
					} else if (child instanceof IFile) {
						filesInSelectedDir.add(resourceConverter(child));
					}
				}
			}
		}
		filesInSelectedDir = filesInSelectedDir.stream().filter(Objects::nonNull).collect(Collectors.toList());

		if (parentFolder != null) {
			filesInSelectedDir.add(parentFolder.getParent());
		}

		return filesInSelectedDir.stream().toArray(Object[]::new);
	}

	public static Object resourceConverter(IResource resource) {
		// We only want audio files and directories
		// TODO: Add a toggle for this (issue #40)
		if (resource.getType() == IResource.FOLDER) {
			return Adapters.adapt(resource, IFolder.class);
		} else if (resource.getType() == IResource.FILE) {
			AudioFile file = new AudioFile(resource);
			if (AudioPlayer.canPlay(file)) {
				return file;
			}
		}
		return null;
	}

}
