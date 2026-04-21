package loja.service;

import javafx.collections.FXCollections;
import loja.repository.ProdutoRepository;
import javafx.collections.ObservableList;
import loja.model.entity.Produto;

import java.util.ArrayList;


public class ProdutoService {
    private ObservableList<Produto> produtos;
    private ProdutoRepository produtoRepo;

    public ProdutoService() {
        produtoRepo = new ProdutoRepository();
        produtos = produtoRepo.carregarProdutos();

        if (produtos == null)
            produtos = FXCollections.observableArrayList();
    }

    //Getters
    public ObservableList<Produto> getProdutos() { return produtos; }

    public void cadastrarProduto(Produto produto) {
        produtos.add(produto);
        salvarProdutos();
    }

    public void removerProduto(Produto produto) {
        produtos.remove(produto);
        salvarProdutos();
    }
    public void atualizarEstoque(Produto produto, int quantidadeVendida) {
        if (produto.getQuantidade() < quantidadeVendida) {
            throw new IllegalArgumentException("Estoque insuficiente");
        }

        produto.setQuantidade(produto.getQuantidade() - quantidadeVendida);
        salvarProdutos();
    }
    public void adicionarEstoque(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade inválida");
        }

        produto.setQuantidade(produto.getQuantidade() + quantidade);
        salvarProdutos();
    }

    public ObservableList<Produto> listarProdutos() {
        return produtos;
    }

    public void salvarProdutos() {
        produtoRepo.salvarProdutos(new ArrayList<>(produtos));
    }
}

