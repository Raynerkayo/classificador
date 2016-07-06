package br.ufc.quixada.escritor.arquivo;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EscreverArquivo {
	//FALTA COLOCAR O NOME DO ARQUIVO
	private static String caminhoArquivo = "/home/rayner/Documentos/tcc/";

	public void escrever(String classificacao, String conteudoArquivo) throws IOException{
		
		Path path = Paths.get(caminhoArquivo);
		Charset utf8 = StandardCharsets.UTF_8;
		
		BufferedWriter w = null;
		
		try{
			w = Files.newBufferedWriter(path, utf8);
			String conteudo = "[ (" + classificacao + ") \t -->\t (" + conteudoArquivo + ")]";
			System.out.println(conteudo);
			w.write(conteudo);
			w.flush();
			w.close();
		}catch(IOException erro){
			erro.printStackTrace();
		}finally {
			if(w != null){
				w.close();
			}
		}
		
	}
	
}
