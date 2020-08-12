package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public abstract class ReversableViewerComparator extends ViewerComparator {
	
	private boolean reverse;

	public ReversableViewerComparator(boolean reverse) {
		this.reverse = reverse;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return reverse ? -1 * internalCompare(viewer,e1, e2) : internalCompare(viewer,e1, e2); 
	}

	protected abstract int internalCompare(Viewer viewer, Object e1, Object e2);

}
