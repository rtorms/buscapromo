package utfpr.edu.br.buscapromo.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.DAO.FindListObjectGeneric;
import utfpr.edu.br.buscapromo.DAO.FindObjectGeneric;
import utfpr.edu.br.buscapromo.DAO.ListObjectInterface;
import utfpr.edu.br.buscapromo.DAO.ObjectCallbackInterface;
import utfpr.edu.br.buscapromo.DAO.Permissoes;
import utfpr.edu.br.buscapromo.Model.Produto;
import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.Model.SolicitaCadastroProduto;
import utfpr.edu.br.buscapromo.Model.Supermercado;
import utfpr.edu.br.buscapromo.R;

public class CadastroPromocaoActivity extends AppCompatActivity implements LocationListener {

    static final int DATE_DIALOG_ID = 0;
    final Activity camera = this;
    private Permissoes permissoes = new Permissoes();

    private Button btnCancelar;
    private Button btnFindProduto;

    private EditText edtCadCodBarProd;
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

    private DatabaseReference databaseReference;
    private FindObjectGeneric findObjectGeneric;
    private FindListObjectGeneric findListObjectGeneric;

    private ProgressBar progressBar;
    private Date dataInicial = new Date();
    private Date dataFinal = new Date();

    private String tagButtonDatain;
    private String tipoUsuarioLogado;
    private String provider;
    private String nomeUserLogado;
    private double latitude;
    private double longitude;

    private Produto produto;
    private List<Supermercado> supermercados;
    private LocationManager lm;


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
                                alert("Data Inicial deve ser atual ou posterior, verifique!");
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
                                alert("Data final deve ser posterior ou igual a inicial, verifique!");
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

        permissoes.permissoes(this, this);

        Intent i = getIntent();
        tipoUsuarioLogado = i.getStringExtra("tipoUsuario");
        provider = i.getStringExtra("provider");
        nomeUserLogado = i.getStringExtra("nomeUserLogado");

