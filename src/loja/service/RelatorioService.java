package loja.service;

import loja.model.dto.ClienteResumo;
import loja.model.dto.LucroMensalDTO;
import loja.model.entity.Vendas;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RelatorioService {

    private final VendaService vendaService;

    public RelatorioService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    public Map<String, ClienteResumo> gerarRelatorioPorCliente(List<Vendas> vendas) {
        return vendas.stream()
                .collect(Collectors.groupingBy(
                        Vendas::getNomeCliente,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lista -> new ClienteResumo(
                                        lista.get(0).getNomeCliente(),
                                        lista.size(),
                                        lista.stream()
                                                .mapToDouble(Vendas::getValorTotal)
                                                .sum()
                                )
                        )
                ));
    }

    public double calcularFaturamento(List<Vendas> vendas) {
        return vendas.stream()
                .mapToDouble(Vendas::getValorTotal)
                .sum();
    }

    public double calcularLucro(List<Vendas> vendas) {
        return vendas.stream()
                .mapToDouble(Vendas::getLucroTotal)
                .sum();
    }

    public Map<String, Integer> agruparQuantidadePorProduto(List<Vendas> vendas) {
        return vendas.stream()
                .collect(Collectors.groupingBy(
                        Vendas::getNomeProduto,
                        Collectors.summingInt(Vendas::getQuantidade)
                ));
    }
    public List<LucroMensalDTO> gerarLucroMensal(List<Vendas> vendas) {

        Map<String, Double> lucroPorMes = vendas.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getData().format(DateTimeFormatter.ofPattern("MM/yyyy")),
                        Collectors.summingDouble(Vendas::getValorTotal)
                ));

        return lucroPorMes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new LucroMensalDTO(
                        formatarMesAno(entry.getKey()),
                        entry.getValue()
                ))
                .collect(Collectors.toList());
    }

    private String formatarMesAno(String mesAno) {
        String[] partes = mesAno.split("/");
        int mesNum = Integer.parseInt(partes[0]);
        int ano = Integer.parseInt(partes[1]);

        String[] meses = {
                "Jan", "Fev", "Mar", "Abr", "Mai", "Jun",
                "Jul", "Ago", "Set", "Out", "Nov", "Dez"
        };

        return meses[mesNum - 1] + "/" + ano;
    }
}
