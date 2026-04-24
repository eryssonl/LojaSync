package loja.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import loja.model.entity.Vendas;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class VendaRepository {
    private static final String ARQUIVO = "vendas.example.json";

    private final Gson gson;

    public VendaRepository() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    // Salvar TODAS as vendas
    public void salvarVendas(List<Vendas> vendas) {
        try (Writer writer = new FileWriter(ARQUIVO)) {
            gson.toJson(vendas, writer);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar vendas", e);
        }
    }

    // Carregar vendas
    public ObservableList<Vendas> carregarVendas() {
        File arquivo = new File(ARQUIVO);

        if (!arquivo.exists()) {
            return FXCollections.observableArrayList();
        }

        try (Reader reader = new FileReader(arquivo)) {

            Type tipoLista = new TypeToken<List<Vendas>>() {}.getType();
            List<Vendas> lista = gson.fromJson(reader, tipoLista);

            if (lista == null) {
                lista = new ArrayList<>();
            }

            return FXCollections.observableArrayList(lista);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar vendas", e);
        }
    }

}
