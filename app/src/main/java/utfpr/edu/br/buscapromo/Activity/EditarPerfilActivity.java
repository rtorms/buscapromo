package utfpr.edu.br.buscapromo.Activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import utfpr.edu.br.buscapromo.DAO.ConfiguracaoFirebase;
import utfpr.edu.br.buscapromo.Helper.UsuarioLogado;
import utfpr.edu.br.buscapromo.R;

public class EditarPerfilActivity extends AppCompatActivity {


    private Button btnSalvar;
    private Button btnCancelar;
    private CheckBox checkboxAlteraNomeSenha;
    private ImageView imgFotoPerfil;
    private String emailUsuario;
    private EditText newNomeUsuario;
    private EditText oldNomeUsuario;
    private EditText senhaUsuario1;
    private EditText senhaUsuario2;
    private StorageReference storageReference;
    private FirebaseDatabase database;
    private FirebaseAuth autenticacao;
    private DatabaseReference reference;
    private FirebaseUser usuario;
    private AlertDialog alerta;
    private LinearLayout editarNomeSenhaLinearLayout;
    private String tipoUsuarioCad;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        storageReference = ConfiguracaoFirebase.getFirebaseStorageReference();
        reference = FirebaseDatabase.getInstance().getReference();
        newNomeUsuario = (EditText) findViewById(R.id.edtEditarNome);
        senhaUsuario1 = (EditText) findViewById(R.id.edtEditarSenha1);
        senhaUsuario2 = (EditText) findViewById(R.id.edtEditarSenha2);
        imgFotoPerfil = (ImageView) findViewById(R.id.imgPerfil);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);
        btnCancelar = (Button) findViewById(R.id.btnCancelar);
        usuario = autenticacao.getCurrentUser();
        emailUsuario = usuario.getEmail().toString();
        oldNomeUsuario = (EditText) findViewById(R.id.edtEditarNome);
        checkboxAlteraNomeSenha = (CheckBox) findViewById(R.id.checkboxAlteraNomeSenha);
        editarNomeSenhaLinearLayout = (LinearLayout) findViewById(R.id.editarNomeSenhaLinearLayout);


        //para não gerar erro primeiro acesso após cadastro
        Intent it = getIntent();
        String nomeUser = it.getStringExtra("nome");
        String usuarioCadastrado =  it.getStringExtra("tipoUsuarioCad");
        oldNomeUsuario.setText(nomeUser);
        tipoUsuarioCad = usuarioCadastrado;
        if (tipoUsuarioCad.equals("Supermercado")){
            checkboxAlteraNomeSenha.setText("Alterar senha");
        }

        editarNomeSenhaLinearLayout.setVisibility(View.INVISIBLE);
        editarNomeSenhaLinearLayout.setEnabled(false);

        carregaImagemPerfil();


        imgFotoPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditarPerfilActivity.this);
                builder.setCancelable(true);
                builder.setTitle("IMAGEM PERFIL");
                builder.setMessage("Como deseja editar sua imagem?");

                builder.setPositiveButton("CÂMERA", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE_SECURE);
                            if (cameraIntent.resolveActivity((getPackageManager())) != null) {
                                startActivityForResult(cameraIntent, 321);
                            }
                        } catch (Exception e) {
                            Toast.makeText(EditarPerfilActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                builder.setNegativeButton("ARQUIVO", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                        startActivityForResult(Intent.createChooser(intent, "Selecione uma imagem"), 123);
                    }
                });
                alerta = builder.create();
                alerta.show();
            }
        });
    }

    private void cadastroFoto() {
        StorageReference salvaFotoReferencia = storageReference.child("fotoPerfilUsuario/" + emailUsuario + ".jpg");

        imgFotoPerfil.setDrawingCacheEnabled(true);
        imgFotoPerfil.buildDrawingCache();

        Bitmap bitmap = imgFotoPerfil.getDrawingCache();

        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArray);
        byte[] data = byteArray.toByteArray();

        UploadTask uploadTask = salvaFotoReferencia.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                Toast.makeText(EditarPerfilActivity.this, "Imagem salva com sucesso!!", Toast.LENGTH_SHORT).show();
                finish();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        final int heigth = 300;
        final int width = 300;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 123) {
                Uri imagemSelecionada = data.getData();
                Picasso.get().load(imagemSelecionada.toString()).resize(width, heigth).centerCrop().into(imgFotoPerfil);
            } else if (requestCode == 321) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                imgFotoPerfil.setImageBitmap(imageBitmap);
            }
        }
    }

    private void carregaImagemPerfil() {

            FirebaseStorage storage = FirebaseStorage.getInstance();

            final StorageReference storageReference = storage.getReferenceFromUrl
                    ("gs://buscapromo-7627b.appspot.com/fotoPerfilUsuario/" + emailUsuario + ".jpg");

            final int heigth = 300;
            final int width = 300;

            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).resize(width, heigth).centerCrop().into(imgFotoPerfil);


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditarPerfilActivity.this, "Imagem perfil não encontrada!!", Toast.LENGTH_SHORT).show();

                }
            });


    }

    public void btnCancelarOnclickListener(View view) {
        Intent intent = new Intent(EditarPerfilActivity.this, TelaPrincipalActivity.class);
        finish();
        startActivity(intent);

    }

    public void btnSalvarPerfilOnClickListener(View view) {
        if (checkboxAlteraNomeSenha.isChecked()) {
            if (!newNomeUsuario.getText().equals("") && !senhaUsuario1.getText().toString().isEmpty()
                    && senhaUsuario1.getText().toString().equals(senhaUsuario2.getText().toString())) {


                final String novoNome = newNomeUsuario.getText().toString();
                final String novaSenha = senhaUsuario1.getText().toString();
                final String email = autenticacao.getCurrentUser().getEmail().toString();

//                final String oldNomeUsuario = autenticacao.getCurrentUser().getDisplayName().toString();

                    reference.child("usuarios").orderByChild("email").equalTo(email.toString()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot posSnapshot : dataSnapshot.getChildren()) {
                            final String keyUser = posSnapshot.child("key").getValue().toString();

                            if (email != null) {
                                if(!tipoUsuarioCad.equals("Supermercado")) {
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(novoNome).build();
                                    usuario.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        reference.child("usuarios").child(keyUser).child("nome").setValue(novoNome);

                                                        Toast.makeText(EditarPerfilActivity.this, "Nome salvo com sucesso", Toast.LENGTH_SHORT).show();

                                                    }
                                                }
                                            });
                                }
                                usuario.updatePassword(novaSenha).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(EditarPerfilActivity.this, "Senha salva com sucesso!!", Toast.LENGTH_SHORT).show();

                                            }
                                        }
                                    });
                                }
                                Toast.makeText(EditarPerfilActivity.this, "Edição realizada com sucesso!!", Toast.LENGTH_SHORT).show();

                                autenticacao.signOut();
                                Intent intent = new Intent(EditarPerfilActivity.this, MainActivity.class);
                                finish();
                                startActivity(intent);
                            }

                        }


                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(EditarPerfilActivity.this, "Cancelado pelo usuário!!", Toast.LENGTH_LONG).show();
                    }
                });
                cadastroFoto();
            } else {
                Toast.makeText(EditarPerfilActivity.this, "Erro!! Verifique nome e senhas digitadas!", Toast.LENGTH_LONG).show();
            }

        } else {
            cadastroFoto();
        }
    }


    public void onCheckboxClicked(View view) {
        if (checkboxAlteraNomeSenha.isChecked()) {
            if(tipoUsuarioCad.equals("Supermercado")) {
               newNomeUsuario.setEnabled(false);

            }
            newNomeUsuario.setText(oldNomeUsuario.getText());
            editarNomeSenhaLinearLayout.setVisibility(View.VISIBLE);


        } else {
            editarNomeSenhaLinearLayout.setVisibility(View.INVISIBLE);

        }

    }
}
