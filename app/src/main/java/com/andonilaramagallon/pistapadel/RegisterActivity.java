package com.andonilaramagallon.pistapadel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Atributos de la clase
    private FirebaseFirestore db; // Declarar objeto FirebaseFirestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Instanciar la conexión a la BD FirebaseFirestore
        db = FirebaseFirestore.getInstance();

        // Llamar al metodo setup() para configurar los elementos de la UI
        setup();
    }

    /**
     * Configura e inicia correctamente la actividad.
     * Asigna funcionalidad a los botones de la UI
     */
    private void setup() {
        // Instanciar los elementos del layout
        Button backButton = findViewById(R.id.backButton);
        Button signUpButton = findViewById(R.id.registerButton);

        // OnClickListener para el botón volver
        backButton.setOnClickListener(v -> finish()); // Finaliza esta actividad y vuelve a la anterior -> AuthActivity

        // OnClickListener para el botón registrarse
        signUpButton.setOnClickListener(v -> registerUser());
    }

    /**
     * Si los campos de los EditText son válidos:
     * Registra el usuario con las credenciales obtenidas Mediante el servicio FirebaseAuth.
     * Almacena los datos del usuario en la colección users.
     * Accede a la HomeActivity.
     */
    private void registerUser() {
        // Verificar la validación de los campos mediante validateFields()
        if (validateFields()) {
            // Instanciar elementos del layout
            EditText emailEditText = findViewById(R.id.emailEditText);
            EditText passwordEditText = findViewById(R.id.passwordEditText);

            // Almacenar valores obtenidos de los EditText
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Obtener una instancia de FirebaseAuth y llamar al metodo createUserWithEmailAndPassword para registrar el usuario mediante email y contraseña
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        // Registro exitoso
                        if (task.isSuccessful()) {
                            // Almacenar los datos del usuario en la BD mediante saveData()
                            saveData();
                            // Mostrar mensaje de éxito, finalizar actividad y volver a AuthActivity
                            registerSuccessPopup();
                        } else {
                            // Si el task no es exitoso, se manejan las excepciones
                            if (task.getException() != null) {
                                // Obtener el código de error
                                FirebaseAuthException e = (FirebaseAuthException) task.getException();
                                String errorCode = e.getErrorCode();

                                // Personalizar el mensaje de error según el código de error
                                switch (errorCode) {
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        showPopup("Error", "Ya existe un usuario registrado con la dirección de correo electrónico " + email);
                                        // Limpiar los campos de email y contraseña
                                        passwordEditText.setText("");
                                        passwordEditText.setText("");
                                        break;
                                    case "ERROR_INVALID_EMAIL":
                                        showPopup("Error", "El formato del correo electrónico no es válido.");
                                        // Limpiar los campos de email y contraseña
                                        emailEditText.setText("");
                                        passwordEditText.setText("");
                                        break;
                                    default:
                                        showPopup("Error", "Error al registrar usuario. Por favor inténtelo de nuevo.");
                                        break;
                                }
                            }
                        }
                    });
        }
    }

    /**
     * Guarda los datos obtenidos de los campos en la BD
     */
    private void saveData() {
        // Instanciar elementos del layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText surnamesEditText = findViewById(R.id.surnamesEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);

        // Almacenar valores obtenidos de los EditText
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String surnames = surnamesEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Crear un HashMap con los datos del usuario
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", name);
        userData.put("surnames", surnames);
        userData.put("phone", phone);

        // Guardar los datos en Firestore -> Sirve tanto para guardar o actualizar los datos si ya existían previamente en la BD
        db.collection("users").document(email)
                .set(userData);
                //.addOnSuccessListener(succes -> showPopup("¡Enhorabuena!", "Datos guardados correctamente"))
                //.addOnFailureListener(e -> showPopup("Error", "Error al guardar los datos: " + e.getMessage()));
    }

    /**
     * Valida el valor de los campos EditText del layout
     *
     * @return Devuelve true o false en función e si los campos son validos o no
     */
    private boolean validateFields() {

        // Instanciar elementos del layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        EditText nameEditText = findViewById(R.id.nameEditText);
        EditText surnamesEditText = findViewById(R.id.surnamesEditText);
        EditText phoneEditText = findViewById(R.id.phoneEditText);

        // Almacenar valores obtenidos de los EditText
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String surnames = surnamesEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();

        // Validación de los campos
        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || surnames.isEmpty() || phone.isEmpty()) {
            showPopup("Error en el registro", "Debe cumplimentar todos los campos");
            return false;
        }
        if (password.length() < 6) {
            showPopup("Error en el registro", "La contraseña debe tener al menos 6 dígitos");
            // Limpiar el campo de contraseña
            passwordEditText.setText("");
            return false;
        }
        if (phone.length() != 9) {
            showPopup("Error en el registro", "El número de teléfono debe tener 9 dígitos");
            return false;
        }
        // Si la validación de los campos es exitosa retorna true
        return true;
    }

    /**
     * Muestra un mensaje con formato popup y un botón aceptar para cerrarlo.
     *
     * @param titulo  Titulo que se muestra en el popup
     * @param mensaje Mensaje que se muestra en el popup
     */
    private void showPopup(String titulo, String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", (dialog, which) -> dialog.dismiss());
        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Muestra un mensaje de éxito en el registro del nuevo usuario.
     * Al pulsar aceptar, finaliza la actividad y vuelve a la anterior AuthActivity.
     */
    private void registerSuccessPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("¡Enhorabuena!");
        builder.setMessage("Ha sido registrado correctamente como nuevo usuario");
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss(); // Cierra el diálogo
            finish(); // Finaliza la actividad actual y vuelve a la anterior
        });
        // Crear y mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}