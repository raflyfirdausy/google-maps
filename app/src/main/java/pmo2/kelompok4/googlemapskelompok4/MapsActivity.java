package pmo2.kelompok4.googlemapskelompok4;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.PolyUtil;

import java.util.List;

import pmo2.kelompok4.googlemapskelompok4.Network.ApiServices;
import pmo2.kelompok4.googlemapskelompok4.Network.InitLibrary;
import pmo2.kelompok4.googlemapskelompok4.Response.Distance;
import pmo2.kelompok4.googlemapskelompok4.Response.Duration;
import pmo2.kelompok4.googlemapskelompok4.Response.LegsItem;
import pmo2.kelompok4.googlemapskelompok4.Response.ResponseRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lokasi_sekarang;
    private int REQUEST_CODE;
    private String API_KEY = "AIzaSyDjkDzp8QfPMPVKqn6gyFY3zflpZUsIFHw";
    private NestedScrollView bottomSheetLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private FloatingActionButton fab, fab2, fabPetunjuk;
    private TextView tv_namaTempatTujuan, tv_origin, tv_destination, tv_distance, tv_duration;
    private Button btn_petunjukArah;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        tv_namaTempatTujuan = (TextView) findViewById(R.id.tv_namaTempatTujuan);
        tv_origin = (TextView) findViewById(R.id.tv_origin);
        tv_destination = (TextView) findViewById(R.id.tv_destination);
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        btn_petunjukArah = (Button) findViewById(R.id.btn_petunjukArah);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fabPetunjuk = (FloatingActionButton) findViewById(R.id.fabPetunjuk);

        fab2.setVisibility(View.INVISIBLE);
        fabPetunjuk.setVisibility(View.INVISIBLE);

        bottomSheetLayout = (NestedScrollView) findViewById(R.id.nv_bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); //hide bottom sheet

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    fab.setVisibility(View.INVISIBLE);
                    fab2.setVisibility(View.VISIBLE);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    fab.setVisibility(View.VISIBLE);
                    fab2.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        tv_namaTempatTujuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    tv_namaTempatTujuan.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.ic_keyboard_arrow_down_black_24dp, 0);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    tv_namaTempatTujuan.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                            R.drawable.ic_keyboard_arrow_up_black_24dp, 0);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    fabPetunjuk.setVisibility(View.INVISIBLE);
                }
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                getLokasiSekarang();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.clear();
                getLokasiSekarang();
            }
        });


        cek_pemission();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //placeauto complete
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("ID").build();
        PlaceAutocompleteFragment autoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        autoCompleteFragment.setFilter(typeFilter);
        autoCompleteFragment.setHint("Cari Tempat Disini");
        autoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                actionRoute(place);
            }

            @Override
            public void onError(Status status) {
                gawe_toast(status.getStatusMessage());
            }
        });

    }


    // 7
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLokasiSekarang();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setTrafficEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
    }

    private void getLokasiSekarang() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            Task lokasi = fusedLocationProviderClient.getLastLocation();
            lokasi.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        gawe_toast("Berhasil mendapatkan Lokasi");
                        lokasi_sekarang = (Location) task.getResult();
                        pindah_kamera(new LatLng(lokasi_sekarang.getLatitude(),
                                        lokasi_sekarang.getLongitude()),
                                "Lokasi Saya", false);
                    } else {
                        gawe_toast("Gagal mendapatkan Lokasi");
                    }
                }
            });

        } catch (SecurityException e) {
            gawe_toast(e.getMessage());
        }
    }

    private void getBottomSheet(String tempatTujuan, String tempatAsal, String tujuan, String jarak, String durasi) {
        tv_namaTempatTujuan.setText(tempatTujuan);
        tv_origin.setText(tempatAsal);
        tv_destination.setText(tujuan);
        tv_distance.setText(jarak);
        tv_duration.setText(durasi);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void cek_pemission() {
        String[] permission = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permission, 1234);
        }
    }

    // 2
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1234: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            finish();
                            return;
                        }
                    }
                    gawe_toast("Permission granted");
                }
            }
        }
    }


    // 5
    private void pindah_kamera(LatLng latLng, String title, boolean marker) {

        LatLngBounds.Builder latLongBuilder = new LatLngBounds.Builder();
        latLongBuilder.include(latLng);

        // Bounds Coordinata
        LatLngBounds bounds = latLongBuilder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int paddingMap = (int) (width * 0.2); //jarak dari
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, paddingMap);
        mMap.animateCamera(cu);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));

        if (marker) {
            mMap.clear();
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(title);
            mMap.addMarker(markerOptions);
        }

    }


