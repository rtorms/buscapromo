package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.R;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Serializable {

    public static final String PREFS_NAME = "prefs";
    private GoogleMap mMap;
    private List<Promocao> listPromocoes;
    private Double somaTotal = 0.0;
    private Promocao promocao;
    private LatLng pato;
    private List<String> nomes = new ArrayList<>();
    private DecimalFormat precision = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listPromocoes = new ArrayList<>();
        promocao = new Promocao();


        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        Gson gson = new Gson();
        String json = settings.getString("prefs", "");
        listPromocoes = gson.fromJson(json, new TypeToken<List<Promocao>>() {
        }.getType());

        pato = new LatLng(-26.2271, -52.6718);

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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pato, 13));

        Map<String, Promocao> map = new HashMap<>();
        Map<Double, Promocao> map2 = new HashMap<>();
        for (Promocao p : listPromocoes) {
            String nomeSuper = p.getSupermercado();
            if (!map.containsKey(nomeSuper)) {
                map.put(p.getSupermercado(), p);
                map2.put(p.getValorPromocional(), p);
            } else {
                Promocao comp = map.get(nomeSuper);
            }
        }

        for (String key : map.keySet()) {
            Promocao c = map.get(key);
            for(Promocao p2 : listPromocoes ){
                if(p2.getSupermercado().equals(key)){
                    somaTotal += p2.getValorPromocional();
                }
            }
            mMap.addMarker(new MarkerOptions().position
                    (new LatLng(Double.parseDouble(c.getLatitude())
                            , Double.parseDouble(c.getLongitude())))
                    .title(c.getSupermercado())
                    .snippet("R$ " + precision.format(somaTotal)));
            somaTotal = 0.0;
        }


    }
}


