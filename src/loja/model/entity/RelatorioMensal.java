package loja.model.entity;

public class RelatorioMensal {
    private String mesAno;
    private double totalVendas;
    private double lucro;

    public RelatorioMensal(String mesAno, double totalVendas, double lucro) {
        this.mesAno = mesAno;
        this.totalVendas = totalVendas;
        this.lucro = lucro;
    }

    public String getMesAno() { return mesAno; }
    public double getTotalVendas() { return totalVendas; }
    public double getLucro() { return lucro; }

    public void setMesAno(String mesAno) {this.mesAno = mesAno;}
    public void setTotalVendas(double totalVendas) {this.totalVendas = totalVendas;}
    public void setLucro(double lucro) {this.lucro = lucro;}
}


