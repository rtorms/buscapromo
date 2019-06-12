package utfpr.edu.br.buscapromo.activity;

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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import utfpr.edu.br.buscapromo.dao.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.dao.FindListStringGeneric;
import utfpr.edu.br.buscapromo.dao.FindObjectGeneric;
import utfpr.edu.br.buscapromo.dao.ListStringCallbackInterface;
import utfpr.edu.br.buscapromo.dao.ObjectCallbackInterface;
import utfpr.edu.br.buscapromo.model.Produto;
import utfpr.edu.br.buscapromo.R;


public class CadastroProdutoActivity extends AppCompatActivity {

    final Activity camera = this;
    private ImageView fotoCadProduto;
    private EditText edtCadNomeProduto;
    private EditText edtCadTipoProduto;
    private EditText edtCadMarcaProduto;
    private EditText edtCadConteudoProduto;
    private EditText edtCadCodBarrasProduto;
    private EditText edtCadEmbalagemProduto;
    private AlertDialog alerta;
    private String urlImagem;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseDatabase database;
    private Spinner spDepartamento;
    private String idProd;
    private ProgressBar progressBar;
    private FindObjectGeneric findObjectGeneric;
    private Produto produto;
    private LinearLayout linearLayoutImg;
    private LinearLayout linearLayoutEdicaoProd;
    private FindListStringGeneric findListStringGeneric;


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
        linearLayoutImg = findViewById(R.id.linearLayoutImg);
        linearLayoutImg.setVisibility(View.GONE);
        linearLayoutEdicaoProd = findViewById(R.id.linearLayoutEdicaoProd);
        findObjectGeneric = new FindObjectGeneric();
        enableDisableView(linearLayoutEdicaoProd, false);

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        findListStringGeneric = new FindListStringGeneric();
        findListStringGeneric.listaStringGenericCallback("departamentos", "descricao", this, new ListStringCallbackInterface() {
            @Override
            public void onCallback(ArrayAdapter<String> depAdapter) {
                spDepartamento.setAdapter(depAdapter);
            }
        });
    }

    private void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null && requestCode == 49374) {
            if (intentResult.getContents() != null) {
                edtCadCodBarrasProduto.setVisibility(View.VISIBLE);
                edtCadCodBarrasProduto.setText(intentResult.getContents());
                linearLayoutImg.setVisibility(View.VISIBLE);
                findProduto();

            } else {
                edtCadCodBarrasProduto.setVisibility(View.VISIBLE);
                enableDisableView(linearLayoutEdicaoProd, true);

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

    private void findProduto() {

        linearLayoutImg.setVisibility(View.VISIBLE);
        enableDisableView(linearLayoutEdicaoProd, true);
        findObjectGeneric.findObjectCallback("produtos", "codBarras", edtCadCodBarrasProduto.getText().toString(), new Produto(), this, new ObjectCallbackInterface() {
            @Override
            public void onCallback(Object object) {

                if (object != null) {
                    produto = (Produto) object;
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

                    Picasso.get().load(produto.getUrlImagem()).resize(300, 300).centerCrop().into(fotoCadProduto);

                } else {
                    edtCadNomeProduto.setText("");
                    edtCadTipoProduto.setText("");
                    edtCadMarcaProduto.setText("");
                    edtCadEmbalagemProduto.setText("");
                    edtCadConteudoProduto.setText("");
                    spDepartamento.setSelection(0);
                    fotoCadProduto.setImageResource(R.drawable.ic_menu_camera);

                }
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

                produto.setIdProd(idProd);
                databaseReference.child(idProd).setValue(produto);
                Toast.makeText(CadastroProdutoActivity.this, "Produto Editado com Sucesso!!", Toast.LENGTH_LONG).show();

            } catch (Exception e) {
                Toast.makeText(CadastroProdutoActivity.this, "Erro ao salvar produto", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        recreate();
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

    public void btnBuscaCodBarOnClick(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroProdutoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Cadastro Produto");
        builder.setMessage("Digitar ou Scannear Cod. Barras?");

        builder.setPositiveButton("Scanner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
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
                edtCadCodBarrasProduto.setVisibility(View.VISIBLE);
                edtCadCodBarrasProduto.setEnabled(true);
                edtCadCodBarrasProduto.setFocusable(true);
            }
        });
        alerta = builder.create();
        alerta.show();
    }

    public void edtCadCodBarrasOnClick(View view) {
        findProduto();
    }
}
