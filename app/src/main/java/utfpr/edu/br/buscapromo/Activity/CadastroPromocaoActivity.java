package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.R;

public class CadastroPromocaoActivity extends AppCompatActivity {

    private Button btnSalvar;
    private Button btnCancelar;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private DatabaseReference referenceFirebase;
    private FirebaseAuth autenticacao;
    private ImageView imgProduto;
    private String tipoUsuarioLogado;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_promocao);

        storageReference = ConfiguracaoFirebase.getFirebaseStorageReference();

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        tipoUsuarioLogado = autenticacao.getCurrentUser().getDisplayName();

        imgProduto = (ImageView) findViewById(R.id.imagemProduto);
    }


    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroPromocaoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);

    }


}
