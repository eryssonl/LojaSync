package loja.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import loja.service.AppService;

import java.io.IOException;

public class LojaAppController {

    private final AppService appService = new AppService();

    @FXML
    private BorderPane rootPane;
    @FXML
    private VBox containerPrincipal;

    private void carregarConteudo(String caminhoFXML) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(caminhoFXML));
            Parent conteudo = loader.load();

            Object controller = loader.getController();

            injetarDependencias(controller);

            containerPrincipal.getChildren().setAll(conteudo);
            containerPrincipal.maxWidthProperty()
                    .bind(rootPane.widthProperty().multiply(0.9));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void injetarDependencias(Object controller){
        // 🔥 injeção correta (sem reflection genérico)


        if (controller instanceof CadastroProdutoController c) {
            c.setService(appService);
        }

        else if (controller instanceof VendasController c) {
            c.setService(appService);
        }

        else if (controller instanceof SelecionarContaController c) {
            c.setService(appService.getContaService(),
                         appService.getVendaService());
        }

        else if (controller instanceof ContaController c) {
            c.setServices(appService.getContaService(),
                          appService.getVendaService());
        }

        else if (controller instanceof RankingProdutosController c) {
            c.setServices(appService);
        }

        else if (controller instanceof RelatorioClientesController c) {
            c.setService(appService);
        }

        else if (controller instanceof RelatoriosController c) {
            c.setService(appService);
        }

        else if (controller instanceof LucroMensalController c) {
            c.setServices(appService);
        }

        else if (controller instanceof ListagemProdutoController c) {
            c.setProdutoService(appService);
        }

        else if (controller instanceof HistoricoVendasController c) {
            c.setService(appService);
        }
    }

    @FXML
    private void abrirCadastro() {
        carregarConteudo("/loja/view/CadastroProdutos.fxml");
    }
    @FXML
    private void abrirListagem() {
        carregarConteudo("/loja/view/ListagemProdutos.fxml");
    }
    @FXML
    private void abrirVendas() {
        carregarConteudo("/loja/view/VendasProdutos.fxml");
    }
    @FXML
    private void abrirHistorico() {
        carregarConteudo("/loja/view/HistoricoVendas.fxml");
    }
    @FXML
    private void abrirTelaContas(){
        carregarConteudo("/loja/view/SelecionarConta.fxml");
    }
    @FXML
    private void abrirLucroMensal() {
        carregarConteudo("/loja/view/LucroMensal.fxml");
    }
    @FXML
    private void abrirRelatorios() {
        carregarConteudo("/loja/view/Relatorios.fxml");
    }
    @FXML
    private void abrirCliente(){
        carregarConteudo("/loja/view/RelatorioClientes.fxml");
    }
    @FXML
    private void abrirRankingProdutos(){
        carregarConteudo("/loja/view/RankingProdutos.fxml");
    }

    @FXML
    private void sairSistema() {
        // Fechar a aplicação
        appService.getProdutoService().salvarProdutos();
        javafx.application.Platform.exit();
    }
}

