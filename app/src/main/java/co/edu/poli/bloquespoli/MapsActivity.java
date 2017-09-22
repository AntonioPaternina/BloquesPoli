package co.edu.poli.bloquespoli;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    private GoogleMap mMap;
    private Location ubicacionUsuario;
    private double distanciaBloqueA;
    private double distanciaBloqueB;
    private double distanciaBloqueC;
    private double distanciaBloqueD;
    private double distanciaBloqueE;

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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        buscarUbicacionActual(locationManager);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.ubicacionUsuario = location;
        actualizarDistancias();
        actualizarLabelsDistancias();
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
            return;
        }
        this.ubicacionUsuario = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (this.ubicacionUsuario != null) {
            this.onLocationChanged(this.ubicacionUsuario);
            LatLng ubiccionActual = new LatLng(this.ubicacionUsuario.getLatitude(), this.ubicacionUsuario.getLongitude());
            mMap.addMarker(new MarkerOptions().position(ubiccionActual).title("Mi Posición"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubiccionActual, 15));

            actualizarDistancias();
            actualizarLabelsDistancias();
        }
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
}
