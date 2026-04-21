package loja.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Conta {
    private String nomeCliente;
    private List<Vendas> compras = new ArrayList<>();

    // 🔹 Construtor obrigatório pro Gson
    public Conta() {
        this.compras = new ArrayList<>();
    }

    public Conta(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank()) {
            throw new IllegalArgumentException("Nome do cliente não pode ser vazio");
        }
        this.nomeCliente = nomeCliente;
        this.compras = new ArrayList<>();
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    // 🔹 Retorna lista protegida
    public List<Vendas> getCompras() {
        return Collections.unmodifiableList(compras);
    }

    public void adicionarCompra(Vendas venda) {
        if (venda == null) {
            throw new IllegalArgumentException("Venda não pode ser nula");
        }
        compras.add(venda);
    }

    public void fecharConta() {
        compras.clear();
    }

    public double getTotal() {
        return compras.stream()
                .mapToDouble(Vendas::getValorTotal)
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta)) return false;
        Conta conta = (Conta) o;

        if (nomeCliente == null || conta.nomeCliente == null) return false;

        return nomeCliente.equalsIgnoreCase(conta.nomeCliente);
    }

    @Override
    public int hashCode() {
        return nomeCliente != null
                ? nomeCliente.toLowerCase().hashCode()
                : 0;
    }

    @Override
    public String toString() {
        return "Conta{" +
                "cliente='" + nomeCliente + '\'' +
                ", total=" + getTotal() +
                ", compras=" + compras.size() +
                '}';
    }
}

