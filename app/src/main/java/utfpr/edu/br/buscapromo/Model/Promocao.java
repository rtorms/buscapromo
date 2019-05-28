package utfpr.edu.br.buscapromo.Model;

import java.sql.Timestamp;

public class Promocao {

    private Produto produto;
    private Usuario usuario;
    private Supermercado supermercado;
    private Double valorOriginal;
    private Double valorPromocional;
    private String dataInsercao;
    private Timestamp dataValidade;
    private String origem;
    private String key;


    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Supermercado getSupermercado() {
        return supermercado;
    }

    public void setSupermercado(Supermercado supermercado) {
        this.supermercado = supermercado;
    }

    public Double getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(Double valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public Double getValorPromocional() {
        return valorPromocional;
    }

    public void setValorPromocional(Double valorPromocional) {
        this.valorPromocional = valorPromocional;
    }

    public String getDataInsercao() {
        return dataInsercao;
    }

    public void setDataInsercao(String dataInsercao) {
        this.dataInsercao = dataInsercao;
    }

    public Timestamp getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Timestamp dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}


