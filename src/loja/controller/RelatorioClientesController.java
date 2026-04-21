package loja.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import loja.model.dto.ClienteResumo;
import loja.model.entity.Vendas;
import loja.service.AppService;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.collections.transformation.FilteredList;
import loja.service.RelatorioService;
import loja.service.VendaService;

public class RelatorioClientesController {

    @FXML private ComboBox<String> comboMes;
    @FXML private ComboBox<String> comboAno;
    @FXML private TableView<ClienteResumo> tabelaClientes;
    @FXML private TableColumn<ClienteResumo, String> colCliente;
    @FXML private TableColumn<ClienteResumo, Integer> colCompras;
    @FXML private TableColumn<ClienteResumo, Double> colTotal;
    @FXML private Label lblTotalGeral;
    @FXML private TextField campoBusca;
    @FXML private Label statusLabel;

    private VendaService vendaService;
    private RelatorioService relatorioService;
    private FilteredList<ClienteResumo> clientesFiltrados;

    public void setService(AppService appService) {
        this.vendaService = appService.getVendaService();
        this.relatorioService = appService.getRelatorioService();
        carregarMesesAnos();
    }

    @FXML
    public void initialize() {

        colCliente.setCellValueFactory(new PropertyValueFactory<>("nomeCliente"));
        colCompras.setCellValueFactory(new PropertyValueFactory<>("quantidadeCompras"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colTotal.setCellFactory(tc -> new TableCell<ClienteResumo, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });

        campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            if (clientesFiltrados != null) {
                clientesFiltrados.setPredicate(c ->
                        newVal == null || newVal.isBlank() ||
                                c.getNomeCliente().toLowerCase().contains(newVal.toLowerCase())
                );
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

        // 🔥 BUSCA NO SERVICE CORRETO
        List<Vendas> vendas = vendaService.getHistoricoMensal(
                Integer.parseInt(ano),
                Integer.parseInt(mes)
        );

        // 🔥 PROCESSA NO SERVICE CORRETO
        Map<String, ClienteResumo> resumo =
                relatorioService.gerarRelatorioPorCliente(vendas);

        ObservableList<ClienteResumo> dados =
                FXCollections.observableArrayList(resumo.values());

        clientesFiltrados = new FilteredList<>(dados, c -> true);
        tabelaClientes.setItems(clientesFiltrados);

        double totalGeral = dados.stream()
                .mapToDouble(ClienteResumo::getValorTotal)
                .sum();

        lblTotalGeral.setText(
                NumberFormat.getCurrencyInstance(new Locale("pt","BR"))
                        .format(totalGeral)
        );

        if (dados.isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-gray;");
            statusLabel.setText("Nenhum resultado.");
        } else {
            statusLabel.setStyle("-fx-text-fill: -cor-success;");
            statusLabel.setText(dados.size() + " cliente(s) encontrado(s)");
        }
    }
}

