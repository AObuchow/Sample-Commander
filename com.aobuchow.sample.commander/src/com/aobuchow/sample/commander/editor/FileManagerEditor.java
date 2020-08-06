package com.aobuchow.sample.commander.editor;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import com.aobuchow.sample.commander.resources.AudioFile;

public class FileManagerEditor extends EditorPart implements IEditorPart {

	private TableViewer viewer;
	private Object[] model;
	private IContainer inputContainer = null;
	private IWorkspace workspace;
	private IResourceChangeListener workspaceChangeListener;
	private IOperationHistory history;
	private IUndoContext undoContext;
	private Map<IContainer, IResource> visitedDirectories = new HashMap<IContainer, IResource>();

	@PostConstruct
	@Override
	public void createPartControl(Composite parent) {
		// TODO: Extract the class to a viewer?
		this.viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		this.viewer.setContentProvider(ArrayContentProvider.getInstance());
		this.viewer.setComparator(new ResourceComparator());
		TableViewerColumn nameColumn = createColumnFor(viewer, "File");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IResource) {
					if ((element instanceof IFolder || element instanceof IProject)
							&& element.equals(inputContainer.getParent())) {
						return "..";
					}
					return ((IResource) element).getName();
				}
				return "null";
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof AudioFile) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/AudioFile3_16.png")
							.createImage();
				}
				if (element instanceof IContainer) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/folder.png").createImage();
				}
				return null;
			}
		});
		
		TableViewerColumn sizeColumn = createColumnFor(viewer, "Size");
		sizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {

				IFileStore fileStore;
				try {
					if (element instanceof IFile) {
						fileStore = org.eclipse.core.filesystem.EFS.getStore(((IFile) element).getLocationURI());
						return humanReadableByteCountSI(fileStore.fetchInfo().getLength());
					}

				} catch (CoreException e) {
					e.printStackTrace();
				}
				return "";
			}
		});
		
		viewer.getControl().addKeyListener( new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
		
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				// e.doit gets disabled regardless if successful as the beep it creates on Linux
				// is annoying
				switch(e.keyCode) {
				case SWT.ARROW_RIGHT:
					IStructuredSelection selection = viewer.getStructuredSelection();
					if (selection.size() == 1 && selection.getFirstElement() instanceof IContainer) {
						changeActiveContainer(Adapters.adapt(selection.getFirstElement(), IContainer.class));
					}
					e.doit = false;
					break;
				case SWT.ARROW_LEFT:
					if (!(inputContainer instanceof IProject)) {
						changeActiveContainer(inputContainer.getParent());
					}
					e.doit = false;
					break;
				}
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
		this.viewer.getTable().setLinesVisible(false);
		this.viewer.getTable().setHeaderVisible(true);
		this.viewer.setInput(model);

		this.setPartName(inputContainer.getName());
	}
	
	public static String humanReadableByteCountSI(long bytes) {
	    if (-1000 < bytes && bytes < 1000) {
	        return bytes + " B";
	    }
	    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
	    while (bytes <= -999_950 || bytes >= 999_950) {
	        bytes /= 1000;
	        ci.next();
	    }
	    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}

	private TableViewerColumn createColumnFor(TableViewer viewer, String label) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(400);
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
			// both are folders/projects, projects go first otherwise sort alphabetically
			if (e1 instanceof IContainer && e2 instanceof IContainer) {
				if (!e1.getClass().equals(e2.getClass())) {
					if (e1 instanceof IProject) {
						return -1;
					} else {
						return 1;
					}
				}
				return Comparator.comparing(IContainer::getName, String.CASE_INSENSITIVE_ORDER).compare((IContainer) e1,
						(IContainer) e2);
			}
			// one is audio file, one is folder/project, folder/project goes first
			if (!e1.getClass().equals(e2.getClass())) {
				if (e1 instanceof IContainer) {
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
		IFolder parentFolder = null;
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

		if (parentFolder != null) {
			filesInSelectedDir.add(parentFolder.getParent());
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
		Activator.getDefault().getClipboard().dispose();
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
		// this.setPartName(inputContainer);
	}

	public void delete() {
		DeleteResourceAction action = new DeleteResourceAction(getSite());
		action.selectionChanged(this.viewer.getStructuredSelection());
		action.run();
	}

	// TODO: Always call this in setInputFile?
	public void refresh() {
		if (this.viewer == null) {
			return;
		}
		try {
			this.model = createModel(inputContainer);
			viewer.getControl().getDisplay().syncExec(() -> this.viewer.setInput(model));
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private void changeActiveContainer(IContainer container) {
		IResource currentSelection = (IResource) this.viewer.getStructuredSelection().getFirstElement();
		if (currentSelection != null) {
			visitedDirectories.put(inputContainer, currentSelection);	
		}
		setInputFile(container);
		refresh();
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

}
