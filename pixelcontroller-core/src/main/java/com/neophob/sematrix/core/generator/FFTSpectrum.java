/**
 * Copyright (C) 2011-2013 Michael Vogt <michu@neophob.com>
 *
 * This file is part of PixelController.
 *
 * PixelController is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PixelController is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PixelController.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.neophob.sematrix.core.generator;

import com.neophob.sematrix.core.glue.MatrixData;
import com.neophob.sematrix.core.resize.Resize.ResizeName;
import com.neophob.sematrix.core.sound.ISound;
import com.neophob.sematrix.core.sound.SoundMinim;

import ddf.minim.*;
import ddf.minim.analysis.*;


import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * idea ripped from http://www.macetech.com/blog/
 * 
 * @author mvogt
 * 
 */
public class FFTSpectrum extends Generator {

	/** The sound. */
	private ISound sound;
	
	/** The fft smooth. */
	private float[] fftSmooth;
	
	/** The y block. */
	private int yBlock;

	private static final Logger LOG = Logger.getLogger(SoundMinim.class.getName());
	
	/**
	 * Instantiates a new fFT spectrum.
	 *
	 * @param matrix the matrix
	 * @param sound the sound
	 */
	public FFTSpectrum(MatrixData matrix, ISound sound) {
		super(matrix, GeneratorName.FFT, ResizeName.PIXEL_RESIZE);
		this.sound = sound;
		
		int bands = sound.getFftAvg();
		fftSmooth = new float[bands];
		yBlock = this.internalBufferYSize / bands;
	}


	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.generator.Generator#update()
	 */
	@Override
	public void update() {
		int avg = sound.getFftAvg(); // does fft.forward and return avgSize = 32 (bands)
		
		for (int i = 0; i < avg; i++) {

			fftSmooth[i] = 0.3f * fftSmooth[i] + 0.7f * sound.getFftAvg(i);
		    int h = (int)(Math.log(fftSmooth[i]*3.0f)*30);

		    h=255+h;
		    if (h>255) {
		    	h=255;
		    }
		    h = h*h/255;
//			rect(col,x1,  y1,           x2,                     y2 )
//		    rect(h, 0, i*yBlock, this.internalBufferXSize, i*yBlock+yBlock);
//			rect(h, 0, 1, 200, 20);

		}

		for (int i=0; i<avg; i++) {
			int col = 20;
			int h = (int)(Math.log(fftSmooth[i]*3.0f)*30);

			h=255+h;
			if (h>255) {
				h=255;
			}
			h = h*h/255;
			fftSmooth[i] = 0.3f * fftSmooth[i] + 0.7f * sound.getFftAvg(i);
//			LOG.log(Level.INFO, "fftSmooth: "+fftSmooth[i]);
//			float x = map(i, 0, avg, 0, this.internalBufferXSize);
//			float y = map(fftSmooth[i]*this.internalBufferYSize, -1, 1, 0, this.internalBufferYSize);
//			rect(x+spacing, height, width/grid-2*spacing, -y);
            int x = (int)this.internalBufferXSize/avg;
            float currentband = sound.getBand(i);
			rect2(i*x ,0, i*x+x, (int)currentband, 50);
		}
//        rect2(0,0,200,32,50);
		// See https://lernprocessing.wordpress.com/2012/06/18/minim-audio-analyse/
		// Breite der Rechtecke berechnen
//		for (int i=0; i < fftR.avgSize(); i+=fftR.avgSize()/grid) {
//			float x = map(i, 0, fftR.avgSize(), 0, width);
//			float y = map(fftR.getAvg(i)*yScale, 0, 100, 0, height/5) ;
//
//			// Rechteck zeichnen
//			rect(x+spacing, height, width/grid-2*spacing, -y);
//		}

		// Schlussfolgeumsetzung
		// Anzahl der Peaks
//		int grid=32;
//		for (int i=0; i<avg; i+=avg/grid) {
//			fftSmooth[i] = 0.3f * fftSmooth[i] + 0.7f * sound.getFftAvg(i);
//			int h = (int)(Math.log(fftSmooth[i]*3.0f)*30);
//
//			h=255+h;
//			if (h>255) {
//				h=255;
//			}
//			h = h*h/255;
//			// Rechteck zeichnen
//			rect(h, 0, i*yBlock, this.internalBufferXSize, (int)(Math.floor(sound.getFftAvg(i)*this.internalBufferYSize)));
//		}

	}
	/**
	 * draw rectangle in buffer.
	 *
	 * @param xofs the xofs
	 * @param yofs the yofs
	 * @param xsize the xsize
	 * @param ysize the ysize
	 * @param col the col
	 */
	private void rect2(int xofs, int yofs, int xsize, int ysize, int col) {
		if (ysize+yofs>internalBufferYSize) {
			ysize=ysize+yofs-internalBufferYSize;
		}
		if (xsize+xofs>internalBufferXSize) {
			xsize=xsize+xofs-internalBufferXSize;
		}

		for (int y=0; y<ysize; y++) {
			int ofs=(yofs+y)*internalBufferXSize+xofs;
			for (int x=0; x<xsize; x++) {
				this.internalBuffer[ofs++] = col;
			}
		}
	}
	
	/**
	 * Rect.
	 *
	 * @param col the col
	 * @param x1 the x1
	 * @param y1 the y1
	 * @param x2 the x2
	 * @param y2 the y2
	 */
	private void rect(int col, int x1, int y1, int x2, int y2) {
		int ofs;
		for (int y=y1; y<y2; y++) {
			ofs = y*this.internalBufferXSize;
			for (int x=x1; x<x2; x++) {		
				this.internalBuffer[ofs++] = col;
			}
		}
	}

	
}
