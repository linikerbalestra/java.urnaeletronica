import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Client implements Runnable {
	
	private Socket socket;
	
	private BufferedReader in;
	private PrintStream out;
	
	private boolean inicializado;
	private boolean executando;
	
	private static boolean flag;
	
	private static List<Candidato> candidatos;
	
	private static int brancos = 0;
	private static int nulos = 0;
	
	private Thread thread;
	
	public Client(String host, int port) throws Exception{
		
		candidatos = new ArrayList<Candidato>();
		
		inicializado = false;
		executando = false;
		
		open(host,port);
		
	}
	
	private void open(String host, int port) throws Exception{
		
		try{
			socket = new Socket(host,port);
		
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
		
			inicializado = true;
			
			flag = false;
		}
		catch (Exception e) {
			System.out.println("Não foi possível se conectar ao servidor");
			System.out.println(e);
			close();
			throw e;
		}
	}
	
	private void close(){
		if(in!=null){
			try{
				in.close();
			}
			catch (Exception e){
				System.out.println(e);
			}
			
			if(out!=null){
				try{
					out.close();
				}
				catch (Exception e){
					System.out.println(e);
				}
			}
			
			if(socket!=null){
				try{
					socket.close();
				}
				catch (Exception e){
					System.out.println(e);
				}
			}
			
			in = null;
			out = null;
			
			socket = null;
			
			inicializado = false;
			executando = false;
			
			thread = null;
			
		}
		
	}
	
	public void start(){
		if(!inicializado || executando){
			return;
		}
		
		executando = true;
		thread = new Thread(this);
		thread.start();
		
	}
	
	public void stop() throws Exception{
		executando = false;
		
		if(thread != null){
			thread.join();
		}
	}
	
	public boolean isExecutando(){
		return executando;
	}
	
	public void send(String mensagem){
		out.println(mensagem);
	}
	
	@Override
	public void run() {
		while(executando){
			try{
				socket.setSoTimeout(2500);
				
				String mensagem = in.readLine();
				
				if(mensagem == null){
					break;
				}else{
					
					List<String> lista = new ArrayList<String>(Arrays.asList(mensagem.split(",")));
					
					for(String linha : lista){
		
							List<String> campo = new ArrayList<String>(Arrays.asList(linha.split(";")));

							Candidato c = new Candidato();
							
							if(!campo.get(0).equals(null) && !campo.get(0).equals("")){
								
								c.setCódigo_votacao(campo.get(0));
								c.setNome_candidato(campo.get(1));
								c.setPartido(campo.get(2));
							
								candidatos.add(c);
							}
					}
				}	
				
			}
			catch(SocketTimeoutException e){
				
			}
			catch(Exception e){
				System.out.println(e);
				break;
			}
		}
		close();
	}
	
	
	public static void main(String[] args) throws Exception{
		
	int i = 0;
		
	System.out.println("Iniciando cliente ...");
	
	System.out.println("Iniciando conexão com servidor ...");
	
	 Client client = new Client("localhost",5050);
	 client.start();
	 
	 System.out.println("Conexão estabelecida com sucesso ...");
	 
	 
	 Scanner scanner = new Scanner(System.in);
	 
	   
	 while(true) {
	    	 
	   	 System.out.println("Escolha sua opcao:");
		     System.out.println("1 - Votar");
		     System.out.println("2 - Votar branco");
		     System.out.println("3 - Votar nulo");
		     System.out.println("4 - Carregar candidatos");
		     System.out.println("5 - Finalizar as votacoes da urna e enviar ao servidor");
		     System.out.println("6 - Sair");
		     System.out.println("****************************************");
	     
		 
		 System.out.print("Digite uma opcão:");
		 String mensagem = scanner.nextLine();
		 System.out.println("****************************************");
		 
		 if(!client.isExecutando()){
			 break;
		 }
		 
		 if(("4".equals(mensagem)) && flag == false){
			 flag = true;
			 client.send("999");
			 System.out.println("Lista de candidatos carregada com sucesso");
			 System.out.println("##########################################");
			 
		 }else if(("4".equals(mensagem)) && flag == true){
			 
			 System.out.println("Lista já carregada");
			 System.out.println("****************************************");
		 }else{
			 
		 }
		 
		 if ("6".equals(mensagem)){
			 client.send("FIM");
			 break;
		 }
		 
		 if(flag == true){
			 
			 if(("1".equals(mensagem))){
				 
				 System.out.println("##############################################");
				 System.out.println("Lista de candidatos participantes da eleicão");
				 System.out.println("##############################################");
				 for(Candidato candidato : candidatos){
					 System.out.println("Número para votacão : " +candidato.getCódigo_votacao() + "\n" 
							+ "Nome: "+ candidato.getNome_candidato() + "\n" 
							+ "Partido:" +candidato.getPartido() +"\n"+"*********************");
				 }
				 System.out.print("Digite uma opcão:");
				 mensagem = scanner.nextLine();
				 
				 for(Candidato candidato : candidatos){
					 if(candidato.getCódigo_votacao().equals(mensagem)){
						 candidato.setNum_votos(candidato.getNum_votos() +1);
						 System.out.println("Voto computado com sucesso para o cadidato :" + candidato.getNome_candidato());
						 System.out.println("##############################################");
						 
					 }
				 }
				 
			 	}
		
			 if(("2".equals(mensagem))){
				 brancos+=1;
				 System.out.println("Voto computado com sucesso");
				 System.out.println("****************************************");
				 
			 }
		 
			 if(("3".equals(mensagem))){
				 nulos+=1;
				 System.out.println("Voto computado com sucesso");
				 System.out.println("****************************************");
			 }
			 
			 if(("5".equals(mensagem))){
				 
				 String candidatosvoto = "";
				 String qtdbrancos = "";
				 String qtdnulos = "";
				 
				 for(Candidato candidato : candidatos){
					 
					 candidatosvoto = ("candidatos;"+candidato.getCódigo_votacao()+";"+candidato.getNum_votos());
					 client.send(candidatosvoto);
				}
				 
				 qtdbrancos = Integer.toString(brancos);
				 qtdnulos = Integer.toString(nulos);
				 
				 client.send("brancos;"+qtdbrancos);
				 client.send("nulos;"+qtdnulos);
				 
				 
				 System.out.println("Votos computados com sucessos");
				 System.out.println("****************************************");
				 System.out.println("Encerrando conexão ...");
				 
				client.stop();
				client.close();
				break;

			 }
		 
		 }else{
			 System.out.println("A lista de candidatos não foi carregada...");
			 System.out.println("****************************************");
		 }
		 
	 }
	 
	 System.out.println("Encerrando conexão...");
	 
	 client.stop();
				 	 
		 
}

}
	 
	 



