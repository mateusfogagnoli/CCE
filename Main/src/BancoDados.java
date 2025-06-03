import java.sql.Connection;
import java.sql.Statement;

public class BancoDados {
    public static void inicializarBanco() {
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nome TEXT UNIQUE,
                senha TEXT
            );
        """;

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

        String sqlConfig = """
            CREATE TABLE IF NOT EXISTS config (
                chave TEXT,
                valor REAL,
                usuario_id INTEGER,
                PRIMARY KEY (chave, usuario_id)
            );
        """;

        try (Connection conn = Conexao.conectar(); Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlAparelhos);
            stmt.execute(sqlConfig);
        } catch (Exception e) {
            System.out.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }
}
