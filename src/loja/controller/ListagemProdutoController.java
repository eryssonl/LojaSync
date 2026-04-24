package loja.controller;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
import java.util.Optional;
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

        tabelaProdutos.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Produto produto, boolean empty) {
                super.updateItem(produto, empty);

                if (produto == null || empty) {
                    setTooltip(null);
                } else {
                    String texto = produto.getLotes().stream()
                            .map(l -> "• " + l.getQuantidade() + " un. (R$ " + l.getCusto() + ")")
                            .collect(Collectors.joining("\n"));

                    setTooltip(new Tooltip("Lotes:\n" + texto));
                }
            }
        });

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
        colQuantidade.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantidadeTotal()).asObject()
        );
        colCusto.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getCustoMedio()).asObject()
        );
        colMargem.setCellValueFactory(new PropertyValueFactory<>("margemLucro"));
        colMargemPercentual.setCellValueFactory(new PropertyValueFactory<>("margemPercentual"));
        colLucro.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getLucroTotal()).asObject()
        );

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

        // Estoque baixo
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
            tabelaProdutos.refresh();
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

        try {
            TextInputDialog dialogQtd = new TextInputDialog();
            dialogQtd.setHeaderText("Quantidade para " + selecionado.getNome());
            dialogQtd.setContentText("Quantidade:");

            Optional<String> qtdInput = dialogQtd.showAndWait();
            if (qtdInput.isEmpty()) return;

            int quantidade =  Integer.parseInt(qtdInput.get());

            TextInputDialog dialogCusto = new TextInputDialog();
            dialogCusto.setHeaderText("Custo do novo lote");
            dialogCusto.setContentText("Custo unitário:");

            Optional<String> custoInput = dialogCusto.showAndWait();
            if (custoInput.isEmpty()) return;

            double custo =  Double.parseDouble(custoInput.get());

            produtoService.adicionarEstoque(selecionado, custo, quantidade);

            tabelaProdutos.refresh();
            atualizarResumo();

            statusLabel.setStyle("-fx-text-fill: -cor-success;");
            statusLabel.setText("Novo lote adicionado com sucesso!");

        } catch (NumberFormatException e) {
            statusLabel.setStyle("-fx-text-fill: -cor-danger;");
            statusLabel.setText("Valores inválidos.");
        } catch (IllegalArgumentException e) {
            statusLabel.setStyle("-fx-text-fill: -cor-danger;");
            statusLabel.setText(e.getMessage());
        }

    }

    // ==============================
    // RESUMO / ALERTAS
    // ==============================

    private void atualizarResumo() {

        int totalItens = produtoService.getProdutos().stream()
                .mapToInt(Produto::getQuantidadeTotal)
                .sum();

        double valorTotal = produtoService.getProdutos().stream()
                .mapToDouble(p -> p.getPreco() * p.getQuantidadeTotal())
                .sum();

        labelResumo.setText(
                "Total de itens: " + totalItens +
                        " | Valor total: " + formatoMoeda.format(valorTotal)
        );
    }

    private void verificarEstoqueMinimo() {

        String alertas = produtoService.getProdutos().stream()
                .filter(p -> p.getQuantidadeTotal() < p.getEstoqueMinimo())
                .map(p -> "• " + p.getNome() + " — " + p.getQuantidadeTotal() + " un.")
                .collect(Collectors.joining("\n"));

        if (!alertas.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("⚠ Estoque baixo:\n" + alertas);
        }
    }
}
