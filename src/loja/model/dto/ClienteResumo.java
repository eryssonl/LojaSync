package loja.model.dto;

public class ClienteResumo {

    private String nomeCliente;
    private int quantidadeCompras;
    private double valorTotal;

    public ClienteResumo(String nomeCliente, int quantidadeCompras, double valorTotal) {
        this.nomeCliente = nomeCliente;
        this.quantidadeCompras = quantidadeCompras;
        this.valorTotal = valorTotal;
    }

    public String getNomeCliente() { return nomeCliente; }
    public int getQuantidadeCompras() { return quantidadeCompras; }
    public double getValorTotal() { return valorTotal; }

}
