package br.ufc.quixada.leitor.arquivo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import br.ufc.quixada.classificador.Classifier;

public class LeitorArquivo {
	private static String caminhoArquivo = "/home/rayner/Documentos/tcc/comentariosNotasRanking2.txt";

	@SuppressWarnings("resource")
	public void lerArquivos(String caminho) throws Exception {
		 Classifier classifier = new Classifier();
		 

		// Path caminhoTexto = Paths.get(caminho);
		// byte[] texto = Files.readAllBytes(caminhoTexto);
		// String leitor = new String(texto);
		//
		// System.out.println(leitor);
		System.out.println("----------------------------");
		FileInputStream arquivo = new FileInputStream(caminho);
		InputStreamReader entrada = new InputStreamReader(arquivo);
		BufferedReader reader = new BufferedReader(entrada);
		String linha;
		do {
			linha = reader.readLine();
			if (linha != null) {
				String[] comentario = linha.split("((\\w(.*\\[)|(\\].*\\w)|(\\])))");
				for (int i = 0; i < comentario.length; i++) {
					classifier.classify(comentario[i]);
				}
			}
		} while (linha != null);

		// vou ler o arquivo de comentários, então, vou pegar cada linha, e
		// vou chamar o método de classificação
		// então, eu mando salvar o comentário e a sua classificação, em um
		// arquivo.

	}

	public static void main(String[] args) throws Exception {

		LeitorArquivo arquivo = new LeitorArquivo();

		arquivo.lerArquivos(caminhoArquivo);
	}
	// public void escreverArquivo(String caminhoArquivoSaida){
	// File arquivo = new File("resultado.txt");
	// Path caminho = Paths.get(caminhoArquivoSaida);
	//
	//
	//
	// }

}
