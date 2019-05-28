package utfpr.edu.br.buscapromo.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import utfpr.edu.br.buscapromo.Model.Usuario;
import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Helper.UsuarioLogado;
import utfpr.edu.br.buscapromo.R;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth autenticacao;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText edtEmailLogin;
    private EditText edtSenhaLogin;
    private Button btnLogin;
    private Usuario usuario;
    private CallbackManager callbackManager;
    private TextView tvRecuperarSenha;
    private AlertDialog alerta;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmailLogin = ( EditText ) findViewById( R.id.edtEmail );
        edtSenhaLogin = ( EditText ) findViewById( R.id.edtSenha );
        btnLogin = ( Button ) findViewById( R.id.btnLogin );
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        tvRecuperarSenha = (TextView) findViewById(R.id.tvRecuperarSenha);

        final EditText edEmailRecuperar = new EditText(LoginActivity.this);
        edEmailRecuperar.setHint("e-mail@exemplo.com");


        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), R.string.cancel_login, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {

                Toast.makeText(LoginActivity.this, "Usuário ou senha inválidos! Tente novamente", Toast.LENGTH_SHORT).show();

            }
        });

        autenticacao = FirebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(getApplicationContext(),"Login efetuado via Facebook com sucesso!", Toast.LENGTH_LONG).show();
                }
            }
        };

        tvRecuperarSenha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setCancelable(false);
                builder.setTitle("Recuperar Senha");
                builder.setMessage("Informe seu email:");
                builder.setView(edEmailRecuperar);

                if(!edEmailRecuperar.getText().equals("")){

                    builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            autenticacao = FirebaseAuth.getInstance();

                            String emailRecuperar = edEmailRecuperar.getText().toString();

                            autenticacao.sendPasswordResetEmail(emailRecuperar).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        Toast.makeText(getApplicationContext(), "Email enviado, verifique sua caixa de entrada!" ,Toast.LENGTH_SHORT).show();
                                        resetIntent();
                                    }else {
                                        Toast.makeText(getApplicationContext(), "Falha ao enviar email!" ,Toast.LENGTH_SHORT).show();
                                        resetIntent();
                                    }
                                }
                            });
                        }
                    });

                    builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetIntent();
                        }
                    });

                }else{
                    Toast.makeText(getApplicationContext(), "Necessário informar seu email!" ,Toast.LENGTH_SHORT).show();

                }

                alerta = builder.create();
                alerta.show();
            }
        });
    }

    private void resetIntent(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        autenticacao.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        autenticacao.removeAuthStateListener(firebaseAuthListener);
    }

    public void btnLoginOnclick(View view) {
        if (!edtEmailLogin.getText().toString().equals("") && !edtSenhaLogin.getText().toString().equals("")) {
            usuario = new Usuario();
            usuario.setEmail(edtEmailLogin.getText().toString());
            usuario.setSenha(edtSenhaLogin.getText().toString());

            validarLogin();
        } else {
            Toast.makeText(LoginActivity.this, "Preencha os campos de E-Mail e Senha", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void validarLogin(){
        progressBar.setVisibility(View.GONE);
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.signInWithEmailAndPassword(usuario.getEmail().toString(), usuario.getSenha().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if ( task.isSuccessful()){

                    UsuarioLogado usuarioLogado = new UsuarioLogado(LoginActivity.this);
                    usuarioLogado.salvarUsuarioPreferencias(usuario.getEmail(), usuario.getSenha());
                    abrirTelaPrincipal();
                    Toast.makeText(LoginActivity.this, "Login efetuado com sucesso", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(LoginActivity.this, "Usuário ou senha não conferem!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void handleFacebookAccessToken(AccessToken accessToken) {
        progressBar.setVisibility(View.VISIBLE);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());

        autenticacao.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (!task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),"Login efetuado via Facebook com sucesso!", Toast.LENGTH_LONG).show();

                }
                abrirTelaPrincipal();
            }
        });
    }
    private void abrirTelaPrincipal(){
        Intent intent = new Intent(LoginActivity.this, TelaPrincipalActivity.class);
        startActivity(intent);
        finish();
    }


    public void btnCadUserSimplesOnclick(View view) {
        Intent intent = new Intent(LoginActivity.this, CadastroUsuarioSimplesActivity.class);
        startActivity(intent);
        finish();
    }
}
