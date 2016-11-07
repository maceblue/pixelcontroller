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
package com.neophob.sematrix.core.sound;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import ddf.minim.AudioInput;
import ddf.minim.AudioPlayer;
import ddf.minim.Minim;
import ddf.minim.analysis.BeatDetect;
import ddf.minim.analysis.FFT;

import com.neophob.sematrix.core.glue.FileUtils;

/**
 * The Class SoundMinim.
 */
public final class SoundMinim implements ISound, Runnable {

	//samples per 1/4s
	/** The Constant SOUND_BUFFER_RESOLUTION. */
	private static final int SOUND_BUFFER_RESOLUTION = 8;

	/** The log. */
	private static final Logger LOG = Logger.getLogger(SoundMinim.class.getName());

	/** The minim. */
	private Minim minim;
	
	/** The in. */
	private AudioInput in;

	private AudioPlayer input;
	
	/** The beat. */
	private BeatDetect beat;

	private FileUtils fileUtils;
	
	/** The bl. */
	@SuppressWarnings("unused")
	private BeatListener bl;

	/** The fft. */
	private FFT fft;

	/* thread to collect volume information */
	/** The runner. */
	private Thread runner;

	/** The snd volume max. */
	private float sndVolumeMax=0;

	private float silenceThreshold;
	private long dropedVolumeRequests;
	private static final String SOUNDFILE = "Kalimba.mp3";
	
	/**
	 * Instantiates a new sound minim.
	 */
	public SoundMinim(float silenceThreshold) {
		minim = new Minim(this);
		//in = minim.getLineIn( Minim.STEREO, 512 );
		in = minim.getLineIn( Minim.MONO, 1024 );

//		input = minim.loadFile("http://mp3stream1.apasf.apa.at:8000");
		fileUtils = new FileUtils();
		String fileToLoad = fileUtils.getDataDir()+ File.separator+SOUNDFILE;
		input = minim.loadFile(fileToLoad);
		input.play();

		// a beat detection object that is FREQ_ENERGY mode that 
		// expects buffers the length of song's buffer size
		// and samples captured at songs's sample rate
		beat = new BeatDetect(in.bufferSize(), in.sampleRate());

		// set the sensitivity to 300 milliseconds
		// After a beat has been detected, the algorithm will wait for 300 milliseconds 
		// before allowing another beat to be reported. You can use this to dampen the 
		// algorithm if it is giving too many false-positives. The default value is 10, 
		// which is essentially no damping. If you try to set the sensitivity to a negative value, 
		// an error will be reported and it will be set to 10 instead. 
		beat.setSensitivity(250); 
		beat.detectMode(BeatDetect.FREQ_ENERGY);

		bl = new BeatListener(beat, in);		 

//		fft = new FFT(in.bufferSize(), in.sampleRate());
		fft = new FFT(input.bufferSize(), input.sampleRate());
		fft.window(FFT.HAMMING);
		fft.logAverages(120,4); // 32 bands
//		fft.logAverages(8,1); // ?? bands

		this.silenceThreshold = silenceThreshold;
		
		this.runner = new Thread(this);
		this.runner.setName("ZZ Sound stuff");
		this.runner.setDaemon(true);
		this.runner.start();
	}

	/**
	 * Minim requirement
	 * @param fileName
	 * @return
	 */
	public String sketchPath(String fileName) {
		LOG.log(Level.INFO, "Not implemented, not used, sketchPath: "+fileName);
		return "";
	}

	/**
	 * Minim requirement
	 * @param fileName
	 * @return
	 */
//	public InputStream createInput(String fileName) {
//		LOG.log(Level.INFO, "Not implemented, not used, createInput: "+fileName);
//		return null;
//	}
    public InputStream createInput(String filename) {
        InputStream input = this.createInputRaw(filename);
        if(input != null && filename.toLowerCase().endsWith(".gz")) {
            try {
                return new GZIPInputStream(input);
            } catch (IOException var4) {
                var4.printStackTrace();
                return null;
            }
        } else {
            return input;
        }
    }

