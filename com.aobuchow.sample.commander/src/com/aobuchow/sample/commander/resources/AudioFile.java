package com.aobuchow.sample.commander.resources;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.ui.PlatformUI;

public class AudioFile {
	
	IResource resource;
	
	public AudioFile(IResource resource) {
		this.resource = resource;
	}
	
	public String getName() {
		return resource.getName();
	}

}
