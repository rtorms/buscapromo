package utfpr.edu.br.buscapromo.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import utfpr.edu.br.buscapromo.Classes.Departamento;
import utfpr.edu.br.buscapromo.R;

public class CadastroDepartamentoActivity extends AppCompatActivity {

    private DatabaseReference reference;
    private EditText edtDepartamento;
    private FirebaseDatabase database;
    private String key;
    private Departamento departamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_departamento);

        edtDepartamento = findViewById(R.id.edtCadDepartamento);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("departamentos");


    }


    public void btnCadDepartamento(View view) {

        departamento = new Departamento();
        departamento.setDescricao(edtDepartamento.getText().toString());

        try {

            key = reference.push().getKey();
            departamento.setIdDepartamento(key);
            reference.push().setValue(departamento);
            Toast.makeText(CadastroDepartamentoActivity.this, "Departamento Cadastrado com Sucesso!!", Toast.LENGTH_LONG).show();
            edtDepartamento.setText("");
        } catch (Exception e) {
            Toast.makeText(CadastroDepartamentoActivity.this, "Erro ao salvar departamento", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    public void btnCancelarOnclickListener(View view) {

        Intent intent = new Intent(CadastroDepartamentoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }
}
