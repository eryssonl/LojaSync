package loja.model.entity;

public class Lote {

    private double custo;
    private int quantidade;

    public Lote() {}

    public Lote(double custo, int quantidade) {
        this.custo = custo;
        this.quantidade = quantidade;
    }

    public double getCusto() { return custo; }
    public int getQuantidade() { return quantidade; }

    public void setQuantidade(int qtd) { this.quantidade = qtd; }

    public void removerQuantidade(int qtd) {
        this.quantidade -= qtd;
    }

    public boolean estaVazio() {
        return quantidade <= 0;
    }
}