        tvDataFinal = findViewById(R.id.tvDataFinal);
        tvDataInicial = findViewById(R.id.tvDataInicial);
        tvDataInicial.setText(getDateTime());
        tvDataFinal.setText(getDateTime());
        tvProduto = findViewById(R.id.tvProduto);
        tvMostrarCodBar = findViewById(R.id.tvMostrarCodBar);
        btnCancelar = findViewById(R.id.btnCancelar);

        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);
        edtValorProdutoOriginal = findViewById(R.id.edtValorProdutoOriginal);
        edtValorProdutoPromocional = findViewById(R.id.edtValorProdutoPromocional);
        edtCadCodBarProd = findViewById(R.id.edtCadCodBarProd);
        actvSupermercado = findViewById(R.id.actvSupermercado);
        edtCadCodBarProd.requestFocus();

        imgProduto = findViewById(R.id.fotoProduto);
        imgLinearLayout = findViewById(R.id.imgLinearLayout);
        imgLinearLayout.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBarPromocao);
        btnFindProduto = findViewById(R.id.btnFindProduto);

        findObjectGeneric = new FindObjectGeneric();
        findListObjectGeneric = new FindListObjectGeneric();

        if (provider.contains("facebook")) {
            tipoUsuarioLogado = "facebook";
        }
        findSupermercado();
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void insereDataOnclick(View view) {
        if ((tipoUsuarioLogado.equals("Supermercado")) || (tipoUsuarioLogado.equals("Administrador"))) {
            tagButtonDatain = view.getTag().toString();
            showDialog(DATE_DIALOG_ID);

        } else {
            alert("Alteração de datas permitidas somente para supermercados!");
        }
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

        if (intentResult.getContents() != null) {
            edtCadCodBarProd.setEnabled(true);
            edtCadCodBarProd.setVisibility(View.VISIBLE);
            edtCadCodBarProd.setText(intentResult.getContents());
            findProduto();
        } else {
            edtCadCodBarProd.setEnabled(true);
            edtCadCodBarProd.setVisibility(View.VISIBLE);
            alert("Scan cancelado");
        }
    }

    private void alert(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }

    private void findProduto() {

        findObjectGeneric.findObjectCallback("produtos", "codBarras",
                (edtCadCodBarProd.getText().toString()), new Produto(), this, new ObjectCallbackInterface() {

                    @Override
                    public void onCallback(Object object) {
                        produto = (Produto) object;
                        if (produto.getIdProd() != null) {
                            tvProduto.setText(produto.getNomeProduto() + " " +
                                    produto.getTipo() + " " +
                                    produto.getMarca() + " " +
                                    produto.getEmbalagem() + " " +
                                    produto.getConteudo());
                            tvMostrarCodBar.setText(produto.getCodBarras());
                            tvMostrarCodBar.setVisibility(View.VISIBLE);
                            edtCadCodBarProd.setVisibility(View.GONE);
                            imgLinearLayout.setVisibility(View.VISIBLE);
                            imgProduto.setVisibility(View.VISIBLE);

                            Picasso.get().load(produto.getUrlImagem()).resize(350, 300).centerCrop().into(imgProduto);
                        } else {
                            imgProduto.setVisibility(View.GONE);
                            imgLinearLayout.setVisibility(View.VISIBLE);
                            tvMostrarCodBar.setVisibility(View.GONE);
                            tvProduto.setVisibility(View.VISIBLE);
                            tvProduto.setText("Produto não cadastrado, enviando solicitação de cadastro...");
                            solicitaCadastroProduto();
                        }
                    }
                });
    }

    public void btnFindProdutoOnClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CadastroPromocaoActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Cadastro Promoção");
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
                    alert("Erro: " + e.getMessage());
                }
            }
        });
        builder.setNegativeButton("Digitar", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                edtCadCodBarProd.setEnabled(true);
                edtCadCodBarProd.setVisibility(View.VISIBLE);
                edtCadCodBarProd.setFocusable(true);
                tvMostrarCodBar.setVisibility(View.GONE);
            }
        });
        alerta = builder.create();
        alerta.show();
    }

    private void salvar() {

        Supermercado sup1 = new Supermercado();
        sup1 = getSupermercado(actvSupermercado.getText().toString());

        Promocao promocao = new Promocao();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        promocao.setDataInsercao(Integer.parseInt(dateFormat.format(dataInicial)));
        promocao.setDataValidade(Integer.parseInt(dateFormat.format(dataFinal)));
        promocao.setProduto(produto);
        promocao.setUsuario(nomeUserLogado);

        if (sup1.getNome() == null) {
            promocao.setSupermercado(actvSupermercado.getText().toString());
            promocao.setLatitude(String.valueOf(latitude));
            promocao.setLongitude(String.valueOf(longitude));
        } else {
            promocao.setSupermercado(sup1.getNome());
            promocao.setLatitude(sup1.getLatitude());
            promocao.setLongitude(sup1.getLongitude());
        }

        promocao.setValorOriginal(Double.parseDouble(edtValorProdutoOriginal.getText().toString()));
        promocao.setValorPromocional(Double.parseDouble(edtValorProdutoPromocional.getText().toString()));

        databaseReference = ConfiguracaoFirebase.getFirebase().child("promocoes");
        String key = databaseReference.push().getKey();

        if (promocao.getKey() == null) {
            try {
                promocao.setKey(key);
                databaseReference.child(key).setValue(promocao);
                recreate();
                alert("Promoção Cadastrada com Sucesso!!");
                edtValorProdutoPromocional.setText("");
                edtValorProdutoOriginal.setText("");
            } catch (Exception e) {
                alert("Erro ao salvar Promoção");
                e.printStackTrace();
            }
        }
    }

    private void findSupermercado() {
        if (tipoUsuarioLogado.equals("Supermercado")) {
            actvSupermercado.setEnabled(false);
            actvSupermercado.setText(nomeUserLogado);
        }

        findListObjectGeneric.findListObjectGeneric(new Supermercado(), "supermercados",
                this, new ListObjectInterface() {
                    @Override
                    public void onCallback(List<Object> objects) {
                        supermercados = (List<Supermercado>) (Object) objects;

                        final List<String> sup = new ArrayList<String>();
                        for (Supermercado s : supermercados) {
                            String nomeSup = s.getNome();
                            sup.add(nomeSup);
                        }

                        ArrayAdapter<String> supAdapter = new ArrayAdapter<String>(CadastroPromocaoActivity.this,
                                android.R.layout.simple_dropdown_item_1line, sup);
                        supAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                        actvSupermercado.setAdapter(supAdapter);
                    }
                });
    }

    private Supermercado getSupermercado(String nomeSupermercado) {

        Supermercado sup2 = new Supermercado();
        for (int i = 0; i < supermercados.size(); i++) {
            if (supermercados.get(i).getNome().equals(nomeSupermercado)) {
                sup2 = supermercados.get(i);
            }
        }
        return sup2;
    }

    public void salvarOnclick(View view) {

        Boolean salvarOk = true;
        Double valorIn = 0.0;
        Double valorOut = 0.0;

        if (produto == null) {
            salvarOk = false;
            alert("Produto não informado, não é possível completar operação!");
        } else if (actvSupermercado.getText().toString().isEmpty()) {
            salvarOk = false;
            alert("Supermercado não informado, verifique!");
        } else if (edtValorProdutoOriginal.getText().length() < 1
                || edtValorProdutoPromocional.getText().length() < 1) {
            salvarOk = false;
            alert("Valores não informados corretamente, verifique!");

        } else if (edtValorProdutoOriginal.getText().toString().length() >= 1
                || edtValorProdutoPromocional.getText().toString().length() >= 1) {
            valorOut = Double.parseDouble(edtValorProdutoPromocional.getText().toString());
            valorIn = Double.parseDouble(edtValorProdutoOriginal.getText().toString());
            if (valorIn < valorOut) {
                salvarOk = false;
                alert("Valor promocional maior que original, verifique os valores!");
            }
            if (salvarOk == true) {
                salvar();
            }
        }
    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroPromocaoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }


    public void edtCadCodBarProdOnClick(View view) {
        if (edtCadCodBarProd.getText().length() != 13) {
            alert("Erro! \n Código digitado incorreto!");
        } else {
            findProduto();
        }
    }

    public void solicitaCadastroProduto() {

        findObjectGeneric.findObjectCallback("solicitaCadastroProduto", "codBarras",
                (edtCadCodBarProd.getText().toString()), new SolicitaCadastroProduto(), this, new ObjectCallbackInterface() {
                    @Override
                    public void onCallback(Object object) {
                        SolicitaCadastroProduto novoCadastro = new SolicitaCadastroProduto();
                        novoCadastro = (SolicitaCadastroProduto) object;
                        if (novoCadastro.getKey() == null) {
                            try {
                                databaseReference = FirebaseDatabase.getInstance().getReference();
                                SolicitaCadastroProduto solicitaCadastroProduto = new SolicitaCadastroProduto();
                                String key = databaseReference.push().getKey();
                                solicitaCadastroProduto.setKey(key);
                                solicitaCadastroProduto.setCodBarras(edtCadCodBarProd.getText().toString());
                                databaseReference.child("solicitaCadastroProduto").child(key).setValue(solicitaCadastroProduto);
                                alert("Solicitação efetuada com Sucesso!!");
                                edtCadCodBarProd.setText("");
                                recreate();
                            } catch (Exception e) {
                                alert("Erro! Repita a operação");
                                e.printStackTrace();
                            }
                        } else {
                            alert("Cadastro em andamento, aguarde!");
                            edtCadCodBarProd.setText("");
                            recreate();
                        }
                    }

                });
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
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


}

