package loja.model.entity;

import java.util.ArrayList;
import java.util.List;

public class Produto {

    //private String idProduto; FUTURAMENTE

    private String nome;
    private double preco;
    private List <Lote> lotes = new ArrayList<>();
    private int estoqueMinimo = 2;

    public Produto() {}
    public Produto(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;

    }

    // =====================
    //         FIFO
    // =====================
    public void adicionarLote(double custo, int quantidade) {
        lotes.add(new Lote(custo, quantidade));
    }

    // =====================
    //   GETTERS DERIVADOS
    // =====================
    public int getQuantidadeTotal(){
        return lotes.stream().mapToInt(Lote::getQuantidade).sum();
    }

    public double getCustoMedio() {
        int total = getQuantidadeTotal();
        if (total == 0) return 0;

        double soma = lotes.stream()
                .mapToDouble(l -> l.getCusto() * l.getQuantidade())
                .sum();

        return soma / total;
    }

    public double getMargemLucro(){
        return preco - getCustoMedio();
    }
    public double getMargemPercentual(){
        double custo = getCustoMedio();
        if(custo == 0) return 0;
        return ((preco - custo ) / custo) * 100;
    }
    public double getLucroTotal(){
        return (preco - getCustoMedio()) * getQuantidadeTotal();
    }

    // =====================
    //  GETTERS and SETTERS
    // =====================
    public String getNome() {
        return nome;
    }
    public double getPreco() {
        return preco;
    }
    public int getEstoqueMinimo() {
        return estoqueMinimo;
    }
    public List <Lote> getLotes() {
        return lotes;
    }

    public void setEstoqueMinimo(int estoqueMinimo) {
        this.estoqueMinimo = estoqueMinimo;
    }
    public void setLotes(List <Lote> lotes) {
        this.lotes = lotes;
    }
}
