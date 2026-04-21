package loja.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import loja.model.entity.Conta;
import loja.model.entity.Vendas;
import loja.service.AppService;
import loja.service.ContaService;
import loja.service.VendaService;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ContaController {
    @FXML private Label labelCliente;
    @FXML private Label labelTotal;
    @FXML private TableView<Vendas> tabelaCompras;
    @FXML private TableColumn<Vendas, String> colProduto;
    @FXML private TableColumn<Vendas, Integer> colQuantidade;
    @FXML private TableColumn<Vendas, Double> colValorTotal;
    @FXML private TableColumn<Vendas, LocalDateTime> colData;

    private SelecionarContaController selecionarContaController;
    private ContaService contaService;
    private VendaService vendaService;

    private Conta conta;

    public void setServices(ContaService contaService, VendaService vendaService) {
        this.contaService = contaService;
        this.vendaService = vendaService;
    }

    public void setSelecionarContaController(SelecionarContaController controller) {
        this.selecionarContaController = controller;
    }

    public void setConta(Conta conta) {
        this.conta = conta;

        labelCliente.setText("Conta de: " + conta.getNomeCliente());
        tabelaCompras.setItems(FXCollections.observableArrayList(conta.getCompras()));
        atualizarTotal();
    }

    @FXML
    public void initialize() {
        colProduto.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colValorTotal.setCellValueFactory(new PropertyValueFactory<>("valorTotal"));
        colData.setCellValueFactory(new PropertyValueFactory<>("data"));

        tabelaCompras.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        colValorTotal.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Double valor, boolean empty) {
                super.updateItem(valor, empty);
                setText(empty || valor == null ? null : currencyFormat.format(valor));
            }
        });

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colData.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : data.format(formatter));
            }
        });
    }

    private void atualizarTotal() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        labelTotal.setText(currencyFormat.format(conta.getTotal()));
    }

    @FXML
    private void fecharConta() {

        if (conta.getCompras().isEmpty()) {
            mostrarAlerta("Conta Vazia", "Esta conta não possui compras pendentes.");
            return;
        }

        double total = conta.getTotal();

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Fechar Conta");
        confirmacao.setHeaderText("Deseja fechar a conta de " + conta.getNomeCliente() + "?");
        confirmacao.setContentText("Total: R$ " + String.format("%.2f", total));

        confirmacao.showAndWait().ifPresent(resultado -> {
            if (resultado == ButtonType.OK) {

                // 🔥 Agora tudo delegado corretamente
                contaService.fecharConta(conta, vendaService);

                if (selecionarContaController != null) {
                    selecionarContaController.removerClienteDaLista(conta.getNomeCliente());
                }

                ((Stage) tabelaCompras.getScene().getWindow()).close();
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
