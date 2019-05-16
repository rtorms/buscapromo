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

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utfpr.edu.br.buscapromo.Classes.Supermercado;
import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Helper.MaskEditText;
import utfpr.edu.br.buscapromo.R;

public class CadastroPromocaoActivity extends AppCompatActivity {

    static final int DATE_DIALOG_ID = 0;
    final Activity camera = this;
    private Button btnSalvar;
    private Button btnCancelar;
    private ImageView imgProduto;
    private String tipoUsuarioLogado;
    private Button btnFindCodBarDig;
    private AlertDialog alerta;
    private EditText edtCadCodBarProd;
    private TextView tvProduto;
    private AutoCompleteTextView actvSupermercado;
    private LinearLayout informarProdutoLinearLayout;
    private FirebaseStorage storage;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private FirebaseAuth autenticacao;
    private ProgressBar progressBar;
    private String urlImagem;
    private Supermercado todosSupermercados;
    private List<Supermercado> supermercados;
    private TextView tvDataFinal;
    private TextView tvDataInicial;
    private EditText teste;
    private EditText edtValorProdutoOriginal;
    private EditText edtValorProdutoPromocional;
    private String inOut;
    private Date dataInicial;
    private Date dataFinal;

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

                    try {
                        dataInicial = sdf.parse(dataAtual);
                        dataFinal = sdf.parse(tvDataFinal.getText().toString());
                    }catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (inOut.equals("dataIn")) {
                        try {
                            dataInicial = sdf.parse(data);
                            if((dataInicial.before(sdf.parse(dataAtual))) || (dataInicial.after(dataFinal))){

                                Toast.makeText(getApplicationContext(), "Data Inicial deve ser atual ou posterior e anterior a data final, verifique!", Toast.LENGTH_LONG).show();

                            }else {
                                tvDataInicial.setText(sdf.format(dataInicial));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }



                    } else {
                        try {
                            dataFinal = sdf.parse(data);
                            if (dataFinal.before(dataInicial)) {

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

        storageReference = ConfiguracaoFirebase.getFirebaseStorageReference();

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        tipoUsuarioLogado = autenticacao.getCurrentUser().getDisplayName();
        tvDataFinal = findViewById(R.id.tvDataFinal);
        tvDataInicial = findViewById(R.id.tvDataInicial);


        tvDataInicial.setText(getDateTime());
        tvDataFinal.setText(getDateTime());


        edtValorProdutoOriginal = findViewById(R.id.edtValorProdutoOriginal);
        edtValorProdutoOriginal.addTextChangedListener(MaskEditText.mask(edtValorProdutoOriginal, MaskEditText.FORMAT_MONEY));
        edtValorProdutoPromocional = findViewById(R.id.edtValorProdutoPromocional);
        edtValorProdutoPromocional.addTextChangedListener(MaskEditText.mask(edtValorProdutoPromocional, MaskEditText.FORMAT_MONEY));


        teste = findViewById(R.id.txtTeste);


        imgProduto = findViewById(R.id.fotoProduto);
        tvProduto = findViewById(R.id.tvProduto);
        btnFindCodBarDig = findViewById(R.id.btnFindCodBarDig);
        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);
        actvSupermercado = findViewById(R.id.actvSupermercado);
        edtCadCodBarProd.requestFocus();
        informarProdutoLinearLayout = findViewById(R.id.informarProdutoLinearLayout);
        progressBar = findViewById(R.id.progressBarPromocao);
        storage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        findSupermercado();
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void insereDataOnclick(View view) {

        inOut = view.getTag().toString();
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
                                tvProduto.setText(posSnapshot.child("nomeProduto").getValue().toString() + " " +
                                        posSnapshot.child("tipo").getValue().toString() + " " +
                                        posSnapshot.child("marca").getValue().toString() + " " +
                                        posSnapshot.child("embalagem").getValue().toString() + " " +
                                        posSnapshot.child("conteudo").getValue().toString()
                                );
                                informarProdutoLinearLayout.setVisibility(View.GONE);
                                carregaImagemProduto();
                            }
                        }
                        if (!dataSnapshot.exists()) {
                            Toast.makeText(CadastroPromocaoActivity.this, "Produto não cadastrado!!" + "Deseja informar?", Toast.LENGTH_SHORT).show();
                            tvProduto.setText("Produto não cadastrado, se desejar informe os dados abaixo:");
                            informarProdutoLinearLayout.setVisibility(View.VISIBLE);
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
        builder.setTitle("Cadastro Promoção");
        builder.setMessage("Digitar ou Scannear Cod. Barras?");

        builder.setPositiveButton("Scanner", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    btnFindCodBarDig.setVisibility(View.GONE);
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
        carregaProduto();
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

    private void cadastraImagemPromocao() {

        if (imgProduto.getDrawable().toString().contains("android.graphics.drawable.Vector")) {
            Toast.makeText(CadastroPromocaoActivity.this, "Verifique se imagem esta correta!!", Toast.LENGTH_LONG).show();

        } else {

            final StorageReference salvaFotoReferencia = storageReference.child("imagemPromocao/" + edtCadCodBarProd.getText().toString() + ".jpg");

            imgProduto.setDrawingCacheEnabled(true);
            imgProduto.buildDrawingCache();

            Bitmap bitmap = imgProduto.getDrawingCache();

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
                        Toast.makeText(CadastroPromocaoActivity.this, "Erro ao salvar Promoção", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void salvar() {

    }


    private void findSupermercado() {

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


    public int getItemCount() {
        return supermercados.size();
    }

    private void resetIntent() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);

    }


}
