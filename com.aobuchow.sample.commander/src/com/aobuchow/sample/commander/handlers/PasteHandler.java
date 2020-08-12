package com.aobuchow.sample.commander.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import com.aobuchow.sample.commander.editor.FileManagerEditor;

public class PasteHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof FileManagerEditor) {
			FileManagerEditor editor = ((FileManagerEditor) part);
			// TODO: Paste shouldn't even be enabled when editor is in flat mode..
			if (!editor.isFlatMode()) {
				editor.paste();	
			}
			
		}
		return null;
	}

}
