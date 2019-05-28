package utfpr.edu.br.buscapromo.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import utfpr.edu.br.buscapromo.Model.Produto;
import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.DAO.FindProdutos;
import utfpr.edu.br.buscapromo.R;


public class CadastroProdutoActivity extends AppCompatActivity {

    private ImageView fotoCadProduto;
    private EditText edtCadNomeProduto;
    private EditText edtCadTipoProduto;
    private EditText edtCadMarcaProduto;
    private EditText edtCadConteudoProduto;
    private EditText edtCadCodBarrasProduto;
    private EditText edtCadEmbalagemProduto;
    private AlertDialog alerta;
    final Activity camera = this;
    private String urlImagem;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private Spinner spDepartamento;
    private String idProd;
    private ProgressBar progressBar;
    private Button btnBuscaCodBarDig;
    private FindProdutos findProduto;
    private Produto produto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_produto);

        fotoCadProduto = findViewById(R.id.fotoCadProduto);
        edtCadNomeProduto = findViewById(R.id.edtCadNomeProduto);
        edtCadTipoProduto = findViewById(R.id.edtCadTipoProduto);
        edtCadMarcaProduto = findViewById(R.id.edtCadMarcaProduto);
        edtCadConteudoProduto = findViewById(R.id.edtCadConteudoProduto);
        edtCadCodBarrasProduto = findViewById(R.id.edtCadCodBarrasProduto);
        edtCadEmbalagemProduto = findViewById(R.id.edtCadEmbalagemProduto);
        spDepartamento = findViewById(R.id.spDepartamento);
        progressBar = findViewById(R.id.progressBarProduto);
        btnBuscaCodBarDig = findViewById(R.id.btnBuscaCodBarDig);
        btnBuscaCodBarDig.setVisibility(View.INVISIBLE);
        findProduto = new FindProdutos();

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
//        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child("departamentos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> dep = new ArrayList<String>();

                for (DataSnapshot depSnapshot : dataSnapshot.getChildren()) {
                    String nomeDep = depSnapshot.child("descricao").getValue(String.class);
                    dep.add(nomeDep);
                }

                ArrayAdapter<String> depAdapter = new ArrayAdapter<String>(CadastroProdutoActivity.this,
                        android.R.layout.simple_spinner_item, dep);
                depAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spDepartamento.setAdapter(depAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null && requestCode == 49374) {
            if (intentResult.getContents() != null) {
                edtCadCodBarrasProduto.setText(intentResult.getContents());

//                findProduto();
                carregaProduto();
            } else {
                alert("Scan cancelado");
            }
        } else {

            final int heigth = 300;
            final int width = 300;

            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 123) {
                    Uri imagemSelecionada = data.getData();
                    Picasso.get().load(imagemSelecionada.toString()).resize(width, heigth).centerCrop().into(fotoCadProduto);

                } else if (requestCode == 321) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    fotoCadProduto.setImageBitmap(imageBitmap);
                }
            }
        }
    }

    private void alert(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void cadastraImagemProduto() {

        if (fotoCadProduto.getDrawable().toString().contains("android.graphics.drawable.Vector")) {
            Toast.makeText(CadastroProdutoActivity.this, "Verifique se imagem do produto esta correta!!", Toast.LENGTH_LONG).show();

        } else {
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference salvaFotoReferencia = storageReference.child("imagemProduto/" + edtCadCodBarrasProduto.getText().toString() + ".jpg");

            fotoCadProduto.setDrawingCacheEnabled(true);
            fotoCadProduto.buildDrawingCache();

            Bitmap bitmap = fotoCadProduto.getDrawingCache();

            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray);
            byte[] data = byteArray.toByteArray();

            UploadTask uploadTask = salvaFotoReferencia.putBytes(data);

            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return salvaFotoReferencia.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();
                        if (!downloadUri.equals("")) {
                            urlImagem = downloadUri.toString();
                            salvar();
                        }
                    } else {
                        Toast.makeText(CadastroProdutoActivity.this, "Erro ao salvar Produto", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    // busca através de classe generica, não funciona ver com professor
    private void findProduto() {
        produto = new Produto();
        findProduto.carregaProduto(edtCadCodBarrasProduto.getText().toString(), this);
        produto = findProduto.getNovoproduto();

        if (produto == null) {
            Toast.makeText(CadastroProdutoActivity.this, "Produto não cadastrado!!", Toast.LENGTH_SHORT).show();
        }else {


            edtCadNomeProduto.setText(produto.getNomeProduto());
            edtCadTipoProduto.setText(produto.getTipo());
            edtCadMarcaProduto.setText(produto.getMarca());
            edtCadEmbalagemProduto.setText(produto.getEmbalagem());
            edtCadConteudoProduto.setText(produto.getConteudo());
            for (int i = 0; i < spDepartamento.getCount(); i++)
                if (spDepartamento.getItemAtPosition(i).equals(produto.getDepartamento())) {

                    spDepartamento.setSelection(i);
                }
            idProd = produto.getIdProd();
        }
    }

    private void carregaProduto() {

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("produtos").orderByChild("codBarras").equalTo(edtCadCodBarrasProduto.getText().toString())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
//                            produto = posSnapshot.getValue(Produto.class);
                            edtCadNomeProduto.setText(posSnapshot.child("nomeProduto").getValue().toString());
                            edtCadTipoProduto.setText(posSnapshot.child("tipo").getValue().toString());
                            edtCadMarcaProduto.setText(posSnapshot.child("marca").getValue().toString());
                            edtCadEmbalagemProduto.setText(posSnapshot.child("embalagem").getValue().toString());
                            edtCadConteudoProduto.setText(posSnapshot.child("conteudo").getValue().toString());
                            for (int i = 0; i < spDepartamento.getCount(); i++)
                                if (spDepartamento.getItemAtPosition(i).equals(posSnapshot.child("departamento").getValue().toString())) {

                                    spDepartamento.setSelection(i);
                                }
                            idProd = posSnapshot.getKey();
                            carregaImagemProduto();
                        }
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(CadastroProdutoActivity.this, "Produto não cadastrado!!", Toast.LENGTH_SHORT).show();
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
                ("gs://buscapromo-7627b.appspot.com/imagemProduto/" + edtCadCodBarrasProduto.getText() + ".jpg");

        final int heigth = 300;
        final int width = 300;

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {

                Picasso.get().load(uri).resize(width, heigth).centerCrop().into(fotoCadProduto);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CadastroProdutoActivity.this, "Imagem produto não encontrada!!", Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void salvar() {

        Produto produto = new Produto();

        produto.setNomeProduto(edtCadNomeProduto.getText().toString());
        produto.setCodBarras(edtCadCodBarrasProduto.getText().toString());
        produto.setConteudo(edtCadConteudoProduto.getText().toString());
        produto.setEmbalagem(edtCadEmbalagemProduto.getText().toString());
        produto.setMarca(edtCadMarcaProduto.getText().toString());
        produto.setTipo(edtCadTipoProduto.getText().toString());
        produto.setUrlImagem(urlImagem);
        produto.setDepartamento(spDepartamento.getSelectedItem().toString());

        databaseReference = ConfiguracaoFirebase.getFirebase().child("produtos");

        if (idProd == null) {
            try {
                idProd = databaseReference.push().getKey();
                produto.setIdProd(idProd);
                databaseReference.child(idProd).setValue(produto);
                Toast.makeText(CadastroProdutoActivity.this, "Produto Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(CadastroProdutoActivity.this, "Erro ao salvar produto", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            try {

                databaseReference.child(idProd).setValue(produto);
                Toast.makeText(CadastroProdutoActivity.this, "Produto Editado com Sucesso!!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(CadastroProdutoActivity.this, "Erro ao salvar produto", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        resetIntent();
    }

    private void resetIntent() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void btnCancelarOnclickListener(View view) {

        Intent intent = new Intent(CadastroProdutoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }

    public void fotoCadProdutoOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroProdutoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("IMAGEM PRODUTO");
        builder.setMessage("Como deseja inserir a imagem?");

        builder.setPositiveButton("CÂMERA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                    if (cameraIntent.resolveActivity((getPackageManager())) != null) {
                        startActivityForResult(cameraIntent, 321);
                    }
                } catch (Exception e) {
                    Toast.makeText(CadastroProdutoActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        builder.setNegativeButton("ARQUIVO", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), 123);
            }
        });
        alerta = builder.create();
        alerta.show();
    }

    public void btnSalvarProdutoOnClick(View view) {

        if ((edtCadNomeProduto.length() <= 1) || (edtCadEmbalagemProduto.length() <= 1) ||
                (edtCadCodBarrasProduto.length() <= 1) || (edtCadConteudoProduto.length() <= 1) ||
                (edtCadMarcaProduto.length() <= 1) || (edtCadTipoProduto.equals(""))) {

            Toast.makeText(CadastroProdutoActivity.this, "Verifique se todos os campos estão corretos!!", Toast.LENGTH_LONG).show();
        } else {

            cadastraImagemProduto();
        }
    }

    public void edtCadCodBarrasOnClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroProdutoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Cadastro Produto");
        builder.setMessage("Digitar ou Scannear Cod. Barras?");

        builder.setPositiveButton("Scanner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    btnBuscaCodBarDig.setVisibility(View.INVISIBLE);
                    IntentIntegrator intentIntegrator = new IntentIntegrator(camera);
                    intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                    intentIntegrator.setPrompt("SCAN");
                    intentIntegrator.setCameraId(0);
                    intentIntegrator.initiateScan();

                } catch (Exception e) {
                    Toast.makeText(CadastroProdutoActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Digitar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edtCadCodBarrasProduto.setFocusable(true);
                btnBuscaCodBarDig.setVisibility(View.VISIBLE);
            }
        });
        alerta = builder.create();
        alerta.show();
    }

    public void btnBuscaCodBarDigOnclick(View view) {
        carregaProduto();
    }
}
