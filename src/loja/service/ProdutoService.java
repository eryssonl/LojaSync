package loja.service;

import javafx.collections.FXCollections;
import loja.model.entity.Lote;
import loja.repository.ProdutoRepository;
import javafx.collections.ObservableList;
import loja.model.entity.Produto;

import java.util.ArrayList;
import java.util.Iterator;

public class ProdutoService {
    private ObservableList<Produto> produtos;
    private ProdutoRepository produtoRepo;

    public ProdutoService() {
        produtoRepo = new ProdutoRepository();
        produtos = produtoRepo.carregarProdutos();

        if (produtos == null)
            produtos = FXCollections.observableArrayList();
    }

    // =========================
    //         GETTERS
    // =========================
    public ObservableList<Produto> getProdutos() {
        return produtos;
    }
    public ObservableList<Produto> listarProdutos() {
        return produtos;
    }

    // =========================
    //          CRUD
    // =========================
    public void cadastrarProduto(Produto produto) {
        produtos.add(produto);
        salvarProdutos();
    }
    public void removerProduto(Produto produto) {
        produtos.remove(produto);
        salvarProdutos();
    }

    // =========================
    //       ESTOQUE (FIFO)
    // =========================
    public void adicionarEstoque(Produto produto, double custo, int quantidade) {

        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade inválida");

        if (custo <= 0) throw new IllegalArgumentException("Custo inválido");

        produto.adicionarLote(custo, quantidade);
        salvarProdutos();
    }

    public double baixarEstoqueFIFO (Produto produto, int quantidade) {
        if (produto.getQuantidadeTotal() < quantidade) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        if (produto.getLotes() == null || produto.getLotes().isEmpty()) {
            throw new IllegalStateException("Produto sem lotes");
        }

        double custoTotal = 0;

        Iterator<Lote> iterator = produto.getLotes().iterator();

        while (iterator.hasNext() && quantidade > 0) {

            Lote lote = iterator.next();

            int removido = Math.min(lote.getQuantidade(), quantidade);

            custoTotal += removido * lote.getCusto();

            lote.removerQuantidade(removido);
            quantidade -= removido;

            if (lote.estaVazio()) {
                iterator.remove();
            }
        }
        salvarProdutos();
        return custoTotal;
    }

    // =========================
    //       PERSISTÊNCIA
    // =========================
    public void salvarProdutos() {
        produtoRepo.salvarProdutos(new ArrayList<>(produtos));
    }
}

