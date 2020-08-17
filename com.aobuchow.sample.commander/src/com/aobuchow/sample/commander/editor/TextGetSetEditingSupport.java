/*******************************************************************************
 * Copyright (c) 2019 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Bug 548344
 *     IBM Corporation - Bug 548557 - Warnings in I20190621-1800 for debug.ui
 *     Andrew Obuchowicz - Use in Sample Commander
 *     
// *******************************************************************************/

package com.aobuchow.sample.commander.editor;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.swt.widgets.Composite;

public class TextGetSetEditingSupport<T> extends EditingSupport {
	private Function<T, String> getter;
	private BiConsumer<T, String> setter;
	private ColumnViewer viewer;
	private CellEditor cellEditor;
	private Class<T> clazz;

	public TextGetSetEditingSupport(ColumnViewer viewer, Class<T> clazz, Function<T, String> getter, BiConsumer<T, String> setter,
			CellEditor cellEditor) {
		super(viewer);
		this.viewer = viewer;
		this.getter = getter;
		this.setter = setter;
		this.cellEditor = cellEditor;
		this.clazz = clazz;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return cellEditor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return clazz.isAssignableFrom(element.getClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object getValue(Object element) {
		return getter.apply((T) element);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void setValue(Object element, Object value) {
		setter.accept((T) element, (String) value);
		viewer.update(element, null);
	}

}
