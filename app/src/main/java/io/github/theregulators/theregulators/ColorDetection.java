/**
 * Static class to determine color of blood from image
 */

package io.github.theregulators.theregulators;

import android.os.Environment;

import java.util.Vector;

import io.github.theregulators.theregulators.VectorRGB;

public class ColorDetection {

  // main function
  // should maybe be modularized
  public static VectorRGB getColor(int[] bitmap, int width, int height) {

    // scale image
    // very rough -- just excludes many pixels
    final int maxDim = 100;
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

    return significantColorAverage;
  }

}
