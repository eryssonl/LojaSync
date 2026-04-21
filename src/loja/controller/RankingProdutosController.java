package loja.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import loja.model.dto.ProdutoRankingDTO;
import loja.model.entity.Vendas;
import loja.service.AppService;
import loja.service.RankingService;
import loja.service.VendaService;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RankingProdutosController {
    @FXML private ComboBox<String> comboMes;
    @FXML private ComboBox<String> comboAno;

    @FXML private TableView<ProdutoRankingDTO> tabelaRanking;
    @FXML private TableColumn<ProdutoRankingDTO, String> colProduto;
    @FXML private TableColumn<ProdutoRankingDTO, Long> colQuantidade;
    @FXML private TableColumn<ProdutoRankingDTO, Double> colValorTotal;

    @FXML private BarChart<String, Number> barChartRanking;

    @FXML private Label lblTotalProdutos;
    @FXML private Label lblTotalQuantidade;
    @FXML private Label lblProdutoTop;

    // HERO / TOP 3
    @FXML private VBox heroCard;
    @FXML private Label heroProduto;
    @FXML private Label heroValor;
    @FXML private Label heroQtd;

    @FXML private TilePane top3Container;

    @FXML private Label rank1Nome;
    @FXML private Label rank1Valor;
    @FXML private Label rank1Qtd;

    @FXML private Label rank2Nome;
    @FXML private Label rank2Valor;
    @FXML private Label rank2Qtd;

    @FXML private Label rank3Nome;
    @FXML private Label rank3Valor;
    @FXML private Label rank3Qtd;

    @FXML private HBox kpiContainer;

    @FXML private Button btnRankingVendas;
    @FXML private Button btnRankingLucro;

    @FXML private Label statusLabel;

    // 🔥 SERVICES CORRETOS
    private VendaService vendaService;
    private RankingService rankingService;

    private NumberFormat currencyFormat;
    private boolean ordenarPorLucro = false;
    private List<ProdutoRankingDTO> rankingAtual;

    // ✅ INJEÇÃO
    public void setServices(AppService appService) {
        this.vendaService = appService.getVendaService();
        this.rankingService = appService.getRankingService();
        carregarMesesAnos();
    }

    @FXML
    public void initialize() {
        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        // ✅ CONFIGURAÇÃO FIXA (ANTES ESTAVA ERRADO)
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeVendida"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        tabelaRanking.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colValorTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });

        configurarToggle();
    }

    private void configurarToggle() {
        btnRankingVendas.setOnAction(e -> {
            ordenarPorLucro = false;
            aplicarEstiloToggle();
            if (rankingAtual != null) mostrarRanking(rankingAtual);
        });

        btnRankingLucro.setOnAction(e -> {
            ordenarPorLucro = true;
            aplicarEstiloToggle();
            if (rankingAtual != null) mostrarRanking(rankingAtual);
        });
    }

    private void aplicarEstiloToggle() {
        btnRankingVendas.setStyle(ordenarPorLucro
                ? "-fx-background-color: transparent; -fx-text-fill: -cor-dark;"
                : "-fx-background-color: -cor-secondary; -fx-text-fill: white;");

        btnRankingLucro.setStyle(ordenarPorLucro
                ? "-fx-background-color: -cor-secondary; -fx-text-fill: white;"
                : "-fx-background-color: transparent; -fx-text-fill: -cor-dark;");
    }

    private void carregarMesesAnos() {
        comboMes.setItems(FXCollections.observableArrayList(
                "01","02","03","04","05","06","07","08","09","10","11","12"
        ));

        if (vendaService != null) {
            List<String> anos = vendaService.listarVendas().stream()
                    .map(v -> String.valueOf(v.getData().getYear()))
                    .distinct()
                    .sorted()
                    .toList();

            comboAno.setItems(FXCollections.observableArrayList(anos));
        }
    }

    @FXML
    private void gerarRanking() {

        String mes = comboMes.getValue();
        String ano = comboAno.getValue();

        if (mes == null || ano == null) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione mês e ano.");
            return;
        }

        List<Vendas> vendas = vendaService.getHistoricoMensal(
                Integer.parseInt(ano),
                Integer.parseInt(mes)
        );

        if (vendas.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-gray;");
            statusLabel.setText("Nenhuma venda encontrada.");
            ocultarSecciones();
            return;
        }

        rankingAtual = rankingService.gerarRankingProdutos(vendas);

        if (ordenarPorLucro) {
            rankingAtual = rankingAtual.stream()
                    .sorted(Comparator.comparingDouble(ProdutoRankingDTO::getValorTotal).reversed())
                    .toList();
        }

        mostrarRanking(rankingAtual);
    }

    private void mostrarRanking(List<ProdutoRankingDTO> ranking) {

        // HERO
        heroCard.setVisible(true);
        heroCard.setManaged(true);

        ProdutoRankingDTO top1 = ranking.get(0);

        heroProduto.setText(top1.getNomeProduto());
        heroValor.setText(currencyFormat.format(top1.getValorTotal()));
        heroQtd.setText(top1.getQuantidadeVendida() + " un.");

        // TOP 3
        top3Container.setVisible(true);
        top3Container.setManaged(true);

        preencherCard(rank1Nome, rank1Valor, rank1Qtd, ranking, 0);
        preencherCard(rank2Nome, rank2Valor, rank2Qtd, ranking, 1);
        preencherCard(rank3Nome, rank3Valor, rank3Qtd, ranking, 2);

        // TABELA
        tabelaRanking.setItems(FXCollections.observableArrayList(ranking));

        // GRÁFICO
        barChartRanking.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        ranking.forEach(dto ->
                series.getData().add(new XYChart.Data<>(
                        dto.getNomeProduto(),
                        dto.getQuantidadeVendida()
                ))
        );

        barChartRanking.getData().add(series);

        // KPIs
        long totalQtd = ranking.stream().mapToLong(ProdutoRankingDTO::getQuantidadeVendida).sum();
        double totalValor = ranking.stream().mapToDouble(ProdutoRankingDTO::getValorTotal).sum();

        lblTotalProdutos.setText("Total: " + ranking.size());
        lblTotalQuantidade.setText("Qtd: " + totalQtd + " (" + currencyFormat.format(totalValor) + ")");
        lblProdutoTop.setText("Top: " + top1.getNomeProduto());

        kpiContainer.setVisible(true);
        kpiContainer.setManaged(true);

        statusLabel.setStyle("-fx-text-fill: -cor-success;");
        statusLabel.setText("Ranking gerado com sucesso!");
    }

    private void preencherCard(Label nome, Label valor, Label qtd,
                               List<ProdutoRankingDTO> ranking, int index) {

        if (ranking.size() > index) {
            ProdutoRankingDTO dto = ranking.get(index);
            nome.setText(dto.getNomeProduto());
            valor.setText(currencyFormat.format(dto.getValorTotal()));
            qtd.setText(dto.getQuantidadeVendida() + " un.");
        } else {
            nome.setText("—");
            valor.setText("—");
            qtd.setText("—");
        }
    }

    private void ocultarSecciones() {
        heroCard.setVisible(false);
        heroCard.setManaged(false);

        top3Container.setVisible(false);
        top3Container.setManaged(false);

        kpiContainer.setVisible(false);
        kpiContainer.setManaged(false);

        barChartRanking.getData().clear();
        tabelaRanking.setItems(FXCollections.emptyObservableList());
    }
}
