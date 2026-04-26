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
    private ProdutoService produtoService;

    public VendaService(ProdutoService produtoService) {
        this.produtoService = produtoService;
        this.vendaRepo = new VendaRepository();
        this.vendas = vendaRepo.carregarVendas();

        if (this.vendas == null) {
            this.vendas = FXCollections.observableArrayList();
        }
    }

    // =========================
    //  CRIAR E PROCESSAR VENDA
    // =========================
    public Vendas processarVenda(Produto produto, int quantidade, String cliente) {

        if (produto.getQuantidadeTotal() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        double custoTotal = produtoService.baixarEstoqueFIFO(produto, quantidade);
        double custoUnitario = custoTotal / quantidade;

        String nomeCliente = (cliente == null || cliente.isBlank())
                ? "Venda direta"
                : cliente;

        Vendas venda = new Vendas(
                nomeCliente,
                produto.getNome(),
                quantidade,
                produto.getPreco(),
                custoUnitario,
                LocalDateTime.now()
        );
        return venda;
    }

    // =========================
    //    LISTS/PERSISTENCIA
    // =========================
    public void adicionarVendas(List<Vendas> novasVendas) {
        vendas.addAll(novasVendas);
        salvarVendas();
    }
    public void registrarVenda(Vendas venda){
        vendas.add(venda);
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
}
