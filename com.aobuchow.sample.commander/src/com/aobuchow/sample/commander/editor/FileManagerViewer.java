package com.aobuchow.sample.commander.editor;

import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Comparator;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

import com.aobuchow.sample.commander.Activator;
import com.aobuchow.sample.commander.Images;
import com.aobuchow.sample.commander.resources.AudioFile;

public class FileManagerViewer extends TableViewer {

	boolean sortReversed = false;
	private boolean autoPlayEnabled;
	private CellEditor nameCellEditor;
	private TableViewerColumn nameColumn;

	public FileManagerViewer(Composite parent) {
		super(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);

		this.setContentProvider(new FileManagerContentProvider(false));
		this.setComparator(new NameComparator(sortReversed));
		this.addSelectionChangedListener((event) -> {
			if (event.getStructuredSelection().size() == 1) {
				Object selection = event.getStructuredSelection().getFirstElement();
				if (selection instanceof AudioFile && autoPlayEnabled) {
					Activator.getDefault().getAudioPlayer().play((AudioFile) selection);
				}
			}
		});

		nameColumn = createColumnFor(this, Messages.FileManagerViewer_ColumnText_Name, 450);
		nameColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IResource) {
					if ((element instanceof IFolder || element instanceof IProject)
							&& element.equals(((IResource) FileManagerViewer.this.getInput()).getParent())) {
						return ".."; //$NON-NLS-1$
					}
					return ((IResource) element).getName();
				}
				return "null"; //$NON-NLS-1$
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof AudioFile) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.AUDIO_FILE).createImage();
				}
				if (element instanceof IContainer) {
					return Activator.imageDescriptorFromPlugin(Activator.PLUGIN_ID, Images.FOLDER).createImage();
				}
				return null;
			}
		});

		TableViewerColumn sizeColumn = createColumnFor(this, Messages.FileManagerViewer_ColumnText_Size, 100);
		sizeColumn.getColumn().setAlignment(SWT.RIGHT);
		sizeColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IFileStore fileStore;
				try {
					if (element instanceof IFile) {
						fileStore = org.eclipse.core.filesystem.EFS.getStore(((IFile) element).getLocationURI());
						return humanReadableByteCountSI(fileStore.fetchInfo().getLength());
					}

					if (element instanceof IContainer) {
						int numberOfItems = ((IContainer) element).members().length;
						String plural = numberOfItems != 0 ? "s" : ""; //$NON-NLS-1$ //$NON-NLS-2$
						return numberOfItems + " item" + plural; //$NON-NLS-1$
					}

				} catch (CoreException e) {
					e.printStackTrace();
				}
				return ""; //$NON-NLS-1$
			}
		});

		TableViewerColumn dateColumn = createColumnFor(this, Messages.FileManagerViewer_ColumnText_Date, 300);
		dateColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IResource) {
					if ((element instanceof IFolder || element instanceof IProject)
							&& element.equals(((IResource) FileManagerViewer.this.getInput()).getParent())) {
						// Don't show for parent
						return ""; //$NON-NLS-1$
					}

					SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a"); //$NON-NLS-1$
					return dateFormat.format(((IResource) element).getLocation().toFile().lastModified());
				}

				return ""; //$NON-NLS-1$
			}
		});

		Listener sortListener = new Listener() {
			public void handleEvent(Event e) {
				// TODO: This could probably be refactored?
				TableColumn column = (TableColumn) e.widget;
				// A column should always sort in normal order on first click
				if (Math.max(column.getText().indexOf('▲'), column.getText().indexOf('▼')) == -1) {
					sortReversed = false;
				}

				String direction = sortReversed ? "▲" : "▼"; //$NON-NLS-1$ //$NON-NLS-2$
				if (column == sizeColumn.getColumn()) {
					FileManagerViewer.this.setComparator(new SizeComparator(sortReversed));
					column.setText(Messages.FileManagerViewer_ColumnText_Size + " " + direction); //$NON-NLS-1$
					nameColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Name);
					dateColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Date);
				}
				if (column == nameColumn.getColumn()) {
					FileManagerViewer.this.setComparator(new NameComparator(sortReversed));
					column.setText(Messages.FileManagerViewer_ColumnText_Name + " " + direction); //$NON-NLS-1$
					dateColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Date);
					sizeColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Size);
				}
				if (column == dateColumn.getColumn()) {
					FileManagerViewer.this.setComparator(new DateComparator(sortReversed));
					column.setText(Messages.FileManagerViewer_ColumnText_Date + " " + direction); //$NON-NLS-1$
					nameColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Name);
					sizeColumn.getColumn().setText(Messages.FileManagerViewer_ColumnText_Size);
				}
				FileManagerViewer.this.update(FileManagerViewer.this, null);
				sortReversed = !sortReversed;
			}
		};

		sizeColumn.getColumn().addListener(SWT.Selection, sortListener);
		nameColumn.getColumn().addListener(SWT.Selection, sortListener);
		dateColumn.getColumn().addListener(SWT.Selection, sortListener);

		super.getTable().setLinesVisible(false);
		this.getTable().setHeaderVisible(true);

		setupColumnEditingSupport(nameColumn);

		initPreferenceListeners();
	}

	private void setupColumnEditingSupport(TableViewerColumn nameColumn) {
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(this) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
						// Disabled double click rename for folders as it interferes with navigation
						|| (event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
								&& ((ViewerCell) event.getSource()).getElement() instanceof IFile)
						|| event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
			}

		};

		// TODO: Tabbing doesn't work?
		int feature = ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION
				| ColumnViewerEditor.TABBING_CYCLE_IN_VIEWER;

		TableViewerEditor.create(this, actSupport, feature);

		nameCellEditor = new FileNameCellEditor((Composite) this.getControl());
		nameColumn.setEditingSupport(
				new TextGetSetEditingSupport<>(this, IResource.class, IResource::getName, (resource, newName) -> {
					if (!(resource instanceof IResource)) {
						return;
					}
					rename(resource, newName);
				}, nameCellEditor));
	}

	public void renameSelection() {
		IStructuredSelection sel = (IStructuredSelection) this.getSelection();
		if (sel.size() > 1) {
			return;
		}
		IResource selection = Adapters.adapt(sel.getFirstElement(), IResource.class);
		if (selection != null) {
			nameColumn.getViewer().editElement(selection, 0);
		}
	}

	private void rename(IResource resource, String newName) {
		IPath newPath = resource.getFullPath().removeLastSegments(1).append(newName);
		if (newPath.equals(resource.getFullPath())) {
			return;
		}

		MoveResourcesOperation op = new MoveResourcesOperation(resource, newPath,
				Messages.FileManagerViewer_RenameLabel6 + resource.getName());
		try {
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, new NullProgressMonitor(),
					WorkspaceUndoUtil.getUIInfoAdapter(this.getControl().getShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private void initPreferenceListeners() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(Activator.PREFERENCES_NODE_QUALIFER);
		autoPlayEnabled = preferences.getBoolean(AutoPlayToggleAction.AUTO_PLAY_PREFERENCE_KEY, true);
		preferences.addPreferenceChangeListener(new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				if (event.getKey() == AutoPlayToggleAction.AUTO_PLAY_PREFERENCE_KEY) {
					if (event.getNewValue().equals("false")) { //$NON-NLS-1$
						autoPlayEnabled = false;
					} else {
						autoPlayEnabled = true;
					}
				}
			}
		});
	}

	public boolean copy() {
		if (nameCellEditor.isActivated()) {
			((Text) nameCellEditor.getControl()).copy();
			return true;
		}
		return false;
	}

	public boolean cut() {
		if (nameCellEditor.isActivated()) {
			((Text) nameCellEditor.getControl()).cut();
			return true;
		}
		return false;
	}

	public boolean paste() {
		if (nameCellEditor.isActivated()) {
			((Text) nameCellEditor.getControl()).paste();
			return true;
		}
		return false;
	}

	public boolean delete() {
		if (nameCellEditor.isActivated()) {
			((Text) nameCellEditor.getControl()).clearSelection();
			return true;
		}
		return false;
	}

	public static String humanReadableByteCountSI(long bytes) {
		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B"; //$NON-NLS-1$
		}
		CharacterIterator ci = new StringCharacterIterator("kMGTPE"); //$NON-NLS-1$
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}
		return String.format("%.1f %cB", bytes / 1000.0, ci.current()); //$NON-NLS-1$
	}

	private TableViewerColumn createColumnFor(TableViewer viewer, String label, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(width);
		column.getColumn().setText(label);
		column.getColumn().setMoveable(true);
		return column;
	}

	// TODO: Refactor all of these by providing a method to run for the three
	// comparison cases?

	public class DateComparator extends ReversableViewerComparator {
		public DateComparator(boolean reverse) {
			super(reverse);
		}

		@Override
		protected int internalCompare(Viewer viewer, Object e1, Object e2) {
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

				long v1 = ((IFolder) e1).getLocation().toFile().lastModified();
				long v2 = ((IFolder) e2).getLocation().toFile().lastModified();
				return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
			}
			// one is audio file, one is folder/project, folder/project goes first
			if (!e1.getClass().equals(e2.getClass())) {
				if (e1 instanceof IContainer) {
					return -1;
				} else {
					return 1;
				}
			}

			// both are audio files, sort by size
			if (e1 instanceof AudioFile && e2 instanceof AudioFile) {
				long v1 = ((IFile) e1).getLocation().toFile().lastModified();
				long v2 = ((IFile) e2).getLocation().toFile().lastModified();
				return v1 < v2 ? -1 : v1 > v2 ? +1 : 0;
			}
			return 0;
		}
	}

	public class SizeComparator extends ReversableViewerComparator {
		public SizeComparator(boolean reverse) {
			super(reverse);
		}

		@Override
		public int internalCompare(Viewer viewer, Object e1, Object e2) {
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

				try {
					int v1 = ((IContainer) e1).members().length;
					int v2 = ((IContainer) e2).members().length;
					return v1 < v2 ? +1 : v1 > v2 ? -1 : 0;
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			// one is audio file, one is folder/project, folder/project goes first
			if (!e1.getClass().equals(e2.getClass())) {
				if (e1 instanceof IContainer) {
					return -1;
				} else {
					return 1;
				}
			}

			// both are audio files, sort by size
			if (e1 instanceof AudioFile && e2 instanceof AudioFile) {

				try {
					IFileStore fileStore1 = org.eclipse.core.filesystem.EFS.getStore(((AudioFile) e1).getLocationURI());
					IFileStore fileStore2 = org.eclipse.core.filesystem.EFS.getStore(((AudioFile) e2).getLocationURI());
					long v1 = fileStore1.fetchInfo().getLength();
					long v2 = fileStore2.fetchInfo().getLength();
					return v1 < v2 ? +1 : v1 > v2 ? -1 : 0;
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
			return 0;
		}
	}

	public class NameComparator extends ReversableViewerComparator {
		public NameComparator(boolean reverse) {
			super(reverse);
		}

		@Override
		public int internalCompare(Viewer viewer, Object e1, Object e2) {
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
