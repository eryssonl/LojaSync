package loja.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import loja.model.entity.Vendas;
import loja.service.AppService;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Locale;

import javafx.collections.transformation.FilteredList;
import loja.service.VendaService;

public class HistoricoVendasController {

    private VendaService vendaService;

    @FXML private TableView<Vendas> tabelaHistorico;

    @FXML private TableColumn<Vendas, String> colProduto;
    @FXML private TableColumn<Vendas, Integer> colQuantidade;
    @FXML private TableColumn<Vendas, Double> colPreco;
    @FXML private TableColumn<Vendas, Double> colTotal;
    @FXML private TableColumn<Vendas, Double> colLucroTotal;
    @FXML private TableColumn<Vendas, LocalDateTime> colData;

    // KPI
    @FXML private Label labelTotal;
    @FXML private Label labelLucro;

    @FXML private TextField campoBusca;
    @FXML private Label statusLabel;
    @FXML private StackPane root;

    //  DETALHES
    @FXML private VBox cardDetalhes;
    @FXML private Label detProduto;
    @FXML private Label detQuantidade;
    @FXML private Label detPreco;
    @FXML private Label detCusto;
    @FXML private Label detLucroUnit;
    @FXML private Label detLucroPercentual;

    private FilteredList<Vendas> vendasFiltradas;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // =========================
    //         INJEÇÃO
    // =========================
    public void setService(AppService appService) {
        this.vendaService = appService.getVendaService();

        vendasFiltradas = new FilteredList<>(vendaService.listarVendas(), v -> true);
        tabelaHistorico.setItems(vendasFiltradas);

        atualizarResumo();
    }

    // =========================
    //           INIT
    // =========================
    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            tabelaHistorico.getScene().setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ESCAPE) {
                    fecharDetalhes();
                }
            });
        });
        root.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {

            if (!cardDetalhes.isVisible()) return;

            // verifica se clicou FORA do card
            if (!cardDetalhes.localToScene(cardDetalhes.getBoundsInLocal())
                    .contains(event.getSceneX(), event.getSceneY())) {

                fecharDetalhes();
            }
        });

        configurarColunas();
        configurarBusca();
        configurarSelecao();

        // estado inicial
        cardDetalhes.setVisible(false);
        cardDetalhes.setManaged(false);
    }

    // =========================
    //       CONFIG TABELA
    // =========================
    private void configurarColunas() {

        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colLucroTotal.setCellValueFactory(new PropertyValueFactory<>("lucroTotal"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));

        tabelaHistorico.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        configurarMoeda(colPreco);
        configurarMoeda(colTotal);
        configurarMoeda(colLucroTotal);

        // data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colData.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : data.format(formatter));
            }
        });
        colLucroTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);

                if (empty || valor == null) {
                    setText(null);
                    return;
                }

                setText(moeda.format(valor));

                if (valor < 0) {
                    setStyle("-fx-text-fill: #e74c3c;");
                } else {
                    setStyle("-fx-text-fill: #2ecc71;");
                }
            }
        });
    }
    private void configurarMoeda(TableColumn<Vendas, Double> col) {
        col.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : moeda.format(valor));
            }
        });
    }

    // =========================
    //          BUSCA
    // =========================
    private void configurarBusca() {
        campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            vendasFiltradas.setPredicate(v -> {

                if (newVal == null || newVal.isBlank()) return true;

                String filtro = newVal.toLowerCase();

                return v.getNomeProduto().toLowerCase().contains(filtro)
                        || (v.getNomeCliente() != null &&
                        v.getNomeCliente().toLowerCase().contains(filtro));
            });
        });
    }

    // =========================
    //          DETALHES
    // =========================
    private void configurarSelecao() {

        tabelaHistorico.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, venda) -> {

                    if (venda == null) {
                        fecharDetalhes();
                        return;
                    }

                    preencherDetalhes(venda);
                    abrirDetalhes();
                }
        );
    }

    private void preencherDetalhes(Vendas venda) {
        detProduto.setText(venda.getNomeProduto());
        detQuantidade.setText(venda.getQuantidade() + " un.");
        detPreco.setText(moeda.format(venda.getPrecoUnitario()));
        detCusto.setText(moeda.format(venda.getCustoUnitario()));
        detLucroUnit.setText(moeda.format(venda.getLucroUnitario()));
        detLucroPercentual.setText(String.format("%.2f%%", venda.getLucroPercentual()));
    }

    private void abrirDetalhes() {
        tabelaHistorico.setRowFactory(tv -> {
            TableRow<Vendas> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    preencherDetalhes(row.getItem());
                    abrirDetalhes();
                }
            });

            return row;
        });

        cardDetalhes.setVisible(true);
        cardDetalhes.setManaged(true);

        TranslateTransition anim = new TranslateTransition(Duration.millis(250), cardDetalhes);
        anim.setFromX(350);
        anim.setToX(0);
        anim.play();
    }

    @FXML
    private void fecharDetalhes() {
        TranslateTransition anim = new TranslateTransition(Duration.millis(200), cardDetalhes);
        anim.setFromX(0);
        anim.setToX(400);

        anim.setOnFinished(e -> {
            cardDetalhes.setVisible(false);
            cardDetalhes.setManaged(false);
        });

        anim.play();

        tabelaHistorico.getSelectionModel().clearSelection();
    }

    // =========================
    //           KPI
    // =========================
    private void atualizarResumo() {

        double total = 0;
        double lucro = 0;

        for (Vendas v : vendaService.listarVendas()) {
            total += v.getValorTotal();
            lucro += v.getLucroTotal();
        }

        labelTotal.setText(moeda.format(total));
        labelLucro.setText(moeda.format(lucro));
    }

}
