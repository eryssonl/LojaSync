package loja.service;

import loja.model.dto.ProdutoRankingDTO;
import loja.model.entity.Vendas;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RankingService {

    private final VendaService vendaService;

    public RankingService(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    public List<ProdutoRankingDTO> gerarRankingProdutos(List<Vendas> vendas) {
        Map<String, ProdutoRankingDTO> ranking = new HashMap<>();

        for (Vendas v : vendas) {
            ranking.compute(v.getNomeProduto(), (nome, dto) -> {
                if (dto == null) {
                    return new ProdutoRankingDTO(nome, v.getQuantidade(), v.getValorTotal());
                } else {
                    return new ProdutoRankingDTO(nome,
                            dto.getQuantidadeVendida() + v.getQuantidade(),
                            dto.getValorTotal() + v.getValorTotal());
                }
            });
        }

        return ranking.values().stream()
                .sorted(Comparator.comparingLong(ProdutoRankingDTO::getQuantidadeVendida).reversed())
                .toList();
    }
}
