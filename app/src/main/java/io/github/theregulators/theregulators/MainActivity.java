package io.github.theregulators.theregulators;

import io.github.theregulators.theregulators.ColorDetection;
import io.github.theregulators.theregulators.BGLDetermination;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

	private TextView mTextMessage;

	/**
	 * Much of the camera handling is from this tutorial: https://android.jlelse.eu/the-least-you-can-do-with-camera2-api-2971c8c81b8b
	 */
	private TextureView textureView;
	private TextView bglTextView;
	private TextView colorTextView;
	private View colorPreview;

	private CameraDevice cameraDevice;
	private CameraManager cameraManager;
	private int cameraFacing;
	private TextureView.SurfaceTextureListener surfaceTextureListener;
	private String cameraId;
	private Size previewSize;
	private HandlerThread backgroundThread;
	private Handler backgroundHandler;
	private CameraCaptureSession cameraCaptureSession;

	private MainActivity _this  = this;

	private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

		@Override
		public boolean onNavigationItemSelected(@NonNull MenuItem item) {
			switch (item.getItemId()) {
				case R.id.navigation_home:
					mTextMessage.setText(R.string.title_home);
					return true;
				case R.id.navigation_dashboard:
					mTextMessage.setText(R.string.title_dashboard);
					return true;
				case R.id.navigation_about:
					mTextMessage.setText(R.string.title_about);
					return true;
			}
			return false;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextMessage = (TextView) findViewById(R.id.message);
		BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
		navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

		bglTextView = findViewById(R.id.bglTextView);
		//colorTextView = findViewById(R.id.colorTextView);
    //colorPreview = findViewById(R.id.colorPreview);

		// test toast!
//		Toast.makeText(getBaseContext(), "Testing hello world!", Toast.LENGTH_SHORT).show();

		// preview camera
		cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
		textureView = findViewById(R.id.textureView);

		surfaceTextureListener = new TextureView.SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
				setUpCamera();
				openCamera();
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
				return false;
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
				parseBitmap();
			}
		};
	}

	private void setUpCamera() {
		try {
			for (String cameraId : cameraManager.getCameraIdList()) {
				CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
				if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == cameraFacing) {
					StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
					this.previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];
					System.out.println("SETTING PREVIEW SIZE: " + previewSize);
					this.cameraId = cameraId;
					this.configureTransform(textureView.getWidth(), textureView.getHeight());
				}
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	private void openCamera() {
		final MainActivity _this = this;
		CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
			@Override
			public void onOpened(CameraDevice cameraDevice) {
				_this.cameraDevice = cameraDevice;
				createPreviewSession();
			}

			@Override
			public void onDisconnected(CameraDevice cameraDevice) {
				cameraDevice.close();
				_this.cameraDevice = null;
			}

			@Override
			public void onError(CameraDevice cameraDevice, int error) {
				cameraDevice.close();
				_this.cameraDevice = null;
			}
		};

		try {
			if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
					== PackageManager.PERMISSION_GRANTED) {
				cameraManager.openCamera(cameraId, stateCallback, backgroundHandler);
			}
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}


	private void openBackgroundThread() {
		backgroundThread = new HandlerThread("camera_background_thread");
		backgroundThread.start();
		backgroundHandler = new Handler(backgroundThread.getLooper());
	}

	@Override
	protected void onResume() {
		super.onResume();
		openBackgroundThread();
		if (textureView.isAvailable()) {
			setUpCamera();
			openCamera();
		} else {
			textureView.setSurfaceTextureListener(surfaceTextureListener);
		}
	}

	public volatile static boolean parseBitmapLock = false;
  //public volatile static String colorTextViewText = "";
  //public volatile static int colorPreviewBackgroundColor = 0;
  public volatile static String bglTextViewText = "---";
	private void parseBitmap() {
		// lock while running to avoid too much
		if(MainActivity.parseBitmapLock) {
      return;
    }
    //colorTextView.setText(colorTextViewText);
    //colorPreview.setBackgroundColor(colorPreviewBackgroundColor);
    bglTextView.setText(bglTextViewText);

    new Thread(new Runnable() {
      @Override
      public void run() {

        MainActivity.parseBitmapLock = true;

        Bitmap bitmap = textureView.getBitmap();
        if(bitmap == null) return;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for(int i = 0; i < 100; i++) {
          int j = pixels[i];
        }

        VectorRGB averageColor = ColorDetection.getColor(pixels, width, height);

        //colorTextViewText = averageColor.toString();
        //colorPreviewBackgroundColor = averageColor.toColorInt();

        double bgl = BGLDetermination.colorToBGL(averageColor);

        bglTextViewText = "" + (Math.round(bgl * 10.0) / 10.0);

        // remove lock
        MainActivity.parseBitmapLock = false;

      }
    }).start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		closeCamera();
		closeBackgroundThread();
	}

	private void closeCamera() {
		if (cameraCaptureSession != null) {
			cameraCaptureSession.close();
			cameraCaptureSession = null;
		}

		if (cameraDevice != null) {
			cameraDevice.close();
			cameraDevice = null;
		}
	}

	private void closeBackgroundThread() {
		if (backgroundHandler != null) {
			backgroundThread.quitSafely();
			backgroundThread = null;
			backgroundHandler = null;
		}
	}

	private void createPreviewSession() {
		try {
			SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
			surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
			Surface previewSurface = new Surface(surfaceTexture);
			final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			captureRequestBuilder.addTarget(previewSurface);

			final MainActivity _this = this;
			cameraDevice.createCaptureSession(Collections.singletonList(previewSurface), new CameraCaptureSession.StateCallback() {
				@Override
				public void onConfigured(CameraCaptureSession cameraCaptureSession) {
					if (cameraDevice == null) {
						return;
					}

					try {
						CaptureRequest captureRequest = captureRequestBuilder.build();
						_this.cameraCaptureSession = cameraCaptureSession;
						_this.cameraCaptureSession.setRepeatingRequest(captureRequest, null, backgroundHandler);
					} catch (CameraAccessException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

				}
			}, backgroundHandler);
		} catch (CameraAccessException e) {
			e.printStackTrace();
		}
	}

	/**
	 * The following method was modified from https://stackoverflow.com/a/35553445/2397327
	 *
	 * Configures the necessary {@link android.graphics.Matrix} transformation to `mTextureView`.
	 * This method should be called after the camera preview size is determined in
	 * setUpCameraOutputs and also the size of `mTextureView` is fixed.
	 *
	 * @param viewWidth  The width of `mTextureView`
	 * @param viewHeight The height of `mTextureView`
	 */
	private void configureTransform(int viewWidth, int viewHeight) {
		System.out.println("PREVIEW SIZE: " + this.previewSize + " " + (this.previewSize == null));
		//previewSize = new Size(1280,960);
		if(previewSize == null) return;
		int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
		System.out.println("ROTATION: " + rotation);
		Matrix matrix = new Matrix();
		RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
		RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
		float centerX = viewRect.centerX();
		float centerY = viewRect.centerY();
		if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
			bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
			matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
			float scale = Math.max(
					(float) viewHeight / previewSize.getHeight(),
					(float) viewWidth / previewSize.getWidth());
			matrix.postScale(scale, scale, centerX, centerY);
			matrix.postRotate(90 * (rotation - 2), centerX, centerY);
		} else if (Surface.ROTATION_180 == rotation) {
			matrix.postRotate(180, centerX, centerY);
		}
		textureView.setTransform(matrix);
	}
}
