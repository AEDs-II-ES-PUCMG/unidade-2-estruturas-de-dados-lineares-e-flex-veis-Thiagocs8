import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;

    /** Nome do arquivo de persistência de pedidos */
    static final String nomeArquivoPedidos = "pedidos.txt";

    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Vetor de produtos cadastrados */
    static Produto[] produtosCadastrados;

    /** Quantidade de produtos cadastrados atualmente no vetor */
    static int quantosProdutos = 0;

    /** Pilha de pedidos finalizados */
    static Pilha<Pedido> pilhaPedidos = new Pilha<>();

    /** Pilha de produtos mais recentemente pedidos (Tarefa 2) */
    static Pilha<Produto> pilhaProdutosRecentes = new Pilha<>();

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {

    	T valor;

    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }

    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * @return Um inteiro com a opção do usuário.
     */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar por um produto, por código");
        System.out.println("3 - Procurar por um produto, por nome");
        System.out.println("4 - Iniciar novo pedido");
        System.out.println("5 - Fechar pedido");
        System.out.println("6 - Listar produtos dos pedidos mais recentes");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos. Arquivo-texto no formato
     * N  (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna um vetor vazio em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Um vetor com os produtos carregados, ou vazio em caso de problemas de leitura.
     */
    static Produto[] lerProdutos(String nomeArquivoDados) {

    	Scanner arquivo = null;
    	int numProdutos;
    	String linha;
    	Produto produto;
    	Produto[] produtosCadastrados;

    	try {
    		arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

    		numProdutos = Integer.parseInt(arquivo.nextLine());
    		produtosCadastrados = new Produto[numProdutos];

    		for (int i = 0; i < numProdutos; i++) {
    			linha = arquivo.nextLine();
    			produto = Produto.criarDoTexto(linha);
    			produtosCadastrados[i] = produto;
    		}
    		quantosProdutos = numProdutos;

    	} catch (IOException excecaoArquivo) {
    		produtosCadastrados = null;
    	} finally {
    		arquivo.close();
    	}

    	return produtosCadastrados;
    }

    /** Localiza um produto no vetor de produtos cadastrados, a partir do código de produto informado pelo usuário, e o retorna.
     *  Em caso de não encontrar o produto, retorna null
     */
    static Produto localizarProduto() {

    	Produto produto = null;
    	Boolean localizado = false;

    	cabecalho();
    	System.out.println("Localizando um produto...");
        int idProduto = lerOpcao("Digite o código identificador do produto desejado: ", Integer.class);
        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
        	if (produtosCadastrados[i].hashCode() == idProduto) {
        		produto = produtosCadastrados[i];
        		localizado = true;
        	}
        }

        return produto;
    }

    /** Localiza um produto no vetor de produtos cadastrados, a partir do nome de produto informado pelo usuário, e o retorna.
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null
     *  @return O produto encontrado ou null, caso o produto não tenha sido localizado no vetor de produtos cadastrados.
     */
    static Produto localizarProdutoDescricao() {

    	Produto produto = null;
    	Boolean localizado = false;
    	String descricao;

    	cabecalho();
    	System.out.println("Localizando um produto...");
    	System.out.println("Digite o nome ou a descrição do produto desejado:");
        descricao = teclado.nextLine();
        for (int i = 0; (i < quantosProdutos && !localizado); i++) {
        	if (produtosCadastrados[i].descricao.equals(descricao)) {
        		produto = produtosCadastrados[i];
        		localizado = true;
    		}
        }

        return produto;
    }

    private static void mostrarProduto(Produto produto) {

        cabecalho();
        String mensagem = "Dados inválidos para o produto!";

        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }

        System.out.println(mensagem);
    }

    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static void listarTodosOsProdutos() {

        cabecalho();
        System.out.println("\nPRODUTOS CADASTRADOS:");
        for (int i = 0; i < quantosProdutos; i++) {
        	System.out.println(String.format("%02d - %s", (i + 1), produtosCadastrados[i].toString()));
        }
    }

    /**
     * Inicia um novo pedido.
     * Permite ao usuário escolher e incluir produtos no pedido.
     * @return O novo pedido
     */
    public static Pedido iniciarPedido() {

    	int formaPagamento = lerOpcao("Digite a forma de pagamento do pedido, sendo 1 para pagamento à vista e 2 para pagamento a prazo", Integer.class);
    	Pedido pedido = new Pedido(LocalDate.now(), formaPagamento);
    	Produto produto;
    	int numProdutos;
    	int quantidade;

    	listarTodosOsProdutos();
    	System.out.println("Incluindo produtos no pedido...");
    	numProdutos = lerOpcao("Quantos produtos serão incluídos no pedido?", Integer.class);
        for (int i = 0; i < numProdutos; i++) {
        	produto = localizarProdutoDescricao();
        	if (produto == null) {
        		System.out.println("Produto não encontrado");
        		i--;
        	} else {
        		quantidade = lerOpcao("Quantos itens desse produto serão incluídos no pedido?", Integer.class);
        		pedido.incluirProduto(produto, quantidade);
        	}
        }

        return pedido;
    }

    /**
     * Finaliza um pedido: imprime o resumo, empilha em pilhaPedidos e registra
     * cada produto do pedido em pilhaProdutosRecentes.
     * @param pedido O pedido que deve ser finalizado.
     */
    public static void finalizarPedido(Pedido pedido) {

    	if (pedido == null) {
    		System.out.println("Nenhum pedido em andamento. Inicie um pedido primeiro (opção 4).");
    		return;
    	}

    	System.out.println("Pedido finalizado com sucesso!");
    	System.out.println(pedido);

    	pilhaPedidos.empilhar(pedido);

    	// Empilha cada produto do pedido em pilhaProdutosRecentes (Tarefa 2)
    	ItemDePedido[] itens = pedido.getItensDoPedido();
    	for (int i = 0; i < pedido.getQuantItensDoPedido(); i++) {
    		pilhaProdutosRecentes.empilhar(itens[i].getProduto());
    	}
    }

    /**
     * Lista os K produtos mais recentemente pedidos usando subPilha (Tarefa 3).
     */
    public static void listarProdutosPedidosRecentes() {

    	cabecalho();

    	if (pilhaProdutosRecentes.vazia()) {
    		System.out.println("Nenhum produto pedido recentemente.");
    		return;
    	}

    	int disponivel = pilhaProdutosRecentes.tamanho();
    	Integer k = lerOpcao(
    		"Quantos produtos recentes deseja visualizar? (disponível: " + disponivel + ")",
    		Integer.class);

    	if (k == null || k <= 0) {
    		System.out.println("Número inválido.");
    		return;
    	}
    	if (k > disponivel) {
    		System.out.println("Há apenas " + disponivel + " produto(s) disponível(is). Exibindo todos.");
    		k = disponivel;
    	}

    	Pilha<Produto> visualizacao = pilhaProdutosRecentes.subPilha(k);

    	System.out.println("\n=== " + k + " PRODUTO(S) MAIS RECENTEMENTE PEDIDO(S) ===");
    	int pos = 1;
    	while (!visualizacao.vazia()) {
    		System.out.println(pos++ + ". " + visualizacao.desempilhar());
    	}
    }

    /**
     * Salva todos os pedidos da pilhaPedidos em arquivo texto ao encerrar a aplicação.
     */
    static void salvarPedidos() {

    	// Esvazia a pilha para uma lista temporária
    	ArrayList<Pedido> lista = new ArrayList<>();
    	while (!pilhaPedidos.vazia()) {
    		lista.add(pilhaPedidos.desempilhar());
    	}
    	// Restaura a pilha (empilha em ordem inversa para manter o topo)
    	for (int i = lista.size() - 1; i >= 0; i--) {
    		pilhaPedidos.empilhar(lista.get(i));
    	}

    	try (PrintWriter writer = new PrintWriter(new FileWriter(nomeArquivoPedidos))) {
    		writer.println(lista.size());
    		for (Pedido p : lista) {
    			writer.print(p.gerarDadosTexto());
    		}
    		System.out.println(lista.size() + " pedido(s) salvo(s) em '" + nomeArquivoPedidos + "'.");
    	} catch (IOException e) {
    		System.out.println("Erro ao salvar pedidos: " + e.getMessage());
    	}
    }

    /**
     * Carrega pedidos do arquivo de persistência para pilhaPedidos e pilhaProdutosRecentes.
     */
    static void carregarPedidos() {

    	File arquivo = new File(nomeArquivoPedidos);
    	if (!arquivo.exists()) return;

    	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    	try (Scanner leitor = new Scanner(arquivo, Charset.forName("UTF-8"))) {
    		int numPedidos = Integer.parseInt(leitor.nextLine().trim());
    		ArrayList<Pedido> lista = new ArrayList<>();
    		int maxId = 0;

    		for (int i = 0; i < numPedidos; i++) {
    			String[] partesPedido = leitor.nextLine().trim().split(";");
    			int id = Integer.parseInt(partesPedido[0]);
    			LocalDate data = LocalDate.parse(partesPedido[1], fmt);
    			int forma = Integer.parseInt(partesPedido[2]);
    			int quantItens = Integer.parseInt(partesPedido[3]);

    			Pedido pedido = new Pedido(id, data, forma);

    			for (int j = 0; j < quantItens; j++) {
    				String[] partesItem = leitor.nextLine().trim().split(";");
    				int idProduto = Integer.parseInt(partesItem[0]);
    				int quantidade = Integer.parseInt(partesItem[1]);
    				double preco = Double.parseDouble(partesItem[2]);

    				Produto produto = null;
    				for (int k = 0; k < quantosProdutos; k++) {
    					if (produtosCadastrados[k].hashCode() == idProduto) {
    						produto = produtosCadastrados[k];
    						break;
    					}
    				}
    				if (produto != null) {
    					pedido.incluirProduto(produto, quantidade);
    				}
    			}
    			lista.add(pedido);
    			if (id > maxId) maxId = id;
    		}

    		// Empilha do mais antigo ao mais recente para que o mais recente fique no topo
    		for (int i = lista.size() - 1; i >= 0; i--) {
    			pilhaPedidos.empilhar(lista.get(i));
    		}
    		// Reconstrói pilhaProdutosRecentes na mesma ordem cronológica
    		for (int i = lista.size() - 1; i >= 0; i--) {
    			ItemDePedido[] itens = lista.get(i).getItensDoPedido();
    			for (int j = 0; j < lista.get(i).getQuantItensDoPedido(); j++) {
    				pilhaProdutosRecentes.empilhar(itens[j].getProduto());
    			}
    		}

    		Pedido.atualizarUltimoID(maxId + 1);
    		System.out.println(numPedidos + " pedido(s) carregado(s) de '" + nomeArquivoPedidos + "'.");

    	} catch (IOException e) {
    		System.out.println("Erro ao carregar pedidos: " + e.getMessage());
    	}
    }

	public static void main(String[] args) {

		teclado = new Scanner(System.in, Charset.forName("UTF-8"));

		nomeArquivoDados = "produtos.txt";
        produtosCadastrados = lerProdutos(nomeArquivoDados);

        carregarPedidos();

        // ===== TAREFA 1: Testes Preliminares da Pilha =====
        System.out.println("\n===== TAREFA 1: TESTES PRELIMINARES DA PILHA =====\n");

        // Dígitos do número de matrícula sem repetição
        // TODO: substitua pelos dígitos do seu número de matrícula
        int[] digitosMatricula = {2, 0, 2, 3, 1, 5, 4, 7, 6, 8};

        Pilha<Integer> pilhaMatricula = new Pilha<>();
        boolean[] digitosUsados = new boolean[10];

        System.out.println("Empilhando dígitos da matrícula (sem repetição):");
        for (int digito : digitosMatricula) {
            if (!digitosUsados[digito]) {
                pilhaMatricula.empilhar(digito);
                digitosUsados[digito] = true;
                System.out.println("  Empilhado: " + digito);
            } else {
                System.out.println("  Dígito " + digito + " já está na pilha — ignorado.");
            }
        }

        System.out.println("\nConteúdo da pilha após empilhamento:");
        System.out.println("  " + pilhaMatricula);

        System.out.println("\n-- Teste de desempilhar --");
        int desempilhado = pilhaMatricula.desempilhar();
        System.out.println("  Desempilhado: " + desempilhado);
        System.out.println("  Pilha após desempilhar: " + pilhaMatricula);

        System.out.println("\n-- Teste de empilhar de volta --");
        pilhaMatricula.empilhar(desempilhado);
        System.out.println("  Reempilhado: " + desempilhado);
        System.out.println("  Pilha final: " + pilhaMatricula);

        System.out.println("\n== Fim dos testes preliminares ==\n");
        pausa();

        Pedido pedido = null;

        int opcao = -1;

        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos();
                case 2 -> mostrarProduto(localizarProduto());
                case 3 -> mostrarProduto(localizarProdutoDescricao());
                case 4 -> pedido = iniciarPedido();
                case 5 -> { finalizarPedido(pedido); pedido = null; }
                case 6 -> listarProdutosPedidosRecentes();
            }
            if (opcao != 0) pausa();
        }while(opcao != 0);

        salvarPedidos();
        teclado.close();
    }
}
