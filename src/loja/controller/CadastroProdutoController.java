package loja.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextFormatter;
import loja.model.entity.Produto;
import loja.service.AppService;
import loja.service.ProdutoService;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.UnaryOperator;

import javafx.scene.control.Label;

public class CadastroProdutoController {

    @FXML
    private TextField nomeField;
    @FXML
    private TextField precoField;
    @FXML
    private TextField custoField;
    @FXML
    private TextField quantidadeField;
    @FXML
    private Label statusLabel;

    private ProdutoService produtoService;

    public void setService(AppService appService) {
        this.produtoService = appService.getProdutoService();
    }

    @FXML
    public void initialize() {
        setupCurrencyFormatter(precoField);
        setupCurrencyFormatter(custoField);
        resetCurrencyFields();
    }

    private void setupCurrencyFormatter(TextField field) {
        NumberFormat formatoMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        UnaryOperator<TextFormatter.Change> filtro = change -> {
            String newText = change.getControlNewText().replaceAll("[^0-9]", "");

            if (newText.isEmpty()) {
                change.setText("R$ 0,00");
                change.setRange(0, change.getControlText().length());
                return change;
            }

            // Converte para número inteiro e divide por 100 para ter centavos
            long valorInteiro = Long.parseLong(newText);
            double valor = valorInteiro / 100.0;

            // Formata como moeda brasileira
            change.setText(formatoMoeda.format(valor));
            change.setRange(0, change.getControlText().length());
            return change;
        };

        TextFormatter<String> formatter = new TextFormatter<>(filtro);
        field.setTextFormatter(formatter);
        field.setText("R$ 0,00"); // valor inicial
        field.textProperty().addListener((obs, oldText, newText) -> {
            Platform.runLater(() -> field.positionCaret(field.getText().length()));
        });
    }

    @FXML
    private void cadastrarProduto() {
        try {
            Produto novoProduto = construirProdutoDoFormulario();

            produtoService.cadastrarProduto(novoProduto);

            mostrarSucesso("Produto \"" + novoProduto.getNome() + "\" cadastrado com sucesso!");
            limparCampos();

        } catch (IllegalArgumentException ex) {
            mostrarErro(ex.getMessage());
        }
    }


    private Produto construirProdutoDoFormulario()  {

        String nome = nomeField.getText();
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Informe o nome do produto");
        }

        // === Preço ===
        double preco = parseCurrencyField(precoField);
        if (preco <= 0) {
            throw new IllegalArgumentException("O preço de venda deve ser maior que zero");
        }

        // === Custo ===
        double custo = parseCurrencyField(custoField);
        if (custo <= 0) {
            throw new IllegalArgumentException("O custo deve ser maior que zero");
        }

        // === Quantidade ===
        String qtdTexto = quantidadeField.getText().trim();
        if (qtdTexto.isEmpty()) {
            throw new IllegalArgumentException("Informe a quantidade em estoque");
        }
        int quantidade = Integer.parseInt(qtdTexto);
        if (quantidade < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa");
        }

        Produto produto = new Produto(nome, preco);
        produto.adicionarLote(custo,quantidade);

        return produto;

    }
    private void mostrarSucesso(String mensagem) {
        statusLabel.setStyle("-fx-text-fill: -cor-success;");
        statusLabel.setText(mensagem);
    }

    private void mostrarErro(String mensagem) {
        statusLabel.setStyle("-fx-text-fill: -cor-danger;");
        statusLabel.setText(mensagem);
    }

    private double parseCurrencyField(TextField field) {
        String texto = field.getText()
                .replaceAll("[^0-9,]", "")
                .replace(",", ".");

        if (texto.isEmpty()) {
            throw new NumberFormatException("Valor vazio");
        }
        return Double.parseDouble(texto);
    }

    @FXML
    private void limparCampos() {
        nomeField.clear();
        quantidadeField.clear();
        resetCurrencyFields();
    }

    private void resetCurrencyFields() {
        precoField.setText("R$ 0,00");
        custoField.setText("R$ 0,00");
    }
}