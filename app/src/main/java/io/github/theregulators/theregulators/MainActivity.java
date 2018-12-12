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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTextMessage = (TextView) findViewById(R.id.message);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
  }

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

    Fragment fragment;
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction transaction = fm.beginTransaction();

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          fragment = new ScanFragment();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_home);
          return true;
        case R.id.navigation_dashboard:
          fragment = new ScanFragment();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_dashboard);
          return true;
        case R.id.navigation_about:
          fragment = new ScanFragment();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_about);
          return true;
      }
      return false;
    }
  };
}
