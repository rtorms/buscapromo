package utfpr.edu.br.buscapromo.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class UsuarioLogado {

    private Context context;
    private SharedPreferences preferences;
    private String NOME_ARQUIVO = "app.preference";
    private int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String EMAIL_USUARIO_LOGADO = "email_usuario_logado";
    private final String SENHA_USUARIO_LOGADO = "senha_usuario_logado";

    public UsuarioLogado(Context contextParametro){
        context = contextParametro;
        preferences = context.getSharedPreferences(NOME_ARQUIVO, MODE);

        editor = preferences.edit();
    }

    public void salvarUsuarioPreferencias(String email, String senha){
        editor.putString(EMAIL_USUARIO_LOGADO, email);
        editor.putString(SENHA_USUARIO_LOGADO, senha);
        editor.commit();
    }

    public String getEmail_Usuario_Logado(){
        return preferences.getString(EMAIL_USUARIO_LOGADO, null);
    }

    public String getSenha_Usuario_Logado(){
        return preferences.getString(SENHA_USUARIO_LOGADO, null);
    }

}
