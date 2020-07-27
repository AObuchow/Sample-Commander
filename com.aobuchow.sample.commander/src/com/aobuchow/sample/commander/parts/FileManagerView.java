package com.aobuchow.sample.commander.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.aobuchow.sample.commander.resources.AudioFile;

// TODO: Rename to FileMangerEditor
public class FileManagerView extends EditorPart implements IEditorPart {

	private TableViewer viewer;
	private Object[] model;
	private String titleName = null;
	private IFile inputFile;

	@PostConstruct
	@Override
	public void createPartControl(Composite parent) {
		this.viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
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
					return "/" + ((IFolder) element).getName() + "/";
				}
				return "null";
			}
		});
		this.viewer.getTable().setLinesVisible(true);
		this.viewer.getTable().setHeaderVisible(true);
		this.viewer.setInput(model);

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
				filesInSelectedDir = Arrays.asList(parentFolder.members()).stream()
						.map(FileManagerView::resourceConverter).collect(Collectors.toList());
			}
		}

		return filesInSelectedDir.stream().toArray(Object[]::new);
	}

	@Focus
	public void setFocus() {

	}

	@Override
	public void addPropertyListener(IPropertyListener listener) {
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
		return null;
	}

	@Override
	public String getTitleToolTip() {
		// TODO: Return full path of the folder
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
		inputFile = null;
		try {
			inputFile = ((IFileEditorInput) input).getFile();
			this.model = createModel(inputFile);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		createPartControl(site.getShell());
		setSite(site);
		setInput(input);
	}

}
