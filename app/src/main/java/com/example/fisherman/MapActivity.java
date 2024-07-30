package com.example.fisherman;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String currentUserUid;
    private Marker tempMarker;
    private View bottomSheet;
    private View bottomSheetSecond;
    private EditText edtTypeOfFishing, edtFishSpecies, edtComments;
    private List<Port> portsList = new ArrayList<>();

    private Spinner spinnerMarkerIcons;
    private int selectedMarkerIcon;
    private double selectedLat, selectedLng;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "MainActivity"; // Etiqueta para los logs
    private LatLng selectedLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        db = FirebaseFirestore.getInstance();
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetSecond = findViewById(R.id.bottom_sheet_second);

        edtTypeOfFishing = findViewById(R.id.edtTypeOfFishing);
        edtFishSpecies = findViewById(R.id.edtFishSpecies);
        edtComments = findViewById(R.id.edtComments);

        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);
        ImageView btnClose = findViewById(R.id.btnClose);

        btnSave.setOnClickListener(v -> savePinDetails());
        btnCancel.setOnClickListener(v -> cancelPinCreation());
        btnClose.setOnClickListener(v -> hideBottomSheet());

        bottomSheet.setVisibility(View.GONE);
        bottomSheetSecond.setVisibility(View.GONE);

        spinnerMarkerIcons = findViewById(R.id.spinnerMarkerIcons);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.marker_icons_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMarkerIcons.setAdapter(adapter);

        spinnerMarkerIcons.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMarkerIcon = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se hace nada
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Habilitar controles de zoom y ubicación
        mMap.getUiSettings().setZoomControlsEnabled(true); // Deshabilitar los controles de zoom por defecto
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Solicitar permisos de ubicación y centrar el mapa en la ubicación del usuario
        enableMyLocation();

        mMap.setOnMapClickListener(latLng -> {
            if (tempMarker != null) {
                tempMarker.remove();
            }
            tempMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Nuevo Pin")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

            selectedLat = latLng.latitude;
            selectedLng = latLng.longitude;

            bottomSheet.setVisibility(View.VISIBLE);
            bottomSheetSecond.setVisibility(View.GONE);
        });

        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        obtainSavedLocations();



    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            mMap.setMyLocationEnabled(true);
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            float zoomLevel = 15.0f;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, zoomLevel));
                        }
                    });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                // Permiso denegado, manejarlo según sea necesario
            }
        }
    }


    private void savePinDetails() {
        String typeOfFishing = edtTypeOfFishing.getText().toString();
        String fishSpecies = edtFishSpecies.getText().toString();
        String comments = edtComments.getText().toString();

        if (typeOfFishing.isEmpty()) {
            Toast.makeText(this, "Ingrese el tipo de pesca", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cerrar el teclado virtual
        hideKeyboard();

        Map<String, Object> pinData = new HashMap<>();
        pinData.put("latLng", new GeoPoint(selectedLat, selectedLng));
        pinData.put("typeOfFishing", typeOfFishing);
        pinData.put("fishSpecies", fishSpecies);
        pinData.put("comments", comments);
        pinData.put("userId", currentUserUid);

        db.collection("usuarios").document(currentUserUid)
                .collection("savedLocations")
                .add(pinData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(MapActivity.this, "Pin guardado exitosamente", Toast.LENGTH_SHORT).show();
                    addMarkerToMap(new GeoPoint(selectedLat, selectedLng), typeOfFishing, fishSpecies, comments);
                    hideBottomSheet();

                    // Borrar los campos
                    edtTypeOfFishing.setText("");
                    edtFishSpecies.setText("");
                    edtComments.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MapActivity.this, "Error al guardar el pin", Toast.LENGTH_SHORT).show();
                });
    }


    private void cancelPinCreation() {
        if (tempMarker != null) {
            tempMarker.remove();
            tempMarker = null;
        }
        bottomSheet.setVisibility(View.GONE);
    }

    private void obtainSavedLocations() {
        db.collection("usuarios").document(currentUserUid)
                .collection("savedLocations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            GeoPoint geoPoint = document.getGeoPoint("latLng");
                            String typeOfFishing = document.getString("typeOfFishing");
                            String fishSpecies = document.getString("fishSpecies");
                            String comments = document.getString("comments");

                            if (geoPoint != null) {
                                addMarkerToMap(geoPoint, typeOfFishing, fishSpecies, comments);
                            }
                        }
                    } else {
                        Toast.makeText(MapActivity.this, "Error al obtener lugares guardados", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void addMarkerToMap(GeoPoint geoPoint, String typeOfFishing, String fishSpecies, String comments) {
        LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(typeOfFishing)
                .snippet("Especie: " + fishSpecies + "\nComentarios: " + comments + "\nUbicación: " + latLng.toString());

        // Seleccionar el ícono del marcador basado en la selección del Spinner
        switch (selectedMarkerIcon) {
            case 0:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                break;
            case 1:
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                break;
            // Agrega más casos aquí según los íconos disponibles
        }

        mMap.addMarker(markerOptions);
    }


    private void showSecondBottomSheet() {
        View bottomSheetSecond = findViewById(R.id.bottom_sheet_second);
        bottomSheetSecond.setVisibility(View.VISIBLE);

        // Configurar acciones para los botones del segundo bottom sheet
        Button btnAction1 = findViewById(R.id.btnAction1);
        Button btnAction2 = findViewById(R.id.btnAction2);
        ImageView btnCloseSecond = findViewById(R.id.btnCloseSecond);
        btnAction1.setOnClickListener(v -> fetchPortsAndShowNearest(selectedLocation));
        String targetDate = getCurrentDate();
        btnAction2.setOnClickListener(v -> fetchFiveDayForecast(selectedLocation,targetDate));

        btnCloseSecond.setOnClickListener(v -> {
            // Ocultar el segundo bottom sheet al hacer clic en el botón de cerrar
            bottomSheetSecond.setVisibility(View.GONE);
        });
    }
    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.now().format(formatter);
    }

    private void fetchFiveDayForecast(LatLng location, String targetDate) {
        String apiKey = "5a2112d4e8efe6b74cec896e15f1a230"; // Reemplaza con tu clave API
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + location.latitude + "&lon=" + location.longitude + "&appid=" + apiKey + "&units=metric";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error al obtener la predicción", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // Iniciar WeatherActivity con los datos del pronóstico y la fecha objetivo
                            Intent intent = new Intent(MapActivity.this, WeatherActivity.class);
                            intent.putExtra("weatherData", responseData);
                            intent.putExtra("targetDate", targetDate);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error al procesar la respuesta", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Respuesta no exitosa de la API: Código " + response.code());
                    runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error en la respuesta de la API: Código " + response.code(), Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    private void fetchPortsAndShowNearest(LatLng location) {
        String url = "https://ideihm.covam.es/api-ihm/getmarea?request=getlist&format=json";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MapActivity.this, "Error al obtener la lista de puertos", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    runOnUiThread(() -> {
                        try {
                            // Parsear la respuesta JSON
                            JSONObject jsonObject = new JSONObject(responseData);
                            JSONObject estaciones = jsonObject.getJSONObject("estaciones");
                            JSONArray puertosArray = estaciones.getJSONArray("puertos");

                            // Limpiar la lista de puertos antes de agregar los nuevos
                            portsList.clear();
                            for (int i = 0; i < puertosArray.length(); i++) {
                                JSONObject portObject = puertosArray.getJSONObject(i);
                                String id = portObject.getString("id");
                                String code = portObject.getString("code");
                                String puerto = portObject.getString("puerto");
                                double lat = portObject.getDouble("lat");
                                double lon = portObject.getDouble("lon");

                                // Crear un objeto Port y agregarlo a la lista
                                portsList.add(new Port(id, code, puerto, lat, lon));
                            }

                            // Encontrar y mostrar el puerto más cercano
                            Port nearestPort = findNearestPort(location);
                            if (nearestPort != null) {
                                showNearestPortInfo(nearestPort);
                            } else {
                                Toast.makeText(MapActivity.this, "No se encontró un puerto cercano", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error al parsear la respuesta", e);
                        }
                    });
                } else {
                    Log.e(TAG, "Respuesta no exitosa de la API: Código " + response.code());
                }
            }
        });
    }


    private Port findNearestPort(LatLng location) {
        Port nearestPort = null;
        double minDistance = Double.MAX_VALUE;

        for (Port port : portsList) {
            LatLng portLocation = new LatLng(port.getLatitude(), port.getLongitude());
            double distance = SphericalUtil.computeDistanceBetween(location, portLocation);

            if (distance < minDistance) {
                minDistance = distance;
                nearestPort = port;
            }
        }

        if (nearestPort != null) {
            showNearestPortInfo(nearestPort);
        }
        return nearestPort;
    }
    private void showNearestPortInfo(Port port) {
        Log.d(TAG, "Puerto más cercano: " + port.getPuerto());
        Intent intent = new Intent(MapActivity.this, TideChartActivity.class);
        intent.putExtra("portId", port.getId());
        startActivity(intent);
    }



    private void hideSecondBottomSheet() {
        bottomSheetSecond.setVisibility(View.GONE);
    }
    public void removeTempMarker() {
        if (tempMarker != null) {
            tempMarker.remove();
            tempMarker = null;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(edtTypeOfFishing.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(edtFishSpecies.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(edtComments.getWindowToken(), 0);
        }
    }

    public void hideBottomSheet() {
        bottomSheet.setVisibility(View.GONE);
        removeTempMarker();
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        CustomInfoWindowAdapter() {
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            render(marker, mWindow);
            return mWindow;
        }

        private void render(Marker marker, View view) {
            String title = marker.getTitle();
            String snippet = marker.getSnippet();
            String[] snippetParts = snippet != null ? snippet.split("\n") : new String[]{};

            TextView titleUi = view.findViewById(R.id.title);
            TextView tvFishSpecies = view.findViewById(R.id.tvFishSpecies);
            TextView tvComments = view.findViewById(R.id.tvComments);
            TextView tvLatLng = view.findViewById(R.id.tvLatLng);

            titleUi.setText(title != null ? title : "");
            tvFishSpecies.setText(snippetParts.length > 0 ? snippetParts[0] : "");
            tvComments.setText(snippetParts.length > 1 ? snippetParts[1] : "");
            tvLatLng.setText(snippetParts.length > 2 ? snippetParts[2] : "");

            // Ocultar el bottomSheet
            if (bottomSheet.getVisibility() == View.VISIBLE) {
                bottomSheet.setVisibility(View.GONE);
            }

            // Mostrar el segundo bottomSheet
            showSecondBottomSheet();

            selectedLocation = marker.getPosition();

        }
    }
}
