package com.aobuchow.sampler.commander.views;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AudioFileView {
	private Canvas canva;

	@PostConstruct
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		canva = new Canvas(parent, SWT.BORDER);
		canva.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	@Focus
	public void setFocus() {
		canva.setFocus();
	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not mix
	 * E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and you
	 * do not receive a ISelection
	 * 
	 * @param s the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s == null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example we
	 * listen to a single Object (even the ISelection already captured in E3 mode).
	 * <br/>
	 * You should change the parameter type of your received Object to manage your
	 * specific selection
	 * 
	 * @param o : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {

		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

		if (canva != null && o instanceof IFile) {

			IFile file = (IFile) o;
			if (file.getFileExtension().equals("wav")) {

				// Draw waveform
				try {
					int[] waveformData = WaveFormUtils.getWavAmplitudes(file.getLocation().toFile(), 1);
					float[] waveFormAmplitudePoints = WaveFormUtils.processAmplitudes(waveformData,
							canva.getBounds().width * 2);
					paintWaveForm(waveFormAmplitudePoints);
				} catch (UnsupportedAudioFileException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// canva.setBackground(new Color(canva.getParent().getDisplay(), new RGB(0, 0,
			// 255)));
		}

	}

	public void paintWaveForm(float[] waveData) {

		// Draw a Background Rectangle
		GC gc = new GC(canva);
		gc.setForeground(new Color(canva.getDisplay(), new RGB(0, 0, 0)));
		gc.setBackground(new Color(canva.getDisplay(), new RGB(0, 0, 0)));
		gc.fillRectangle(0, 0, canva.getBounds().width, canva.getBounds().height);
		int height = canva.getBounds().height;

		// Draw the waveform
		gc.setForeground(new Color(canva.getDisplay(), new RGB(255, 255, 255)));
		if (waveData != null)
			for (int i = 0; i < waveData.length; i++) {
				int value = (int) (waveData[i] * height);
				int y1 = (height - 2 * value) / 2;
				int y2 = y1 + 2 * value;
				gc.drawLine(i, y1, i, y2);
			}
		gc.dispose();

	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage your
	 * specific selection
	 * 
	 * @param o : the current array of objects received in case of multiple
	 *          selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

	}
}
