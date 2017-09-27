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
import android.support.v4.content.ContextCompat;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileInputStream;
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
    private static final int SOLICITUD_ACCESS_FINE_LOCATION = 1;
    private static final int PETICION_CAPTURA_IMAGEN = 2;
    private static final int SOLICITUD_ACCESS_COARSE_LOCATION = 3;
    private static final int SOLICITUD_CAMERA = 4;
    private static final int SOLICITUD_WRITE_EXTERNAL_STORAGE = 5;
    private static final int SOLICITUD_READ_EXTERNAL_STORAGE = 6;


    public static final String ARCHIVO_PREFERENCIAS = "BLOQUES_PREFERENCIAS";
    public static final String KEY_LATITUD_UBICACION_GUARDADA = "latitud";
    public static final String KEY_LONGITUD_UBICACION_GUARDADA = "longitud";
    public static final String KEY_NOMBRE_MARCADOR = "nombreMarcador";
    public static final String NOMBRE_IMAGEN = "FOTO";
    public static final String KEY_LATITUD_FOTO = "KEY_LATITUD_FOTO";
    public static final String KEY_LONGITUD_FOTO = "KEY_LONGITUD_FOTO";

    private GoogleMap mMap;
    private Location ubicacionUsuario;
    private double distanciaBloqueA;
    private double distanciaBloqueB;
    private double distanciaBloqueC;
    private double distanciaBloqueD;
    private double distanciaBloqueE;

    private MarkerOptions markerOptionsUbicacionActual;
    private Marker markerUbicacionActual;
    private MarkerOptions markerOptionsUbicacionGuardada;
    private Marker markerUbicacionGuardada;
    private MarkerOptions markerOptionsFoto;
    private Marker markerFoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_maps);
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            agregarHandlerBotonGuardado(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        try {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                verificarGPS();
                return;
            }
            this.mMap.setMyLocationEnabled(true);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, this);
            buscarPosicionGuardada(locationManager);
            cargarMarcadorFotoGuardada();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void agregarHandlerBotonGuardado(final Context that) {
        final Button button = findViewById(R.id.botonGuardar);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                verificarAlmacenamiento();
                SharedPreferences settings = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
                SharedPreferences.Editor editor = settings.edit();

                EditText campoTextoMarcador = findViewById(R.id.campoTextoMarcador);
                String texto = campoTextoMarcador.getText().toString();
                editor.putString(KEY_NOMBRE_MARCADOR, texto);
                editor.commit();

                Toast.makeText(that, "Guardado Exitosamente", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        verificarGPS();
        try {
            this.ubicacionUsuario = location;
            actualizarDistancias();
            actualizarLabelsDistancias();
            actualizarMarcadorPosicionActual();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void buscarPosicionGuardada(LocationManager locationManager) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            verificarGPS();
            return;
        }
        this.ubicacionUsuario = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (this.ubicacionUsuario == null) {
            this.ubicacionUsuario = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (this.ubicacionUsuario != null) {
            this.onLocationChanged(this.ubicacionUsuario);
            actualizarMarcadorPosicionActual();

            this.markerOptionsUbicacionGuardada = new MarkerOptions();
            this.markerOptionsUbicacionGuardada.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            LatLng ubicacionGuardada = cargarUbicacionGuardada(KEY_LATITUD_UBICACION_GUARDADA, KEY_LONGITUD_UBICACION_GUARDADA);
            String textoGuardado = cargarNombreMarcadorGuardado(KEY_NOMBRE_MARCADOR);
            if (ubicacionGuardada != null) {
                this.markerOptionsUbicacionGuardada.position(ubicacionGuardada);
                if (textoGuardado != null && !textoGuardado.trim().equals("")) {
                    this.markerOptionsUbicacionGuardada.title(textoGuardado);
                } else {
                    this.markerOptionsUbicacionGuardada.title("Marcador Guardado");
                }
            } else {
                ubicacionGuardada = new LatLng(4.636775, -74.054765);
                this.markerOptionsUbicacionGuardada.position(new LatLng(4.636775, -74.054765));
                this.markerOptionsUbicacionGuardada.title("Posición Predeterminada");
            }
            markerUbicacionGuardada = mMap.addMarker(this.markerOptionsUbicacionGuardada);
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

    private LatLng cargarUbicacionGuardada(String keyLatitud, String keyLongitud) {
        verificarAlmacenamiento();
        LatLng ubicacionGuardada = null;
        SharedPreferences sharedPreferences = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
        if (sharedPreferences.contains(keyLatitud) && sharedPreferences.contains(keyLongitud)) {
            String latitudString = sharedPreferences.getString(keyLatitud, "");
            String longitudString = sharedPreferences.getString(keyLongitud, "");
            float latitudGuardada = Float.parseFloat(latitudString);
            float longitudGuardada = Float.parseFloat(longitudString);
            ubicacionGuardada = new LatLng(latitudGuardada, longitudGuardada);
        }
        return ubicacionGuardada;
    }

    private String cargarNombreMarcadorGuardado(String keyMarcador) {
        String textoGuardado = null;
        SharedPreferences sharedPreferences = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
        if (sharedPreferences.contains(keyMarcador)) {
            textoGuardado = sharedPreferences.getString(keyMarcador, null);
        }
        return textoGuardado;
    }

    private void cargarMarcadorFotoGuardada() {
        Bitmap foto = leerImagen();
        if (foto != null) {
            reemplazarMarcadorPosicionFoto(foto, cargarUbicacionGuardada(KEY_LATITUD_FOTO, KEY_LONGITUD_FOTO));
        }
    }

    private void actualizarMarcadorPosicionActual() {
        if (this.markerUbicacionActual != null) {
            this.markerUbicacionActual.remove();
        }
        LatLng ubicacionActual = new LatLng(this.ubicacionUsuario.getLatitude(), this.ubicacionUsuario.getLongitude());
        this.markerOptionsUbicacionActual = new MarkerOptions();
        this.markerOptionsUbicacionActual.position(ubicacionActual)
                .title("Posicion Actual")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        this.markerUbicacionActual = mMap.addMarker(this.markerOptionsUbicacionActual);
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
    protected void onStop() {
        super.onStop();

        try {
            guardarUbicacion(this.ubicacionUsuario, KEY_LATITUD_UBICACION_GUARDADA, KEY_LONGITUD_UBICACION_GUARDADA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            guardarUbicacion(this.ubicacionUsuario, KEY_LATITUD_UBICACION_GUARDADA, KEY_LONGITUD_UBICACION_GUARDADA);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void guardarUbicacion(Location ubicacion, String keyLatitud, String keyLongitud) {
        SharedPreferences settings = getSharedPreferences(ARCHIVO_PREFERENCIAS, 0);
        SharedPreferences.Editor editor = settings.edit();

        String t = String.valueOf(ubicacion.getLatitude());
        editor.putString(keyLatitud, t);
        editor.commit();

        t = String.valueOf(ubicacion.getLongitude());
        editor.putString(keyLongitud, t);
        editor.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case SOLICITUD_ACCESS_FINE_LOCATION: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        onMapReady(this.mMap);
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                case SOLICITUD_CAMERA: {
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        handlerTomarFoto();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tomarFoto(View view) {
        try {
            handlerTomarFoto();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handlerTomarFoto() {
        verificarCamara();
        enviarSolicitudTomarFoto();
    }

    private void enviarSolicitudTomarFoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, PETICION_CAPTURA_IMAGEN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == PETICION_CAPTURA_IMAGEN && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                Bitmap foto = (Bitmap) extras.get("data");
                guardarImagen(foto);
                guardarUbicacion(this.ubicacionUsuario, KEY_LATITUD_FOTO, KEY_LONGITUD_FOTO);
                LatLng ubicacionActual = new LatLng(this.ubicacionUsuario.getLatitude(), this.ubicacionUsuario.getLongitude());

                if (this.markerOptionsFoto == null) {
                    this.markerOptionsFoto = new MarkerOptions();
                    this.markerOptionsFoto.title("Foto")
                            .icon(BitmapDescriptorFactory.fromBitmap(foto))
                            .position(ubicacionActual);
                    this.markerFoto = mMap.addMarker(this.markerOptionsFoto);
                } else {
                    reemplazarMarcadorPosicionFoto(foto, ubicacionActual);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reemplazarMarcadorPosicionFoto(Bitmap foto, LatLng ubicacionActual) {
        if (this.markerFoto != null) {
            this.markerFoto.remove();
        }
        if (this.markerOptionsFoto == null) {
            this.markerOptionsFoto = new MarkerOptions();
        }
        this.markerOptionsFoto.title("Foto Guardada")
                .icon(BitmapDescriptorFactory.fromBitmap(foto))
                .position(ubicacionActual);
        this.markerFoto = mMap.addMarker(this.markerOptionsFoto);
    }

    private void guardarImagen(Bitmap imagen) {
        verificarAlmacenamiento();
        try {
            FileOutputStream fos = openFileOutput(NOMBRE_IMAGEN, Context.MODE_PRIVATE);
            imagen.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Bitmap leerImagen() {
        verificarAlmacenamiento();
        Bitmap foto = null;

        try (FileInputStream fileInputStream = openFileInput(NOMBRE_IMAGEN);) {
            foto = BitmapFactory.decodeStream(fileInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return foto;
    }

    private void verificarGPS() {
        verificarPermiso(Manifest.permission.ACCESS_FINE_LOCATION, SOLICITUD_ACCESS_FINE_LOCATION);
    }

    private void verificarCamara() {
        verificarPermiso(Manifest.permission.CAMERA, SOLICITUD_CAMERA);
    }

    private void verificarAlmacenamiento() {
        verificarPermiso(Manifest.permission.WRITE_EXTERNAL_STORAGE, SOLICITUD_WRITE_EXTERNAL_STORAGE);
    }

    private void verificarPermiso(String nombrePermiso, int codigoSolicitud) {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, nombrePermiso);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermission(nombrePermiso, codigoSolicitud);
        }
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }
}
