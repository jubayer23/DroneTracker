package com.creative.dronetracker.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.creative.dronetracker.R;
import com.creative.dronetracker.Utility.CommonMethods;
import com.creative.dronetracker.Utility.DeviceInfoUtils;
import com.creative.dronetracker.Utility.GpsEnableTool;
import com.creative.dronetracker.Utility.LastLocationOnly;
import com.creative.dronetracker.Utility.UserLastKnownLocation;
import com.creative.dronetracker.alertbanner.AlertDialogForAnything;
import com.creative.dronetracker.appdata.GlobalAppAccess;
import com.creative.dronetracker.appdata.MydApplication;
import com.creative.dronetracker.service.GpsServiceUpdate;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by jubayer on 3/20/2018.
 */

public class HomeFragment extends Fragment implements View.OnClickListener{

    private static final String TAG_REQUEST_HOME_PAGE = "volley_request_home_page";
    Button btn_start, btn_stop;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container,
                false);

        init(view);

        changeUiBasedOnDrivingStatus();

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void init(View view){
        btn_start = (Button) view.findViewById(R.id.btn_start);
        btn_start.setOnClickListener(this);

        btn_stop = (Button) view.findViewById(R.id.btn_stop);
        btn_stop.setOnClickListener(this);
    }

    private void changeUiBasedOnDrivingStatus(){
        boolean isDriving = MydApplication.getInstance().getPrefManger().getDrivingStatus();

        if(isDriving){
            btn_start.setVisibility(View.GONE);
            btn_stop.setVisibility(View.VISIBLE);
        }else{
            btn_stop.setVisibility(View.GONE);
            btn_start.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if(id == R.id.btn_start){
            LastLocationOnly lastLocationOnly = new LastLocationOnly(getActivity());

            if (!lastLocationOnly.canGetLocation()) {
                GpsEnableTool gpsEnableTool = new GpsEnableTool(getActivity());
                gpsEnableTool.enableGPs();
                return;
            }

            if (!DeviceInfoUtils.isConnectingToInternet(getActivity())) {
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Error", "Please connect to working internet connection!", false);
                return;
            }

            showProgressDialog("please wait..", true, false);
            UserLastKnownLocation.LocationResult locationResult = new UserLastKnownLocation.LocationResult() {
                @Override
                public void gotLocation(Location location) {
                    final double loc_lat = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLatitude());
                    final double loc_lng = CommonMethods.roundFloatToFiveDigitAfterDecimal(location.getLongitude());
                    //Got the location!
                    //dismissProgressDialog();
                    hitUrlForStartOrStopGps(GlobalAppAccess.URL_UPDATE_LOCATION,
                            MydApplication.getInstance().getPrefManger().getUser().getId(),
                            loc_lat, loc_lng,true);
                }
            };
            UserLastKnownLocation myLocation = new UserLastKnownLocation();
            myLocation.getLocation(getActivity(), locationResult);
        }

        if(id == R.id.btn_stop){

            if (!DeviceInfoUtils.isConnectingToInternet(getActivity())) {
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Error", "Please connect to working internet connection!", false);
                return;
            }

            showProgressDialog("please wait..", true, false);
            hitUrlForStartOrStopGps(GlobalAppAccess.URL_UPDATE_LOCATION,
                    MydApplication.getInstance().getPrefManger().getUser().getId(),
                    GpsServiceUpdate.lastUpdatedLat, GpsServiceUpdate.getLastUpdatedLang,false);
        }
    }

    private void hitUrlForStartOrStopGps(String url, final String id, final double lat, final double lng, final boolean status) {
        // TODO Auto-generated method stub

        //showProgressDialog("Start Delivery....", true, false);

        if(status){
            url = url + "?user_id=" + id + "&lat=" + lat + "&lang=" +lng + "&status=1" ;
        }else{
            url = url + "?user_id=" + id + "&lat=" + lat + "&lang=" +lng + "&status=0" ;
        }


        final StringRequest req = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        dismissProgressDialog();


                        response = response.replaceAll("\\s+", "");

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String result = jsonObject.getString("success");
                            if (result.equals("1")) {


                                if(status){
                                    MydApplication.getInstance().getPrefManger().setDrivingStatus(true);

                                    changeUiBasedOnDrivingStatus();

                                    //RESTART SERVICE
                                    getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));
                                    getActivity().startService(new Intent(getActivity(), GpsServiceUpdate.class));
                                }else{
                                    getActivity().stopService(new Intent(getActivity(), GpsServiceUpdate.class));
                                    MydApplication.getInstance().getPrefManger().setDrivingStatus(false);
                                    changeUiBasedOnDrivingStatus();
                                }




                            } else {
                                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                                        "Error", response, false);
                                // AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                                //         "Error", "Something went wrong!", false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(),
                                    "Error", "Server Down!! Please contact with server person!!!", false);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dismissProgressDialog();
                AlertDialogForAnything.showAlertDialogWhenComplte(getActivity(), "Error", "Network error!", false);
            }
        }) ;

        req.setRetryPolicy(new DefaultRetryPolicy(30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // TODO Auto-generated method stub
        MydApplication.getInstance().addToRequestQueue(req, TAG_REQUEST_HOME_PAGE);
    }

    private ProgressDialog progressDialog;

    public void showProgressDialog(String message, boolean isIntermidiate, boolean isCancelable) {
       /**/
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog.setIndeterminate(isIntermidiate);
        progressDialog.setCancelable(isCancelable);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    public void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
