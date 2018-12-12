package io.github.theregulators.theregulators;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ScanFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ScanFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScanFragment extends Fragment {
  // TODO: Rename parameter arguments, choose names that match
  // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
  private static final String ARG_PARAM1 = "param1";
  private static final String ARG_PARAM2 = "param2";

  // TODO: Rename and change types of parameters
  private String mParam1;
  private String mParam2;

  private OnFragmentInteractionListener mListener;

  public ScanFragment() { /* Required empty public constructor */  }

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @param param1 Parameter 1.
   * @param param2 Parameter 2.
   * @return A new instance of fragment ScanFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static ScanFragment newInstance(String param1, String param2) {
    ScanFragment fragment = new ScanFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PARAM1, param1);
    args.putString(ARG_PARAM2, param2);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_scan, container, false);
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    void onFragmentInteraction(Uri uri);
  }

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

  @Override
  public void onCreate(Bundle savedInstanceState) {

    // check these over
    super.onCreate(savedInstanceState);
    if (getArguments() != null) {
      mParam1 = getArguments().getString(ARG_PARAM1);
      mParam2 = getArguments().getString(ARG_PARAM2);
    }

    bglTextView = getView().findViewById(R.id.bglTextView);
    //colorTextView = findViewById(R.id.colorTextView);
    //colorPreview = findViewById(R.id.colorPreview);

    // test toast!
//		Toast.makeText(getBaseContext(), "Testing hello world!", Toast.LENGTH_SHORT).show();

    // preview camera
    cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
    cameraFacing = CameraCharacteristics.LENS_FACING_BACK;
    textureView = getView().findViewById(R.id.textureView);

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
    final ScanFragment _this = this;
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
      if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.CAMERA)
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
  public void onResume() {
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
    if(ScanFragment.parseBitmapLock) {
      return;
    }
    //colorTextView.setText(colorTextViewText);
    //colorPreview.setBackgroundColor(colorPreviewBackgroundColor);
    bglTextView.setText(bglTextViewText);

    new Thread(new Runnable() {
      @Override
      public void run() {

        ScanFragment.parseBitmapLock = true;

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
        ScanFragment.parseBitmapLock = false;

      }
    }).start();
  }

  @Override
  public void onStop() {
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

      final ScanFragment _this = this;
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
    int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
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
