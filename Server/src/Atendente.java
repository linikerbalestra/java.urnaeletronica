import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Atendente implements Runnable{
	
	private Socket socket;
	
	private BufferedReader in;
	private PrintStream out;
	
	private List<Candidato> candidatos;
	
	private Candidato cadidat01 = new Candidato("1","Lulalá", "PT");
	private Candidato cadidat02 = new Candidato("2","Bolsonada", "PSDB");
	private Candidato cadidat03 = new Candidato("3","Dilmãe", "PT");
	private Candidato cadidat04 = new Candidato("4","Marina", "PSOL");
	
	private int nulos = 0;
	private int brancos = 0;
	
	
	
	private boolean inicializado;
	private boolean executando;
	
	private Thread thread;
	
	public Atendente(Socket socket) throws Exception{
		
		candidatos = new ArrayList<Candidato>();
		
		candidatos.add(cadidat01);
		candidatos.add(cadidat02);
		candidatos.add(cadidat03);
		candidatos.add(cadidat04);
		
		this.socket = socket;
		
		this.inicializado = false;
		this.executando = false;
		
		open();
		
	}
	
	
	private void open() throws Exception{
		try{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintStream(socket.getOutputStream());
			
			inicializado = true;
		}
		catch (Exception e){
			close();
			throw e;
		}
	}
	
	private void close() throws Exception{
		if( in != null){
			try{
				in.close();
			}
			catch(Exception e){
				System.out.println();
			}
		}
		
		if(out != null){
			try{
				out.close();
			}
			catch (Exception e){
				System.out.println(e);
			}
		}  
		
		try{
			socket.close();
		}
		catch (Exception e){
			System.out.print(e);
		}
		
		in = null;
		out = null;
		socket = null;
		
		this.inicializado = false;
		this.executando = false;
		
		thread = null;
		
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
		if(thread!=null){
			thread.join();
		}
		
	}
	
	

	@Override
	public void run() {
		
		
		while(executando){
			try{
				socket.setSoTimeout(2500);
				
				String mensagem = in.readLine();
				String envio = "";
				
				if("999".equals(mensagem)){
					for(Candidato candidato : candidatos){
						envio = (candidato.getCódigo_votacao()+";"+
						candidato.getNome_candidato()+";"+
						candidato.getPartido()+";");
						out.println(envio);
					}
				}else if("FIM".equals(mensagem)){
					break;
				}else{
					
					List<String> lista = new ArrayList<String>(Arrays.asList(mensagem.split(";")));
							
							if("candidatos".equals(lista.get(0))){
								String codigo = lista.get(1);
								int voto = Integer.parseInt(lista.get(2));
								
								for(Candidato candidato : candidatos){
									if(candidato.getCódigo_votacao().equals(codigo)){
										candidato.setNum_votos(candidato.getNum_votos()+voto);
										System.out.println("Candidato:" + candidato.getNome_candidato() + 
												" ---- " +" Votos computados:" + candidato.getNum_votos());
										System.out.println("****************************************");
									}
									
								}
							}
							
							if("brancos".equals(lista.get(0))){
								brancos+= Integer.parseInt(lista.get(1));
								System.out.println("Votos brancos computados:" + brancos);
								System.out.println("****************************************");
							}
							
							if("nulos".equals(lista.get(0))){
								nulos += Integer.parseInt(lista.get(1));
								System.out.println("Votos nulos computados:" + nulos);
								System.out.println("****************************************");
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
		
		System.out.println("Encerrando conexão ...");
		
		try {
			close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
