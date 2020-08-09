package com.aobuchow.sample.commander.editor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;

// Taken mainly from https://www.vogella.com/tutorials/EclipseDragAndDrop/article.html
public class FileManagerDropListener extends ViewerDropAdapter {

	private final FileManagerEditor editor;
	private IContainer targetContainer = null;

	public FileManagerDropListener(FileManagerEditor editor) {
		super(editor.getViewer());
		this.editor = editor;
	}

	@Override
	public void drop(DropTargetEvent event) {
		int location = this.determineLocation(event);
		IResource target = (IResource) determineTarget(event);
		switch (location) {
		case 1:
		case 2:
		case 4:
			targetContainer = editor.getContainer();
			break;
		case 3:
			if (target instanceof IContainer) {
				targetContainer = (IContainer) target;
			}
			break;

		}
		super.drop(event);
	}

	@Override
	public boolean performDrop(Object data) {
		IResource[] resourcesToMove = (IResource[]) data;
		MoveResourcesOperation op = new MoveResourcesOperation(resourcesToMove, targetContainer.getFullPath(),
				"Move file" + (resourcesToMove.length > 1 ? "s" : ""));
		try {
			PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, new NullProgressMonitor(),
					WorkspaceUndoUtil.getUIInfoAdapter(editor.getSite().getShell()));
		} catch (ExecutionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean validateDrop(Object target, int operation, TransferData transferType) {
		// TODO: Should probably implement this...
		return true;
	}

}
