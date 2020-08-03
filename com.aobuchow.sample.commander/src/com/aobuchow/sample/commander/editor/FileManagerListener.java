package com.aobuchow.sample.commander.editor;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.ui.part.ResourceTransfer;

public class FileManagerListener implements DragSourceListener {
	
    private final TableViewer viewer;

    public FileManagerListener(TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dragFinished(DragSourceEvent event) {
    	// Nothing to do
    }

    @Override
    public void dragSetData(DragSourceEvent event) {
        IStructuredSelection selection = viewer.getStructuredSelection(); 
        List<IResource> resourceList = (List<IResource>) selection.toList().stream().filter(sel -> sel instanceof IResource).collect(Collectors.toList());

        if (ResourceTransfer.getInstance().isSupportedType(event.dataType)) {
            event.data = resourceList.toArray(new IResource[0]);;
        }

    }

    @Override
    public void dragStart(DragSourceEvent event) {
    	// Nothing to do
    }

}
