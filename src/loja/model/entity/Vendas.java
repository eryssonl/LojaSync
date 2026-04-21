package loja.model.entity;

import java.time.LocalDateTime;

public class Vendas {

    private String nomeCliente;
    private String nomeProduto;
    private int quantidade;
    private double precoUnitario;
    private double custoUnitario;
    private LocalDateTime data;

    public Vendas( String nomeCliente, String nomeProduto, int quantidade, double precoUnitario, double custoUnitario, LocalDateTime data) {
        this.nomeCliente = nomeCliente;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.custoUnitario = custoUnitario;
        this.data = data;
    }

    public String getNomeCliente() { return nomeCliente; }
    public String getNomeProduto() { return nomeProduto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }
    public double getCustoUnitario() { return custoUnitario; }
    public LocalDateTime getData() { return data; }

    @Override
    public String toString() {
        return "Venda{" +
                "produto='" + nomeProduto + '\'' +
                ", quantidade=" + quantidade +
                ", preco=" + precoUnitario +
                ", custo=" + custoUnitario +
                ", data=" + data +
                '}';
    }

    public double getValorTotal() {
        return precoUnitario * quantidade;
    }
    public double getLucroUnitario() {
        return precoUnitario - custoUnitario;
    }
    public double getLucroPercentual() {
        if(custoUnitario == 0) return 0;
        return (getLucroUnitario() / custoUnitario) * 100;
    }
    public double getLucroTotal() {
        return getLucroUnitario() * quantidade;
    }
}

