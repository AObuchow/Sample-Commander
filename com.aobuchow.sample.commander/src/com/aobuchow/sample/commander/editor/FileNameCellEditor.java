package com.aobuchow.sample.commander.editor;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.widgets.Composite;

public class FileNameCellEditor extends TextCellEditor {
	
	public FileNameCellEditor(Composite parent) {
		super(parent);
	}

	@Override
	protected void doSetFocus() {
		super.doSetFocus();
		int index = text.getText().lastIndexOf('.');
		text.setSelection(0, index > -1 ? index : text.getText().length());
	}

}
