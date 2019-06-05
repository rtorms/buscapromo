package utfpr.edu.br.buscapromo.Activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Model.Supermercado;
import utfpr.edu.br.buscapromo.Model.Usuario;
import utfpr.edu.br.buscapromo.R;

public class CadastroSupermercadoActivity extends AppCompatActivity implements LocationListener {

    private EditText nome;
    private EditText endereco;
    private EditText telefone;
    private EditText cnpj;
    private EditText email;
    private EditText senha1;
    private EditText senha2;
    private TextView latitude;
    private TextView longitude;
    private Button btnCadastrar;
    private Button btnCancelar;
    private FirebaseAuth autenticacao;
    private DatabaseReference reference;
    private Supermercado supermercado;
    private Usuario usuario;
    private double latit;
    private double longit;

    //verificar para retirar, não funcionou como esperado, ver import no gradle
    private Geofence geofence;
    private GeofencingClient geofencingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_supermercado);

        geofencingClient = LocationServices.getGeofencingClient(this);

        nome = (EditText) findViewById(R.id.edtCadNome);
        endereco = (EditText) findViewById(R.id.edtCadEndereco);
        telefone = (EditText) findViewById(R.id.edtCadTelefone);
        cnpj = (EditText) findViewById(R.id.edtCadCnpj);
        email = (EditText) findViewById(R.id.edtCadEmail);
        senha1 = (EditText) findViewById(R.id.edtCadSenha1);
        senha2 = (EditText) findViewById(R.id.edtCadSenha2);
        latitude = (TextView) findViewById(R.id.edtCadLatitude);
        longitude = (TextView) findViewById(R.id.edtCadLongitude);
        btnCadastrar = (Button) findViewById(R.id.btnCadastrar);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);


        //ativar GPS
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Habilitar GPS", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(i);
        }
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        //fim GPs


        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (senha1.getText().toString().equals(senha2.getText().toString())) {
                    usuario = new Usuario();
                    usuario.setEmail(email.getText().toString());
                    usuario.setSenha(senha1.getText().toString());
                    usuario.setNome(nome.getText().toString());
                    usuario.setTipoUsuario("Supermercado");

                    supermercado = new Supermercado();

                    supermercado.setNome(nome.getText().toString());
                    supermercado.setEndereco(endereco.getText().toString());
                    supermercado.setTelefone(telefone.getText().toString());
                    supermercado.setCnpj(cnpj.getText().toString());
                    supermercado.setLatitude(latitude.getText().toString());
                    supermercado.setLongitude(longitude.getText().toString());
                    supermercado.setUsuario(usuario);
//                    supermercado.setGeofence(addGeofence());
                    cadastrarUsuario();
                    insereSupermercado(supermercado);
                } else {
                    Toast.makeText(CadastroSupermercadoActivity.this, "Senhas Informadas Não Conferem!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cadastrarUsuario() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(CadastroSupermercadoActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    insereUsuario(usuario);
                    finish();
                    autenticacao.signOut();

                } else {

                    String erroExcecao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthActionCodeException e) {
                        erroExcecao = "Senha muito fraca, deve conter mínimo 6 caracteres com letras e números!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExcecao = "E-mail inválido, digite novo e-mail!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExcecao = "E-mail já cadastrado!";
                    } catch (Exception e) {
                        erroExcecao = "Erro ao efetuar cadastro!";
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroSupermercadoActivity.this, "Erro!" + erroExcecao, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean insereUsuario(Usuario usuario) {
        try {
            reference = ConfiguracaoFirebase.getFirebase().child("usuarios");
            String key = reference.push().getKey();
            usuario.setKey(key);
            reference.child(key).setValue(usuario);
            Toast.makeText(CadastroSupermercadoActivity.this, "Usuário Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(CadastroSupermercadoActivity.this, "Erro ao salvar usuário", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }

    }

    private boolean insereSupermercado(Supermercado supermercado) {
        try {
            reference = ConfiguracaoFirebase.getFirebase().child("supermercados");
            reference.push().setValue(supermercado);
            Toast.makeText(CadastroSupermercadoActivity.this, "Supermercado Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(CadastroSupermercadoActivity.this, "Erro ao salvar supermercado", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    //retorna endereço através das coordenadas
    class ConexaoEnderecoThread extends Thread {

        public void run() {

            try {
                String url = "https://maps.googleapis.com/maps/api/geocode/xml?latlng=" + latitude.getText().toString() +
                        "," + longitude.getText().toString() + "&key=AIzaSyBDyAx4LGfJIDRJqn8bWYqR_JiZOhg4m0A";

                URL caminho = new URL(url);
                URLConnection con = caminho.openConnection();
                InputStream in = con.getInputStream();

                final StringBuilder msg = new StringBuilder();
                BufferedReader entrada = new BufferedReader(new InputStreamReader(in));

                String linha = entrada.readLine();
                while (linha != null) {
                    msg.append(linha);
                    linha = entrada.readLine();
                }

                final String enderecoRetornado = msg.toString().substring(
                        msg.toString().indexOf("<formatted_address>") + 19,
                        msg.toString().indexOf("</formatted_address>")
                );


                CadastroSupermercadoActivity.this.runOnUiThread(new Thread() {
                    public void run() {
                        endereco.setText(enderecoRetornado);
                    }
                });

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    // seta dados obtidos pelo GPS
    @Override
    public void onLocationChanged(Location location) {

        latit = location.getLatitude();
        longit = location.getLongitude();

        latitude.setText(String.valueOf(latit));
        longitude.setText(String.valueOf(longit));

        new ConexaoEnderecoThread().start();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroSupermercadoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }

    private Geofence addGeofence() {

      return  new Geofence.Builder()
              .setRequestId(nome.getText().toString())
              .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
              .setCircularRegion(latit, longit, 20)
              .setExpirationDuration(Geofence.NEVER_EXPIRE)
              .build();
    }
}
