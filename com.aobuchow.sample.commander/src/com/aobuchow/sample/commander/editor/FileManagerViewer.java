package com.aobuchow.sample.commander.editor;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Comparator;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.resources.AudioFile;

public class FileManagerViewer extends TableViewer {

	public FileManagerViewer(Composite parent) {
		super(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		this.setContentProvider(new FileManagerContentProvider());
		this.setComparator(new ResourceComparator());
		TableViewerColumn nameColumn = createColumnFor(this, "File");
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IResource) {
					if ((element instanceof IFolder || element instanceof IProject)
							&& element.equals(((IResource) FileManagerViewer.this.getInput()).getParent())) {
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

		TableViewerColumn sizeColumn = createColumnFor(this, "Size");
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

		this.addSelectionChangedListener((event) -> {
			if (event.getStructuredSelection().size() == 1) {
				Object selection = event.getStructuredSelection().getFirstElement();
				if (selection instanceof AudioFile) {
					Activator.getDefault().getAudioPlayer().play((AudioFile) selection);
				}
			}
		});

		super.getTable().setLinesVisible(false);
		this.getTable().setHeaderVisible(true);
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

	public class ResourceComparator extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			// both are folders/projects, projects go first otherwise sort alphabetically
			if (e1 instanceof IContainer && e2 instanceof IContainer) {
				// the parent directory (..) should always be at the top
				if (((IContainer) e1).equals(((IContainer) FileManagerViewer.this.getInput()).getParent())) {
					return -1;
				}
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

}
