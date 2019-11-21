package com.example.projet_mobile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final LatLng Namur = new LatLng(50.4674, 4.8720); //Your LatLong
    private GoogleMap mMap;
    private Button filter;
    String[] listItems = {"Afficher le traffic", "Afficher les routes endommagées", "Afficher les routes étroites"};
    boolean[] checkedItems = new boolean[]{false, false, false};
    ArrayList<LatLng> marqueur_vibr =new ArrayList<LatLng>();
    ArrayList<LatLng> marqueur_proxi =new ArrayList<LatLng>();




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //recup donnée marqueur dans json

        //TODO Modifier le str pour avoir le string du server web
        String jsonStr = loadJSONFromAsset();

        if(jsonStr !=null){
            try{
                JSONObject jsonObj =new JSONObject(jsonStr);

                JSONArray vibrations = jsonObj.getJSONArray("vibrations");
                JSONArray proximite = jsonObj.getJSONArray("proximite");

                for(int i=0;i<vibrations.length(); i++){
                    JSONObject v = vibrations.getJSONObject(i);
                    LatLng coord=new LatLng(v.getDouble("latitude"), v.getDouble("longitude"));
                    marqueur_vibr.add(coord);


                }
                for(int i=0;i<proximite.length(); i++){
                    JSONObject v = proximite.getJSONObject(i);
                    LatLng coord=new LatLng(v.getDouble("latitude"), v.getDouble("longitude"));
                    marqueur_proxi.add(coord);
                }




            }catch(JSONException e){
                e.printStackTrace();

            }
        }




        View view = inflater.inflate(R.layout.fragment_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        filter = (Button) view.findViewById(R.id.filter);

        filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                alertDialog.setTitle("Choisissez vos filtres :");
                alertDialog.setMultiChoiceItems(listItems, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which){
                            case 0:
                                mMap.setTrafficEnabled(isChecked);
                                break;
                            case 1 :
                                for (int i =0; i<marqueur_vibr.size(); i++){
                                    MarkerOptions markerOptions =new MarkerOptions();
                                    markerOptions.position(marqueur_vibr.get(i));
                                    markerOptions.title("Vibration détectée, route en mauvaise état ! ");
                                    mMap.addMarker(markerOptions);
                                }

                                break;
                            case 2:
                                for (int i = 0; i < marqueur_proxi.size(); i++) {
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    markerOptions.position(marqueur_proxi.get(i));
                                    markerOptions.title("Route étroite ! ");
                                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    mMap.addMarker(markerOptions);
                                }
                                break;
                        }
                        Toast.makeText(getContext(), "Filtre Mis à jour" , Toast.LENGTH_SHORT).show();

                    }
                });
                alertDialog.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = alertDialog.create();
                dialog.show();



            }
        });



        return view;
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // centre la caméra sur namur
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Namur));
        mMap.setMinZoomPreference(14.0f);
        mMap.setMaxZoomPreference(29.0f);

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("data_test.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}

