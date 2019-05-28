package utfpr.edu.br.buscapromo.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.R;

public class PromocaoAdapter  extends RecyclerView.Adapter<PromocaoAdapter.ViewHolder> {

    private List<Promocao> promocaoList;
    private Context context;
    private DatabaseReference referenciaFirebase;
    private List<Promocao> promocao;
    private Promocao todasPromocoes;
    private StorageReference storageReference;

    @GlideModule
    public final class MyAppGlideModule extends AppGlideModule {
        // leave empty for now
    }

    public PromocaoAdapter(List<Promocao> list, Context c) {
        context = c;
        promocaoList = list;
    }

    @Override
    public PromocaoAdapter.ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lista_promocoes, viewGroup, false);

        return new PromocaoAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PromocaoAdapter.ViewHolder holder, int position) {

        final Promocao item = promocaoList.get(position);

        promocao = new ArrayList<>();

        referenciaFirebase = FirebaseDatabase.getInstance().getReference();

        referenciaFirebase.child("promocoes").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promocao.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
            //        todasPromocoes.setProduto(postSnapshot.child("produto").getValue(Produto.class));
//                    todasPromocoes = postSnapshot.getValue(Promocao.class);
//                    todasPromocoes.setProduto(postSnapshot.child("produto").getValue(Produto.class));
//                    todasPromocoes.setSupermercado(postSnapshot.child("supermercado").getValue(Supermercado.class));

                    promocao.add(todasPromocoes);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.txtDescricaoProduto.setText(item.getProduto().getNomeProduto() +" " + item.getProduto().getTipo()
                + " " + item.getProduto().getMarca()
                + " " + item.getProduto().getEmbalagem()
                + " " + item.getProduto().getConteudo());
        holder.txtMarcaProduto.setText(item.getProduto().getMarca());
        holder.txtValorPromocional.setText(item.getSupermercado().getNome());
      //  holder.txtDataValidadePromocao.setText(item.getDataValidade().toString());


        // ---- busca de imagem do produto
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int height = (displayMetrics.heightPixels / 6);
        final int width = (displayMetrics.widthPixels / 4);
        Picasso.get().load(item.getProduto().getUrlImagem()).resize(width, height).into(holder.imgProduto);
        // ----

        holder.linearLayoutpromocoes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return promocaoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView txtMarcaProduto;
        protected TextView txtDescricaoProduto;
        protected ImageView imgProduto;
        protected TextView txtValorOriginal;
        protected TextView txtValorPromocional;
        protected TextView txtDataValidadePromocao;
        protected LinearLayout linearLayoutpromocoes;



        public ViewHolder(View itemView) {
            super(itemView);

            txtMarcaProduto = (TextView) itemView.findViewById(R.id.txtMarcaProduto);
            txtDescricaoProduto = (TextView) itemView.findViewById(R.id.txtDescricaoProduto);
            imgProduto = (ImageView)  itemView.findViewById(R.id.imgProduto);
            txtValorOriginal = itemView.findViewById(R.id.txtValorProdutoOficial);
            txtValorPromocional = itemView.findViewById(R.id.txtValorProdutoUsuario);
            txtDataValidadePromocao = itemView.findViewById(R.id.txtDataValidadePromocao);
            linearLayoutpromocoes = (LinearLayout) itemView.findViewById(R.id.linearLayoutPromocoes);
        }
    }
}
