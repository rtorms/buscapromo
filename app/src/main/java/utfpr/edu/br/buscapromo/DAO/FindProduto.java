package utfpr.edu.br.buscapromo.DAO;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import utfpr.edu.br.buscapromo.Classes.Produto;

public class FindProduto {

    private DatabaseReference databaseReference;
    private String codBarProd;
    private Produto produto;

    private Context context;

    public Produto carregaProduto(String codBar, Context c) {
        this.codBarProd = codBar;
        this.context = c;

        databaseReference = FirebaseDatabase.getInstance().getReference();

        produto = new Produto();
        databaseReference.child("produtos").orderByChild("codBarras").equalTo(codBarProd)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            produto = dataSnapshot.getValue(Produto.class);
                        }
                        if (!dataSnapshot.exists()) {

                            Toast.makeText(context, "Produto não cadastrado!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Produto getProduto() {

        return produto;
    }
}
