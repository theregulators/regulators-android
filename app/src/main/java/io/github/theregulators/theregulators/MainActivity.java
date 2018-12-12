package io.github.theregulators.theregulators;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity
    extends FragmentActivity
    implements ScanFragment.OnFragmentInteractionListener, AboutFragment.OnFragmentInteractionListener {

  private TextView mTextMessage;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mTextMessage = (TextView) findViewById(R.id.message);
    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    /*Fragment fragment = new ScanFragment();
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction transaction = fm.beginTransaction();
    transaction.replace(R.id.contentFragment, fragment);
    transaction.commit();*/
  }

  private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
      switch (item.getItemId()) {
        case R.id.navigation_home: {
          Fragment fragment = new ScanFragment();
          FragmentManager fm = getSupportFragmentManager();
          FragmentTransaction transaction = fm.beginTransaction();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_home);
          return true;
        }
        case R.id.navigation_dashboard: {
          Fragment fragment = new ScanFragment();
          FragmentManager fm = getSupportFragmentManager();
          FragmentTransaction transaction = fm.beginTransaction();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_dashboard);
          return true;
        }
        case R.id.navigation_about: {
          Fragment fragment = new AboutFragment();
          FragmentManager fm = getSupportFragmentManager();
          FragmentTransaction transaction = fm.beginTransaction();
          transaction.replace(R.id.contentFragment, fragment);
          transaction.commit();
          mTextMessage.setText(R.string.title_about);
          return true;
        }
      }
      return false;
    }
  };

  @Override
  public void onFragmentInteraction(Uri uri) {

  }
}
