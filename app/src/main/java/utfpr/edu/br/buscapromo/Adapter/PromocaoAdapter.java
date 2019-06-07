package utfpr.edu.br.buscapromo.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import utfpr.edu.br.buscapromo.Model.Promocao;
import utfpr.edu.br.buscapromo.R;

public class PromocaoAdapter  extends RecyclerView.Adapter<PromocaoAdapter.ViewHolder> implements Serializable {

    private List<Promocao> promocaoList;
    private Context context;
    private  List<Promocao> promocoesSelect = new ArrayList<>();
    private AlertDialog alerta;
    private Date dataIn = new Date();
    private Date dataOut = new Date();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
    private SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy");


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
    public void onBindViewHolder(final PromocaoAdapter.ViewHolder holder, final int position) {

        final Promocao item = promocaoList.get(position);

        holder.txtMarcaProduto.setText(item.getProduto().getMarca());
        holder.txtDescricaoProduto.setText(item.getProduto().getNomeProduto() +" " + item.getProduto().getTipo()
                + " " + item.getProduto().getMarca()
                + " " + item.getProduto().getEmbalagem()
                + " " + item.getProduto().getConteudo());

        holder.txtValorOriginal.setText("DE: R$ " + item.getValorOriginal());
        holder.txtValorPromocional.setText( "POR: R$ " + item.getValorPromocional());
        holder.txtLocal.setText(item.getSupermercado());

        String datValid = item.getDataValidade().toString() ;
        String datIn = item.getDataInsercao().toString();
        try {
            dataIn = dateFormat.parse(datIn);
            dataOut = dateFormat.parse(datValid);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // ---- busca de imagem do produto
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int height = (displayMetrics.heightPixels / 6);
        final int width = (displayMetrics.widthPixels / 4);
        Picasso.get().load(item.getProduto().getUrlImagem()).resize(width, height).into(holder.imgProduto);
        // ----


        holder.checkboxPromocao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.checkboxPromocao.isChecked()){
                    promocoesSelect.add(promocaoList.get(position));
                }
            }
        });

        holder.imgProduto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setCancelable(true);
                builder.setTitle("Detalhes da Promoção");
                builder.setMessage("INSERIDO POR:\n" + "\t" + item.getUsuario()
                        + " \n\nPERÍODO DA PROMOÇÃO:\n" + "\t" + dateFormat2.format(dataIn) +" a "+ dateFormat2.format(dataOut));

                builder.setPositiveButton("Adicionar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      holder.checkboxPromocao.setChecked(true);
                    }
                });
                builder.setNegativeButton("Voltar", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                     alerta.dismiss();
                    }
                });
                alerta = builder.create();
                alerta.show();
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
        protected TextView txtLocal;
        protected CheckBox checkboxPromocao;

        public ViewHolder(View itemView) {
            super(itemView);

            txtMarcaProduto = (TextView) itemView.findViewById(R.id.txtMarcaProduto);
            txtDescricaoProduto = (TextView) itemView.findViewById(R.id.txtDescricaoProduto);
            imgProduto = (ImageView)  itemView.findViewById(R.id.imgProduto);
            txtValorOriginal = itemView.findViewById(R.id.txtValorProdutoOficial);
            txtValorPromocional = itemView.findViewById(R.id.txtValorProdutoUsuario);
            txtLocal = itemView.findViewById(R.id.txtLocal);
            checkboxPromocao = itemView.findViewById(R.id.checkboxPromocao);
        }
    }

    public List<Promocao> getPromocoesSelect() {
        if(promocoesSelect.isEmpty()){
            return promocaoList;
        }else {
            return promocoesSelect;
        }
    }
}
