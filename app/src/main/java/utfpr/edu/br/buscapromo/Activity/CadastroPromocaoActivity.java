package utfpr.edu.br.buscapromo.Activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Helper.MaskEditText;
import utfpr.edu.br.buscapromo.Model.Produto;
import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.Model.SolicitaCadastroProduto;
import utfpr.edu.br.buscapromo.Model.Supermercado;
import utfpr.edu.br.buscapromo.Model.Usuario;
import utfpr.edu.br.buscapromo.R;

public class CadastroPromocaoActivity extends AppCompatActivity {

    static final int DATE_DIALOG_ID = 0;
    final Activity camera = this;

    private Button btnCancelar;
    private Button btnFindProduto;

    private EditText edtCadCodBarProd;
    private EditText teste;
    private EditText edtValorProdutoPromocional;
    private EditText edtValorProdutoOriginal;
    private AutoCompleteTextView actvSupermercado;

    private TextView tvProduto;
    private TextView tvDataFinal;
    private TextView tvDataInicial;
    private TextView tvMostrarCodBar;

    private ImageView imgProduto;
    private AlertDialog alerta;
    private LinearLayout imgLinearLayout;


    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private FirebaseAuth autenticacao;

    private ProgressBar progressBar;
    private Date dataInicial = new Date();
    private Date dataFinal = new Date();

    private String tagButtonDatain;
    private String getTipoUsuarioLogado;
    private String keyUser;
    private String tipoUsuarioLogado;
    private String urlImagem;

    private Usuario usuario;
    private Produto produto;
    private Supermercado supermercado;
    private Supermercado todosSupermercados;
    private List<Supermercado> supermercados;


    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    String day = String.valueOf(dayOfMonth);
                    String month = String.valueOf(monthOfYear + 1);
                    if (dayOfMonth < 10) {
                        day = "0" + String.valueOf(dayOfMonth);
                    }
                    if (monthOfYear < 10) {
                        month = "0" + String.valueOf(monthOfYear + 1);
                    }
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    String data = String.valueOf(day) + "/" + String.valueOf(month) + "/" + String.valueOf(year);
                    String dataAtual = sdf.format(new Date());
                    if (tagButtonDatain.equals("dataIn")) {
                        try {
                            dataInicial = sdf.parse(data);
                            dataFinal = sdf.parse(data);
                            if ((dataInicial.before(sdf.parse(dataAtual)))) {
                                Toast.makeText(getApplicationContext(), "Data Inicial deve ser atual ou posterior, verifique!", Toast.LENGTH_LONG).show();
                            } else {
                                tvDataInicial.setText(sdf.format(dataInicial));
                                tvDataFinal.setText(sdf.format(dataInicial));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else if (tagButtonDatain.equals("dataOut")) {
                        try {
                            dataFinal = sdf.parse(data);
                            if (dataFinal.before(dataInicial)) {
                                tvDataFinal.setText(sdf.format(dataInicial));
                                Toast.makeText(getApplicationContext(), "Data final deve ser posterior ou igual a inicial, verifique!", Toast.LENGTH_LONG).show();
                            } else {
                                tvDataFinal.setText(sdf.format(dataFinal));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_promocao);

        Intent i = getIntent();
        getTipoUsuarioLogado = i.getStringExtra("tipoUsuario");
        keyUser = i.getStringExtra("keyUser");

        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        tvDataFinal = findViewById(R.id.tvDataFinal);
        tvDataInicial = findViewById(R.id.tvDataInicial);
        tvDataInicial.setText(getDateTime());
        tvDataFinal.setText(getDateTime());
        tvProduto = findViewById(R.id.tvProduto);
        tvMostrarCodBar = findViewById(R.id.tvMostrarCodBar);

        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);
        edtValorProdutoOriginal = findViewById(R.id.edtValorProdutoOriginal);
      //  edtValorProdutoOriginal.addTextChangedListener(MaskEditText.mask(edtValorProdutoOriginal, MaskEditText.FORMAT_MONEY));
        edtValorProdutoPromocional = findViewById(R.id.edtValorProdutoPromocional);
     //   edtValorProdutoPromocional.addTextChangedListener(MaskEditText.mask(edtValorProdutoPromocional, MaskEditText.FORMAT_MONEY));
        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);
        actvSupermercado = findViewById(R.id.actvSupermercado);
        edtCadCodBarProd.requestFocus();

        imgProduto = findViewById(R.id.fotoProduto);
        imgLinearLayout = findViewById(R.id.imgLinearLayout);
        imgLinearLayout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBarPromocao);
        btnFindProduto = findViewById(R.id.btnFindProduto);

        getUsuario(keyUser);

    }

    private void getUsuario(String keyUser) {
        usuario = new Usuario();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("usuarios").orderByChild("key").equalTo(keyUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        usuario = posSnapshot.getValue(Usuario.class);
                        findSupermercado(usuario);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void insereDataOnclick(View view) {
        tagButtonDatain = view.getTag().toString();
        showDialog(DATE_DIALOG_ID);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar calendario = Calendar.getInstance();
        int ano = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, ano, mes,
                        dia);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null && requestCode == 49374) {
            if (intentResult.getContents() != null) {
                edtCadCodBarProd.setEnabled(true);
                edtCadCodBarProd.setText(intentResult.getContents());

                carregaProduto();

            } else {
                alert("Scan cancelado");
            }
        } else {

            final int heigth = 200;
            final int width = 200;

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

                            if (dataSnapshot.exists()) {
                                produto = posSnapshot.getValue(Produto.class);
                                tvProduto.setText(produto.getNomeProduto() + " " +
                                        produto.getTipo() + " " +
                                        produto.getMarca() + " " +
                                        produto.getEmbalagem() + " " +
                                        produto.getConteudo()
                                );
                                tvMostrarCodBar.setText(produto.getCodBarras());
                                tvMostrarCodBar.setVisibility(View.VISIBLE);
                                edtCadCodBarProd.setVisibility(View.GONE);
                                imgLinearLayout.setVisibility(View.VISIBLE);
                                imgProduto.setVisibility(View.VISIBLE);

                                carregaImagemProduto();
                            }
                        }
                        if (!dataSnapshot.exists()) {
                            tvMostrarCodBar.setVisibility(View.GONE);
                            imgLinearLayout.setVisibility(View.VISIBLE);
                            imgProduto.setVisibility(View.GONE);
                            tvProduto.setText("Produto não cadastrado, enviando solicitação de cadastro");
                            solicitaCadastroProduto();
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

    public void btnFinProdutoOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroPromocaoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Cadastro Promoção");
        builder.setMessage("Digitar ou Scannear Cod. Barras?");

        builder.setPositiveButton("Scanner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
//                    edtCadCodBarProd.setVisibility(View.VISIBLE);
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
                imgLinearLayout.setVisibility(View.GONE);
                edtCadCodBarProd.setEnabled(true);
                edtCadCodBarProd.setVisibility(View.VISIBLE);
                edtCadCodBarProd.setFocusable(true);
//                btnFindCodBarDig.setVisibility(View.VISIBLE);
//                btnFindProduto.setEnabled(false);
                tvMostrarCodBar.setVisibility(View.GONE);

            }
        });
        alerta = builder.create();
        alerta.show();
    }


