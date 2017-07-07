import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements Runnable {
	
	private ServerSocket server;
	
	private List<Atendente> atendentes;
	
	private boolean inicializado; //atributo boleano para sabermos se o server já foi inicializado
	private boolean executando;   //atributo para sabermos se o server está em execucao ou não
	
	private Thread thread;//thread para controlar o recebimento de conexões
	
	public Server(int porta) throws Exception{
		
		atendentes = new ArrayList<Atendente>();
		
		this.inicializado = false;
		this.executando = false;
		
		open(porta); //vamos delegar o servico de inicializar o server para o método open
	}
	
	private void open(int porta) throws Exception{
		
		System.out.println(porta);
		server = new ServerSocket(porta);
		inicializado = true;
	}
	
	private void close(){
		
		for(Atendente atendente : atendentes){
			try{
				atendente.stop();
			}
			catch (Exception e){
				System.out.println(e);
			}
		}
		
		try{
			server.close();
		}
		catch (Exception e){
			System.out.println(e);
		}
		
		server = null;
		inicializado = false;
		executando = false;
		
		thread = null;
	}
	//método para iniciar o servidor/thread auxiliar do servidor
	public void start(){
		
		if (!inicializado || executando){ //verifica se o server já foi inicializado ou está em execucão
			return;
		}
		
		executando = true;
		thread = new Thread(this);//objeto passado como parametro da runnable
		thread.start(); //faz com que a thread executa o método run
	}
	
	public void stop() throws Exception{
		executando = false;
		
		if(thread!=null){
			thread.join();
		}
	}
		
	
	@Override
	public void run() {
		while(executando){
			try{
				server.setSoTimeout(2500);
				
				Socket socket = server.accept();
				
				System.out.println("Conexão estabelecida");
				
				Atendente atendente = new Atendente(socket);
				atendente.start();
				
				atendentes.add(atendente);
				  
				
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
		
		 System.out.println("Iniciando servidor");
		 
		 Server server = new Server(40004);  // criando o objeto serversocket 
		 server.start();
		 
		 System.out.println("Pressione Enter para encerrar o servidor");
		 new Scanner(System.in).nextLine();
		 
		 
		 System.out.println("Encerrando servidor");
		 
		 server.stop();		 
		 
		 
	}



}
