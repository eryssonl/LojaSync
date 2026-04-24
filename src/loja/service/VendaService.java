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
    //        CRIAR VENDA
    // =========================
    public Vendas criarVenda(Produto produto,
                             int quantidade,
                             String cliente,
                             double custoUnitarioReal) {

        String nomeCliente = (cliente == null || cliente.isBlank())
                ? "Venda direta"
                : cliente;

        return new Vendas(
                nomeCliente,
                produto.getNome(),
                quantidade,
                produto.getPreco(),
                custoUnitarioReal,
                LocalDateTime.now()
        );
    }

    // =========================
    //   PROCESSAR VENDA (FIFO)
    // =========================
    public Vendas processarVenda(Produto produto, int quantidade, String cliente) {

        if (produto.getQuantidadeTotal() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        double custoTotal = produtoService.baixarEstoqueFIFO(produto, quantidade);
        double custoUnitario = custoTotal / quantidade;

        // Cria venda com custo REAL
        Vendas venda = criarVenda(produto, quantidade, cliente, custoUnitario);

        vendas.add(venda);
        salvarVendas();

        return venda;
    }

    // =========================
    //    LISTS/PERSISTENCIA
    // =========================
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
}
