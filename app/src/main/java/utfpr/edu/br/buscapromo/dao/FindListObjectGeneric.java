package utfpr.edu.br.buscapromo.dao;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindListObjectGeneric {

    private DatabaseReference databaseReference;
    private Context context;
    private Object objectFind;
    private List<Object> objects;


    public void findListObjectGeneric(Object objeto, String filtro1, Context c, final ListObjectInterface callback) {
        this.objectFind = objeto;
        this.context = c;

        objects = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(filtro1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                objects.clear();
                for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                    objects.add(posSnapshot.getValue(objectFind.getClass()));
                }
                callback.onCallback(objects);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "ERRO!!" + databaseError, Toast.LENGTH_LONG).show();
            }
        });
    }
}
