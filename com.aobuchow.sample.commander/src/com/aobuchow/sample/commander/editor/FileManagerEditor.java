package com.aobuchow.sample.commander.editor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.ResourceTransfer;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.handlers.OpenResourceHandler;

public class FileManagerEditor extends EditorPart implements IEditorPart {

	private FileManagerViewer viewer;
	private IContainer inputContainer = null;
	private IWorkspace workspace;
	private IResourceChangeListener workspaceChangeListener;
	private IOperationHistory history;
	private IUndoContext undoContext;
	private Map<IContainer, IResource> visitedDirectories = new HashMap<IContainer, IResource>();
	private boolean flatMode = false;

	@PostConstruct
	@Override
	public void createPartControl(Composite parent) {
		viewer = new FileManagerViewer(parent);
		viewer.setInput(inputContainer);
		viewer.getControl().addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// e.doit gets disabled regardless if successful as the beep it creates on Linux
				// is annoying
				switch (e.keyCode) {
				case SWT.ARROW_RIGHT:
					IStructuredSelection selection = viewer.getStructuredSelection();
					if (selection.size() == 1 && selection.getFirstElement() instanceof IContainer) {
						changeActiveContainer(Adapters.adapt(selection.getFirstElement(), IContainer.class));
					}
					e.doit = false;
					break;
				case SWT.ARROW_LEFT:
					if (!(viewer.getInput() instanceof IProject)) {
						changeActiveContainer(((IContainer) viewer.getInput()).getParent());
					}
					e.doit = false;
					break;
				}
			}
		});

		this.viewer.addDoubleClickListener((event) -> {
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			if (selection.getFirstElement() instanceof IContainer) {
				IContainer selectedDirectory = (IContainer) selection.getFirstElement();

				// Check if an editor already exists for the selected directory
				IEditorReference[] editorRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.getEditorReferences();
				List<IEditorReference> editorRefsList = Arrays.asList(editorRefs);
				editorRefsList = editorRefsList.stream().filter(editorRef -> {
					return FileManagerMatchingStrategy.staticMatches(editorRef,
							new ContainerEditorInput(selectedDirectory));
				}).collect(Collectors.toList());

				if (!editorRefsList.isEmpty()) {
					try {
						IDE.openEditor(this.getEditorSite().getPage(),
								new ContainerEditorInput((IContainer) selection.getFirstElement()),
								OpenResourceHandler.EDITOR_ID);
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				} else {
					changeActiveContainer(selectedDirectory);
				}
			}
		});

		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[] { ResourceTransfer.getInstance() };
		this.viewer.addDropSupport(operations, transferTypes, new FileManagerDropListener(this));
		this.viewer.addDragSupport(operations, transferTypes, new FileManagerDragListener(viewer));

		this.setPartName(inputContainer.getName());
	}

	@Focus
	public void setFocus() {
		this.viewer.getControl().setFocus();
	}

	@Override
	// TODO: Remove?
	public void addPropertyListener(IPropertyListener listener) {
	}

	@Override
	public void dispose() {
		viewer.getControl().dispose();
		workspace.removeResourceChangeListener(workspaceChangeListener);
		// Related to issue #31
		// TODO: Only when the last FileManagerEditor closes should the clipboard be
		// disposed
		// Activator.getDefault().getClipboard().dispose();
	}

	@Override
	public Image getTitleImage() {
		return null;
	}

	@Override
	public void removePropertyListener(IPropertyListener listener) {
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO: Should refresh the project
	}

	@Override
	public void doSaveAs() {
		// Disabled
	}

	@Override
	public boolean isDirty() {
		// TODO: Return whether a file operation has occurred in this directory
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		return false;
	}

	@Override
	public IEditorInput getEditorInput() {
		return super.getEditorInput();
	}

	@Override
	public IEditorSite getEditorSite() {
		return this.getSite();
	}

	@Override
	public IEditorSite getSite() {
		return (IEditorSite) super.getSite();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		if (input instanceof IContainerEditorInput) {
			setInputFile(((IContainerEditorInput) input).getContainer());
		} else if (input instanceof IFileEditorInput) {
			setInputFile(((IFileEditorInput) input).getFile().getParent());
		}

		setSite(site);
		// setInput is called in setInputFile
		createPartControl(site.getShell());

		workspace = ResourcesPlugin.getWorkspace();
		workspaceChangeListener = event -> {
			switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				this.refreshViewerInput();
				break;
			}
		};
		workspace.addResourceChangeListener(workspaceChangeListener);

		undoContext = (IUndoContext) ResourcesPlugin.getWorkspace().getAdapter(IUndoContext.class);
		IWorkbench workbench = getSite().getWorkbenchWindow().getWorkbench();
		history = workbench.getOperationSupport().getOperationHistory();

		UndoActionHandler undoAction = new UndoActionHandler(site, IOperationHistory.GLOBAL_UNDO_CONTEXT);
		RedoActionHandler redoAction = new RedoActionHandler(site, IOperationHistory.GLOBAL_UNDO_CONTEXT);
		IActionBars actionBars = site.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
		actionBars.updateActionBars();

		Activator.getDefault().initClipBoard(site.getWorkbenchWindow().getShell().getDisplay());
	}

	private void setInputFile(IContainer inputContainer) {
		this.inputContainer = inputContainer;
		setInput(new ContainerEditorInput(inputContainer));
		this.setTitleToolTip(inputContainer.getFullPath().toString());
		firePropertyChange(IWorkbenchPartConstants.PROP_INPUT);
		firePropertyChange(IWorkbenchPartConstants.PROP_TITLE);
		// this.setPartName(inputDirectory);
		refreshViewerInput();
	}

	public void delete() {
		DeleteResourceAction action = new DeleteResourceAction(getSite());
		action.selectionChanged(this.viewer.getStructuredSelection());
		action.run();
	}

	public void refreshViewerInput() {
		if (this.viewer == null) {
			return;
		}
		viewer.getControl().getDisplay().syncExec(() -> this.viewer.setInput(inputContainer));
	}

	private void changeActiveContainer(IContainer container) {
		IResource currentSelection = (IResource) this.viewer.getStructuredSelection().getFirstElement();
		if (currentSelection != null) {
			visitedDirectories.put(inputContainer, currentSelection);
		}
		setInputFile(container);
		IResource lastSelectedFile = visitedDirectories.get(container);
		if (lastSelectedFile != null) {
			try {
				IResource[] model = (IResource[]) inputContainer.members();
				Optional<IResource> newSelection = Arrays.asList(model).stream()
						.filter(resource -> resource.equals(lastSelectedFile)).findFirst();
				newSelection.ifPresent(selection -> this.setSelection(new StructuredSelection(selection)));
			} catch (CoreException e) {
				e.printStackTrace();
			}
		} else {
			if (inputContainer.getParent() != null) {
				this.setSelection(new StructuredSelection(inputContainer.getParent()));
			}
		}

		// TODO: These don't work?
		setPartName(container.getName());
		firePropertyChange(IWorkbenchPartConstants.PROP_PART_NAME);
	}

	public void setSelection(IStructuredSelection aSelection) {
		if (this.viewer != null) {
			this.viewer.setSelection(aSelection, true);
		}
	}

	public void copy() {
		internalCopy(false);
	}

	public void cut() {
		internalCopy(true);
	}

	private void internalCopy(boolean doCut) {
		List<IResource> selectedFilesList = (List<IResource>) this.viewer.getStructuredSelection().toList().stream()
				.filter(selection -> selection instanceof IResource).collect(Collectors.toList());

		if (!selectedFilesList.isEmpty()) {
			IResource[] selectedFiles = selectedFilesList.toArray(new IResource[selectedFilesList.size()]);
			if (doCut) {
				Activator.getDefault().getClipboard().cut(selectedFiles);
			} else {
				Activator.getDefault().getClipboard().copy(selectedFiles);
			}
		}
	}

	public void undo() {
		try {
			history.undo(undoContext, new NullProgressMonitor(),
					WorkspaceUndoUtil.getUIInfoAdapter(PlatformUI.getWorkbench().getDisplay().getActiveShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void redo() {
		try {
			history.redo(undoContext, new NullProgressMonitor(),
					WorkspaceUndoUtil.getUIInfoAdapter(PlatformUI.getWorkbench().getDisplay().getActiveShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void paste() {
		IResource pastedItem = Activator.getDefault().getClipboard().paste(inputContainer,
				PlatformUI.getWorkbench().getDisplay().getActiveShell());

		// This is disabled for now as it doesn't scroll to the selection and causes the
		// pasted file to be played
		// TODO: Make this a preference?
		if (pastedItem != null && false) {
			try {
				// Select the first of the pasted items
				List<IResource> itemInView = Arrays.asList(inputContainer.members()).stream()
						.filter(res -> res.getName().equals(pastedItem.getName())).collect(Collectors.toList());
				if (!itemInView.isEmpty()) {
					this.setSelection(new StructuredSelection(itemInView.get(0)));
				}

			} catch (CoreException e) {
				e.printStackTrace();
			}

		}

	}

	public Viewer getViewer() {
		return this.viewer;
	}

	public IContainer getContainer() {
		return inputContainer;
	}

	public void toggleFlatMode() {
		this.flatMode = !flatMode;
		
		// This blocks the ui thread :/
		// TODO: FileMangerContentProvider should be async/deffered?
		viewer.getControl().getDisplay()
				.asyncExec(() -> this.viewer.setContentProvider(new FileManagerContentProvider(flatMode)));
	}

	public boolean isFlatMode() {
		return this.flatMode;
	}

}
