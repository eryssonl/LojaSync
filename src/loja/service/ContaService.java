package loja.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import loja.model.entity.Conta;
import loja.model.entity.Vendas;
import loja.repository.ContaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ContaService {

    private final ObservableList<Conta> contas;
    private final ContaRepository contaRepo;

    public ContaService() {
        this.contaRepo = new ContaRepository();

        List<Conta> carregadas = contaRepo.carregarContas();
        this.contas = FXCollections.observableArrayList(
                carregadas != null ? carregadas : new ArrayList<>()
        );
    }

    // 🔹 Buscar conta (sem criar)
    public Optional<Conta> buscarConta(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente inválido");
        }

        return contas.stream()
                .filter(c -> c.getNomeCliente().equalsIgnoreCase(nomeCliente))
                .findFirst();
    }

    // 🔹 Criar conta se não existir
    public Conta obterOuCriarConta(String nomeCliente) {
        return buscarConta(nomeCliente)
                .orElseGet(() -> {
                    Conta nova = new Conta(nomeCliente);
                    contas.add(nova);
                    salvarContas();
                    return nova;
                });
    }

    // 🔹 Registrar compra no fiado
    public void registrarCompraNaConta(String nomeCliente, Vendas venda) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente não pode ser vazio");
        }

        if (venda == null) {
            throw new IllegalArgumentException("Venda não pode ser nula");
        }

        Conta conta = obterOuCriarConta(nomeCliente);
        conta.adicionarCompra(venda);

        salvarContas();
    }
    public void fecharConta(Conta conta, VendaService vendaService) {

        if (conta.getCompras().isEmpty()) return;

        // 1. joga vendas no histórico geral
        vendaService.adicionarVendas(conta.getCompras());

        // 2. salvar histórico mensal (opcional manter no repo)
        //contaRepo.salvarHistoricoMensalCliente(conta);

        // 3. limpar conta
        conta.fecharConta();

        // 4. remover conta da lista
        contas.remove(conta);

        // 5. persistir
        salvarContas();
    }
    /*public void salvarHistoricoMensal(Conta conta) {
        contaRepo.salvarHistoricoMensalCliente(conta);
    }*/
    public void salvarContas() {
        contaRepo.salvarContas(new ArrayList<>(contas));
    }

    public ObservableList<Conta> getContas() {
        return contas;
    }

}
