package io.github.theregulators.theregulators;

import io.github.theregulators.theregulators.VectorRGB;

public class BGLDetermination {

  final static private VectorRGB colorStart = new VectorRGB(255, 0, 255);
  final static private VectorRGB colorEnd = new VectorRGB(255, 255, 0);

  // act like a static class
  private BGLDetermination() {}

  /**
   * Calculate BGL from percentage in color range
   * @param percent   percent of color range (from colorStart to colorEnd)
   * @return          blood glucose level
   */
  private static double percentToBGL(double percent) {
    return percent;
  }

  public static double colorToBGL(VectorRGB color) {

    // perform orthogonal decomposition
    VectorRGB p0p1 = colorEnd.subtract(colorStart);
    VectorRGB p0p = color.subtract(colorStart);

    VectorRGB o = p0p.ortProjOn(p0p1);
    VectorRGB q = color.add(o);
    VectorRGB p0q = q.subtract(colorStart);

    double z = p0q.norm() / p0p1.norm();
    double bgl = percentToBGL(z);

    return bgl;

  }


}
