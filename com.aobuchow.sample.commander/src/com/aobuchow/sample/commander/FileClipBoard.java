package com.aobuchow.sample.commander;

import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.part.ResourceTransfer;

public class FileClipBoard {
	Clipboard clipboard;

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
	}
	


	public void paste(IContainer container, Shell shell, IOperationHistory history) {
		// TODO: Do this async to avoid blocking?
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) clipboard.getContents(resTransfer);
	
		CopyFilesAndFoldersOperation op  = new CopyFilesAndFoldersOperation(shell); 
		op.copyResources(resourceData, container);
		
		clipboard.clearContents();
	}

}
