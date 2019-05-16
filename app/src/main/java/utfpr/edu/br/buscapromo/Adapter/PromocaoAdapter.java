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

import com.bumptech.glide.Glide;
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

import utfpr.edu.br.buscapromo.Classes.Produto;
import utfpr.edu.br.buscapromo.R;

public class PromocaoAdapter  extends RecyclerView.Adapter<PromocaoAdapter.ViewHolder> {

    private List<Produto> produtosList;
    private Context context;
    private DatabaseReference referenciaFirebase;
    private List<Produto> produtos;
    private Produto todosProdutos;
    private StorageReference storageReference;

    @GlideModule
    public final class MyAppGlideModule extends AppGlideModule {
        // leave empty for now
    }

    public PromocaoAdapter(List<Produto> list, Context c) {
        context = c;
        produtosList = list;
    }

    @Override
    public PromocaoAdapter.ViewHolder onCreateViewHolder( ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.lista_promocoes, viewGroup, false);

        return new PromocaoAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final PromocaoAdapter.ViewHolder holder, int position) {

        final Produto item = produtosList.get(position);

        produtos = new ArrayList<>();

        referenciaFirebase = FirebaseDatabase.getInstance().getReference();

        referenciaFirebase.child("produtos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                produtos.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    todosProdutos = postSnapshot.getValue(Produto.class);

                    produtos.add(todosProdutos);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        holder.txtDescricaoProduto.setText(item.getNomeProduto() +" " + item.getTipo() + " " + item.getMarca()
                + " " + item.getEmbalagem() + " " + item.getConteudo());
        holder.txtMarcaProduto.setText(item.getMarca());


        // ---- busca de imagem do produto
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int height = (displayMetrics.heightPixels / 6);
        final int width = (displayMetrics.widthPixels / 4);
        Picasso.get().load(item.getUrlImagem()).resize(width, height).into(holder.imgProduto);
        // ----

        holder.linearLayoutProdutos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }



    @Override
    public int getItemCount() {
        return produtosList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected TextView txtMarcaProduto;
        protected TextView txtDescricaoProduto;
        protected ImageView imgProduto;
        protected LinearLayout linearLayoutProdutos;



        public ViewHolder(View itemView) {
            super(itemView);

            txtMarcaProduto = (TextView) itemView.findViewById(R.id.txtMarcaProduto);
            txtDescricaoProduto = (TextView) itemView.findViewById(R.id.txtDescricaoProduto);
            imgProduto = (ImageView)  itemView.findViewById(R.id.imgProduto);
            linearLayoutProdutos = (LinearLayout) itemView.findViewById(R.id.linearLayoutProdutos);
        }
    }
}
