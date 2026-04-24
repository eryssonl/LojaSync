package loja.repository;

import com.google.gson.*;
import loja.model.entity.Conta;
import com.google.gson.reflect.TypeToken;
import loja.model.entity.Vendas;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ContaRepository {
    private static final String ARQUIVO_CONTAS = "contas.example.json";

    private final Gson gson;

    public ContaRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    public void salvarContas(List<Conta> contas) {
        try (Writer writer = new FileWriter(ARQUIVO_CONTAS)) {
            gson.toJson(contas, writer);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar contas", e);
        }
    }

    public List<Conta> carregarContas() {
        File arquivo = new File(ARQUIVO_CONTAS);

        if (!arquivo.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(arquivo)) {
            Type listType = new TypeToken<List<Conta>>(){}.getType();
            List<Conta> contas = gson.fromJson(reader, listType);

            return contas != null ? contas : new ArrayList<>();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar contas", e);
        }
    }

    //SÓ VALE A PENA SE QUISER:
    //Exportar relatorio por cliente
    //Auditoria
    //Backup individual
    //Multi-tenant

    /*public void salvarHistoricoMensalCliente(Conta conta) {
        File pastaCliente = new File("historicos/" + conta.getNomeCliente());

        if (!pastaCliente.exists()) {
            pastaCliente.mkdirs();
        }

        String nomeArquivo = pastaCliente.getPath() + File.separator +
                "historico_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".json";

        List<Vendas> historicoExistente = new ArrayList<>();

        File arquivo = new File(nomeArquivo);

        // Se já existe, carrega
        if (arquivo.exists()) {
            try (Reader reader = new FileReader(arquivo)) {
                Type listType = new TypeToken<List<Vendas>>(){}.getType();
                historicoExistente = gson.fromJson(reader, listType);

                if (historicoExistente == null) {
                    historicoExistente = new ArrayList<>();
                }

            } catch (IOException e) {
                throw new RuntimeException("Erro ao ler histórico mensal", e);
            }
        }

        // Adiciona novas vendas
        historicoExistente.addAll(conta.getCompras());

        // Salva tudo
        try (Writer writer = new FileWriter(arquivo)) {
            gson.toJson(historicoExistente, writer);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar histórico mensal", e);
        }
    }*/
}
