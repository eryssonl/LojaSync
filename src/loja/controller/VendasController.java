package loja.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import loja.model.entity.Produto;
import loja.model.entity.Vendas;
import loja.service.AppService;
import loja.service.ContaService;
import loja.service.ProdutoService;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.scene.control.Label;
import loja.service.VendaService;

public class VendasController {
    @FXML private TextField campoPesquisa;
    @FXML private TextField campoQuantidade;
    @FXML private TextField campoCliente;
    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, Double> colPreco;
    @FXML private TableColumn<Produto, Integer> colQuantidade;

    @FXML private Label resumoProduto;
    @FXML private Label resumoPreco;
    @FXML private Label resumoEstoque;
    @FXML private Label resumoTotal;
    @FXML private Label statusLabel;

    private FilteredList<Produto> produtosFiltrados;

    private ProdutoService produtoService;
    private VendaService vendaService;
    private ContaService contaService;
    private Produto produtoSelecionado;
    private NumberFormat currencyFormat;

    public void setService(AppService appService) {
        this.produtoService = appService.getProdutoService();
        this.vendaService = appService.getVendaService();
        this.contaService = appService.getContaService();

        configurarTabela(); // inicializa a tabela com os dados do serviço
    }

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        // Formatação monetária para colTotal
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colPreco.setCellFactory(tc -> new TableCell<Produto, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });

        // Listener de seleção para atualizar resumo automaticamente
        tabelaProdutos.getSelectionModel().selectedItemProperty().addListener(
            (obs, antigo, novo) -> {
                produtoSelecionado = novo;
                atualizarResumo();
            }
        );
    }

    private void configurarTabela() {
        if (produtoService != null) {
            FilteredList<Produto> produtosFiltrados =
                    new FilteredList<>(FXCollections.observableArrayList(produtoService.listarProdutos()), p -> true);

            campoPesquisa.textProperty().addListener((obs, oldValue, newValue) -> {
                produtosFiltrados.setPredicate(produto -> {
                    if (newValue == null || newValue.isEmpty()) return true;
                    String filtro = newValue.toLowerCase();
                    return produto.getNome().toLowerCase().contains(filtro);
                });
            });

            tabelaProdutos.setItems(produtosFiltrados);
        }
    }

    private void atualizarResumo() {
        if (produtoSelecionado != null) {
            resumoProduto.setText(produtoSelecionado.getNome());
            resumoPreco.setText(currencyFormat.format(produtoSelecionado.getPreco()));
            resumoEstoque.setText(produtoSelecionado.getQuantidade() + " un.");

            // Calcula total com base na quantidade informada
            try {
                int qtd = Integer.parseInt(campoQuantidade.getText().trim());
                if (qtd > 0) {
                    resumoTotal.setText(currencyFormat.format(produtoSelecionado.getPreco() * qtd));
                } else {
                    resumoTotal.setText("R$ 0,00");
                }
            } catch (NumberFormatException e) {
                resumoTotal.setText("R$ 0,00");
            }
        } else {
            resumoProduto.setText("—");
            resumoPreco.setText("—");
            resumoEstoque.setText("—");
            resumoTotal.setText("R$ 0,00");
        }
    }

    @FXML
    private void registrarVenda() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione um produto.");
            return;
        }

        try {
            int qtd = Integer.parseInt(campoQuantidade.getText());

            if (qtd <= 0) {
                statusLabel.setStyle("-fx-text-fill: -cor-danger;");
                statusLabel.setText("Quantidade inválida.");
                return;
            }

            if (qtd > selecionado.getQuantidade()) {
                statusLabel.setStyle("-fx-text-fill: -cor-danger;");
                statusLabel.setText("Estoque insuficiente.");
                return;
            }

            String cliente = campoCliente.getText();

            // 🔥 CENTRALIZA TUDO NO SERVICE
            vendaService.processarVenda(selecionado, qtd, cliente);

            // 🔥 Se for fiado → vai pra conta
            if (cliente != null && !cliente.isBlank()) {
                Vendas venda = vendaService.criarVenda(selecionado, qtd, cliente);
                contaService.registrarCompraNaConta(cliente, venda);
            }

            statusLabel.setStyle("-fx-text-fill: -cor-success;");
            statusLabel.setText("Venda registrada com sucesso!");

            tabelaProdutos.refresh();
            limparCampos();

        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: -cor-danger;");
            statusLabel.setText("Quantidade inválida.");
        }
    }

    @FXML
    private void limparCampos() {
        campoQuantidade.clear();
        campoCliente.clear();
        tabelaProdutos.getSelectionModel().clearSelection();
        statusLabel.setText("");
        atualizarResumo();
    }
}
