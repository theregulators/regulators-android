/**
 * Static class to determine color of blood from image
 */

package io.github.theregulators.theregulators;

import android.os.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import io.github.theregulators.theregulators.VectorRGB;

public class ColorDetection {

  private static class Pixel {
    public int x;
    public int y;
    //public VectorRGB color;
    public Pixel(int x, int y) {
      setPos(x, y);
    }
    public void setPos(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }

  private static class PixelCluster {
    public int pixelCount;
    public List<Pixel> pixelList = new ArrayList<>();
    //public Pixel center;
    public void addPixel(Pixel pixel) {
      pixelList.add(pixel);
      /*if(pixelCount == 0) {
        center = pixel;
      } else {
        center.setPos((pixelCount * center.x + pixel.x)/pixelCount + 1, (pixelCount * center.x + pixel.x)/pixelCount + 1);
      }*/
      pixelCount++;
    }
  }

  // main function
  // should maybe be modularized
  public static VectorRGB getColor(int[] bitmap, int width, int height) {

    PixelCluster[][][] pixelClusters = new PixelCluster[16][16][16];

    System.out.println("STEP 1: INITIALIZING PIXEL CLUSTERS");

    int i, j, k;
    for(i = 0; i < 16; i++) {
      for(j = 0; j < 16; j++) {
        for(k = 0; k < 16; k++) {
          pixelClusters[i][j][k] = new PixelCluster();
        }
      }
    }

    System.out.println("STEP 2: FILLING PIXEL CLUSTERS");

    int x, y;
    VectorRGB color = new VectorRGB(0);
    for(y = 0; y < height; y++) {
      for(x = 0; x < width; x++) {
        color.setColor(bitmap[y * width + x]);

        int thresholdR = (int) color.r >> 4;
        int thresholdG = (int) color.g >> 4;
        int thresholdB = (int) color.b >> 4;

        Pixel pixel = new Pixel(x,y);
        pixelClusters[thresholdR][thresholdG][thresholdB].addPixel(pixel);
      }
    }

    System.out.println("STEP 3: ANALYZING PIXEL CLUSTERS");

    int lowThresholdCount = width * height / 100;
    int highThresholdCount = width * height / 2;
    int thresholdNum = 0;
    VectorRGB closestColor = new VectorRGB(0);

    int maxR = 0, maxG = 0, maxB = 0;
    for(i = 0; i < 16; i++) {
      for(j = 0; j < 16; j++) {
        for(k = 0; k < 16; k++) {
          // get pixel cluster
          PixelCluster currentPixelCluster = pixelClusters[i][j][k];

          // filter out clusters with too few pixels
          if(currentPixelCluster.pixelCount < lowThresholdCount || currentPixelCluster.pixelCount > highThresholdCount) {
            // FILTER CONDITION 1
            continue;
          }

          // check "jump" method
          int xAvg = 0;
          int yAvg = 0;
          for(Pixel clusterPixel : currentPixelCluster.pixelList) {
            xAvg += clusterPixel.x;
            yAvg += clusterPixel.y;
          }
          xAvg /= currentPixelCluster.pixelCount;
          yAvg /= currentPixelCluster.pixelCount;

          if(Math.abs(xAvg - width/2) < width/4 || Math.abs(yAvg - height/2) > height/4) {
            // FILTER CONDITION 2
            continue;
          }

          double error = 0;
          for(Pixel clusterPixel : currentPixelCluster.pixelList) {
            error += Math.sqrt(Math.pow(clusterPixel.x - xAvg, 2) + Math.pow(clusterPixel.y - yAvg, 2));
          }
          double relativeError = error / currentPixelCluster.pixelCount;

          if(relativeError > 200) {
            // FILTER CONDITION 3
            continue;
          }

          System.out.println(thresholdNum++ + ": " + new VectorRGB(i << 4, j << 4, k << 4).toString() + " ERROR: " + error + " RELATIVE ERROR: " + (error / currentPixelCluster.pixelCount) + " PIXEL COUNT: " + currentPixelCluster.pixelCount);
          System.out.println("\t\t" + xAvg + " " + yAvg);

        }
      }
    }

    return closestColor;

    // scale image
    // very rough -- just excludes many pixels
    /*final int maxDim = 100;
    int scaledWidth, scaledHeight, ratio;
    if(width >= height) {
      ratio = width / maxDim;
      scaledWidth = maxDim;
      scaledHeight = height * maxDim / width;
    } else {
      ratio = height / maxDim;
      scaledWidth = width * maxDim / height;
      scaledHeight = maxDim;
    }

    int scaledBitmap[] = new int[scaledWidth * scaledHeight];
    for(int i = 0; i < ratio * scaledHeight; i += ratio) {
      for(int j = 0; j < ratio * scaledWidth; j += ratio) {
        scaledBitmap[(i / ratio * maxDim + j / ratio)] = bitmap[i * width + j * ratio];
      }
    }

    // get average of four corners
    VectorRGB corner1 = new VectorRGB(scaledBitmap[0]);
    VectorRGB corner2 = new VectorRGB(scaledBitmap[scaledWidth - 1]);
    VectorRGB corner3 = new VectorRGB(scaledBitmap[scaledWidth * (scaledHeight - 1)]);
    VectorRGB corner4 = new VectorRGB(scaledBitmap[scaledBitmap.length - 1]);
    VectorRGB cornerAverage = corner1.add(corner2).add(corner3).add(corner4).timesScalar(0.25);

    // get off-background pixels
    final int THRESHOLD = 100;
    int significantColorCount = 0;
    VectorRGB significantColorSum = new VectorRGB(0, 0, 0);
    for(int i = 0; i < scaledBitmap.length; i++) {
      VectorRGB currentColor = new VectorRGB(scaledBitmap[i]);

      if(currentColor.distanceTo(cornerAverage) > THRESHOLD) {
        significantColorSum = significantColorSum.add(currentColor);
        significantColorCount++;
      }
    }
    if(significantColorCount == 0) {
      System.out.println("No significant points.");
      return new VectorRGB(0);
    }
    VectorRGB significantColorAverage = significantColorSum.timesScalar(1.0/significantColorCount);

    System.out.println("SUM: " + significantColorSum.toString() + " COUNT: " + significantColorCount + "AVERAGE COLOR: " + significantColorAverage.toString());

    return significantColorAverage;*/
  }

}
