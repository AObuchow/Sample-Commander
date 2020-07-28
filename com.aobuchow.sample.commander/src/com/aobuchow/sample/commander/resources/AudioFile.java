package com.aobuchow.sample.commander.resources;


import org.eclipse.core.internal.resources.File;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IResource;

public class AudioFile extends File{
	
	IResource resource;
	
	public AudioFile(IResource resource) {
		super(resource.getFullPath(), (Workspace) resource.getWorkspace());
		this.resource = resource;
	}
	
	
	public String getName() {
		return resource.getName();
	}

}
