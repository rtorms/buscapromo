package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthActionCodeException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import utfpr.edu.br.buscapromo.Model.Usuario;
import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Helper.UsuarioLogado;
import utfpr.edu.br.buscapromo.R;

public class CadastroUserViaAdmActivity extends AppCompatActivity {

    private EditText email;
    private EditText senha1;
    private EditText senha2;
    private EditText nome;
    private Button btnCadastrar;
    private Button btnCancelar;
    private FirebaseAuth autenticacao;
    private FirebaseDatabase database;
    private DatabaseReference reference;
    private Usuario usuario;
    private RadioButton rbAdmin;
    private RadioButton rbUsuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_user_via_adm);

        email = findViewById(R.id.edtCadEmail);
        senha1 = findViewById(R.id.edtCadSenha1);
        senha2 = findViewById(R.id.edtCadSenha2);
        nome = findViewById(R.id.edtCadNome);
        rbAdmin = findViewById(R.id.rbAdm);
        rbUsuario = findViewById(R.id.rbUser);
        btnCadastrar = findViewById(R.id.btnCadastrar);
        btnCancelar = findViewById(R.id.btnCancelar);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (senha1.getText().toString().equals(senha2.getText().toString())) {
                    usuario = new Usuario();

                    usuario.setEmail(email.getText().toString());
                    usuario.setSenha(senha1.getText().toString());
                    usuario.setNome(nome.getText().toString());
                    if (rbAdmin.isChecked()) {
                        usuario.setTipoUsuario("Administrador");
                    } else if (rbUsuario.isChecked()) {
                        usuario.setTipoUsuario("Usuario");
                    }

                    cadastrarUsuario();
                } else {
                    Toast.makeText(CadastroUserViaAdmActivity.this, "Senhas Informadas Não Conferem!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cadastrarUsuario() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(CadastroUserViaAdmActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    insereUsuario(usuario);
                    finish();
                    autenticacao.signOut();
                    abreTelaPrincipal();
                } else {

                    String erroExcecao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthActionCodeException e) {
                        erroExcecao = "Senha muito fraca, deve conter mínimo 6 caracteres com letras e números!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExcecao = "E-mail inválido, digite novo e-mail!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExcecao = "E-mail já cadastrado!";
                    } catch (Exception e) {
                        erroExcecao = "Erro ao efetuar cadastro!";
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroUserViaAdmActivity.this, "Erro!" + erroExcecao, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private boolean insereUsuario(Usuario usuario) {

        try {
            reference = ConfiguracaoFirebase.getFirebase().child("usuarios");
            String key = reference.push().getKey();
            usuario.setKey(key);
            reference.child(key).setValue(usuario);
            Toast.makeText(CadastroUserViaAdmActivity.this, "Usuário Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();
            return true;
        } catch (Exception e) {
            Toast.makeText(CadastroUserViaAdmActivity.this, "Erro ao salvar usuário", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private void abreTelaPrincipal() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        UsuarioLogado usuarioLogado = new UsuarioLogado(CadastroUserViaAdmActivity.this);
        autenticacao.signInWithEmailAndPassword(usuarioLogado.getEmail_Usuario_Logado(),
                usuarioLogado.getSenha_Usuario_Logado()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    Intent intent = new Intent(CadastroUserViaAdmActivity.this, TelaPrincipalActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CadastroUserViaAdmActivity.this, "Falha", Toast.LENGTH_LONG).show();
                    autenticacao.signOut();
                    Intent intent = new Intent(CadastroUserViaAdmActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroUserViaAdmActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }
}
