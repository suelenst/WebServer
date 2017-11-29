package webserver;


public class CampoForm {
    
    private String nomeCampo;
    private String conteudoCampo;
    
    public CampoForm() {
    }
    
    public CampoForm(String nomeCampo, String conteudoCampo) {
        this.nomeCampo = nomeCampo;
        this.conteudoCampo = conteudoCampo;
    }

    public String getNomeCampo() {
        return nomeCampo;
    }
    public void setNomeCampo(String nomeCampo) {
        this.nomeCampo = nomeCampo;
    }
    public String getConteudoCampo() {
        return conteudoCampo;
    }
    public void setConteudoCampo(String conteudoCampo) {
        this.conteudoCampo = conteudoCampo;
    }
    
    
    
    
}
