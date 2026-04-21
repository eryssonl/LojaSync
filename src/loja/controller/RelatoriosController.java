package loja.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import loja.model.entity.Vendas;
import loja.service.AppService;
import loja.service.RelatorioService;
import loja.service.VendaService;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Map;

public class RelatoriosController {

    @FXML private ComboBox<String> comboMes;
    @FXML private ComboBox<String> comboAno;
    @FXML private TableView<Vendas> tabelaRelatorio;
    @FXML private TableColumn<Vendas, String> colProduto;
    @FXML private TableColumn<Vendas, Integer> colQuantidade;
    @FXML private TableColumn<Vendas, LocalDateTime> colData;
    @FXML private TableColumn<Vendas, Double> colTotal;
    @FXML private Label labelTotal;
    @FXML private Label kpiQtdVendas;
    @FXML private Label kpiFaturamento;
    @FXML private Label kpiLucro;
    @FXML private BarChart<String, Number> barChartVendas;
    @FXML private Label statusLabel;

    private VendaService vendaService;
    private RelatorioService relatorioService;
    private NumberFormat currencyFormat;

    public void setService(AppService appService) {
        this.vendaService = appService.getVendaService();
        this.relatorioService = appService.getRelatorioService();
        carregarMesesAnos();
    }

    @FXML
    public void initialize() {

        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        tabelaRelatorio.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        colData.setCellFactory(tc -> new TableCell<Vendas, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : data.format(formatter));
            }
        });

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colTotal.setCellFactory(tc -> new TableCell<Vendas, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });
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
    private void gerarRelatorio() {
        String mes = comboMes.getValue();
        String ano = comboAno.getValue();

        if (mes == null || ano == null) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione mês e ano.");
            return;
        }

        // 🔥 BUSCA
        List<Vendas> vendas = vendaService.getHistoricoMensal(
                Integer.parseInt(ano),
                Integer.parseInt(mes)
        );

        ObservableList<Vendas> dados = FXCollections.observableArrayList(vendas);
        tabelaRelatorio.setItems(dados);

        // 🔥 KPIs (service)
        double faturamento = relatorioService.calcularFaturamento(vendas);
        double lucro = relatorioService.calcularLucro(vendas);

        labelTotal.setText("Total do mês: " + currencyFormat.format(faturamento));
        kpiQtdVendas.setText(String.valueOf(vendas.size()));
        kpiFaturamento.setText(currencyFormat.format(faturamento));
        kpiLucro.setText(currencyFormat.format(lucro));

        // 🔥 GRÁFICO (service)
        Map<String, Integer> agrupado =
                relatorioService.agruparQuantidadePorProduto(vendas);

        barChartVendas.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        agrupado.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> series.getData().add(
                        new XYChart.Data<>(e.getKey(), e.getValue())
                ));

        barChartVendas.getData().add(series);

        // Feedback
        if (vendas.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-gray;");
            statusLabel.setText("Nenhum resultado.");
        } else {
            statusLabel.setStyle("-fx-text-fill: -cor-success;");
            statusLabel.setText(vendas.size() + " venda(s) encontrada(s)");
        }
    }
}
