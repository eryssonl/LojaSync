package loja.model.entity;

public class Produto {

    //private String idProduto;
    private String nome;
    private double preco;
    private double custo;
    private int quantidade;
    private int estoqueMinimo = 2;

    public Produto(String nome, double preco, double custo, int quantidade) {
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.custo = custo;
        //this.idProduto = java.util.UUID.randomUUID().toString();
    }

    //public String getIdProduto() { return idProduto; }
    public String getNome() {
        return nome;
    }
    public int getQuantidade() {
        return quantidade;
    }
    public double getPreco() { return preco; }
    public double getCusto() { return custo; }
    public int getEstoqueMinimo() { return estoqueMinimo; }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
    public void setEstoqueMinimo(int estoqueMinimo) {this.estoqueMinimo = estoqueMinimo;}

    public double getMargemLucro(){
        return preco - custo; //lucro unitario
    }
    public double getMargemPercentual(){
        if(custo == 0) return 0;
        return ((preco - custo ) / custo) * 100;
    }
    public double getLucroTotal(){
        return (preco - custo) * quantidade;
    }

}
