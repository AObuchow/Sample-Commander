package com.aobuchow.sample.commander;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.CopyFilesAndFoldersOperation;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.UIJob;

public class FileClipBoard {
	Clipboard clipboard;

	public FileClipBoard(Display display) {
		clipboard = new Clipboard(display);
	}

	public void dispose() {
		clipboard.dispose();
	}

	public void copy(IResource[] resourcesToCopy) {
		clipboard.clearContents();
		IResource[] data = resourcesToCopy;
		// TODO: Do this async to avoid blocking?
		clipboard.setContents(new Object[] { data }, new ResourceTransfer[] { ResourceTransfer.getInstance() });
	}

	public void paste(IContainer container, Shell shell) {
		// TODO: Do this async to avoid blocking?
		ResourceTransfer resTransfer = ResourceTransfer.getInstance();
		IResource[] resourceData = (IResource[]) clipboard.getContents(resTransfer);

		if (resourceData != null && resourceData.length > 0) {
			// enablement should ensure that we always have access to a container
			// the container to hold the pasted resources.
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
			operation.copyResources(resourceData, container);
			clipboard.clearContents();
			return;
		}

		// try a file transfer
		FileTransfer fileTransfer = FileTransfer.getInstance();
		String[] fileData = (String[]) clipboard.getContents(fileTransfer);

		if (fileData != null) {
			// enablement should ensure that we always have access to a container
			CopyFilesAndFoldersOperation operation = new CopyFilesAndFoldersOperation(shell);
			operation.copyFiles(fileData, container);
		}
		clipboard.clearContents();
	}

}
