import java.util.NoSuchElementException;

public class Fila<E> {

	// Sentinela: frente aponta para o nó sentinela; tras aponta para o último nó real
	private Celula<E> frente;
	private Celula<E> tras;

	public Fila() {
		Celula<E> sentinela = new Celula<E>();
		frente = sentinela;
		tras = sentinela;
	}

	public boolean vazia() {
		return frente == tras;
	}

	/** Insere um elemento no final da fila. */
	public void enfileirar(E item) {
		Celula<E> novaCelula = new Celula<E>(item);
		tras.setProximo(novaCelula);
		tras = novaCelula;
	}

	/** Remove e retorna o elemento da frente da fila. */
	public E desenfileirar() {
		E desenfileirado = consultarFrente();
		frente = frente.getProximo();
		return desenfileirado;
	}

	/** Retorna o elemento da frente sem removê-lo. */
	public E consultarFrente() {
		if (vazia()) {
			throw new NoSuchElementException("Não há nenhum item na fila!");
		}
		return frente.getProximo().getItem();
	}

	/** Retorna a quantidade de elementos na fila. */
	public int tamanho() {
		int count = 0;
		Celula<E> atual = frente.getProximo();
		while (atual != null) {
			count++;
			atual = atual.getProximo();
		}
		return count;
	}

	@Override
	public String toString() {
		if (vazia()) return "[Fila vazia]";
		StringBuilder sb = new StringBuilder("[FRENTE] ");
		Celula<E> atual = frente.getProximo();
		while (atual != null) {
			sb.append(atual.getItem());
			if (atual.getProximo() != null) sb.append(" -> ");
			atual = atual.getProximo();
		}
		sb.append(" [TRAS]");
		return sb.toString();
	}

	/**
	 * Conta quantas vezes um elemento aparece na fila sem removê-los.
	 * @param elemento O elemento a ser procurado (pode ser null).
	 * @return Número de ocorrências do elemento na fila.
	 */
	public int contarOcorrencias(E elemento) {
		int count = 0;
		Celula<E> atual = frente.getProximo();
		while (atual != null) {
			if (elemento == null ? atual.getItem() == null : elemento.equals(atual.getItem())) {
				count++;
			}
			atual = atual.getProximo();
		}
		return count;
	}

	/**
	 * Desenfileira os primeiros numItens elementos da fila atual, respeitando a ordem
	 * de chegada, e os retorna em uma nova Fila. Caso a fila possua menos de numItens
	 * itens, todos os disponíveis são extraídos, esvaziando a fila de origem.
	 *
	 * @param numItens Quantidade de elementos a extrair.
	 * @return Nova fila com os elementos extraídos.
	 */
	public Fila<E> extrairLote(int numItens) {
		Fila<E> lote = new Fila<>();
		int extraidos = 0;
		while (!vazia() && extraidos < numItens) {
			lote.enfileirar(desenfileirar());
			extraidos++;
		}
		return lote;
	}
}
