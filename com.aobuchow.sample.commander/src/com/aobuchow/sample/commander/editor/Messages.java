package com.aobuchow.sample.commander.editor;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.aobuchow.sample.commander.editor.messages"; //$NON-NLS-1$
	public static String AutoPlayToggleAction_Text;
	public static String FileManagerViewer_ColumnText_Date;
	public static String FileManagerViewer_ColumnText_Name;
	public static String FileManagerViewer_ColumnText_Size;
	public static String FlatDirectoryLayoutAction_Text;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
