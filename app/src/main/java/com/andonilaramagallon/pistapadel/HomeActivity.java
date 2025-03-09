package com.andonilaramagallon.pistapadel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    // Atributos de la clase
    private static final FirebaseFirestore  db = FirebaseFirestore.getInstance(); // Instancia de conexión a la BD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Variables para almacenar los valores obtenidos desde el intent
        // Se inicializan con valores por defecto
        String email = "";
        String provider = "BASIC";

        // Instanciar Bundle asignando valores obtenidos del intent
        Bundle bundle = getIntent().getExtras();

        // Asignar los valores a las variables en caso de que el Bundle no sea null
        if (bundle != null) {
            email = bundle.getString("email");
            provider = bundle.getString("provider");
        }

        // Llamadas a los métodos de la clase
        setup(email, provider);
        logOutSession();
    }

    /**
     * Metodo para configurar e iniciar correctamente la actividad, obteniendo y mostrando en el layout los valores de email y provider del usuario.
     * @param email Email del usuario
     * @param provider Provider del usuario
     */
    private void setup(String email, String provider) {
        // Instanciar los elementos del layout
        TextView emailTextView = findViewById(R.id.emailTextView);
        TextView providerTextView = findViewById(R.id.providerTextView);
        Button saveInfoButton = findViewById((R.id.saveInfoButton));
        Button getInfoButton = findViewById((R.id.getInfoButton));
        Button deleteInfoButton = findViewById((R.id.deleteInfoButton));
        EditText nameEditTextView = findViewById((R.id.nameEditTextView));
        EditText surNameEditTextView = findViewById((R.id.surNameEditTextView));
        EditText phoneEditTextView = findViewById((R.id.phoneEditTextView));

        // Mostrar los valores de email y provider en el layout
        emailTextView.setText(email);
        providerTextView.setText(provider);

        // Listener para el botón saveInfoButton
        saveInfoButton.setOnClickListener(v -> {
            // Obtener los valores ingresados por el usuario en los editText
            String name = nameEditTextView.getText().toString().trim();
            String surname = surNameEditTextView.getText().toString().trim();
            String phone = phoneEditTextView.getText().toString().trim();

            // Crear un HashMap con los datos del usuario
            Map<String, Object> userData = new HashMap<>();
            userData.put("provider", provider);
            userData.put("name", name);
            userData.put("surname", surname);
            userData.put("phone", phone);

            // Guardar los datos en Firestore -> Sirve tanto para guardar o actualizar los datos si ya existían previamente en la BD
            db.collection("users").document(email)
                    .set(userData)
                    .addOnSuccessListener(aVoid -> mostrarPopup("¡Enhorabuena!", "Datos guardados correctamente"))
                    .addOnFailureListener(e -> mostrarPopup("Error", "Error al guardar los datos: " + e.getMessage()));
        });

        // Listener para el botón getInfoButton -> Obtiene los datos del documento con key = email
        getInfoButton.setOnClickListener(v -> {
            db.collection("users").document(email)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            // Obtener los valores del documento
                            String name = task.getResult().getString("name");
                            String surname = task.getResult().getString("surname");
                            String phone = task.getResult().getString("phone");

                            // Mostrar los valores en los EditText
                            nameEditTextView.setText(name != null ? name : "");
                            surNameEditTextView.setText(surname != null ? surname : "");
                            phoneEditTextView.setText(phone != null ? phone : "");
                        } else {
                            mostrarPopup("Error", "Error al obtener los datos");
                        }
                    })
                    .addOnFailureListener(e -> mostrarPopup("Error", "Error: " + e.getMessage()));
        });

        // Listener para el botón deleteInfoButton -> Elimina todos los datos del documento con key = email
        deleteInfoButton.setOnClickListener(v -> {
            db.collection("users").document(email).delete();
        });
    }

    /**
     * Metodo para definir la acción del metodo Cerrar Sesión.
     * Cierra la sesión del usuario conectado mediante el metodo signOut del servicio FirebaseAuth.
     */
    private void logOutSession() {
        Button signOutButton = findViewById(R.id.signOutButton);

        signOutButton.setOnClickListener(v -> {
            // Obtener una instancia de FirebaseAuth y llamar al metodo signOut para cerrar la sesión
            FirebaseAuth.getInstance().signOut();

            //TODO: Queda pendiente borrar este método comentado deprecado.
            // Mirar el tema de la navegación entre fragmentos, mediante el archivo res/navigation/nav_graph.xml
            //onBackPressed();

            // Terminar la actividad actual y volver a la anterior
            finish();
        });
    }

    /**
     * Metodo cuya función es mostrar un mensaje con formato popup y un botón de aceptar para cerrarlo
     *
     * @param titulo  Titulo que se muestra en el popup
     * @param mensaje Mensaje que se muestra en el popup
     */
    private void mostrarPopup(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}