package utfpr.edu.br.buscapromo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import utfpr.edu.br.buscapromo.adapter.PromocaoAdapter;
import utfpr.edu.br.buscapromo.dao.FindListObjectGeneric;
import utfpr.edu.br.buscapromo.dao.FindListStringGeneric;
import utfpr.edu.br.buscapromo.dao.FindObjectGeneric;
import utfpr.edu.br.buscapromo.dao.ListObjectInterface;
import utfpr.edu.br.buscapromo.dao.ListStringCallbackInterface;
import utfpr.edu.br.buscapromo.dao.ObjectCallbackInterface;
import utfpr.edu.br.buscapromo.model.Promocao;
import utfpr.edu.br.buscapromo.model.Usuario;
import utfpr.edu.br.buscapromo.R;

public class TelaPrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Serializable {

    public static final String PREFS_NAME = "prefs";
    private FirebaseAuth autenticacao;
    private Menu menu;
    private String nomeUserLogado;
    private TextView userNome;
    private String tipoUsuarioCad;
    private CircleImageView imgPerfil;
    private String provider;
    private DatabaseReference reference;
    private String email;
    private String filtroSelecionado = "todas";
    private String select = "todas";
    private AlertDialog alerta;
    private FindListStringGeneric findListStringGeneric;
    private DatabaseReference databaseReference;
    private RecyclerView mRecyclerViewPromocao;
    private PromocaoAdapter adapter;
    private List<Promocao> todasPromocoes;
    private Promocao promocao;
    private FirebaseUser user;
    private String keyUser;
    private Spinner spFiltro;
    private FloatingActionButton floatingAcBtnCesta;
    private SearchView searchView;
    private FindObjectGeneric findObjectGeneric;
    private FindListObjectGeneric findListObjectGeneric;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findObjectGeneric = new FindObjectGeneric();
        findListObjectGeneric = new FindListObjectGeneric();

        autenticacao = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        provider = autenticacao.getCurrentUser().getProviders().toString();
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();

        floatingAcBtnCesta = findViewById(R.id.floatingAcBtnCesta);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //recupera dados nav_header_tela_principal (imagem e nome usuário)
        View headView = navigationView.getHeaderView(0);
        imgPerfil = (CircleImageView) headView.findViewById(R.id.imgPerfilDrawer);
        userNome = (TextView) headView.findViewById(R.id.tvNomeUsuario);


        //recupera itens do menu
        menu = navigationView.getMenu();
        final MenuItem inserePromo = menu.findItem(R.id.nav_InserePromocao);
        final MenuItem editarPerfil = menu.findItem(R.id.nav_editarPerfil);
        final MenuItem insereDepartamento = menu.findItem(R.id.nav_cadDepartamento);
        final MenuItem menuAdm = menu.findItem(R.id.itemMenuAdm);
        final MenuItem insereProduto = menu.findItem(R.id.nav_cadProduto);

