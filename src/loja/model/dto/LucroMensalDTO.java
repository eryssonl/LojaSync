package loja.model.dto;

public class LucroMensalDTO {
    private String mes;
    private double total;

    public LucroMensalDTO(String mes, double total) {
        this.mes = mes;
        this.total = total;
    }

    public String getMes() { return mes; }
    public double getTotal() { return total; }
}

