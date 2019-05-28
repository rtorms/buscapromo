package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import utfpr.edu.br.buscapromo.R;

public class CadastroUsuarioSimplesActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_usuario_simples);

        email = findViewById(R.id.edtCadEmail);
        senha1 = findViewById(R.id.edtCadSenha1);
        senha2 = findViewById(R.id.edtCadSenha2);
        nome = findViewById(R.id.edtCadNome);
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
                    usuario.setTipoUsuario("Usuario");
                    cadastrarUsuario();
                } else {
                    Toast.makeText(CadastroUsuarioSimplesActivity.this, "Senhas Informadas Não Conferem!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cadastrarUsuario() {

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(
                usuario.getEmail(),
                usuario.getSenha()
        ).addOnCompleteListener(CadastroUsuarioSimplesActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    insereUsuario(usuario);
                    limparTela();
                } else {

                    String erroExcecao = "";

                    try {
                        throw task.getException();
                    } catch (FirebaseAuthActionCodeException e) {
                        erroExcecao = "Senha muito fraca, deve conter mínimo 6 caracteres com letras e números!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        erroExcecao = "E-mail ou senha inválida, repita operação!";
                    } catch (FirebaseAuthUserCollisionException e) {
                        erroExcecao = "E-mail já cadastrado!";
                    } catch (Exception e) {
                        erroExcecao = "Erro ao efetuar cadastro!";
                        e.printStackTrace();
                    }
                    Toast.makeText(CadastroUsuarioSimplesActivity.this, "Erro!" + erroExcecao, Toast.LENGTH_LONG).show();
                    limparTela();
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
            Toast.makeText(CadastroUsuarioSimplesActivity.this, "Usuário Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();
            abreTelaPrincipal();
            return true;
        } catch (Exception e) {
            Toast.makeText(CadastroUsuarioSimplesActivity.this, "Erro ao salvar usuário", Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }
    }

    private void limparTela() {
        email.setText("");
        senha1.setText("");
        senha2.setText("");
        nome.setText("");
    }

    private void abreTelaPrincipal() {

        Intent intent = new Intent(CadastroUsuarioSimplesActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(CadastroUsuarioSimplesActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }


}
