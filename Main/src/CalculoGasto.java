public class CalculoGasto {
    public static double calcularCustoMensal(Aparelho aparelho, double valorKWh) {
        double consumoMensal = (aparelho.getPotencia() * aparelho.getTempoUso() * 30) / 1000.0;
        return consumoMensal * valorKWh;
    }
}
