package webserver;

import java.io.* ;
import java.net.* ;
import java.util.* ;

final class HttpRequest implements Runnable {
    final static String CRLF = "\r\n";
    protected int maxRequestLines = 50;
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
        
        if (tipo.equalsIgnoreCase("GET")){
  
            fileName = tokens.nextToken();

            // Acrescente um “.” de modo que a requisicao do arquivo esteja dentro do diretorio atual.
            fileName = "." + fileName ;

            try {
                fis = new FileInputStream(fileName);
            } catch (FileNotFoundException e) {
                fileExists = false ;
            }

            // Debug info for private use
            System.out.println("Incoming!!!");
            // Exibir a linha de requisicao.
            System.out.println(requestLine);
            // Obter e exibir as linhas de cabecalho.
            String headerLine = null;
            while ((headerLine = br.readLine()).length() != 0) {
                System.out.println(headerLine);
            }
        

        } else if (tipo.equalsIgnoreCase("POST")){        
        
            String[] inputLines = new String[maxRequestLines];
            int i;

            for (i=0; i<maxRequestLines; i++) {
                inputLines[i] = br.readLine();
                if (inputLines[i] == null) // Conexão fechada pelo cliente.
                    break;
                if (inputLines[i].length() == 0) { // Linha em branco
                        readPostData(inputLines, i, br);
                        i = i + 2;
                    
                        break;
                }
            }
            
            System.out.println("\nCONTEUDO DO POST \n\n");
            
            for (int j=0; j<i; j++) {
                System.out.println(inputLines[j]);
            }
            
            fileExists = false ;


          
        } else {
            fileExists = false ;
        }
        
        // Construir a mensagem de resposta.
        String statusLine = null;
        String contentTypeLine = null;
        String entityBody = null;
        if (fileExists) {
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-Type: " + 
                contentType(fileName) + CRLF;
            
        } else if (tipo.equalsIgnoreCase("POST")){
            statusLine = "HTTP/1.0 200 OK" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML>" + 
                "<HEAD><TITLE>Enviado</TITLE></HEAD>" +
                "<BODY>Enviado com sucesso ao servidor</BODY></HTML>";                       
        } else {
            statusLine = "HTTP/1.0 404 Not Found" + CRLF;
            contentTypeLine = "Content-Type: text/html" + CRLF;
            entityBody = "<HTML>" + 
                "<HEAD><TITLE>Not Found</TITLE></HEAD>" +
                "<BODY>Not Found</BODY></HTML>";
        }
        // Enviar a linha de status.
        os.writeBytes(statusLine);

        // Enviar a linha de tipo de conteudo.
        os.writeBytes(contentTypeLine);

        // Enviar uma linha em branco para indicar o fim das linhas de cabecalho.
        os.writeBytes(CRLF);

        // Enviar o corpo da entidade.
        if (fileExists) {
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
    
    private void readPostData(String[] inputs, int i, BufferedReader in) throws IOException {
        int contentLength = contentLength(inputs);
        char[] postData = new char[contentLength];
        in.read(postData, 0, contentLength);
        inputs[++i] = new String(postData, 0, contentLength);
    }

      // Dada uma linha que começa com Content-Length,
     // isso retorna o valor inteiro especificado.

    private int contentLength(String[] inputs) {
        String input;
        for (int i=0; i<inputs.length; i++) {
            if (inputs[i].length() == 0)
                break;
            input = inputs[i].toUpperCase();
            if (input.startsWith("CONTENT-LENGTH"))
                return(getLength(input));
        }
        return(0);
    }

    private int getLength(String length) {
        StringTokenizer tok = new StringTokenizer(length);
        tok.nextToken();
        return(Integer.parseInt(tok.nextToken()));
    }
    
    
}
