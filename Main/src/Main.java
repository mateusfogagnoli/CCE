import java.sql.*;
import java.util.*;

public class Main {
    private static int usuarioId = -1;
    private static final Map<String, Double> historicoAnterior = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BancoDados.inicializarBanco();
        realizarLoginOuCadastro(scanner);
        double valorKWh = lerValorKWh(scanner);
        definirMeta(scanner);

        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Adicionar aparelho");
            System.out.println("2. Remover aparelho");
            System.out.println("3. Editar aparelho");
            System.out.println("4. Ver historico de aparelhos");
            System.out.println("5. Ver aparelhos e os que mais gastam");
            System.out.println("6. Simular 30 dias e gerar relatorio");
            System.out.println("7. Encerrar aplicativo");
            System.out.print("Escolha uma opcao: ");

            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> adicionarAparelho(scanner);
                case 2 -> removerAparelho(scanner);
                case 3 -> editarAparelho(scanner);
                case 4 -> listarAparelhos();
                case 5 -> listarCustos(valorKWh);
                case 6 -> gerarRelatorio(valorKWh);
                case 7 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opcao invalida");
            }
        }
    }

    private static void realizarLoginOuCadastro(Scanner scanner) {
        try (Connection conn = Conexao.conectar()) {
            while (usuarioId == -1) {
                System.out.println("1. Login\n2. Cadastro");
                int opcao = scanner.nextInt();
                scanner.nextLine();

                System.out.print("Usuario: ");
                String nome = scanner.nextLine();
                System.out.print("Senha: ");
                String senha = scanner.nextLine();

                if (opcao == 1) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "SELECT id FROM usuarios WHERE nome = ? AND senha = ?");
                    stmt.setString(1, nome);
                    stmt.setString(2, senha);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        usuarioId = rs.getInt("id");
                        System.out.println("Login bem-sucedido!");
                    } else {
                        System.out.println("Credenciais invalidas.");
                    }
                } else if (opcao == 2) {
                    PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO usuarios (nome, senha) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                    stmt.setString(1, nome);
                    stmt.setString(2, senha);
                    stmt.executeUpdate();
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        usuarioId = rs.getInt(1);
                        System.out.println("Cadastro realizado com sucesso!");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro no login/cadastro: " + e.getMessage());
        }
    }

    private static double lerValorKWh(Scanner scanner) {
        try (Connection conn = Conexao.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valor FROM config WHERE chave = 'kwh' AND usuario_id = ?");
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getDouble("valor");

            System.out.print("Informe o valor do kWh em reais: ");
            double valor = scanner.nextDouble();
            scanner.nextLine();
            stmt = conn.prepareStatement("INSERT INTO config (chave, valor, usuario_id) VALUES ('kwh', ?, ?)");
            stmt.setDouble(1, valor);
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();
            return valor;
        } catch (SQLException e) {
            System.out.println("Erro ao acessar configuracao: " + e.getMessage());
            return 0.0;
        }
    }

    private static void definirMeta(Scanner scanner) {
        try (Connection conn = Conexao.conectar()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT valor FROM config WHERE chave = 'meta' AND usuario_id = ?");
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                System.out.print("Defina sua meta de consumo em R$: ");
                double meta = scanner.nextDouble();
                scanner.nextLine();
                stmt = conn.prepareStatement("INSERT INTO config (chave, valor, usuario_id) VALUES ('meta', ?, ?)");
                stmt.setDouble(1, meta);
                stmt.setInt(2, usuarioId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao definir meta: " + e.getMessage());
        }
    }

    private static void adicionarAparelho(Scanner scanner) {
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO aparelhos (nome, potencia, tempoUso, usuario_id) VALUES (?, ?, ?, ?)")) {
            System.out.print("Nome do aparelho: ");
            stmt.setString(1, scanner.nextLine());
            System.out.print("Potencia (W): ");
            stmt.setDouble(2, scanner.nextDouble());
            System.out.print("Tempo de uso por dia (h): ");
            stmt.setDouble(3, scanner.nextDouble());
            stmt.setInt(4, usuarioId);
            scanner.nextLine();
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar aparelho: " + e.getMessage());
        }
    }

    private static void removerAparelho(Scanner scanner) {
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(
                "DELETE FROM aparelhos WHERE nome = ? AND usuario_id = ?")) {
            System.out.print("Nome do aparelho a remover: ");
            stmt.setString(1, scanner.nextLine());
            stmt.setInt(2, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao remover aparelho: " + e.getMessage());
        }
    }

    private static void editarAparelho(Scanner scanner) {
        System.out.print("Nome do aparelho a editar: ");
        String nome = scanner.nextLine();

        System.out.println("Editar: 1 - Potência, 2 - Tempo de uso, 3 - Ambos");
        int escolha = scanner.nextInt();

        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(
                "UPDATE aparelhos SET potencia = ?, tempoUso = ? WHERE nome = ? AND usuario_id = ?")) {
            double novaPotencia = 0, novoTempo = 0;
            if (escolha == 1 || escolha == 3) {
                System.out.print("Nova potencia: ");
                novaPotencia = scanner.nextDouble();
            }
            if (escolha == 2 || escolha == 3) {
                System.out.print("Novo tempo de uso: ");
                novoTempo = scanner.nextDouble();
            }
            scanner.nextLine();
            stmt.setDouble(1, novaPotencia);
            stmt.setDouble(2, novoTempo);
            stmt.setString(3, nome);
            stmt.setInt(4, usuarioId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erro ao editar aparelho: " + e.getMessage());
        }
    }

    private static void listarAparelhos() {
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM aparelhos WHERE usuario_id = ? ORDER BY nome")) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println(
                        rs.getString("nome") + " - " +
                                rs.getDouble("potencia") + "W, " +
                                rs.getDouble("tempoUso") + "h/dia");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar aparelhos: " + e.getMessage());
        }
    }

    private static void listarCustos(double valorKWh) {
        try (Connection conn = Conexao.conectar(); PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM aparelhos WHERE usuario_id = ?")) {
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();
            double maiorCusto = 0;
            String aparelhoMaisCaro = "";

            while (rs.next()) {
                Aparelho a = new Aparelho(
                        rs.getString("nome"),
                        rs.getDouble("potencia"),
                        rs.getDouble("tempoUso"));
                double custo = CalculoGasto.calcularCustoMensal(a, valorKWh);
                System.out.printf("%s - R$ %.2f/mês\n", a.getNome(), custo);
                if (custo > maiorCusto) {
                    maiorCusto = custo;
                    aparelhoMaisCaro = a.getNome();
                }
            }
            if (!aparelhoMaisCaro.isEmpty()) {
                System.out.println("Maior gasto: " + aparelhoMaisCaro);
                System.out.println("Considere reduzir o uso de " + aparelhoMaisCaro + " para economizar.");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao listar custos: " + e.getMessage());
        }
    }

    private static void gerarRelatorio(double valorKWh) {
        try (Connection conn = Conexao.conectar()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT valor FROM config WHERE chave = 'meta' AND usuario_id = ?");
            stmt.setInt(1, usuarioId);
            ResultSet rsMeta = stmt.executeQuery();
            double meta = rsMeta.next() ? rsMeta.getDouble("valor") : Double.MAX_VALUE;

            stmt = conn.prepareStatement("SELECT * FROM aparelhos WHERE usuario_id = ?");
            stmt.setInt(1, usuarioId);
            ResultSet rs = stmt.executeQuery();

            double total = 0;
            System.out.println("Relatorio mensal:");
            while (rs.next()) {
                Aparelho a = new Aparelho(
                        rs.getString("nome"),
                        rs.getDouble("potencia"),
                        rs.getDouble("tempoUso"));
                double custo = CalculoGasto.calcularCustoMensal(a, valorKWh);
                String nome = a.getNome();
                System.out.printf("%s: R$ %.2f\n", nome, custo);
                total += custo;

                if (historicoAnterior.containsKey(nome)) {
                    double anterior = historicoAnterior.get(nome);
                    if (anterior > 0) {
                        double variacao = ((custo - anterior) / anterior) * 100;
                        System.out.printf("   Variação de: %.2f%%\n", variacao);
                    }
                }
                historicoAnterior.put(nome, custo);
            }

            System.out.printf("Total: R$ %.2f\n", total);
            if (total > meta) {
                System.out.println("Voce ultrapassou sua meta de R$" + meta);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao gerar relatorio: " + e.getMessage());
        }
    }
}
