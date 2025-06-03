public class Usuario {
    private String username;
    private String senha;
    private double meta;

    public Usuario(String username, String senha, double meta) {
        this.username = username;
        this.senha = senha;
        this.meta = meta;
    }

    String sqlUsuarios = """
    CREATE TABLE IF NOT EXISTS usuarios (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        nome TEXT UNIQUE,
        senha TEXT
    );
""";

    public String getUsername() {
        return username;
    }

    public String getSenha() {
        return senha;
    }

    public double getMeta() {
        return meta;
    }

    public void setMeta(double meta) {
        this.meta = meta;
    }
}