//    // 6
//    private void geolokasi() {
//        String text_cari = et_cari.getText().toString();
//
//        Geocoder geocoder = new Geocoder(this);
//        List<Address> list = new ArrayList<>();
//        try{
//            list = geocoder.getFromLocationName(text_cari, 1);
//        } catch (IOException e) {
//            gawe_toast(e.getMessage());
//        }
//
//        if(list.size() > 0){
//            Address address = list.get(0);
//            pindah_kamera(new LatLng(address.getLatitude(), address.getLongitude()),
//                    address.getAddressLine(0));
//        }
//    }


    // 0
    private void gawe_toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }


    private void showPlaceAutoComplete(int type) {
        this.REQUEST_CODE = type;
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder().setCountry("ID").build();
        try {
            Intent mIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(mIntent, REQUEST_CODE);
        } catch (GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            gawe_toast(e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Place placeData = PlaceAutocomplete.getPlace(this, data);

            if (placeData.isDataValid()) {
                String placeAddress = placeData.getAddress().toString();
                LatLng placeLatLng = placeData.getLatLng();
                String placeName = placeData.getName().toString();

                switch (REQUEST_CODE) {
                    case 1:
                        pindah_kamera(placeData.getLatLng(), placeData.getName().toString(), true);
                        break;
                    case 2:
                        pindah_kamera(placeData.getLatLng(), placeData.getName().toString(), true);
                        break;
                }
            } else {
                gawe_toast("Tempat Tidak Di temukan !");
            }
        }
    }

    private void actionRoute(final Place place) {
        final LatLng lokasiAwal = new LatLng(lokasi_sekarang.getLatitude(), lokasi_sekarang.getLongitude());
        final LatLng lokasiAkhir = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

        // Panggil Retrofit
        ApiServices api = InitLibrary.getInstance();

        // Siapkan request
        Call<ResponseRoute> routeRequest = api.request_route(
                lokasiAwal.latitude + "," + lokasiAwal.longitude,
                lokasiAkhir.latitude + "," + lokasiAkhir.longitude,
                API_KEY, "id");

        // kirim request
        routeRequest.enqueue(new Callback<ResponseRoute>() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onResponse(Call<ResponseRoute> call, Response<ResponseRoute> response) {
                if (response.isSuccessful()) {
                    try {
                        // tampung response ke variable
                        final ResponseRoute dataDirection = response.body();

                        LegsItem dataLegs = dataDirection.getRoutes().get(0).getLegs().get(0);

                        // Dapatkan jarak dan duration
                        Distance dataDistance = dataLegs.getDistance();
                        Duration dataDuration = dataLegs.getDuration();

                        //tujuan, asal, jarak, durasi
                        getBottomSheet(place.getName().toString(),
                                "Lokasi Kamu Sekarang" + "\n" + dataLegs.getStartAddress(),
                                place.getName().toString() + "\n" + dataLegs.getEndAddress(),
                                dataLegs.getDistance().getText(),
                                dataLegs.getDuration().getText());

                        pindah_kamera(place.getLatLng(), place.getName().toString(), true);
                        btn_petunjukArah.setVisibility(View.GONE);
                        fabPetunjuk.setVisibility(View.VISIBLE);
                        fabPetunjuk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Dapatkan garis polyline
                                String polylinePoint = dataDirection.getRoutes().get(0).getOverviewPolyline().getPoints();
                                // Decode
                                List<LatLng> decodePath = PolyUtil.decode(polylinePoint);
                                mMap.clear();
                                // Gambar garis ke maps
                                mMap.addPolyline(new PolylineOptions().addAll(decodePath)
                                        .width(8f).color(Color.argb(255, 56, 167, 252)))
                                        .setGeodesic(true);

                                // Tambah Marker
                                mMap.addMarker(new MarkerOptions().position(lokasiAwal).title("Lokasi Kamu Sekarang"));

                                mMap.addMarker(new MarkerOptions().position(lokasiAkhir).title(place.getName().toString() + "\n" + place.getAddress()));

                                /** START Logic untuk membuat layar berada ditengah2 dua koordinat */

                                LatLngBounds.Builder latLongBuilder = new LatLngBounds.Builder();
                                latLongBuilder.include(lokasiAwal);
                                latLongBuilder.include(lokasiAkhir);

                                // Bounds Coordinata
                                LatLngBounds bounds = latLongBuilder.build();

                                int width = getResources().getDisplayMetrics().widthPixels;
                                int height = getResources().getDisplayMetrics().heightPixels;
                                int paddingMap = (int) (width * 0.2); //jarak dari
                                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, paddingMap);
                                mMap.animateCamera(cu);
                                /** END Logic untuk membuat layar berada ditengah2 dua koordinat */

//                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN); //hide bottom sheet
                                fabPetunjuk.setVisibility(View.INVISIBLE);
                            }
                        });

                    } catch (Exception e) {
                        gawe_toast(e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseRoute> call, Throwable t) {
                gawe_toast(t.getMessage());
            }
        });
    }


}
