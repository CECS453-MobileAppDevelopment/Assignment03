/*
  * @author Juan Carlos Pasillas
  * StudentID: 017350864
  * California State University, Long Beach
  * CECS 453 Section 05
  * Professor: Arjang Fahim
  * @date May 04, 2020
 *
 * Github Project: https://github.com/CECS453-MobileAppDevelopment/Assignment03
 */

package com.example.assignment03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, AddressAdapter.OnItemClickListener {

    private String ADDRESS_URL; //address url used for parsing json
    private LatLng ADDRESS_COORDINATES = new LatLng(); //address coordinates used for address item

    private MapView mapView; //mapview used for viewing and interacting with map
    private MapboxMap map; //mapboxmap used for placing markers
    private Marker locationMarker; //marker for placing on address location on map

    private EditText searchText; //user input for address
    private Button searchButton; //button for searching inputted address
    private TextView addressText; //address text for bottom sheet label and header
    private BottomSheetBehavior bottomSheetBehavior; //bottom sheet behavior used for sliding
    private ImageView bottomSheetIndicator; //bottom sheet indicator for toggling bottom sheet state

    private RecyclerView addressRecycler; //recycler view to hold address list
    private AddressAdapter addressAdapter; //recycler adapter for formatting and holding address items
    private ArrayList<AddressItem> addressList; //list of address items
    private RequestQueue queue; //volley request queue for parsing json

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.access_token));
        setContentView(R.layout.activity_main);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        queue = Volley.newRequestQueue(this);
        searchText = findViewById(R.id.search_text);
        searchButton = findViewById(R.id.search_button);
        addressText = findViewById(R.id.address_text);
        View bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetIndicator = findViewById(R.id.bottom_sheet_indicator);

        addressRecycler = findViewById(R.id.address_recycler);
        addressRecycler.setHasFixedSize(true);
        addressRecycler.setLayoutManager(new LinearLayoutManager(this));

        addressList = new ArrayList<>();

        //when bottom sheet indicator is clicked, toggle bottom sheet state
        bottomSheetIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        //when the search button is clicked, search for address
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!searchText.getText().toString().equals("")) {
                    hideSoftKeyboard(MainActivity.this, v);
                    ADDRESS_URL = createUrl();
                    parseAddressUrl();
                }
            }
        });

        //allow bottom sheet to be slideable and update the text
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //when bottom sheet is collapsed, use last searched address if available
                        if(addressList.size() != 0) {
                            addressText.setText(addressList.get(0).getAddress());
                        } else {
                            addressText.setText("Address");
                        }
                        bottomSheetIndicator.setImageResource(R.drawable.ic_arrow_drop_up);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        //when bottom sheet is change the text view into a list header
                        addressText.setText("Address List");
                        bottomSheetIndicator.setImageResource(R.drawable.ic_arrow_drop_down);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //necessary override, left blank for no sliding behavior
            }
        });
    }

    /* Return the url of the json file within the mapbox api to get address data */
    private String createUrl() {
        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/";
        String address = searchText.getText().toString();
        //replacing spaces with %20 for url formatting
        address = address.replace(" ", "%20");
        //replacing # with %23 for url formatting
        address = address.replace("#", "%23");
        //replacing , with %2C for url formatting
        address = address.replace(",", "%2C");
        url += address;
        //Limits the search results to one
        String limitOne = ".json?limit=1&";
        url += limitOne;
        //Turns off autocomplete: searching based off input only for validation
        String autocompleteOff = "autocomplete=false&";
        url += autocompleteOff;
        //Access Token needed for functionality
        String access_token = getResources().getString(R.string.access_token);
        url += "access_token=" + access_token;
        return url;
    }

    /* Parse the json file to extract address data using the url */
    private void parseAddressUrl() {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, ADDRESS_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("features");
                            //limiting the query to one means we can get the first index only
                            JSONObject feature = jsonArray.getJSONObject(0);
                            //get the address type
                            JSONArray type = feature.getJSONArray("place_type");
                            String place_type = type.getString(0);
                            //get the relevance value for precision
                            double relevance = feature.getDouble("relevance");
                            //require type to be address and no precision doubt
                            if(place_type.equals("address") && relevance == 1.0) {
                                String addressName = feature.getString("place_name");
                                //get the latitude and longitude coordinates
                                JSONArray latlng =  feature.getJSONArray("center");
                                double lat = latlng.getDouble(1);
                                double lng = latlng.getDouble(0);
                                ADDRESS_COORDINATES.setLatitude(lat);
                                ADDRESS_COORDINATES.setLongitude(lng);
                                //check if address exists in list using coordinates
                                int existingAddress = inAddressList();
                                if (existingAddress == -1) {
                                    //add the new address to the beginning of the address list
                                    addressList.add(0, new AddressItem(addressName, lat, lng));
                                    //create a new recycler view adapter with the updated address list
                                    addressAdapter = new AddressAdapter(MainActivity.this, addressList);
                                    addressRecycler.setAdapter(addressAdapter);
                                    addressAdapter.setOnItemClickListener(MainActivity.this);
                                    //place marker and clear search input
                                    placeMarker();
                                    clearSearch();
                                } else {
                                    //if address exists simulate it being clicked
                                    onItemClick(existingAddress);
                                }
                            } else {
                                clearSearch();
                                //launch invalid address dialog if not of type address or max precision
                                InvalidAddressDialog dialog = new InvalidAddressDialog();
                                dialog.show(getSupportFragmentManager(), "InvalidAddressDialog");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            clearSearch();
                            //launch invalid address dialog if errors getting data
                            InvalidAddressDialog dialog = new InvalidAddressDialog();
                            dialog.show(getSupportFragmentManager(), "InvalidAddressDialog");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                clearSearch();
                //launch invalid address dialog if error parsing json file
                InvalidAddressDialog dialog = new InvalidAddressDialog();
                dialog.show(getSupportFragmentManager(), "InvalidAddressDialog");
            }
        });
        //add the json parse request to the volley request queue
        queue.add(request);
    }

    /* Check is address exists in list, if yes, return position */
    private int inAddressList() {
        for(int i = 0; i < addressList.size(); i ++) {
            double addLat = addressList.get(i).getLatitude();
            double addLng = addressList.get(i).getLongitude();
            if((ADDRESS_COORDINATES.getLatitude() == addLat) && (ADDRESS_COORDINATES.getLongitude() == addLng)) {
                return i;
            }
        }
        return -1;
    }

    /* Place a marker on the map using the address coordinates */
    private void placeMarker() {
        if(locationMarker != null) {
            map.removeMarker(locationMarker);
        }
        locationMarker = map.addMarker(new MarkerOptions().position(ADDRESS_COORDINATES));
        //animate to the coordinates location
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ADDRESS_COORDINATES, 15));
    }

    /* Clear the input text field */
    private void clearSearch() {
        //collapse bottom sheet and update text if available
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        if(addressList.size() > 0) {
            addressText.setText(addressList.get(0).getAddress());
        } else {
            addressText.setText("Address");
        }
        searchText.setText("");
        searchText.clearFocus();
    }

    /* Hide the keyboard if open */
    public static void hideSoftKeyboard (Activity activity, View view)
    {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    /* Get the recycler item clicked and update the coordinates of the marker to that */
    @Override
    public void onItemClick(int position) {
        AddressItem clickedItem = addressList.get(position);
        ADDRESS_COORDINATES.setLatitude(clickedItem.getLatitude());
        ADDRESS_COORDINATES.setLongitude((clickedItem.getLongitude()));
        //replace address in list on top
        addressList.remove(position);
        addressList.add(0, clickedItem);
        addressAdapter = new AddressAdapter(MainActivity.this, addressList);
        addressRecycler.setAdapter(addressAdapter);
        addressAdapter.setOnItemClickListener(MainActivity.this);
        //place new marker
        placeMarker();
        clearSearch();
    }

    /* After on create, create the map instance used to place markers */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setStyle(Style.MAPBOX_STREETS, new MapboxMap.OnStyleLoadedListener() {
            @Override
            public void onStyleLoaded(String style) {
                UiSettings uiSettings = map.getUiSettings();
                //move location of compass to below the address search bar
                uiSettings.setCompassMargins(0, 300,100,0);
            }
        });
    }

    /* Left empty to prevent application from closing on back pressed */
    @Override
    public void onBackPressed() {

    }

    /* All methods below add the MapView instance to the application lifecycle
       to increase performance and conserve device battery */
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
