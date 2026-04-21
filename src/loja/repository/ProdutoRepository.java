package loja.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import loja.model.entity.Produto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProdutoRepository {
    private static final String ARQUIVO = "produtos.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Salvar TODOS os produtos (sobrescreve)
    public void salvarProdutos(List<Produto> produtos) {
        try (Writer writer = new FileWriter(ARQUIVO)) {
            gson.toJson(produtos, writer);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar produtos", e);
        }
    }

    // Carregar produtos
    public ObservableList<Produto> carregarProdutos() {
        File arquivo = new File(ARQUIVO);

        if (!arquivo.exists()) {
            return FXCollections.observableArrayList();
        }

        try (Reader reader = new FileReader(arquivo)) {

            Type tipoLista = new TypeToken<List<Produto>>() {}.getType();
            List<Produto> lista = gson.fromJson(reader, tipoLista);

            if (lista == null) {
                lista = new ArrayList<>();
            }

            return FXCollections.observableArrayList(lista);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar produtos", e);
        }
    }
}
