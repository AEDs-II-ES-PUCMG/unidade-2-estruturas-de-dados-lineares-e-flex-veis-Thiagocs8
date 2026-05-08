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

    /** Pilha de pedidos finalizados (acesso LIFO — pedido mais recente no topo) */
    static Pilha<Pedido> pilhaPedidos = new Pilha<>();

    /** Pilha de produtos mais recentemente pedidos */
    static Pilha<Produto> pilhaProdutosRecentes = new Pilha<>();

    /** Fila de pedidos aguardando processamento (acesso FIFO — pedido mais antigo na frente) */
    static Fila<Pedido> filaPedidos = new Fila<>();

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

    /** Imprime o menu principal, lê a opção do usuário e a retorna (int). */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Procurar por um produto, por código");
        System.out.println("3 - Procurar por um produto, por nome");
        System.out.println("4 - Iniciar novo pedido");
        System.out.println("5 - Fechar pedido");
        System.out.println("6 - Listar produtos dos pedidos mais recentes (Pilha)");
        System.out.println("7 - Processar lote de pedidos da fila (Fila)");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        return Integer.parseInt(teclado.nextLine());
    }

    /**
     * Lê os dados de um arquivo-texto e retorna um vetor de produtos.
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

    /** Localiza um produto pelo código informado pelo usuário. Retorna null se não encontrado. */
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

    /** Localiza um produto pela descrição informada pelo usuário. Retorna null se não encontrado. */
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

    /** Inicia um novo pedido interativamente. */
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
     * Finaliza um pedido: imprime o resumo, empilha em pilhaPedidos,
     * enfileira em filaPedidos (Tarefa 2) e registra produtos em pilhaProdutosRecentes.
     */
    public static void finalizarPedido(Pedido pedido) {

    	if (pedido == null) {
    		System.out.println("Nenhum pedido em andamento. Inicie um pedido primeiro (opção 4).");
    		return;
    	}

    	System.out.println("Pedido finalizado com sucesso!");
    	System.out.println(pedido);

    	pilhaPedidos.empilhar(pedido);
    	filaPedidos.enfileirar(pedido);   // Tarefa 2 — enfileira para processamento

    	ItemDePedido[] itens = pedido.getItensDoPedido();
    	for (int i = 0; i < pedido.getQuantItensDoPedido(); i++) {
    		pilhaProdutosRecentes.empilhar(itens[i].getProduto());
    	}
    }

    /**
     * Lista os K produtos mais recentemente pedidos usando subPilha (Pilha — Tarefa 3 da unidade).
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
     * Extrai e exibe um lote de pedidos da filaPedidos usando extrairLote (Fila — Tarefa 3).
     */
    public static void listarLotePedidos() {

    	cabecalho();

    	if (filaPedidos.vazia()) {
    		System.out.println("A fila de pedidos está vazia — nenhum pedido aguardando processamento.");
    		return;
    	}

    	int disponivel = filaPedidos.tamanho();
    	Integer k = lerOpcao(
    		"Quantos pedidos deseja processar da fila? (aguardando: " + disponivel + ")",
    		Integer.class);

    	if (k == null || k <= 0) {
    		System.out.println("Número inválido.");
    		return;
    	}

    	int aProcessar = Math.min(k, disponivel);
    	Fila<Pedido> lote = filaPedidos.extrairLote(k);

    	System.out.println("\n=== LOTE DE " + aProcessar + " PEDIDO(S) EXTRAÍDO(S) DA FILA ===");
    	int pos = 1;
    	while (!lote.vazia()) {
    		Pedido p = lote.desenfileirar();
    		System.out.println("\n--- Pedido " + pos++ + " ---");
    		System.out.println(p);
    	}
    	System.out.println("\nPedidos restantes na fila: " + filaPedidos.tamanho());
    }

    /**
     * Salva todos os pedidos da pilhaPedidos em arquivo ao encerrar a aplicação.
     */
    static void salvarPedidos() {

    	ArrayList<Pedido> lista = new ArrayList<>();
    	while (!pilhaPedidos.vazia()) {
    		lista.add(pilhaPedidos.desempilhar());
    	}
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
     * Carrega pedidos do arquivo e reconstrói pilhaPedidos, filaPedidos e pilhaProdutosRecentes.
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

    		// Reconstrói pilhaPedidos: mais recente no topo (lista[0] = mais recente)
    		for (int i = lista.size() - 1; i >= 0; i--) {
    			pilhaPedidos.empilhar(lista.get(i));
    		}
    		// Reconstrói filaPedidos: mais antigo na frente (lista[N-1] = mais antigo)
    		for (int i = lista.size() - 1; i >= 0; i--) {
    			filaPedidos.enfileirar(lista.get(i));
    		}
    		// Reconstrói pilhaProdutosRecentes
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

        // ===== TAREFA 1 (PILHA): Testes Preliminares da Pilha =====
        System.out.println("\n===== TAREFA 1 (PILHA): TESTES PRELIMINARES =====\n");

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

        System.out.println("\n== Fim dos testes da Pilha ==");
        pausa();

        // ===== TAREFA 1 (FILA): Testes Preliminares da Fila =====
        System.out.println("\n===== TAREFA 1 (FILA): TESTES PRELIMINARES =====\n");

        // TODO: substitua pelo seu primeiro e segundo nome
        String primeiroNome = "Thiago";
        String segundoNome  = "Costa";

        Fila<Character> filaChars = new Fila<>();

        System.out.println("Enfileirando caracteres do primeiro nome: \"" + primeiroNome + "\"");
        for (char c : primeiroNome.toCharArray()) {
            filaChars.enfileirar(c);
            System.out.println("  Enfileirado: '" + c + "'");
        }

        System.out.println("Enfileirando caracteres do segundo nome: \"" + segundoNome + "\"");
        for (char c : segundoNome.toCharArray()) {
            filaChars.enfileirar(c);
            System.out.println("  Enfileirado: '" + c + "'");
        }

        System.out.println("\nConteúdo da fila:");
        System.out.println("  " + filaChars);
        System.out.println("  Tamanho: " + filaChars.tamanho());

        System.out.println("\n-- Teste de contarOcorrencias --");
        char[] charsParaContar = {'a', 'o', 'T', 'z'};
        for (char c : charsParaContar) {
            System.out.println("  Ocorrências de '" + c + "': " + filaChars.contarOcorrencias(c));
        }

        System.out.println("\n-- Teste de desenfileirar --");
        char desenfileirado = filaChars.desenfileirar();
        System.out.println("  Desenfileirado: '" + desenfileirado + "'");
        System.out.println("  Fila após desenfileirar: " + filaChars);

        System.out.println("\n-- Teste de enfileirar de volta --");
        filaChars.enfileirar(desenfileirado);
        System.out.println("  Reenfileirado: '" + desenfileirado + "'");
        System.out.println("  Fila final: " + filaChars);

        System.out.println("\n== Fim dos testes da Fila ==\n");
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
                case 7 -> listarLotePedidos();
            }
            if (opcao != 0) pausa();
        }while(opcao != 0);

        salvarPedidos();
        teclado.close();
    }
}