    public InputStream createInputRaw(String filename) {
        FileInputStream stream = null;
        if(filename == null) {
            return null;
        } else if(filename.length() == 0) {
            return null;
        } else {
            InputStream stream1;
            if(filename.indexOf(":") != -1) {
                try {
                    URL cl2 = new URL(filename);
                    stream1 = cl2.openStream();
                    return stream1;
                } catch (MalformedURLException var17) {
                    ;
                } catch (FileNotFoundException var18) {
                    ;
                } catch (IOException var19) {
                    var19.printStackTrace();
                    return null;
                }
            }

            String e;
            try {
                File cl = new File(filename);
                if(!cl.exists()) {
                    cl = new File(filename);
                }

                if(cl.isDirectory()) {
                    return null;
                }

                if(cl.exists()) {
                    try {
                        e = cl.getCanonicalPath();
                        String url = (new File(e)).getName();
                        String conn = (new File(filename)).getName();
                        if(!url.equals(conn)) {
                            throw new RuntimeException("This file is named " + url + " not " + filename + ". Rename the file " + "or change your code.");
                        }
                    } catch (IOException var14) {
                        ;
                    }
                }

                stream = new FileInputStream(cl);
                if(stream != null) {
                    return stream;
                }
            } catch (IOException var15) {
                ;
            } catch (SecurityException var16) {
                ;
            }

            ClassLoader cl1 = this.getClass().getClassLoader();
            stream1 = cl1.getResourceAsStream("data/" + filename);
            if(stream1 != null) {
                e = stream1.getClass().getName();
                if(!e.equals("sun.plugin.cache.EmptyInputStream")) {
                    return stream1;
                }
            }

            stream1 = cl1.getResourceAsStream(filename);
            if(stream1 != null) {
                e = stream1.getClass().getName();
                if(!e.equals("sun.plugin.cache.EmptyInputStream")) {
                    return stream1;
                }
            }



            try {
                try {

                    try {
                        stream = new FileInputStream(filename);
                        if(stream != null) {
                            return stream;
                        }
                    } catch (IOException var7) {
                        ;
                    }
                } catch (SecurityException var10) {
                    ;
                }
            } catch (Exception var11) {
                var11.printStackTrace();
            }

            return null;
        }
    }

	/**
	 * Gets the current level of the buffer. It is calculated as 
	 * the root-mean-squared of all the samples in the buffer.
	 * @return the RMS amplitude of the buffer
	 */
	public float getVolume() {
		return getVolumeNormalized();
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#getVolumeNormalized()
	 */
	public float getVolumeNormalized() {
		float max = getSndVolumeMax();		
		//volume is too low, normalization would create wrong results.
		if (max<silenceThreshold) {
			dropedVolumeRequests++;
			if (dropedVolumeRequests%1000==0) {
				LOG.log(Level.INFO, "Ignored volume request, as volume is too low ("+ max +
						"), this happend "+ dropedVolumeRequests+" times.");
			}
			return 0;
		}
		
		float f = in.mix.level();		
		float norm=(1.0f/max)*f;	

		//im a bad coder! limit it!
		if (norm>1f) {
			norm=1f;		
		}

		return norm;
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#isKick()
	 */
	public boolean isKick() {
		return beat.isKick();
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#isSnare()
	 */
	public boolean isSnare() {
		return beat.isSnare();
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#isHat()
	 */
	public boolean isHat() {
		return beat.isHat();
	}

	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#isPang()
	 */
	public boolean isPang() {
		return beat.isHat() || beat.isKick() || beat.isSnare();
	}

	/**
	 * Returns the number of averages currently being calculated.
	 *
	 * @return the fft avg
	 */
	public int getFftAvg() {
		// perform a forward FFT on the samples 
		fft.forward(in.mix);

		return fft.avgSize();
	}

	public float getBand(int i) {
        return fft.getBand(i);
    }
	
	/**
	 * Gets the value of the ith average.
	 *
	 * @param i the i
	 * @return the fft avg
	 */
	public float getFftAvg(int i) {
		return fft.getAvg(i);
	}

	
	/* (non-Javadoc)
	 * @see com.neophob.sematrix.core.input.SeSound#shutdown()
	 */
	public void shutdown() {
		in.close();
		minim.stop();
	}

	/**
	 * Dispose.
	 */
	public void dispose() {		
		runner = null;
		//XXX 		this.shutdown();
	}

	/**
	 * the thread runner.
	 */
	public void run() {
		long sleep = (int)(250/SOUND_BUFFER_RESOLUTION);
		LOG.log(Level.INFO,	"Sound thread started...");
		int loop=0;
		while (Thread.currentThread() == runner) {
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {}

			// perform a forward FFT on the samples 
//			fft.forward(in.mix);
			
			//decrement max volume after 1/4s
			if (loop>SOUND_BUFFER_RESOLUTION) {
				sndVolumeMax*=.93f;
			}

			float f = in.mix.level();
			if (f>sndVolumeMax) {
				sndVolumeMax=f;
				loop=0;
			}

			loop++;
		}
	}


	/**
	 * Gets the snd volume max.
	 *
	 * @return the snd volume max
	 */
	public synchronized float getSndVolumeMax() {
		return sndVolumeMax;
	}


	@Override
	public String getImplementationName() {		
		return "Minim Sound";
	}

}
