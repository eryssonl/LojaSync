package loja.controller;

import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import loja.model.entity.Produto;
import loja.service.AppService;
import loja.service.ProdutoService;


import java.text.NumberFormat;
import java.util.Locale;
import java.util.stream.Collectors;


public class ListagemProdutoController {

    private ProdutoService produtoService;

    @FXML private TableView<Produto> tabelaProdutos;
    @FXML private TableColumn<Produto, String> colNome;
    @FXML private TableColumn<Produto, Double> colPreco;
    @FXML private TableColumn<Produto, Double> colCusto;
    @FXML private TableColumn<Produto, Integer> colQuantidade;
    @FXML private TableColumn<Produto, Double> colMargem;
    @FXML private TableColumn<Produto, Double> colMargemPercentual;
    @FXML private TableColumn<Produto, Double> colLucro;

    @FXML private Label labelResumo;
    @FXML private TextField campoBusca;
    @FXML private Label statusLabel;

    private FilteredList<Produto> produtosFiltrados;
    private NumberFormat formatoMoeda;

    // 🔗 INJEÇÃO
    public void setProdutoService(AppService appService) {
        this.produtoService = appService.getProdutoService();
        configurarListaFiltrada();
        atualizarResumo();
        verificarEstoqueMinimo();
    }

    @FXML
    public void initialize() {

        formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        configurarColunas();
        configurarFormatacoes();
        configurarBusca();
    }

    // ==============================
    // CONFIGURAÇÕES
    // ==============================

    private void configurarColunas() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("preco"));
        colCusto.setCellValueFactory(new PropertyValueFactory<>("custo"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colMargem.setCellValueFactory(new PropertyValueFactory<>("margemLucro"));
        colMargemPercentual.setCellValueFactory(new PropertyValueFactory<>("margemPercentual"));
        colLucro.setCellValueFactory(new PropertyValueFactory<>("lucroTotal"));

        tabelaProdutos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void configurarFormatacoes() {

        // Moeda
        Callback<TableColumn<Produto, Double>, TableCell<Produto, Double>> moedaFactory =
                col -> new TableCell<>() {
                    @Override
                    protected void updateItem(Double valor, boolean empty) {
                        super.updateItem(valor, empty);
                        setText(empty || valor == null ? null : formatoMoeda.format(valor));
                    }
                };

        colPreco.setCellFactory(moedaFactory);
        colCusto.setCellFactory(moedaFactory);
        colMargem.setCellFactory(moedaFactory);
        colLucro.setCellFactory(moedaFactory);

        // Percentual
        colMargemPercentual.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : String.format("%.2f %%", valor));
            }
        });

        // Estoque baixo (🔥 melhorado)
        colQuantidade.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer valor, boolean empty) {
                super.updateItem(valor, empty);

                if (empty || valor == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(valor.toString());

                if (valor < 2) {
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configurarBusca() {
        campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            if (produtosFiltrados != null) {
                produtosFiltrados.setPredicate(produto -> {
                    if (newVal == null || newVal.isBlank()) return true;
                    return produto.getNome().toLowerCase().contains(newVal.toLowerCase());
                });
            }
        });
    }

    private void configurarListaFiltrada() {
        produtosFiltrados = new FilteredList<>(produtoService.getProdutos(), p -> true);

        tabelaProdutos.setItems(produtosFiltrados);

        // 🔥 listener automático (melhor que chamar manualmente)
        produtoService.getProdutos().addListener((ListChangeListener<Produto>) c -> {
            atualizarResumo();
            verificarEstoqueMinimo();
        });
    }

    // ==============================
    // AÇÕES
    // ==============================

    @FXML
    private void removerProduto() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione um produto.");
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setHeaderText("Remover produto?");
        confirmacao.setContentText("Produto \"" + selecionado.getNome() + "\" será removido.");

        confirmacao.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {
                produtoService.removerProduto(selecionado);

                statusLabel.setStyle("-fx-text-fill: -cor-success;");
                statusLabel.setText("Produto removido com sucesso.");
            }
        });
    }

    @FXML
    public void atualizarTabela() {
        if (produtoService != null) {
            tabelaProdutos.refresh(); // 🔥 mais eficiente
            atualizarResumo();
            verificarEstoqueMinimo();
        }
    }

    @FXML
    private void adicionarEstoque() {
        Produto selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione um produto.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adicionar Estoque");
        dialog.setHeaderText("Produto: " + selecionado.getNome());
        dialog.setContentText("Quantidade a adicionar:");

        dialog.showAndWait().ifPresent(input -> {
            try {
                int qtd = Integer.parseInt(input);

                produtoService.adicionarEstoque(selecionado, qtd);

                tabelaProdutos.refresh();
                atualizarResumo();

                statusLabel.setStyle("-fx-text-fill: -cor-success;");
                statusLabel.setText("Estoque atualizado com sucesso!");

            } catch (NumberFormatException e) {
                statusLabel.setStyle("-fx-text-fill: -cor-danger;");
                statusLabel.setText("Digite um número válido.");
            } catch (IllegalArgumentException e) {
                statusLabel.setStyle("-fx-text-fill: -cor-danger;");
                statusLabel.setText(e.getMessage());
            }
        });
    }

    // ==============================
    // RESUMO / ALERTAS
    // ==============================

    private void atualizarResumo() {

        int totalItens = produtoService.getProdutos().stream()
                .mapToInt(Produto::getQuantidade)
                .sum();

        double valorTotal = produtoService.getProdutos().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidade())
                .sum();

        labelResumo.setText(
                "Total de itens: " + totalItens +
                        " | Valor total: " + formatoMoeda.format(valorTotal)
        );
    }

    private void verificarEstoqueMinimo() {

        String alertas = produtoService.getProdutos().stream()
                .filter(p -> p.getQuantidade() < p.getEstoqueMinimo())
                .map(p -> "• " + p.getNome() + " — " + p.getQuantidade() + " un.")
                .collect(Collectors.joining("\n"));

        if (!alertas.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("⚠ Estoque baixo:\n" + alertas);
        }
    }
}
