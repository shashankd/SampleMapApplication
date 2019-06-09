package com.exercise.map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private AutoCompleteTextView searchView;
    private PlacesClient placesClient;
    private Context mContext;
    private static final String TAG = "MapsActivity";
    private AutocompleteSessionToken token;
    private Marker marker;
    private ImageView clearButtonImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar toolbar = findViewById(R.id.view_toolbar);
        toolbar.setTitle(R.string.app_name);
        searchView = findViewById(R.id.searchField);
        clearButtonImageView = findViewById(R.id.places_autocomplete_clear_button);
        mContext = this;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchView.setThreshold(1);

        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(mContext);

        token = AutocompleteSessionToken.newInstance();

        Log.i(TAG, "Session Token = " + token);

        attachListeners();
    }

    /*Attach listeners to the views*/
    private void attachListeners() {
        clearButtonImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setText("");
            }
        });

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                final StyleSpan styleSpan = new StyleSpan(Typeface.NORMAL);
                List requiredFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                if (item instanceof AutocompletePrediction) {
                    final AutocompletePrediction prediction = (AutocompletePrediction) item;
                    searchView.setText(prediction.getFullText(styleSpan));
                    String placeId = prediction.getPlaceId();

                    FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, requiredFields).build();
                    Task<FetchPlaceResponse> task = placesClient.fetchPlace(request);

                    task.addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse response) {
                            Log.i(TAG, response.toString());
                            LatLng latlng = response.getPlace().getLatLng();
                            if (latlng != null) {

                                if (marker != null) {
                                    marker.remove();
                                }

                                marker = mMap.addMarker(
                                        new MarkerOptions()
                                                .position(latlng)
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
                                                .title(prediction.getPrimaryText(styleSpan).toString())
                                );

                                mMap.animateCamera(CameraUpdateFactory.newLatLng(latlng));

                                drawStarPolygon(latlng);
                            }
                        }
                    });
                }
            }
        });

        searchView.addTextChangedListener(new TextWatcher() {

            Task<FindAutocompletePredictionsResponse> task = null;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence str, int start, int before, int count) {

                if (str.length() == 0) {
                    searchView.dismissDropDown();
                    clearButtonImageView.setVisibility(View.INVISIBLE);
                } else {
                    clearButtonImageView.setVisibility(View.VISIBLE);
                }

                FindAutocompletePredictionsRequest findAutocompletePredictionsRequest =
                        FindAutocompletePredictionsRequest
                                .builder().setQuery(str.toString())
                                .setSessionToken(token)
                                .build();

                task = placesClient.findAutocompletePredictions(findAutocompletePredictionsRequest);

                task.addOnSuccessListener(new OnSuccessListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onSuccess(FindAutocompletePredictionsResponse response) {
                        Log.i(TAG, response.getAutocompletePredictions().toString());
                        AutocompleteAdapter adapter = new AutocompleteAdapter(
                                mContext,
                                android.R.layout.simple_spinner_dropdown_item,
                                response.getAutocompletePredictions());

                        try {
                            searchView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });

                        task.addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                                Log.i(TAG, "  Request Completed");
                            }
                        });
                    }
                });

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /*Method draws polygon star over mapview*/
    private void drawStarPolygon(LatLng latlng) {
        List<Double> latAdditionDoubles = Arrays.asList(
                -0.0000429000,
                -0.0001321000,
                -0.0000516000,
                -0.0000664000,
                0.0000141000,
                0.0001080000,
                0.0000691000,
                0.0001529000,
                0.0000476000,
                0.0000007000,
                0.0000000000);

        List<Double> lngAdditionDoubles = Arrays.asList(
                -0.0000653000,
                -0.0000800000,
                -0.0001169000,
                -0.0001944000,
                -0.0001267000,
                -0.0001851000,
                -0.0001066000,
                -0.0000594000,
                -0.0000599000,
                0.0000005000,
                0.0000000000);

        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.add(latlng);
        for (int i = 0; i < latAdditionDoubles.size(); i++) {
            polygonOptions.add(new LatLng(latlng.latitude + latAdditionDoubles.get(i), latlng.longitude + lngAdditionDoubles.get(i)));
        }
        Polygon polygon = mMap.addPolygon(polygonOptions);
        polygon.setFillColor(Color.argb(255, 255, 215, 0));

    }

/*Callback receives when the map is ready and loaded*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(mContext, R.raw.map_style));

        LatLng jibestreamOffice = new LatLng(43.654353, -79.426355);
        mMap.addMarker(
                new MarkerOptions().position(jibestreamOffice).title("Office")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker))
        );
        mMap.moveCamera(CameraUpdateFactory.zoomTo(18f));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(jibestreamOffice));
    }
}
