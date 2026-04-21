package loja.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import loja.model.entity.Produto;
import loja.model.entity.Vendas;
import loja.repository.VendaRepository;

import java.time.LocalDateTime;
import java.util.List;

public class VendaService {
    private ObservableList<Vendas> vendas;
    private VendaRepository vendaRepo;

    public VendaService() {
        vendaRepo = new VendaRepository();
        vendas = vendaRepo.carregarVendas();

        if (vendas == null)
            vendas = FXCollections.observableArrayList();
    }

    public Vendas criarVenda(Produto produto, int quantidade, String cliente) {
        String nomeCliente = (cliente == null || cliente.isBlank())
                ? "Venda direta"
                : cliente;

        return new Vendas(
                nomeCliente,
                produto.getNome(),
                quantidade,
                produto.getPreco(),
                produto.getCusto(),
                LocalDateTime.now()
        );
    }

    public void registrarVenda(Vendas venda) {
        vendas.add(venda);
        salvarVendas();
    }

    public void processarVenda(Produto produto, int quantidade, String cliente) {

        if (produto.getQuantidade() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        // Atualiza estoque
        produto.setQuantidade(produto.getQuantidade() - quantidade);

        // Cria venda
        Vendas venda = criarVenda(produto, quantidade, cliente);

        // Salva venda
        vendas.add(venda);
        salvarVendas();
    }

    public void adicionarVendas(List<Vendas> novasVendas) {
        vendas.addAll(novasVendas);
        salvarVendas();
    }

    public List<Vendas> getHistoricoMensal(int ano, int mes) {
        return vendas.stream()
                .filter(v -> v.getData().getYear() == ano &&
                        v.getData().getMonthValue() == mes)
                .toList();
    }

    public ObservableList<Vendas> listarVendas() {
        return vendas;
    }

    public void salvarVendas() {
        vendaRepo.salvarVendas(vendas);
    }
    //public ObservableList<Vendas> getVendas() { return  vendas; }

}
