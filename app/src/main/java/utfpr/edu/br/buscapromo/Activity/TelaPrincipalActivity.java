package utfpr.edu.br.buscapromo.Activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import utfpr.edu.br.buscapromo.Adapter.PromocaoAdapter;
import utfpr.edu.br.buscapromo.Model.Produto;
import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.Model.Supermercado;
import utfpr.edu.br.buscapromo.R;

public class TelaPrincipalActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth autenticacao;
    private Menu menu;
    private String nomeUserLogado;
    private TextView userNome;
    private String tipoUsuarioCad;
    private CircleImageView imgPerfil;
    private String provider;
    private DatabaseReference reference;
    private String email;
    private EditText edtFind;
    private String departamentoSelecionado;
    private AlertDialog alerta;

    private DatabaseReference databaseReference;
    private RecyclerView mRecyclerViewPromocao;
    private PromocaoAdapter adapter;
    private List<Produto> produtos;
    private Produto todosProdutos;
    private List<Promocao> promocoes;
    private Promocao todasPromocoes;
    private LinearLayoutManager mLayoutManagerTodosProdutos;
    private FirebaseUser user;
    private String keyUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_principal);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        edtFind = findViewById(R.id.edtFind);
        edtFind.setEnabled(false);

        autenticacao = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        provider = autenticacao.getCurrentUser().getProviders().toString();
        user = FirebaseAuth.getInstance().getCurrentUser();
        email = user.getEmail();


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
        MenuItem editarPerfil = menu.findItem(R.id.nav_editarPerfil);
        final MenuItem insereDepartamento = menu.findItem(R.id.nav_cadDepartamento);
        final MenuItem menuAdm = menu.findItem(R.id.itemMenuAdm);


        if (provider.contains("facebook")) {

            String uriFacebook = autenticacao.getCurrentUser().getPhotoUrl().toString();
            String userFacebook = autenticacao.getCurrentUser().getDisplayName().toString();
            final int heigth = 200;
            final int width = 200;
            Picasso.get().load(uriFacebook).resize(width, heigth).into(imgPerfil);
            userNome.setText(userFacebook);

            editarPerfil.setVisible(false);
            menuAdm.setVisible(false);

        } else {

            reference.child("usuarios").orderByChild("email").equalTo(email).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                        tipoUsuarioCad = posSnapshot.child("tipoUsuario").getValue().toString();
                        userNome.setText(nomeUserLogado = posSnapshot.child("nome").getValue().toString());
                        keyUser = posSnapshot.getKey();

                        if (!tipoUsuarioCad.equals("Administrador")) {
                            menuAdm.setVisible(false);
                        } else if ((!tipoUsuarioCad.equals("Supermercado")) || (!tipoUsuarioCad.equals("Administrador"))) {
                            insereDepartamento.setVisible(false);
                            inserePromo.setVisible(false);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(TelaPrincipalActivity.this, "Cancelado pelo usuário!!", Toast.LENGTH_LONG).show();
                }
            });
            carregaImagemPerfil();
        }

        mRecyclerViewPromocao = (RecyclerView) findViewById(R.id.recycleViewTodasPromocoes);

        carregarPromocoes();

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


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_InserePromocao) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroPromocaoActivity.class);
            intent.putExtra("tipoUsuario", tipoUsuarioCad);
            intent.putExtra("keyUser", keyUser);
            abrirTela(intent);

        } else if (id == R.id.nav_cadProduto) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroProdutoActivity.class);
            abrirTela(intent);

        } else if (id == R.id.nav_cadDepartamento) {
            Intent intent = new Intent(TelaPrincipalActivity.this, CadastroDepartamentoActivity.class);
            abrirTela(intent);


        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_editarPerfil) {

            Intent intent = new Intent(TelaPrincipalActivity.this, EditarPerfilActivity.class);
            intent.putExtra("nome", nomeUserLogado);
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

//        mRecyclerViewPromocao.setHasFixedSize(true);
        mRecyclerViewPromocao.setLayoutManager(new GridLayoutManager(this, 2));
        //  mLayoutManagerTodosProdutos = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
//            mRecyclerViewPromocao.setLayoutManager(mLayoutManagerTodosProdutos);
        promocoes = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("promocoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promocoes.clear();
                todasPromocoes = new Promocao();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    todasPromocoes.setProduto(postSnapshot.child("produto").getValue(Produto.class));
                    todasPromocoes.setSupermercado(postSnapshot.child("supermercado").getValue(Supermercado.class));

//                    todasPromocoes = postSnapshot.getValue(Promocao.class);
                    promocoes.add(todasPromocoes);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        adapter = new PromocaoAdapter(promocoes, this);
        mRecyclerViewPromocao.setAdapter(adapter);
    }

    // alterei aqui para testar dialog, necessário rever
    public void findPromocaoOnClick(View view) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(TelaPrincipalActivity.this);
        builder.setCancelable(true);
        builder.setTitle("FILTRAR PESQUISA");
        builder.setMessage("Como deseja Filtrar?");

        builder.setPositiveButton("TEXTO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                edtFind.setEnabled(true);
//                edtFind.setFocusable(true);
                // alterei aqui para testar dialog, necessário rever

                Dialog mDialog = new Dialog(TelaPrincipalActivity.this, R.style.AppBaseTheme);
                mDialog.setContentView(R.layout.fullscreen);
                mDialog.show();
            }
        });

        builder.setNegativeButton("Departamento", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                databaseReference.child("departamentos").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<String> dep = new ArrayList<String>();

                        for (DataSnapshot depSnapshot : dataSnapshot.getChildren()) {
                            String nomeDep = depSnapshot.child("descricao").getValue(String.class);
                            dep.add(nomeDep);
                        }

                        ArrayAdapter<String> depAdapter = new ArrayAdapter<String>(TelaPrincipalActivity.this,
                                R.layout.find_departamento, dep);
                        //  AlertDialog.Builder builder1 = new AlertDialog.Builder(TelaPrincipalActivity.this);

                        //    depAdapter.setDropDownViewResource(R.layout.find_departamento);

                        builder.setSingleChoiceItems(depAdapter, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                departamentoSelecionado = dialog.toString();
                                alerta.dismiss();
                            }
                        });

                        alerta = builder.create();
                        alerta.show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
        alerta = builder.create();
        alerta.show();
    }


}

