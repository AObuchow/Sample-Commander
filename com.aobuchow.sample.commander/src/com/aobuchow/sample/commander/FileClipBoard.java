package com.aobuchow.sample.commander;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.ide.undo.MoveResourcesOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.part.ResourceTransfer;

public class FileClipBoard {
	Clipboard clipboard;
	boolean doCut = false;

	public FileClipBoard(Display display) {
		clipboard = new Clipboard(display);
	}

	public void dispose() {
		if (!clipboard.isDisposed()) {
			clipboard.dispose();	
		}
	}

	public void copy(IResource[] resourcesToCopy) {
		clipboard.clearContents();
		IResource[] data = resourcesToCopy;
		// TODO: Do this async to avoid blocking?
		clipboard.setContents(new Object[] { data }, new ResourceTransfer[] { ResourceTransfer.getInstance() });
		doCut = false;
	}
	
	public void cut(IResource[] resourcesToCut) {
		copy(resourcesToCut);
		doCut = true;
	}

	public IResource paste(IContainer destination, Shell shell) {
		// TODO: Do this async to avoid blocking?
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) clipboard.getContents(resTransfer);
		if (resourceData == null) {
			return null;
		}
	
		if (doCut) {
			MoveResourcesOperation op = new MoveResourcesOperation(resourceData, destination.getFullPath(), "Cut file"+ (resourceData.length > 1 ? "s" : ""));
	    	try {
				PlatformUI.getWorkbench().getOperationSupport().getOperationHistory().execute(op, new NullProgressMonitor(), WorkspaceUndoUtil.getUIInfoAdapter(shell));
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} else {
			CopyFilesAndFoldersOperation op  = new CopyFilesAndFoldersOperation(shell); 
			op.copyResources(resourceData, destination);			
		}

		clipboard.clearContents();
		return resourceData[0];
	}

}
