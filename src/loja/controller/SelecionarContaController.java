package loja.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import loja.model.entity.Conta;
import loja.service.AppService;
import loja.service.ContaService;
import loja.service.ProdutoService;
import loja.service.VendaService;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelecionarContaController {

    private ContaService contaService;
    private VendaService vendaService;

    @FXML private ComboBox<String> comboClientes;
    @FXML private Label statusLabel;

    public void setService(ContaService contaService, VendaService vendaService) {
        this.contaService = contaService;
        this.vendaService = vendaService;

        contaService.getContas().addListener((ListChangeListener<Conta>) change -> {
            carregarClientes();
        });

        carregarClientes();
    }
    private void carregarClientes() {
        comboClientes.setItems(
            contaService.getContas().stream()
                .map(Conta::getNomeCliente)
                .collect(Collectors.toCollection(FXCollections::observableArrayList))
        );
    }
    public void removerClienteDaLista(String nomeCliente) {
        comboClientes.getItems().remove(nomeCliente);
    }

    @FXML
    private void abrirConta() {
        String nomeCliente = comboClientes.getValue();
        if (nomeCliente == null || nomeCliente.isBlank()) {
            statusLabel.setStyle("-fx-text-fill: -cor-warning;");
            statusLabel.setText("Selecione ou digite o nome de um cliente.");
            return;
        }
        Conta conta = contaService.obterOuCriarConta(nomeCliente);
        abrirTelaConta(conta);
    }

    @FXML
    private void novaConta() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nova Conta");
        dialog.setHeaderText("Cadastrar novo cliente");
        dialog.setContentText("Nome do cliente:");

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(nome -> {
            if (!nome.isBlank()) {
                Conta conta = contaService.obterOuCriarConta(nome);
                comboClientes.getItems().add(nome);
                comboClientes.getSelectionModel().select(nome);
                abrirTelaConta(conta);
            }
        });
    }
    private void abrirTelaConta(Conta conta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loja/view/Conta.fxml"));
            Parent root = loader.load();

            ContaController controller = loader.getController();

            // 🔥 injeção correta agora
            controller.setServices(contaService, vendaService);
            controller.setSelecionarContaController(this);
            controller.setConta(conta);

            Stage stage = new Stage();
            stage.setTitle("Conta de " + conta.getNomeCliente());
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(mensagem);
        alert.showAndWait();
    }
}
