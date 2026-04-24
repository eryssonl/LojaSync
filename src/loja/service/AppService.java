package loja.service;

public class AppService {
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final ContaService contaService;
    private final RelatorioService relatorioService;
    private final RankingService rankingService;

    public AppService() {
        this.produtoService = new ProdutoService();
        this.vendaService = new VendaService(produtoService);
        this.contaService = new ContaService();
        this.relatorioService = new RelatorioService(vendaService);
        this.rankingService = new RankingService(vendaService);
    }

    public ProdutoService getProdutoService() { return produtoService; }
    public VendaService getVendaService() { return vendaService; }
    public ContaService getContaService() { return contaService; }
    public RelatorioService getRelatorioService() { return relatorioService; }
    public RankingService getRankingService() { return rankingService; }
}
