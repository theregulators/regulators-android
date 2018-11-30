package io.github.theregulators.theregulators;

import android.graphics.Color;

public class VectorRGB {

	public double r;
	public double g;
	public double b;

	public VectorRGB(double r, double g, double b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public VectorRGB(int packedInt) {
	  this.r = Color.red(packedInt);
    this.g = Color.green(packedInt);
    this.b = Color.blue(packedInt);
  }

	public VectorRGB(int[] componentArray) {
	  this.r = componentArray[0];
    this.g = componentArray[1];
    this.b = componentArray[2];
  }

	public double dot(VectorRGB v2) {
	  return r*v2.r + g*v2.g + b*v2.b;
  }

  public double norm() {
    return Math.sqrt(r*r + g*g + b*b);
  }

  public VectorRGB negate() {
    return new VectorRGB(-r, -g, -b);
  }

  public VectorRGB add(VectorRGB v2) {
    return new VectorRGB(r+v2.r, g+v2.g, b+v2.b);
  }

  public VectorRGB subtract(VectorRGB v2) {
    return this.add(v2.negate());
  }

  public double distanceTo(VectorRGB v2) {
    return this.subtract(v2).norm();
  }

  public VectorRGB timesScalar(double k) {
    return new VectorRGB(r*k, g*k, b*k);
  }

  public VectorRGB projOn(VectorRGB v2) {
    return v2.timesScalar(this.dot(v2) / Math.pow(v2.norm(), 2));
  }

  public VectorRGB ortProjOn(VectorRGB v2) {
    return this.subtract(this.projOn(v2));
  }

  public String toString() {
    return String.format("Vector RGB R: %f G: %f B: %f", r, g, b);
  }

  public int toColorInt() {
	  int a = 255;
    return (a & 0xff) << 24 | ((int) r & 0xff) << 16 | ((int) g & 0xff) << 8 | ((int) b & 0xff);
  }
}
