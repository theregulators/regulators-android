package io.github.theregulators.theregulators;

import io.github.theregulators.theregulators.VectorRGB;

public class BGLDetermination {

  final static private VectorRGB colorStart = new VectorRGB(255, 0, 255);
  final static private VectorRGB colorEnd = new VectorRGB(255, 255, 0);

  // act like a static class
  private BGLDetermination() {}

  // these coefficients of determinations and functions are experimentally determined
  // see the calibration curves for more information
  private static double rToBGL(double r) {
    return Math.exp((112.041-r)/13.921);
  }
  private static double gToBGL(double g) {
    return Math.exp((159.955-g)/28.059);
  }
  private static double bToBGL(double b) {
    return Math.exp((165.499-b)/28.44723);
  }
  private static double rR2 = 0.624163;
  private static double gR2 = 0.853277;
  private static double bR2 = 0.887773;

  public static double colorToBGL(VectorRGB color) {

    double rBGLGuess = rToBGL(color.r);
    double gBGLGuess = gToBGL(color.g);
    double bBGLGuess = bToBGL(color.b);

    double weightedBGLGuess = (rBGLGuess * rR2 + gBGLGuess * gR2 + bBGLGuess * bR2) / (rR2 + gR2 + bR2);

    System.out.println("Estimates: " + rBGLGuess + " " + gBGLGuess + " " + bBGLGuess + " " + weightedBGLGuess);

    return weightedBGLGuess;

  }


}
