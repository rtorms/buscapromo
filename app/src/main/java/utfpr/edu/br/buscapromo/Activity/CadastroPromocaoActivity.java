package utfpr.edu.br.buscapromo.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

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
    private Button btnFindCodBarDig;
    private AlertDialog alerta;
    final Activity camera = this;
    private EditText edtCadCodBarProd;

    private FirebaseStorage storage;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_promocao);

        storageReference = ConfiguracaoFirebase.getFirebaseStorageReference();

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        tipoUsuarioLogado = autenticacao.getCurrentUser().getDisplayName();

        imgProduto = (ImageView) findViewById(R.id.fotoProduto);
        btnFindCodBarDig = findViewById(R.id.btnFindCodBarDig);
        btnFindCodBarDig.setVisibility(View.INVISIBLE);
        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null && requestCode == 49374) {
            if (intentResult.getContents() != null) {
                edtCadCodBarProd.setText(intentResult.getContents());

                carregaProduto();
                carregaImagemProduto();


            } else {
                alert("Scan cancelado");
            }
        } else {

            final int heigth = 300;
            final int width = 300;

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 123) {
                    Uri imagemSelecionada = data.getData();
                    Picasso.get().load(imagemSelecionada.toString()).resize(width, heigth).centerCrop().into(imgProduto);

                } else if (requestCode == 321) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imgProduto.setImageBitmap(imageBitmap);
                }
            }
        }
    }

    private void alert(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void carregaProduto() {

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("produtos").orderByChild("codBarras").equalTo(edtCadCodBarProd.getText().toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {

//                            edtCadNomeProduto.setText(posSnapshot.child("nomeProduto").getValue().toString());
//                            edtCadTipoProduto.setText(posSnapshot.child("tipo").getValue().toString());
//                            edtCadMarcaProduto.setText(posSnapshot.child("marca").getValue().toString());
//                            edtCadEmbalagemProduto.setText(posSnapshot.child("embalagem").getValue().toString());
//                            edtCadConteudoProduto.setText(posSnapshot.child("conteudo").getValue().toString());


                        }
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(CadastroPromocaoActivity.this, "Produto não cadastrado!!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void carregaImagemProduto() {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        final StorageReference storageReference = storage.getReferenceFromUrl
                ("gs://buscapromo-7627b.appspot.com/imagemProduto/" + edtCadCodBarProd.getText() + ".jpg");

        final int heigth = 300;
        final int width = 300;

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get().load(uri).resize(width, heigth).centerCrop().into(imgProduto);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CadastroPromocaoActivity.this, "Imagem produto não encontrada!!", Toast.LENGTH_SHORT).show();

            }
        });
    }


    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroPromocaoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);

    }


    public void edtCadCodBarProdOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroPromocaoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Cadastro Produto");
        builder.setMessage("Digitar ou Scannear Cod. Barras?");

        builder.setPositiveButton("Scanner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    btnFindCodBarDig.setVisibility(View.INVISIBLE);
                    IntentIntegrator intentIntegrator = new IntentIntegrator(camera);
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    intentIntegrator.setPrompt("SCAN");
                    intentIntegrator.setCameraId(0);
                    intentIntegrator.initiateScan();

                } catch (Exception e) {
                    Toast.makeText(CadastroPromocaoActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Digitar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edtCadCodBarProd.setFocusable(true);
                btnFindCodBarDig.setVisibility(View.VISIBLE);

            }
        });
        alerta = builder.create();
        alerta.show();
    }

    public void btnFindCodBarDigOnclick(View view) {
        carregaImagemProduto();
        carregaProduto();
    }
}
