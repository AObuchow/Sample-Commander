package com.aobuchow.sample.commander.parts;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.aobuchow.sample.commander.resources.AudioFile;

// TODO: Rename to FileMangerEditor
public class FileManagerView extends EditorPart implements IEditorPart, ISelectionListener  {

	private TableViewer viewer;
	private Object[] model;
	private String titleName = null;
	private IFile inputFile;

	@PostConstruct
	@Override
	public void createPartControl(Composite parent) {

		viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setComparator(new ResourceComparator());
		TableViewerColumn column = createColumnFor(viewer, "File");
		column.setLabelProvider(new ColumnLabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof AudioFile) {
					return ((AudioFile) element).getName();	
				}
				if (element instanceof IFolder) {
					return "/" + ((IFolder) element).getName() + "/";
				}
				return "null";
			}

		});
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.setInput(model);

		
		this.titleName = inputFile.getParent().getName();
		this.setPartName(titleName);
		super.firePropertyChange(PROP_TITLE);
		
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
				return Comparator.comparing(IFolder::getName, String.CASE_INSENSITIVE_ORDER).compare((IFolder) e1, (IFolder) e2);
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
				return Comparator.comparing(AudioFile::getName, String.CASE_INSENSITIVE_ORDER).compare((AudioFile) e1, (AudioFile) e2);
			}
			return 0;
		}
	}

	// TODO: Refactor this 
	private Object[] createModel(IResource o) throws CoreException {
		Collection<Object> filesInSelectedDir = null;
		IFolder parentFolder;
		if (!o.isAccessible()) {
			return null;
		}
		if (o instanceof IProject) {
			IProject project = Adapters.adapt(o, IProject.class);
			filesInSelectedDir = Arrays.asList(project.members()).stream().map(FileManagerView::resourceConverter)
					.collect(Collectors.toList());
		} else if (o instanceof IFolder) {
			parentFolder = Adapters.adapt(o, IFolder.class);
		
				filesInSelectedDir = Arrays.asList(parentFolder.members()).stream().map(FileManagerView::resourceConverter)
						.collect(Collectors.toList());				
			

		} else if (o instanceof IFile) {
			parentFolder = Adapters.adapt(o.getParent(), IFolder.class);
			if (parentFolder == null) {
				IProject project = Adapters.adapt(o.getProject(), IProject.class);
				filesInSelectedDir = Arrays.asList(project.members()).stream().map(FileManagerView::resourceConverter)
						.collect(Collectors.toList());
			} else {
				filesInSelectedDir = Arrays.asList(parentFolder.members()).stream().map(FileManagerView::resourceConverter)
						.collect(Collectors.toList());
			}
		}

		return filesInSelectedDir.stream().toArray(Object[]::new);

	}

	@Focus
	public void setFocus() {

	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not mix
	 * E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and you
	 * do not receive a ISelection
	 * 
	 * @param s the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s == null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example we
	 * listen to a single Object (even the ISelection already captured in E3 mode).
	 * <br/>
	 * You should change the parameter type of your received Object to manage your
	 * specific selection
	 * 
	 * @param o : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {
		// Test if viewer exists (inject methods are called before PostConstruct)
		if (viewer != null && o instanceof IResource) {
			if (viewer.getControl().isDisposed()) {
				createPartControl(viewer.getControl().getParent());
			}
			try {
				viewer.setInput(createModel((IResource) o));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage your
	 * specific selection
	 * 
	 * @param o : the current array of objects received in case of multiple
	 *          selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {
		// Test if label exists (inject methods are called before PostConstruct)
		if (viewer != null) {

		}
	}

	@Override
	public void addPropertyListener(IPropertyListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		viewer.getControl().dispose();
		
	}


	@Override
	public String getTitle() {
		return titleName;
	}

	@Override
	public Image getTitleImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitleToolTip() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removePropertyListener(IPropertyListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveOnCloseNeeded() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IEditorInput getEditorInput() {
		return super.getEditorInput();
	}

	@Override
	public IEditorSite getEditorSite() {
		return  this.getSite();
	}
	
	@Override
	public IEditorSite getSite() {
		return (IEditorSite) super.getSite();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		inputFile = null;
		try {
			inputFile = ((IFileEditorInput) input).getFile();
			this.model = createModel(inputFile);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		createPartControl(site.getShell());
		setSite(site);
		setInput(input);



		//PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().addSelectionListener(this);

		
		//this.setSelection(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection());
		
	}
	

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		this.setSelection(selection);
		
	}
}
