package loja.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import loja.model.dto.LucroMensalDTO;
import loja.service.AppService;
import loja.service.RelatorioService;
import loja.service.VendaService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class LucroMensalController {

    private VendaService vendaService;
    private RelatorioService relatorioService;

    @FXML private TableView<LucroMensalDTO> tabelaLucro;
    @FXML private TableColumn<LucroMensalDTO, String> colMes;
    @FXML private TableColumn<LucroMensalDTO, Double> colTotal;
    @FXML private BarChart<String, Number> barChartLucro;
    @FXML private Label labelTotalGeral;
    @FXML private Label statusLabel;

    private NumberFormat currencyFormat;

    public void setServices(AppService appService) {
        this.vendaService = appService.getVendaService();
        this.relatorioService = appService.getRelatorioService();
        atualizarTabela();
    }

    @FXML
    public void initialize() {
        colMes.setCellValueFactory(new PropertyValueFactory<>("mes"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colTotal.setCellFactory(tc -> new TableCell<LucroMensalDTO, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });
    }

    private void atualizarTabela() {
        if (vendaService == null || relatorioService == null) return;

        // 🔥 regra de negócio vai para o service
        List<LucroMensalDTO> lista = relatorioService.gerarLucroMensal(
                vendaService.listarVendas()
        );

        ObservableList<LucroMensalDTO> dados = FXCollections.observableArrayList(lista);
        tabelaLucro.setItems(dados);

        // KPI
        double totalGeral = lista.stream()
                .mapToDouble(LucroMensalDTO::getTotal)
                .sum();

        labelTotalGeral.setText(currencyFormat.format(totalGeral));

        // Gráfico
        barChartLucro.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Faturamento");

        lista.forEach(dto ->
                series.getData().add(
                        new XYChart.Data<>(dto.getMes(), dto.getTotal())
                )
        );

        barChartLucro.getData().add(series);

        // Feedback
        if (dados.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-gray;");
            statusLabel.setText("Nenhuma venda registrada ainda.");
        } else {
            statusLabel.setText("");
        }
    }
}
