package utfpr.edu.br.buscapromo.DAO;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FindObjectGeneric {

    private DatabaseReference databaseReference;
    private Context context;
    private String filtro1;
    private String filtro2;
    private String filtro3;
    private Object objectFind;

    public void findObjectCallback(String filter1, String filter2, String filter3, Object object, Context c, final ObjectCallbackInterface callback) {
        this.filtro1 = filter1;
        this.filtro2 = filter2;
        this.filtro3 = filter3;
        this.context = c;
        this.objectFind = object;

        databaseReference = FirebaseDatabase.getInstance().getReference();

        databaseReference.child(filtro1).orderByChild(filtro2).equalTo(filtro3)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                            if (dataSnapshot.exists()) {
                                objectFind = posSnapshot.getValue(objectFind.getClass());
                            }
                        }
                        if (!dataSnapshot.exists()) {

                        }
                        callback.onCallback(objectFind);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
}