        if (provider.contains("facebook")) {

            String uriFacebook = autenticacao.getCurrentUser().getPhotoUrl().toString();
            String userFacebook = autenticacao.getCurrentUser().getDisplayName().toString();
            final int heigth = 200;
            final int width = 200;
            Picasso.get().load(uriFacebook).resize(width, heigth).into(imgPerfil);
            userNome.setText(userFacebook);

            editarPerfil.setVisible(false);
            menuAdm.setVisible(false);
            insereDepartamento.setVisible(false);
            insereProduto.setVisible(false);

        } else {
            usuario = new Usuario();
            findObjectGeneric.findObjectCallback("usuarios", "email",
                    email, new Usuario(), this, new ObjectCallbackInterface() {
                        @Override
                        public void onCallback(Object object) {
                            usuario = (Usuario) object;
                            if (usuario.getKey() != null) {
                                tipoUsuarioCad = usuario.getTipoUsuario();
                                userNome.setText(nomeUserLogado = usuario.getNome());
                                keyUser = usuario.getKey();

                                if (!tipoUsuarioCad.equals("Administrador")) {
                                    menuAdm.setVisible(false);
                                }
                                if (tipoUsuarioCad.equals("Usuario")) {
                                    insereDepartamento.setVisible(false);
                                    insereProduto.setVisible(false);
                                }
                            } else {
                                Toast.makeText(TelaPrincipalActivity.this, "Usuário não encontrado!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
            carregaImagemPerfil();
        }

        spFiltro = findViewById(R.id.spFiltro);
        searchView = findViewById(R.id.searchView);

        mRecyclerViewPromocao = (RecyclerView) findViewById(R.id.recycleViewTodasPromocoes);
        carregarPromocoes();

        //busca direta por texto
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                filtroSelecionado = "produto";
                select = query;
                carregarPromocoes();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void selecionaFiltro(String filtro, String filtro2) {
        searchView.setVisibility(View.GONE);
        spFiltro.setVisibility(View.VISIBLE);
        findListStringGeneric = new FindListStringGeneric();
        findListStringGeneric.listaStringGenericCallback(filtro, filtro2, this, new ListStringCallbackInterface() {
            @Override
            public void onCallback(ArrayAdapter<String> filtroAdapter) {
                spFiltro.setAdapter(filtroAdapter);
                setOnItemSelectedListenerSpFiltro();
            }
        });
    }

    private void setOnItemSelectedListenerSpFiltro() {
        spFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                select = spFiltro.getSelectedItem().toString();
                carregarPromocoes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tela_principal, menu);
        return true;
    }

    private void carregaImagemPerfil() {

        user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        final StorageReference storageReference = storage.getReferenceFromUrl
                ("gs://buscapromo-7627b.appspot.com/fotoPerfilUsuario/" + user.getEmail() + ".jpg");

        final int heigth = 300;
        final int width = 300;

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).resize(width, heigth).centerCrop().into(imgPerfil);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                imgPerfil.setImageResource(R.drawable.add_user);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //filtro
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.filtrar_todas) {
            spFiltro.setVisibility(View.GONE);
            searchView.setVisibility(View.GONE);
            filtroSelecionado = "todas";
            carregarPromocoes();
        } else if (id == R.id.filtrar_supermercado) {
            selecionaFiltro("supermercados", "nome");
            filtroSelecionado = "supermercado";
        } else if (id == R.id.filtrar_departamento) {
            selecionaFiltro("departamentos", "descricao");
            filtroSelecionado = "departamento";
        } else if (id == R.id.filtrar_produto) {
            spFiltro.setVisibility(View.GONE);
            searchView.setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }

    //menu
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_InserePromocao) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroPromocaoActivity.class);
            intent.putExtra("tipoUsuario", tipoUsuarioCad);
            intent.putExtra("provider", provider);
            intent.putExtra("keyUser", keyUser);
            intent.putExtra("nomeUserLogado", userNome.getText().toString());
            abrirTela(intent);

        } else if (id == R.id.nav_cadProduto) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroProdutoActivity.class);
            abrirTela(intent);

        } else if (id == R.id.nav_cadDepartamento) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroDepartamentoActivity.class);
            abrirTela(intent);

        } else if (id == R.id.nav_editarPerfil) {

            Intent intent = new Intent(TelaPrincipalActivity.this, EditarPerfilActivity.class);
            intent.putExtra("nome", userNome.getText().toString());
            intent.putExtra("tipoUsuarioCad", tipoUsuarioCad);
            abrirTela(intent);

        } else if (id == R.id.action_deslogar) {
            if (provider.contains("facebook")) {
                LoginManager.getInstance().logOut();
            }
            deslogarUsuario();

        } else if (id == R.id.action_add_usuario_via_adm) {

            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroUserViaAdmActivity.class);
            abrirTela(intent);

        } else if (id == R.id.action_add_supermercado) {

            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroSupermercadoActivity.class);
            abrirTela(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void abrirTela(Intent intent) {
        startActivity(intent);
    }

    private void deslogarUsuario() {

        autenticacao.signOut();
        Intent intent = new Intent(TelaPrincipalActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void carregarPromocoes() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        final Integer dataAtual = Integer.parseInt(dateFormat.format(new Date()));


        mRecyclerViewPromocao.setLayoutManager(new GridLayoutManager(this, 2));
        todasPromocoes = new ArrayList<>();

        findListObjectGeneric.findListObjectGeneric(new Promocao(), "promocoes",
                this, new ListObjectInterface() {
                    @Override
                    public void onCallback(List<Object> objects) {
                        todasPromocoes.clear();
                        List<Promocao> retornoTodasPromocoes = new ArrayList<>();
                        retornoTodasPromocoes = (List<Promocao>) (Object) objects;

                        promocao = new Promocao();
                        for (Promocao promo : retornoTodasPromocoes) {
                            promocao = promo;

                            if (promocao.getDataValidade() >= dataAtual) {

                                if (filtroSelecionado.equals("todas")) {
                                    todasPromocoes.add(promocao);
                                }
                                if ((promocao.getProduto().getDepartamento().equals(select))) {
                                    todasPromocoes.add(promocao);
                                }
                                if (filtroSelecionado.equals("produto")) {
                                    if ((promocao.getProduto().getConteudo().equalsIgnoreCase(select))
                                            || (promocao.getProduto().getDepartamento().equalsIgnoreCase(select))
                                            || (promocao.getProduto().getEmbalagem().equalsIgnoreCase(select))
                                            || (promocao.getProduto().getMarca().equalsIgnoreCase(select))
                                            || (promocao.getProduto().getNomeProduto().equalsIgnoreCase(select)
                                            || (promocao.getProduto().getTipo().equalsIgnoreCase(select)))) {
                                        todasPromocoes.add(promocao);
                                    }
                                }
                                if (promocao.getSupermercado().equals(select)) {
                                    todasPromocoes.add(promocao);
                                }
                            }
                        }
                        if (todasPromocoes.isEmpty()) {
                            Toast.makeText(TelaPrincipalActivity.this, "Não há promoções para este filtro", Toast.LENGTH_LONG).show();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
        adapter = new PromocaoAdapter(todasPromocoes, this);
        mRecyclerViewPromocao.setAdapter(adapter);

    }

    public void findCestaOnClick(View view) {

        List<Promocao> list = adapter.getPromocoesSelect();

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        String connectionsJSONString = new Gson().toJson(list);
        editor.putString(PREFS_NAME, connectionsJSONString);
        editor.commit();

        Intent i = new Intent(this, MapsActivity.class);
        recreate();
        startActivity(i);
    }
}


