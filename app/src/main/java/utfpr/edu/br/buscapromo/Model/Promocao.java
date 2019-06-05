package utfpr.edu.br.buscapromo.Model;


public class Promocao {

    private Produto produto;
    private String usuario;
    private String supermercado;
    private Double valorOriginal;
    private Double valorPromocional;
    private Integer dataInsercao;
    private Integer dataValidade;
    private String key;
    private String latitude;
    private String longitude;

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSupermercado() {
        return supermercado;
    }

    public void setSupermercado(String supermercado) {
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

    public Integer getDataInsercao() {
        return dataInsercao;
    }

    public void setDataInsercao(Integer dataInsercao) {
        this.dataInsercao = dataInsercao;
    }

    public Integer getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(Integer dataValidade) {
        this.dataValidade = dataValidade;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}


