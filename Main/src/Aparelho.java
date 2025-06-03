public class Aparelho {
    private String nome;
    private double potencia;
    private double tempoUso;

    public Aparelho(String nome, double potencia, double tempoUso) {
        this.nome = nome;
        this.potencia = potencia;
        this.tempoUso = tempoUso;
    }

    String sqlAparelhos = """
    CREATE TABLE IF NOT EXISTS aparelhos (
        nome TEXT,
        potencia REAL,
        tempoUso REAL,
        usuario_id INTEGER,
        PRIMARY KEY (nome, usuario_id),
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    );
""";


    public String getNome() { return nome; }
    public double getPotencia() { return potencia; }
    public double getTempoUso() { return tempoUso; }

    public void setPotencia(double potencia) { this.potencia = potencia; }
    public void setTempoUso(double tempoUso) { this.tempoUso = tempoUso; }

    @Override
    public String toString() {
        return nome + " - " + potencia + "W, " + tempoUso + "h/dia";
    }
}
