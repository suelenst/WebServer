package webserver;

import java.io.* ;
import java.net.* ;
import java.util.* ;

public class WebServer {

    public static void main(String argv[]) throws Exception {        	
	// Ajustar o número da porta.
        int port = 6789;
	
	// Estabelecer o socket de escuta.
	ServerSocket socket = new ServerSocket(port);
	
	// Processar a requisicao de serviço HTTP em um laço infinito.
	while (true) {
   	    // Escutar requisicao de conexao TCP.
	    Socket connection = socket.accept();
	    
	    //Construir um objeto para processar a mensagem de requisicao HTTP.
	    HttpRequest request = new HttpRequest(connection);
	    
	    // Criar uma nova thread para processar a requisicao.
	    Thread thread = new Thread(request);
	    
	    //Iniciar a thread.
	    thread.start();
	}
    }
    
}
