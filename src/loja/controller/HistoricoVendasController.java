package loja.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import loja.model.entity.Vendas;
import loja.service.AppService;
import loja.service.ProdutoService;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import javafx.collections.transformation.FilteredList;
import loja.service.VendaService;

public class HistoricoVendasController {

    private VendaService vendaService;

    @FXML private TableView<Vendas> tabelaHistorico;
    @FXML private TableColumn<Vendas, String> colProduto;
    @FXML private TableColumn<Vendas, Integer> colQuantidade;
    @FXML private TableColumn<Vendas, Double> colPreco;
    @FXML private TableColumn<Vendas, Double> colCusto;
    @FXML private TableColumn<Vendas, Double> colLucroUnitario;
    @FXML private TableColumn<Vendas, Double> colLucroPercentual;
    @FXML private TableColumn<Vendas, Double> colLucroTotal;
    @FXML private TableColumn<Vendas, LocalDateTime> colData;
    @FXML private TableColumn<Vendas, Double> colTotal;
    @FXML private Label labelResumo;
    @FXML private TextField campoBusca;
    @FXML private Label statusLabel;

    private FilteredList<Vendas> vendasFiltradas;

    public void setService(AppService appService) {
        this.vendaService = appService.getVendaService();

        if (vendaService != null) {
            vendasFiltradas = new FilteredList<>(vendaService.listarVendas(), v -> true);
            tabelaHistorico.setItems(vendasFiltradas);
        }

        atualizarTabela();
    }

    @FXML
    public void initialize() {
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colPreco.setCellValueFactory(new PropertyValueFactory<>("precoUnitario"));
        colCusto.setCellValueFactory(new PropertyValueFactory<>("custoUnitario"));
        colLucroUnitario.setCellValueFactory(new PropertyValueFactory<>("lucroUnitario"));
        colLucroPercentual.setCellValueFactory(new PropertyValueFactory<>("lucroPercentual"));
        colLucroTotal.setCellValueFactory(new PropertyValueFactory<>("lucroTotal"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));

        tabelaHistorico.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // 💰 Formatações monetárias
        configurarColunaMoeda(colPreco);
        configurarColunaMoeda(colCusto);
        configurarColunaMoeda(colLucroUnitario);
        configurarColunaMoeda(colLucroTotal);
        configurarColunaMoeda(colTotal);

        // 📊 Percentual
        colLucroPercentual.setCellFactory(tc -> new TableCell<Vendas, Double>() {
            @Override
            protected void updateItem(Double percentual, boolean empty) {
                super.updateItem(percentual, empty);
                setText(empty || percentual == null ? null : String.format("%.2f%%", percentual));
            }
        });

        // 📅 Data
        colData.setCellFactory(tc -> new TableCell<Vendas, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : data.format(formatter));
            }
        });

        // 🔍 Busca
        if (campoBusca != null) {
            campoBusca.textProperty().addListener((obs, oldVal, newVal) -> {
                if (vendasFiltradas != null) {
                    vendasFiltradas.setPredicate(v -> {
                        if (newVal == null || newVal.isBlank()) return true;

                        String filtro = newVal.toLowerCase();

                        return v.getNomeProduto().toLowerCase().contains(filtro)
                                || (v.getNomeCliente() != null &&
                                v.getNomeCliente().toLowerCase().contains(filtro));
                    });
                }
            });
        }
    }

    // 🔥 Reaproveitamento (melhor prática)
    private void configurarColunaMoeda(TableColumn<Vendas, Double> coluna) {
        coluna.setCellFactory(tc -> new TableCell<Vendas, Double>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : String.format("R$ %.2f", valor));
            }
        });
    }

    private void atualizarTabela() {
        atualizarResumo();
        vendaService.salvarVendas();

        if (tabelaHistorico.getItems().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: -cor-gray;");
            statusLabel.setText("Nenhuma venda registrada ainda.");
        } else {
            statusLabel.setText("");
        }
    }

    private void atualizarResumo() {
        double totalVendido = 0.0;
        double lucroAcumulado = 0.0;

        for (Vendas v : vendaService.listarVendas()) {
            totalVendido += v.getValorTotal();
            lucroAcumulado += v.getLucroTotal();
        }

        labelResumo.setText(
                "Total vendido: R$ " + String.format("%.2f", totalVendido) +
                        " | Lucro acumulado: R$ " + String.format("%.2f", lucroAcumulado)
        );
    }

}
