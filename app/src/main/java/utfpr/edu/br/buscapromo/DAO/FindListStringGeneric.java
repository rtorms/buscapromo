package utfpr.edu.br.buscapromo.DAO;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class FindListStringGeneric {

    private DatabaseReference databaseReference;
    private Context context;
    private String filtro1;
    private String filtro2;
    private ListStringCallbackInterface callback;

    public void listaStringGenericCallback(final String filter1, final String filter2, Context c, final ListStringCallbackInterface callback) {
        this.context = c;
        this.filtro1 = filter1;
        this.filtro2 = filter2;
        this.callback = callback;

        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child(filtro1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final List<String> filtroList = new ArrayList<String>();

                for (DataSnapshot depSnapshot : dataSnapshot.getChildren()) {
                    String filtroSelect = depSnapshot.child(filtro2).getValue(String.class);
                        filtroList.add(filtroSelect);
                }

                ArrayAdapter<String> filtroAdapter = new ArrayAdapter<String>(context,
                        android.R.layout.simple_spinner_item, filtroList);
                filtroAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                callback.onCallback(filtroAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "ERRO!!" + databaseError, Toast.LENGTH_LONG).show();

            }
        });
    }

}
