package com.aobuchow.sample.commander.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;

import com.aobuchow.sample.commander.editor.ContainerEditorInput;

public class OpenResourceHandler extends AbstractHandler {

	/**
	 * Parameter, which can optionally be passed to the command.
	 */
	public static final String RESOURCE_PATH_PARAMETER = "org.eclipse.ui.ide.showInSystemExplorer.path"; //$NON-NLS-1$
	
	// TODO: Rename..
	private static final String EDITOR_ID = "com.aobuchow.sample.commander.editor1";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IResource item = getResource(event);
		if (item == null) {
			return null;
		}
		IContainer container;
		if (item instanceof IFile) {
			container = Adapters.adapt(item, IFile.class).getParent();
		} else {
			container = Adapters.adapt(item, IContainer.class);
		}
		

			final IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			if (window == null) {
				throw new ExecutionException("no active workbench window"); //$NON-NLS-1$
			}

			final IWorkbenchPage page = window.getActivePage();
			if (page == null) {
				throw new ExecutionException("no active workbench page"); //$NON-NLS-1$
			}

			try {
				IDE.openEditor(page, new ContainerEditorInput(container),EDITOR_ID );
			} catch (final PartInitException e) {
				throw new ExecutionException("error opening file in editor", e); //$NON-NLS-1$
			}
		
		return null;
	}
	
	
	
	private IResource getResource(ExecutionEvent event) {
		IResource resource = getResourceByParameter(event);
		if (resource == null) {
			resource = getSelectionResource(event);
		}
		if (resource == null) {
			resource = getEditorInputResource(event);
		}
		return resource;
	}

	private IResource getResourceByParameter(ExecutionEvent event) {
		String parameter = event.getParameter(RESOURCE_PATH_PARAMETER);
		if (parameter == null) {
			return null;
		}
		IPath path = new Path(parameter);
		return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
	}

	private IResource getSelectionResource(ExecutionEvent event) {
		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		if ((selection == null) || (selection.isEmpty())) {
			return null;
		}

		Object selectedObject = selection
				.getFirstElement();
		return Adapters.adapt(selectedObject, IResource.class);
	}

	private IResource getEditorInputResource(ExecutionEvent event) {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (!(activePart instanceof IEditorPart)) {
			return null;
		}
		IEditorInput input = ((IEditorPart)activePart).getEditorInput();
		if (input instanceof IFileEditorInput) {
			return ((IFileEditorInput)input).getFile();
		}
		return Adapters.adapt(input, IResource.class);
	}

}
