package utfpr.edu.br.buscapromo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import utfpr.edu.br.buscapromo.model.Departamento;
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

        reference.orderByChild("nome").equalTo(departamento.getDescricao())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {

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

                        } else {
                            Toast.makeText(CadastroDepartamentoActivity.this, "Não é possível cadastro, departamento já cadastrado!", Toast.LENGTH_LONG).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(CadastroDepartamentoActivity.this, "ERRO!!" + databaseError, Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void btnCancelarOnclickListener(View view) {

        Intent intent = new Intent(CadastroDepartamentoActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);
    }
}
