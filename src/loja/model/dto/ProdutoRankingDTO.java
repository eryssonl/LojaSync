package loja.model.dto;

public class ProdutoRankingDTO {
    private String nomeProduto;
    private long quantidadeVendida;
    private double valorTotal;

    public ProdutoRankingDTO(String nomeProduto, long quantidadeVendida, double valorTotal) {
        this.nomeProduto = nomeProduto;
        this.quantidadeVendida = quantidadeVendida;
        this.valorTotal = valorTotal;
    }

    public String getNomeProduto() { return nomeProduto; }
    public long getQuantidadeVendida() { return quantidadeVendida; }
    public double getValorTotal() { return valorTotal; }

}

