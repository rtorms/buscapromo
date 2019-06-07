package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utfpr.edu.br.buscapromo.R;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        verificaLogin();
    }

    private void verificaLogin() {
        if (usuarioLogado()) {
            Intent intentPrincipal = new Intent(MainActivity.this, TelaPrincipalActivity.class);
            startActivity(intentPrincipal);
            finish();
        } else {
            Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intentLogin);
            finish();
        }
    }

    public Boolean usuarioLogado() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


        if (user != null) {
            return true;
        } else {
            return false;
        }
    }
}



