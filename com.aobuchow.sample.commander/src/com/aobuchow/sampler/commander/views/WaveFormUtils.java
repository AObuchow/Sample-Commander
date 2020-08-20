package com.aobuchow.sampler.commander.views;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class WaveFormUtils {
	
	//private static final double WAVEFORM_HEIGHT_COEFFICIENT = 1.3; // This fits the waveform to the swing node height
	
	public static int[] getWavAmplitudes(File file, double waveformHeightCoefficient) throws UnsupportedAudioFileException , IOException {
		//Get Audio input stream
		try (AudioInputStream input = AudioSystem.getAudioInputStream(file)) {
			AudioFormat baseFormat = input.getFormat();
			
			//Encoding
			Encoding encoding = AudioFormat.Encoding.PCM_UNSIGNED;
			float sampleRate = baseFormat.getSampleRate();
			int numChannels = baseFormat.getChannels();
			
			AudioFormat decodedFormat = new AudioFormat(encoding, sampleRate, 16, numChannels, numChannels * 2, sampleRate, false);
			int available = input.available();
			
			//Get the PCM Decoded Audio Input Stream
			try (AudioInputStream pcmDecodedInput = AudioSystem.getAudioInputStream(decodedFormat, input)) {
				final int BUFFER_SIZE = 4096; //this is actually bytes
				
				//Create a buffer
				byte[] buffer = new byte[BUFFER_SIZE];
				
				//Now get the average to a smaller array
				int maximumArrayLength = 16384;
				int[] finalAmplitudes = new int[maximumArrayLength];
				int samplesPerPixel =  available / maximumArrayLength;
				if (samplesPerPixel == 0) {
					samplesPerPixel = 1;
				}
				
				//Variables to calculate finalAmplitudes array
				int currentSampleCounter = 0;
				int arrayCellPosition = 0;
				float currentCellValue = 0.0f;
				
				//Variables for the loop
				float amplitude = 0f;
				
				//Read all the available data on chunks
				while (pcmDecodedInput.readNBytes(buffer, 0, BUFFER_SIZE) > 0)
					for (int i = 0; i < buffer.length - 1; i += 2) {
						
						// Calculate the value
						amplitude = (float) (((double) (((buffer[i + 1] << 8) | buffer[i] & 0xff) << 16) / 32767)
								* waveformHeightCoefficient);

						//Tricker
						if (currentSampleCounter != samplesPerPixel) {
							++currentSampleCounter;
							currentCellValue += Math.abs(amplitude);
						} else {
							//Avoid ArrayIndexOutOfBoundsException
							if (arrayCellPosition != maximumArrayLength) {
								finalAmplitudes[arrayCellPosition] = finalAmplitudes[arrayCellPosition + 1] = (int) (currentCellValue / samplesPerPixel);
							}
							
							//Fix the variables
							currentSampleCounter = 0;
							currentCellValue = 0;
							arrayCellPosition += 2;
						}
					}
				
				return finalAmplitudes;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			
		}
		
		//You don't want this to reach here...
		return new int[1];
	}


	public static float[] processAmplitudes(int[] sourcePcmData, int width) {
		
		//The width of the resulting waveform panel
		float[] waveData = new float[width];
		int samplesPerPixel = sourcePcmData.length / width;
		
		//Calculate
		float nValue;
		for (int w = 0; w < width; w++) {
			
			//For performance keep it here
			int c = w * samplesPerPixel;
			nValue = 0.0f;
			
			//Keep going
			for (int s = 0; s < samplesPerPixel; s++) {
				nValue += ( Math.abs(sourcePcmData[c + s]) / 65536.0f );
			}
			
			//Set WaveData
			waveData[w] = nValue / samplesPerPixel;
		}
		
		return waveData;
	}

}
