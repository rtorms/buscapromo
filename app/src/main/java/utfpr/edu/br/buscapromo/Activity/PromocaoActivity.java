package utfpr.edu.br.buscapromo.Activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import utfpr.edu.br.buscapromo.Adapter.PromocaoAdapter;
import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.R;

public class PromocaoActivity extends AppCompatActivity {

    private RecyclerView mRecyclerViewPromocao;
    private PromocaoAdapter adapter;
    private List<Promocao> promocoes;
    private DatabaseReference referenciaFirebase;
    private Promocao todasPromocoes;
    private LinearLayoutManager mLayoutManagerTodosProdutos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promocao);
        
        mRecyclerViewPromocao = (RecyclerView) findViewById(R.id.recycleViewTodasPromocoes);
        
        carregarTodosProdutos();
    }

    private void carregarTodosProdutos() {

        mRecyclerViewPromocao.setHasFixedSize(true);

        mLayoutManagerTodosProdutos = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerViewPromocao.setLayoutManager(mLayoutManagerTodosProdutos);

        promocoes = new ArrayList<>();

        referenciaFirebase = FirebaseDatabase.getInstance().getReference();

        referenciaFirebase.child("promocoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    todasPromocoes = postSnapshot.getValue(Promocao.class);

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

   
    public void btnCancelarOnclickListener(View view) {
    }
}
