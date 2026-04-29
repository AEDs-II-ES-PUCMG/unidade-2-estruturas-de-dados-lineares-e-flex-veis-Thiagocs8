import java.util.NoSuchElementException;

public class Pilha<E> {

	private Celula<E> topo;
	private Celula<E> fundo;

	public Pilha() {

		Celula<E> sentinela = new Celula<E>();
		fundo = sentinela;
		topo = sentinela;

	}

	public boolean vazia() {
		return fundo == topo;
	}

	public void empilhar(E item) {

		topo = new Celula<E>(item, topo);
	}

	public E desempilhar() {

		E desempilhado = consultarTopo();
		topo = topo.getProximo();
		return desempilhado;

	}

	public E consultarTopo() {

		if (vazia()) {
			throw new NoSuchElementException("Nao há nenhum item na pilha!");
		}

		return topo.getItem();

	}

	/** Retorna a quantidade de elementos na pilha. */
	public int tamanho() {
		int count = 0;
		Celula<E> atual = topo;
		while (atual != fundo) {
			count++;
			atual = atual.getProximo();
		}
		return count;
	}

	@Override
	public String toString() {
		if (vazia()) return "[Pilha vazia]";
		StringBuilder sb = new StringBuilder("[TOPO] ");
		Celula<E> atual = topo;
		while (atual != fundo) {
			sb.append(atual.getItem());
			atual = atual.getProximo();
			if (atual != fundo) sb.append(" -> ");
		}
		sb.append(" [FUNDO]");
		return sb.toString();
	}

	/**
	 * Cria e devolve uma nova pilha contendo os primeiros numItens elementos
	 * do topo da pilha atual.
	 *
	 * Os elementos são mantidos na mesma ordem em que estavam na pilha original.
	 * Caso a pilha atual possua menos elementos do que o valor especificado,
	 * uma exceção será lançada.
	 *
	 * @param numItens o número de itens a serem copiados da pilha original.
	 * @return uma nova instância de Pilha<E> contendo os numItens primeiros elementos.
	 * @throws IllegalArgumentException se a pilha não contém numItens elementos.
	 */
	@SuppressWarnings("unchecked")
	public Pilha<E> subPilha(int numItens) {

		if (numItens <= 0 || numItens > tamanho()) {
			throw new IllegalArgumentException(
				"A pilha não contém " + numItens + " elementos. Tamanho atual: " + tamanho());
		}

		E[] temp = (E[]) new Object[numItens];

		// Desempilha os numItens elementos do topo para um array temporário
		for (int i = 0; i < numItens; i++) {
			temp[i] = desempilhar();
		}

		// Restaura a pilha original (empilha do último para o primeiro)
		for (int i = numItens - 1; i >= 0; i--) {
			empilhar(temp[i]);
		}

		// Constrói a sub-pilha com a mesma ordem (temp[0] volta ao topo)
		Pilha<E> subPilha = new Pilha<>();
		for (int i = numItens - 1; i >= 0; i--) {
			subPilha.empilhar(temp[i]);
		}

		return subPilha;
	}
}