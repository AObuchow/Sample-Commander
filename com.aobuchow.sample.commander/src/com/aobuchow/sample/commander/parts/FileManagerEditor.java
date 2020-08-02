package com.aobuchow.sample.commander.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.OperationHistoryFactory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.DeleteResourceAction;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.operations.RedoActionHandler;
import org.eclipse.ui.operations.UndoActionHandler;
import org.eclipse.ui.part.EditorPart;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.resources.AudioFile;

public class FileManagerEditor extends EditorPart implements IEditorPart {

	private TableViewer viewer;
	private Object[] model;
	private String titleName = null;
	private IContainer inputContainer = null;
	private IWorkspace workspace;
	private IResourceChangeListener workspaceChangeListener;
	private IOperationHistory history;
	private IUndoContext undoContext;

	@PostConstruct
	@Override
	public void createPartControl(Composite parent) {
		// TODO: Extract the class to a viewer?
		this.viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		this.viewer.setContentProvider(ArrayContentProvider.getInstance());
		this.viewer.setComparator(new ResourceComparator());
		TableViewerColumn column = createColumnFor(viewer, "File");
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof AudioFile) {
					return ((AudioFile) element).getName();
				}
				if (element instanceof IFolder) {
					// TODO: Should use a folder icon instead...
					return "/" + ((IFolder) element).getName() + "/";
				}
				return "null";
			}
		});

		this.viewer.addSelectionChangedListener((event) -> {
			if (event.getStructuredSelection().size() == 1) {
				Object selection = event.getStructuredSelection().getFirstElement();
				if (selection instanceof AudioFile) {
					Activator.getDefault().getAudioPlayer().play((AudioFile) selection);
				}
			}

		});
		this.viewer.getTable().setLinesVisible(true);
		this.viewer.getTable().setHeaderVisible(true);
		this.viewer.setInput(model);

		this.titleName = inputContainer.getName();
		this.setPartName(titleName);
		super.firePropertyChange(PROP_TITLE);

		// org.eclipse.ui.IWorkbenchCommandConstants.EDIT_DELETE

	}

	private TableViewerColumn createColumnFor(TableViewer viewer, String label) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}

	public static Object resourceConverter(IResource resource) {
		if (resource.getType() == IResource.FOLDER) {
			return Adapters.adapt(resource, IFolder.class);
		} else {
			return new AudioFile(resource);
		}
	}

	public class ResourceComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			// both are folders, sort alphabetically
			if (e1 instanceof IFolder && e2 instanceof IFolder) {
				return Comparator.comparing(IFolder::getName, String.CASE_INSENSITIVE_ORDER).compare((IFolder) e1,
						(IFolder) e2);
			}
			// one is audio file, one is folder, folder goes first
			if (!e1.getClass().equals(e2.getClass())) {
				if (e1 instanceof IFolder) {
					return -1;
				} else {
					return 1;
				}
			}

			// both are audio files, sort alphabetically
			if (e1 instanceof AudioFile && e2 instanceof AudioFile) {
				return Comparator.comparing(AudioFile::getName, String.CASE_INSENSITIVE_ORDER).compare((AudioFile) e1,
						(AudioFile) e2);
			}
			return 0;
		}
	}

	// TODO: Refactor this
	private Object[] createModel(IResource o) throws CoreException {
		Collection<Object> filesInSelectedDir = new ArrayList<Object>();
		IFolder parentFolder;
		if (!o.isAccessible() && !(o instanceof IProject)) {
			return null;
		}
		if (o instanceof IProject) {
			IProject project = Adapters.adapt(o, IProject.class);
			filesInSelectedDir = Arrays.asList(project.members()).stream().map(FileManagerEditor::resourceConverter)
					.collect(Collectors.toList());
		} else if (o instanceof IFolder) {
			parentFolder = Adapters.adapt(o, IFolder.class);

			filesInSelectedDir = Arrays.asList(parentFolder.members()).stream()
					.map(FileManagerEditor::resourceConverter).collect(Collectors.toList());

		} else if (o instanceof IFile) {
			parentFolder = Adapters.adapt(o.getParent(), IFolder.class);
			if (parentFolder == null) {
				IProject project = Adapters.adapt(o.getProject(), IProject.class);
				filesInSelectedDir = Arrays.asList(project.members()).stream().map(FileManagerEditor::resourceConverter)
						.collect(Collectors.toList());
			} else {
				filesInSelectedDir = Arrays.asList(parentFolder.members()).stream()
						.map(FileManagerEditor::resourceConverter).collect(Collectors.toList());
			}
		}

		return filesInSelectedDir.stream().toArray(Object[]::new);
	}

	@Focus
	public void setFocus() {
		// TODO: Remove?
	}

	@Override
	// TODO: Remove?
	public void addPropertyListener(IPropertyListener listener) {
	}

	@Override
	public void dispose() {
		viewer.getControl().dispose();
		workspace.removeResourceChangeListener(workspaceChangeListener);
	}

	@Override
	public String getTitle() {
		return titleName;
	}

	@Override
	public Image getTitleImage() {
		return null;
	}

	@Override
	public String getTitleToolTip() {
		if (inputContainer != null) {
			return inputContainer.getFullPath().toString();
		}
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
		// TODO: Return whether a file operation has occured in this directory
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
		try {
			if (input instanceof IContainerEditorInput) {
				setInputFile(((IContainerEditorInput) input).getContainer());
			} else if (input instanceof IFileEditorInput) {
				setInputFile(((IFileEditorInput) input).getFile().getParent());
			}
			this.model = createModel(inputContainer);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		setSite(site);
		// setInput is called in setInputFile
		createPartControl(site.getShell());

		workspace = ResourcesPlugin.getWorkspace();
		workspaceChangeListener = event -> {
			switch (event.getType()) {
			case IResourceChangeEvent.POST_CHANGE:
				this.refresh();
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
		actionBars.setGlobalActionHandler(ActionFactory.UNDO.getId(),
		undoAction);
		actionBars.setGlobalActionHandler(ActionFactory.REDO.getId(),
		redoAction);
		actionBars.updateActionBars();
		
		Activator.getDefault().initClipBoard(site.getWorkbenchWindow().getShell().getDisplay());
	}

	private void setInputFile(IContainer inputContainer) {
		this.inputContainer = inputContainer;
		setInput(new ContainerEditorInput(inputContainer));
	}

	public void delete() {
		DeleteResourceAction action = new DeleteResourceAction(getSite());
		action.selectionChanged(this.viewer.getStructuredSelection());
		action.run();
	}

	public void refresh() {
		if (this.viewer == null) {
			return;
		}
		try {
			this.model = createModel(inputContainer);
			viewer.getControl().getDisplay().asyncExec(() -> this.viewer.setInput(model));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public void setSelection(IStructuredSelection aSelection) {
		if (this.viewer != null) {
			this.viewer.setSelection(aSelection);
		}
	}

	public void copy() {
		List<IResource> filesToCopy = (List<IResource>) this.viewer.getStructuredSelection().toList().stream()
				.filter(selection -> selection instanceof IResource).collect(Collectors.toList());
		
		if (!filesToCopy.isEmpty()) {
			Activator.getDefault().getClipboard().copy(filesToCopy.toArray(new IResource[filesToCopy.size()]));	
		}
	}
	
	public void undo() {
		try {
			history.undo(undoContext, new NullProgressMonitor(), WorkspaceUndoUtil.getUIInfoAdapter(PlatformUI.getWorkbench().getDisplay().getActiveShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
	
	public void redo() {
		try {
			history.redo(undoContext, new NullProgressMonitor(), WorkspaceUndoUtil.getUIInfoAdapter(PlatformUI.getWorkbench().getDisplay().getActiveShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	public void paste() {
		Activator.getDefault().getClipboard().paste(inputContainer, PlatformUI.getWorkbench().getDisplay().getActiveShell(), history);
	}

}
