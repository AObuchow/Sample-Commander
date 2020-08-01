package com.aobuchow.sample.commander.parts;

import org.eclipse.core.resources.IContainer;
import org.eclipse.ui.IStorageEditorInput;

public interface IContainerEditorInput extends IStorageEditorInput {
	
	/**
	 * Returns the container resource underlying this editor input.
	 * <p>
	 * The <code>IContainer</code> returned can be a handle to a resource
	 * that does not exist in the workspace. As such, an editor should
	 * provide appropriate feedback to the user instead of simply failing
	 * during input validation. For example, a text editor could open
	 * in read-only mode with a message in the text area to inform the
	 * user that the container does not exist.
	 * </p>
	 *
	 * @return the underlying container
	 */
	public IContainer getContainer();

}
