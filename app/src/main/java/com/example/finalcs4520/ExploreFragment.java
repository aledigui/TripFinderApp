package com.example.finalcs4520;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment implements OnMapReadyCallback {
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;
    }

    private MapView mapView;
    private GoogleMap googleMap;

    private Button saveButton;

    private EditText locationSearchText;
    private ImageView searchButtonLocation;

    private ImageView myLocationButton;

    private String myLocation;

    private String destination;
    private LocationManager locationManager;

    private GoogleMap gMap;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private boolean permissions;

    private String finalDeparture;
    private String finalDestination;

    private String departureCity;
    private String destinationCity;

    public ExploreFragment() {
        // Required empty public constructor
    }

    public static ExploreFragment newInstance() {
        ExploreFragment fragment = new ExploreFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View exploreView = inflater.inflate(R.layout.fragment_explore, container, false);
        mapView = (MapView) exploreView.findViewById(R.id.map);
        searchButtonLocation = exploreView.findViewById(R.id.searchButtonLocation);
        locationSearchText = exploreView.findViewById(R.id.locationSearchText);
        myLocationButton = exploreView.findViewById(R.id.myLocationButton);
        saveButton = exploreView.findViewById(R.id.saveButton);

        saveButton.setVisibility(View.INVISIBLE);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            permissions = true;
        } else {
            permissions = true;
        }
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                gMap = googleMap;
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                getDeviceLocation();
                gMap.setMyLocationEnabled(true);
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInternetAvailable())  {
                    Toast.makeText(getContext(), "No network connection", Toast.LENGTH_LONG).show();
                    return;
                }
                if (saveButton.getText().toString().contains("Destination") && destination != null) {
                    saveButton.setVisibility(View.INVISIBLE);
                    saveButton.setText("Save Departure");
                    finalDestination = destination;
                } else if (saveButton.getText().toString().contains("Departure") && myLocation != null) {
                    saveButton.setText("I'm ready to go!");
                    finalDeparture = myLocation;
                } else if (finalDeparture != null
                        && finalDestination != null) {
                    if (departureCity == null) {
                        String[] departureArray = finalDeparture.split(",");
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(Double.parseDouble(departureArray[0]), Double.parseDouble(departureArray[1]), 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String cityName = addresses.get(0).getLocality();
                        String stateName = addresses.get(0).getAdminArea();
                        departureCity = cityName + ", " + stateName;
                    } else if (destinationCity == null) {
                        String[] destinationArray = finalDestination.split(",");
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(Double.parseDouble(destinationArray[0]), Double.parseDouble(destinationArray[1]), 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        String cityName = addresses.get(0).getLocality();
                        String stateName = addresses.get(0).getAdminArea();
                        destinationCity = cityName + ", " + stateName;
                    }
                    String lat_dep = finalDeparture + "_" + departureCity;
                    String long_dest = finalDestination + "_" + destinationCity;
                    exploreUpdate.onLocationSelected(lat_dep, long_dest);
                } else {
                    Toast.makeText(getContext(), "Please select a departure!", Toast.LENGTH_LONG).show();
                }

            }
        });

        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInternetAvailable())  {
                    Toast.makeText(getContext(), "No network connection", Toast.LENGTH_LONG).show();
                    return;
                }
                getDeviceLocation();
                saveButton.setVisibility(View.VISIBLE);

            }
        });

        searchButtonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInternetAvailable())  {
                    Toast.makeText(getContext(), "No network connection", Toast.LENGTH_LONG).show();
                    return;
                }
                saveButton.setVisibility(View.VISIBLE);
                locateDestination();
                locationSearchText.setText("");
                locationSearchText.setHint("Locate yourself or type departure");
                hideKeyBoard();


            }
        });

        return exploreView;
    }

    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());


        try {
            if (permissions) {
                Task<Location> locationDevice = fusedLocationProviderClient.getLastLocation();
                locationDevice.addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            hideKeyBoard();
                            gMap.setMyLocationEnabled(true);
                            Location currentLocation = (Location) task.getResult();
                            if (currentLocation == null) {
                                Toast.makeText(getContext(), "Unable to retrieve your device location. Try again please", Toast.LENGTH_LONG).show();
                            } else {
                                if (finalDestination == null) {
                                    destination = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                                } else {
                                    myLocation = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
                                }

                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 15f, "My location");
                            }
                        } else {
                            Toast.makeText(getContext(), "Unable to retrieve your device location. Try again please", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }

        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    private void locateDestination() {
        hideKeyBoard();
        String location = locationSearchText.getText().toString();

        Geocoder geocoder = new Geocoder(getContext());
        List<Address> addressList = new ArrayList<>();

        try {

            addressList = geocoder.getFromLocationName(location, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            // we do this check in order to get the initial departure if the user does not want to locate themselves
            if (finalDestination != null) {
                String tempDep = "";
                tempDep += address.getLocality() + ", ";
                tempDep += address.getAdminArea();
                departureCity = tempDep;
                myLocation = address.getLatitude() + "," + address.getLongitude();
            } else {
                String tempDep = "";
                tempDep += address.getLocality() + ", ";
                tempDep += address.getAdminArea();
                destinationCity = tempDep;
                destination = address.getLatitude() + "," + address.getLongitude();
            }


            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 10, location);
        } else {
            Toast.makeText(getContext(), "Invalid location. Please try another location", Toast.LENGTH_LONG).show();
        }


    }

    private void moveCamera(LatLng latLng, float zoom, String locationTitle) {
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

        if (!locationTitle.equals("My location")) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng)
                    .title(locationTitle);
            gMap.addMarker(markerOptions);
        }

    }

    private void hideKeyBoard() {
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mapView.getWindowToken(), 0);
    }

    private boolean isInternetAvailable() {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(1000, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
        }
        return inetAddress != null && !inetAddress.equals("");
    }

    IExploreUpdate exploreUpdate;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ExploreFragment.IExploreUpdate) {
            exploreUpdate = (ExploreFragment.IExploreUpdate) context;
        } else {
            throw new RuntimeException(context.toString() + "must implement IFragmentUpdate");
        }
    }

    public interface IExploreUpdate {
        void onLocationSelected(String location, String destination);
    }
}