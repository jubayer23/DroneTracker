package com.creative.dronetracker;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.creative.dronetracker.Utility.GpsEnableTool;
import com.creative.dronetracker.Utility.LastLocationOnly;
import com.creative.dronetracker.alertbanner.AlertDialogForAnything;
import com.creative.dronetracker.appdata.MydApplication;
import com.creative.dronetracker.fragment.HomeFragment;

public class MainActivity extends BaseActivity {
    private static final String TAG_HOME_FRAGMENT = "House List Fragment";
    private HomeFragment homeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();

        if (savedInstanceState == null) {

            /**
             * This is marshmallow runtime Permissions
             * It will ask user for grand permission in queue order[FIFO]
             * If user gave all permission then check whether user device has google play service or not!
             * NB : before adding runtime request for permission Must add manifest permission for that
             * specific request
             * */


            //getSupportFragmentManager()
            //        .beginTransaction()
            //        .add(R.id.content_layout, new HouseListFragment(), TAG_HOME_FRAGMENT)
            //        .commit();


            homeFragment = new HomeFragment();
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.content_layout, homeFragment, TAG_HOME_FRAGMENT)
                    .commit();
        }

        LastLocationOnly lastLocationOnly = new LastLocationOnly(this);
        if (!lastLocationOnly.canGetLocation()) {
            GpsEnableTool gpsEnableTool = new GpsEnableTool(this);
            gpsEnableTool.enableGPs();
            return;
        }
    }

    public boolean onCreateOptionsMenu(Menu paramMenu) {
        getMenuInflater().inflate(R.menu.menu_main, paramMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {

        switch (paramMenuItem.getItemId()) {

            case R.id.action_logout:
               if(MydApplication.getInstance().getPrefManger().getDrivingStatus()){
                   AlertDialogForAnything.showAlertDialogWhenComplte(this,"Alert!","You cannot logout while drone tracking is on. Please stop the tracking and then logout.",false);
                   break;
               }else{
                   MydApplication.getInstance().getPrefManger().setUser("");
                   startActivity(new Intent(MainActivity.this,LoginActivity.class));
                   finish();
               }
                break;

        }

        return false;
    }
}
