package webserver;

import java.io.* ;
import java.net.* ;
import java.util.* ;

final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";

    Socket socket;
    
    // Construtor
    public HttpRequest(Socket socket) throws Exception {
	this.socket = socket;
    }
    
    // Implemente o método run() da interface Runnable.
    public void run() {
	try {
	    processRequest();
	} catch (Exception e) {
	    System.out.println(e);
	}
    }

    private void processRequest() throws Exception {
	// Obter uma referencia para os trechos de entrada e saida do socket.
	InputStream is = socket.getInputStream();
	DataOutputStream os = new DataOutputStream(socket.getOutputStream());
	
	// Ajustar os filtros do trecho de entrada.
	BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Obter a linha de requisicao da mensagem de requisicao HTTP.
        String requestLine = br.readLine();
        
        
        // Extrair o nome do arquivo a linha de requisicao.
        StringTokenizer tokens = new StringTokenizer(requestLine);
        
        String tipo = tokens.nextToken();  // pular o método, que deve ser “GET”
        
            
        boolean fileExists = true ;
        
        String fileName = "";
        
        // Abrir o arquivo requisitado.
        FileInputStream fis = null ;
        
        // Debug info for private use
        
        System.out.println("\n\nREQUISICAO: ");
        System.out.println("============ \n");
        // Exibir a linha de requisicao.
        System.out.println(requestLine);
        // Obter e exibir as linhas de cabecalho.
        String headerLine = null;
        int contentLength = 0;
        
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);            
            
            if (headerLine.startsWith("Content-Length")){
                contentLength = getLength(headerLine);
            }
        }
        
        if (tipo.equalsIgnoreCase("GET")){
  
            fileName = tokens.nextToken();

            // Acrescente um “.” de modo que a requisicao do arquivo esteja dentro do diretorio atual.
            fileName = "." + fileName ;

            try {
                fis = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                fileExists = false ;
            }

        } else if (tipo.equalsIgnoreCase("POST")){        
        
            char[] postData = new char[contentLength];
            br.read(postData, 0, contentLength);
            String post = new String(postData, 0, contentLength);
            
            MostraCampos(SeparaCampos(post));
                              
        }
        
        // Construir a mensagem de resposta.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        
        if (tipo.equalsIgnoreCase("GET")){          
            
            if (fileExists) {
                statusLine = "HTTP/1.0 200 OK" + CRLF;
                contentTypeLine = "Content-Type: " + 
                    contentType(fileName) + CRLF;

            } else {
                statusLine = "HTTP/1.0 404 Not Found" + CRLF;
                contentTypeLine = "Content-Type: text/html" + CRLF;
                entityBody = "<HTML>" + 
                    "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                    "<BODY>Not Found</BODY></HTML>";
            }
            
        } else  if (tipo.equalsIgnoreCase("POST")){
            
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML>" + 
                "<HEAD><TITLE>Enviado</TITLE></HEAD>" +
                "<BODY>Enviado com sucesso ao servidor</BODY></HTML>";  
 
            
        } else {
            statusLine = "HTTP/1.0 400 Bad request" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML>" + 
                "<HEAD><TITLE>Bad request</TITLE></HEAD>" +
                "<BODY>Bad request</BODY></HTML>";
        }
        
        
        // Enviar a linha de status.
        os.writeBytes(statusLine);

        // Enviar a linha de tipo de conteudo.
        os.writeBytes(contentTypeLine);

        // Enviar uma linha em branco para indicar o fim das linhas de cabecalho.
        os.writeBytes(CRLF);

        // Enviar o corpo da entidade.
        if (tipo.equalsIgnoreCase("GET") && fileExists){


            sendBytes(fis, os);
            fis.close();
        } else {
            os.writeBytes(entityBody) ;
        }
        
        

        // Fecha as cadeias e socket.
        os.close();
        br.close();
        socket.close();
    }

    private static void sendBytes(FileInputStream fis,  OutputStream os) throws Exception {
	// Construir um buffer de 1K para comportar os bytes no caminho para o socket.
	byte[] buffer = new byte[1024];
	int bytes = 0;
	
	// Copiar o arquivo requisitado dentro da cadeia de saida do socket.
	while ((bytes = fis.read(buffer)) != -1) {
	    os.write(buffer, 0, bytes);
	}
    }

    private static String contentType(String fileName) {
	if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
	    return "text/html";
	}
	if(fileName.endsWith(".ram") || fileName.endsWith(".ra")) {
	    return "audio/x-pn-realaudio";
	}
        
        // suporte a arquivos de imagem .jpg
        if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
	    return "image/jpeg";
	}
        // suporte a arquivos .gif
        if(fileName.endsWith(".gif ")) {
	    return "image/gif";
	}
              
	return "application/octet-stream" ;
    }
    

    private int getLength(String length) {
        StringTokenizer tok = new StringTokenizer(length);
        tok.nextToken();
        return(Integer.parseInt(tok.nextToken()));
    }
    
    
    private static ArrayList SeparaCampos(String post) {
        String split1[] = post.split("&");

        ArrayList<CampoForm> campos = new ArrayList();

        for (int i = 0; i < split1.length; i ++){
            String split2[] = split1[i].split("=");
            campos.add(new CampoForm(split2[0].replace('+', ' '), split2[1].replace('+', ' ')));
        }
        
        return campos;

    }
    
        
    private static void MostraCampos(ArrayList<CampoForm> campos) {
        System.out.println("\n\nCampos Formulario");
        System.out.println("====================================");
        System.out.println("Campo:                      Conteudo:");

        for (int i = 0; i < campos.size(); i ++){
            System.out.print(campos.get(i).getNomeCampo() + "                    ");
            System.out.println(campos.get(i).getConteudoCampo());

        }
        System.out.println("====================================\n\n");
    }
    
    
}