    public void fotoCadPromocaoOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroPromocaoActivity.this);
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
                    Toast.makeText(CadastroPromocaoActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
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

//    private void cadastraImagemPromocao() {
//
//        if (imgProduto.getDrawable().toString().contains("android.graphics.drawable.Vector")) {
//            Toast.makeText(CadastroPromocaoActivity.this, "Verifique se imagem esta correta!!", Toast.LENGTH_LONG).show();
//
//        } else {
//
//            final StorageReference salvaFotoReferencia = storageReference.child("imagemPromocao/" + edtCadCodBarProd.getText().toString() + ".jpg");
//
//            imgProduto.setDrawingCacheEnabled(true);
//            imgProduto.buildDrawingCache();
//
//            Bitmap bitmap = imgProduto.getDrawingCache();
//
//            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArray);
//            byte[] data = byteArray.toByteArray();
//
//            UploadTask uploadTask = salvaFotoReferencia.putBytes(data);
//
//            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//                    return salvaFotoReferencia.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        final Uri downloadUri = task.getResult();
//                        if (!downloadUri.equals("")) {
//                            urlImagem = downloadUri.toString();
//                            salvar();
//                        }
//
//                    } else {
//                        Toast.makeText(CadastroPromocaoActivity.this, "Erro ao salvar Promoção", Toast.LENGTH_LONG).show();
//                    }
//                }
//            });
//        }
//    }


    private void salvar() {

        Boolean salvarOk = true;
        Double valorIn = 0.0;
        Double valorOut = 0.0;

        if (produto == null && salvarOk == true) {
            Toast.makeText(CadastroPromocaoActivity.this, "Produto não informado, não é possível completar operação!", Toast.LENGTH_LONG).show();
            salvarOk = false;
        }
        if (actvSupermercado.getText().toString().isEmpty() && salvarOk == true) {
            Toast.makeText(CadastroPromocaoActivity.this, "Supermercado não informado, verifique!", Toast.LENGTH_LONG).show();
            salvarOk = false;
        }
        if (edtValorProdutoOriginal.getText().toString().isEmpty()
                || edtValorProdutoPromocional.getText().toString().isEmpty() && salvarOk == true) {
            Toast.makeText(CadastroPromocaoActivity.this, "Valores incorretos!", Toast.LENGTH_LONG).show();
            salvarOk = false;

        }
        if (edtValorProdutoOriginal.getText().toString().length() >= 1
                || edtValorProdutoPromocional.getText().toString().length() >= 1 && salvarOk == true) {
            valorOut = Double.parseDouble(edtValorProdutoPromocional.getText().toString());
            valorIn = Double.parseDouble(edtValorProdutoOriginal.getText().toString());
        }
        if (salvarOk == true) {
            if (valorIn < valorOut) {
                Toast.makeText(CadastroPromocaoActivity.this, "Valor promocional menor que original, verifique os valores!", Toast.LENGTH_LONG).show();
            } else {
                Promocao promocao = new Promocao();
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    String txtDateOut = dateFormat.format(dataFinal);
                    Date parsedDate = dateFormat.parse(txtDateOut);

                    Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
                    promocao.setDataInsercao(dateFormat.format(dataInicial));

                    promocao.setDataValidade(timestamp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                promocao.setProduto(produto);
                promocao.setUsuario(usuario);
                promocao.setSupermercado(getSupermercado(actvSupermercado.getText().toString()));

                if (usuario.getTipoUsuario().equals("Supermercado")) {
                    promocao.setOrigem("Oficial");
                } else {
                    promocao.setOrigem("Usuario");
                }
                promocao.setValorOriginal(valorIn);
                promocao.setValorPromocional(valorOut);

                databaseReference = ConfiguracaoFirebase.getFirebase().child("promocoes");
                String key = databaseReference.push().getKey();

                if (promocao.getKey() == null) {
                    try {

                        promocao.setKey(key);
                        databaseReference.child(key).setValue(promocao);
                        Toast.makeText(CadastroPromocaoActivity.this, "Promoção Cadastrada com Sucesso!!", Toast.LENGTH_LONG).show();
                        resetIntent();
                    } catch (Exception e) {
                        Toast.makeText(CadastroPromocaoActivity.this, "Erro ao salvar Promoção", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }
        } else {
            Toast.makeText(CadastroPromocaoActivity.this, "Erro ao salvar Promoção, verifique todos os campos", Toast.LENGTH_LONG).show();

        }


    }


    private void findSupermercado(Usuario usuario) {
        if (usuario.getTipoUsuario().equals("Supermercado")) {
            actvSupermercado.setText(usuario.getNome());
        } else {

            supermercados = new ArrayList<>();
            databaseReference = FirebaseDatabase.getInstance().getReference();
            databaseReference.child("supermercados").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final List<String> sup = new ArrayList<String>();
                    supermercados.clear();

                    for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                        String nomeSup = posSnapshot.child("nome").getValue(String.class);
                        todosSupermercados = posSnapshot.getValue(Supermercado.class);
                        sup.add(nomeSup);
                        ArrayAdapter<String> supAdapter = new ArrayAdapter<String>(CadastroPromocaoActivity.this,
                                android.R.layout.simple_dropdown_item_1line, sup);

                        supAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                        actvSupermercado.setAdapter(supAdapter);
                        supermercados.add(todosSupermercados);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private Supermercado getSupermercado(String nomeSupermercado) {

        supermercado = new Supermercado();
        for (int i = 0; i <= getSuperCount(); i++) {
            if (supermercados.get(i).getNome().equals(nomeSupermercado)) {
                supermercado = supermercados.get(i);
                return supermercado;
            }
        }
        return supermercado;
    }


    public int getSuperCount() {
        return supermercados.size();
    }

    private void resetIntent() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }


    public void salvarOnclick(View view) {
        salvar();
    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroPromocaoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }


    public void edtCadCodBarProdOnClick(View view) {
        if (edtCadCodBarProd.getText().length() != 13 ) {
            Toast.makeText(CadastroPromocaoActivity.this, "Erro! " +
                    "Código digitado incorreto!", Toast.LENGTH_LONG).show();

        } else {
            carregaProduto();
        }
    }

    public void solicitaCadastroProduto() {

        final SolicitaCadastroProduto solicitaCadastroProduto = new SolicitaCadastroProduto();
        solicitaCadastroProduto.setCodBarras(edtCadCodBarProd.getText().toString());

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("solicitaCadastroProduto").orderByChild("codBarras").equalTo(edtCadCodBarProd.getText().toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()) {

                            try {
                                String key = databaseReference.push().getKey();
                                solicitaCadastroProduto.setKey(key);
                                databaseReference.child("solicitaCadastroProduto").child(key).setValue(solicitaCadastroProduto);
                                Toast.makeText(CadastroPromocaoActivity.this, "Solicitação efetuada com Sucesso!!", Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                Toast.makeText(CadastroPromocaoActivity.this, "Erro! Repita a operação", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }

                        }else{
                            Toast.makeText(CadastroPromocaoActivity.this, "Cadastro em andamento, aguarde!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
}

