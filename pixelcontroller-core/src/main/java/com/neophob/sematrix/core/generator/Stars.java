package com.neophob.sematrix.core.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.neophob.sematrix.core.glue.MatrixData;
import com.neophob.sematrix.core.resize.Resize.ResizeName;

/**
 *
 * @author Mace
 */
public class Stars extends Generator {

    private int frameCount;

    private static final int NR_OF_STARS=35;

    private static final int RENDERSIZE=1;

    /** The random. */
    private Random random=new Random();


    private int lowXRes, lowYRes;

    private int color, new_color;

    /** The starpoints. */
    private List<StarPoint> points=new ArrayList<StarPoint>();

    /**
     * Instantiates Stars Generator
     *
     * @param matrix the Matrix
     */
    public Stars(MatrixData matrix) {
        super(matrix, GeneratorName.STARS, ResizeName.QUALITY_RESIZE);

        // now add stars
        lowXRes = (int)Math.floor(internalBufferXSize/(float)RENDERSIZE);
        lowYRes = (int)Math.floor(internalBufferYSize/(float)RENDERSIZE);

        for (int i=0;i<NR_OF_STARS;i++) {
            points.add(new StarPoint(lowXRes, lowYRes));
        }
    }

    @Override
    public void update() {
        for (int p=0; p<points.size(); p++) {
            StarPoint a = (StarPoint) points.get(p);
            int color = a.color;
            int new_color = 0;
            if ((color + frameCount%255) > 255) {
                new_color = color - frameCount%255;
            } else {
                new_color = color + frameCount%255;
            }
            rect(a.x,a.y, 1, 1, new_color);
//            a.color = frameCount%255;

//            a.color = random.nextInt(255);
//            rect(a.x,a.y, 1, 1, random.nextInt(255));
        }
        frameCount++;
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
    private void rect(int xofs, int yofs, int xsize, int ysize, int col) {
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

    class StarPoint {

        int x;
        int y;
        int dx;
        int dy;
        int color;
        int lowXRes, lowYRes;

        /**
         * Instantiates a new attractor.
         */
        public StarPoint(int lowXRes, int lowYRes) {
            this.lowXRes = lowXRes;
            this.lowYRes = lowYRes;

            if (lowXRes > 0) {
                this.x = random.nextInt(lowXRes);
            } else {
                this.x = 1;
            }

            if (lowYRes > 0) {
                this.y = random.nextInt(lowYRes);
            } else {
                this.y = 1;
            }


            while (this.dx == 0) {
                this.dx = -1 + random.nextInt(2);
            }
            while (this.dy == 0) {
                this.dy = -1 + random.nextInt(2);
            }
            this.color = random.nextInt(255);
        }
    }
}
