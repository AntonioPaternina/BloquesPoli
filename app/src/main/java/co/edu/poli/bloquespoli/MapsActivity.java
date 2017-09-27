package co.edu.poli.bloquespoli;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static final LatLng UBICACION_BLOQE_A = new LatLng(4.637266, -74.054832);
    public static final LatLng UBICACION_BLOQE_B = new LatLng(4.636960, -74.054911);
    public static final LatLng UBICACION_BLOQE_C = new LatLng(4.636575, -74.054591);
    public static final LatLng UBICACION_BLOQE_D = new LatLng(4.636671, -74.054993);
    public static final LatLng UBICACION_BLOQE_E = new LatLng(4.636386, -74.054753);
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    public static final String ARCHIVO_PREFERENCIAS = "BLOQUES_PREFERENCIAS";
    public static final String KEY_LATITUD = "latitud";
    public static final String KEY_LONGITUD = "longitud";
    public static final String KEY_NOMBRE_MARCADOR = "nombreMarcador";

    private GoogleMap mMap;
    private Location ubicacionUsuario;
    private double distanciaBloqueA;
    private double distanciaBloqueB;
    private double distanciaBloqueC;
    private double distanciaBloqueD;
    private double distanciaBloqueE;

    private static final String NOMBRE_IMAGEN = "FOTO";
    private MarkerOptions marcadorUbicacionActual;
    private MarkerOptions marcadorFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final Context that = this;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
        buscarUbicacionActual(locationManager);

        final Button button = findViewById(R.id.botonGuardar);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences settings = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
                SharedPreferences.Editor editor = settings.edit();

                EditText campoTextoMarcador = findViewById(R.id.campoTextoMarcador);
                String texto = campoTextoMarcador.getText().toString();
                editor.putString(KEY_NOMBRE_MARCADOR, texto);
                editor.commit();

                Toast.makeText(that, "Guardado Exitosamente", Toast.LENGTH_SHORT).show();
            }
        });
        this.mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.ubicacionUsuario = location;
        actualizarDistancias();
        actualizarLabelsDistancias();
        actualizarMarcadorPosicionActual();
    }

    private void actualizarDistancias() {
        this.distanciaBloqueA = calcularDistanciaEnMetros(UBICACION_BLOQE_A);
        this.distanciaBloqueB = calcularDistanciaEnMetros(UBICACION_BLOQE_B);
        this.distanciaBloqueC = calcularDistanciaEnMetros(UBICACION_BLOQE_C);
        this.distanciaBloqueD = calcularDistanciaEnMetros(UBICACION_BLOQE_D);
        this.distanciaBloqueE = calcularDistanciaEnMetros(UBICACION_BLOQE_E);
    }

    private void actualizarLabelsDistancias() {
        List<TextView> textViewsDistancias = new ArrayList<>();
        textViewsDistancias.add((TextView) findViewById(R.id.tvBloqueA));
        textViewsDistancias.add((TextView) findViewById(R.id.tvBloqueB));
        textViewsDistancias.add((TextView) findViewById(R.id.tvBloqueC));
        textViewsDistancias.add((TextView) findViewById(R.id.tvBloqueD));
        textViewsDistancias.add((TextView) findViewById(R.id.tvBloqueE));

        Map<Double, String> mapaDistancias = new HashMap<>();
        mapaDistancias.put(this.distanciaBloqueA, "Bloque A");
        mapaDistancias.put(this.distanciaBloqueB, "Bloque B");
        mapaDistancias.put(this.distanciaBloqueC, "Bloque C");
        mapaDistancias.put(this.distanciaBloqueD, "Bloque D");
        mapaDistancias.put(this.distanciaBloqueE, "Bloque E");

        double[] distancias = new double[5];
        distancias[0] = this.distanciaBloqueA;
        distancias[1] = this.distanciaBloqueB;
        distancias[2] = this.distanciaBloqueC;
        distancias[3] = this.distanciaBloqueD;
        distancias[4] = this.distanciaBloqueE;

        Arrays.sort(distancias);

        int i = 0;
        for (double distancia : distancias) {
            TextView label = textViewsDistancias.get(i++);
            String texto = mapaDistancias.get(distancia) + " = " + distancia + " m";
            label.setText(texto);
        }
    }

    private float calcularDistanciaEnMetros(LatLng destino) {
        float[] distancias = new float[1];
        Location.distanceBetween(
                this.ubicacionUsuario.getLatitude(),
                this.ubicacionUsuario.getLongitude(),
                destino.latitude,
                destino.longitude,
                distancias);
        return distancias[0];
    }

    private void buscarUbicacionActual(LocationManager locationManager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        this.ubicacionUsuario = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (this.ubicacionUsuario == null) {
            this.ubicacionUsuario = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (this.ubicacionUsuario != null) {
            this.onLocationChanged(this.ubicacionUsuario);
            actualizarMarcadorPosicionActual();

            SharedPreferences sharedPreferences = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
            MarkerOptions marcadorPosicionGuardada = new MarkerOptions();
            marcadorPosicionGuardada.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            LatLng ubicacionGuardada;
            if (sharedPreferences.contains(KEY_LATITUD) && sharedPreferences.contains(KEY_LONGITUD) && sharedPreferences.contains(KEY_NOMBRE_MARCADOR)) {
                String latitudString = sharedPreferences.getString(KEY_LATITUD, "");
                String longitudString = sharedPreferences.getString(KEY_LONGITUD, "");
                float latitudGuardada = Float.parseFloat(latitudString);

                float longitudGuardada = Float.parseFloat(longitudString);
                String textoGuardado = sharedPreferences.getString(KEY_NOMBRE_MARCADOR, "");
                ubicacionGuardada = new LatLng(latitudGuardada, longitudGuardada);
                marcadorPosicionGuardada.position(ubicacionGuardada);
                marcadorPosicionGuardada.title(textoGuardado);
            } else {
                ubicacionGuardada = new LatLng(4.636775, -74.054765);
                marcadorPosicionGuardada.position(new LatLng(4.636775, -74.054765));
                marcadorPosicionGuardada.title("Posición Predeterminada");
            }
            mMap.addMarker(marcadorPosicionGuardada);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionGuardada, 18));

            mMap.addMarker(new MarkerOptions().position(UBICACION_BLOQE_A).title("Bloque A"));
            mMap.addMarker(new MarkerOptions().position(UBICACION_BLOQE_B).title("Bloque B"));
            mMap.addMarker(new MarkerOptions().position(UBICACION_BLOQE_C).title("Bloque C"));
            mMap.addMarker(new MarkerOptions().position(UBICACION_BLOQE_D).title("Bloque D"));
            mMap.addMarker(new MarkerOptions().position(UBICACION_BLOQE_E).title("Bloque E"));

            actualizarDistancias();
            actualizarLabelsDistancias();
        }
    }

    private void actualizarMarcadorPosicionActual() {
        LatLng ubicacionActual = new LatLng(this.ubicacionUsuario.getLatitude(), this.ubicacionUsuario.getLongitude());
        if (this.marcadorUbicacionActual == null) {
            this.marcadorUbicacionActual = new MarkerOptions();
            mMap.addMarker(marcadorUbicacionActual
                    .position(ubicacionActual)
                    .title("Posicion Actual")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
        this.marcadorUbicacionActual.position(ubicacionActual);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences settings = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
        SharedPreferences.Editor editor = settings.edit();

        String t = String.valueOf(ubicacionUsuario.getLatitude());
        editor.putString(KEY_LATITUD, t);
        editor.commit();

        t = String.valueOf(ubicacionUsuario.getLongitude());
        editor.putString(KEY_LONGITUD, t);
        editor.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    actualizarDistancias();
                    actualizarLabelsDistancias();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    public void tomarFoto(View view) {
        enviarSolicitudTomarFoto();
    }

    private void enviarSolicitudTomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap foto = (Bitmap) extras.get("data");
            guardarImagen(foto);
            LatLng ubicacionActual = new LatLng(this.ubicacionUsuario.getLatitude(), this.ubicacionUsuario.getLongitude());

            if (this.marcadorFoto == null) {
                this.marcadorFoto = new MarkerOptions();
                this.marcadorFoto.title("Foto Guardada")
                        .icon(BitmapDescriptorFactory.fromBitmap(foto))
                        .position(ubicacionActual);
                mMap.addMarker(this.marcadorFoto);
            }
            this.marcadorFoto.title("Foto Guardada")
                    .icon(BitmapDescriptorFactory.fromBitmap(foto))
                    .position(ubicacionActual);
        }
    }

    private void guardarImagen(Bitmap imagen) {
        try {
            FileOutputStream fos = openFileOutput(NOMBRE_IMAGEN, Context.MODE_PRIVATE);
            imagen.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Bitmap leerImagen() {
        Bitmap fotoGuardada = BitmapFactory.decodeFile(NOMBRE_IMAGEN);
        return fotoGuardada;
    }
}
