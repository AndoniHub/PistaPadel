package com.andonilaramagallon.pistapadel;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class AuthActivity extends AppCompatActivity {

    /**
     * Metodo que se ejecuta cuando se crea la actividad.
     *
     * @param savedInstanceState Estado previo de la actividad, si lo hay.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Forzar el modo claro (diurno)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        // Forzar la orientación de pantalla vertical
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_auth);

        // Inicializar la instancia de FireBaseAnalitycs.
        // Permite que la aplicación registre eventos y datos analíticos que se envían a la consola de Firebase.
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Instanciar un nuevo Bundle: contenedor clave-valor
        // Clave -> "message"
        // Valor -> "Integración de Firebase completa"
        Bundle bundle = new Bundle();
        bundle.putString("message", "Integración de Firebase completa");

        // Lanzar evento personalizado a Google Analitycs, pasándole el bundle por parámetro
        // Primer parámetro: nombre del evento
        // Segundo parámetro: bundle
        // En este caso, el evento "InitScreen" simplemente indica que la pantalla de inicio se ha inicializado
        // y que la integración de Firebase Analytics está funcionando correctamente.
        mFirebaseAnalytics.logEvent("InitScreen", bundle);

        // Aplicar los insets para ajustar la vista a los elementos del sistema.
        // Ajusta los márgenes de la vista (R.id.main) para evitar que los elementos del sistema (barra de estado, barra de navegación, etc.)
        // interfieran con la interfaz de usuario.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Llamada a los métodos que definen las acciones de los elementos del layout
        registerUser();
        signInUser();
        recoverPassword();
    }

    /**
     * Metodo que se ejecuta cada que que la activity vuelve a estar en primer plano.
     * Vuelve a poner los valores de los editText de email y contraseña vacíos.
     */
    @Override
    protected void onResume() {
        super.onResume();
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        // Limpiar los campos de email y contraseña
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    /**
     * Metodo que define la acción al pulsar el botón Registrar.
     * Obtiene y verifica que los campos email y contraseña no estén vacíos y sean válidos.
     * Mediante el servicio FirebaseAuth, registra el usuario con las credenciales obtenidas y accede a la HomeActivity.
     */
    private void registerUser() {
        // Instanciar elementos del layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button signUpButton = findViewById(R.id.signOutButton);

        // Añadir el listener al botón de registrar
        signUpButton.setOnClickListener(v -> {
            // Obtener el valor de los campos EditText de email y contraseña
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            // Validaciones de los campos de email y contraseña
            if (email.isEmpty()) {
                mostrarPopup("Error", "Debe introducir una dirección de email");
                return;
            }
            if (password.isEmpty()) {
                mostrarPopup("Error", "Debe introducir una contraseña válida");
                return;
            }
            // Verificar que la contraseña tenga un mínimo de 6 caracteres (requisito de Firebase)
            if (password.length() < 6) {
                mostrarPopup("Error", "La contraseña debe tener al menos 6 caracteres");
                // Limpiar el campo de contraseña
                passwordEditText.setText("");
                return;
            }

            // Obtener una instancia de FirebaseAuth y llamar al metodo createUserWithEmailAndPassword para registrar el usuario mediante email y contraseña
            FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // [ REGISTRO EXITOSO ]
                            // Mostrar mensaje de éxito
                            //TODO Valorar si cambiar el mensaje por un Toast y navegar a HomeActivity
                            mostrarPopup("¡Enhorabuena!", "Ha sido registrado correctamente.\nPulse el botón Acceder para navegar al Home.");
                        } else {
                            // Si el task no es exitoso, se maneja el error
                            if (task.getException() != null) {
                                // Obtener el código de error
                                FirebaseAuthException e = (FirebaseAuthException) task.getException();
                                String errorCode = e.getErrorCode();

                                // Personalizar el mensaje de error según el código de error
                                switch (errorCode) {
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        mostrarPopup("Error", "Ya existe un usuario registrado con la dirección de correo electrónico " + email);
                                        // Limpiar los campos de email y contraseña
                                        passwordEditText.setText("");
                                        passwordEditText.setText("");
                                        break;
                                    case "ERROR_INVALID_EMAIL":
                                        mostrarPopup("Error", "El formato del correo electrónico no es válido.");
                                        // Limpiar los campos de email y contraseña
                                        emailEditText.setText("");
                                        passwordEditText.setText("");
                                        break;
                                    default:
                                        mostrarPopup("Error", "Error al registrar usuario. Por favor inténtelo de nuevo.");
                                        break;
                                }
                            }
                        }
                    });
        });
    }

    /**
     * Metodo que define la acción al pulsar el botón Acceder.
     * Obtiene y verifica que no estén vacíos los campos email y contraseña.
     * Mediante el servicio FirebaseAuth, comprueba que el usuario esta registrado y en tal caso accede a la HomeActivity.
     */
    private void signInUser() {
        // Instanciar elementos del layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        Button signInButton = findViewById(R.id.signInButton);

        // Añadir el listener al botón de acceder
        signInButton.setOnClickListener(v -> {
            // Obtener el valor de los campos EditText de email y contraseña
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validaciones de los campos de email y contraseña
            if (email.isEmpty()) {
                mostrarPopup("Error", "Debe introducir una dirección de email");
                return;
            }
            if (password.isEmpty()) {
                mostrarPopup("Error", "Debe introducir una contraseña válida");
                return;
            }
            // Verificar que la contraseña tenga un mínimo de 6 caracteres (requisito de Firebase)
            if (password.length() < 6) {
                mostrarPopup("Error", "La contraseña debe tener al menos 6 caracteres");
                // Limpiar el campo de contraseña
                passwordEditText.setText("");
                return;
            }

            // Obtener una instancia de FirebaseAuth y llamar al metodo signInWithEmailAndPassword para acceder mediante email y contraseña
            FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String userEmail = (task.getResult().getUser() != null) ? task.getResult().getUser().getEmail() : "";
                            // [ ACCESO EXITOSO ]
                            // Mostrar mensaje toast
                            Toast.makeText(this, "Ha accedido a la aplicación con éxito", Toast.LENGTH_SHORT).show();
                            // Navegar a HomeActivity
                            mostrarHome(userEmail, ProviderType.BASIC);
                        } else {
                            mostrarPopup("Error", "La dirección de correo electrónico o la contraseña no coinciden con las de un usuario registrado.\nPor favor, inténtelo de nuevo.");
                            // Limpiar los campos de email y contraseña
                            emailEditText.setText("");
                            passwordEditText.setText("");
                        }
                    });
        });
    }

    /**
     * Metodo que envía un correo de recuperación de contraseña si el email es válido.
     * Muestra un mensaje de éxito o error según el resultado.
     */
    private void recoverPassword() {
        // Instanciar elementos del layout
        EditText emailEditText = findViewById(R.id.emailEditText);
        EditText passwordEditText = findViewById(R.id.passwordEditText);
        TextView passRecoveryTextView = findViewById(R.id.passRecoveryTextView);

        // Asignar OnClickListener a passRecoveryTextView
        passRecoveryTextView.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();

            // Validar que el email no esté vacío
            if (email.isEmpty()) {
                // Mostrar un mensaje Toast
                Toast.makeText(this, "Por favor, introduce tu email", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enviar el correo de recuperación
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Mostrar un mensaje Toast
                            Toast.makeText(this, "Correo de recuperación enviado.", Toast.LENGTH_LONG).show();
                        } else {
                            // Si el task no es exitoso, se maneja el error
                            if (task.getException() != null) {
                                // Mostrar mensaje de error
                                mostrarPopup("Error", "La dirección de correo electrónico no es valida.\nPor favor, inténtelo de nuevo.");
                                // Limpiar los campos de email y contraseña
                                emailEditText.setText("");
                                passwordEditText.setText("");
                            }
                        }
                    });
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

    /**
     * Metodo que crea un Intent para navegar a la HomeActivity y enviar como datos el email y provider del usuario
     *
     * @param email    Email del usuario
     * @param provider Provider del usuario
     */
    private void mostrarHome(String email, ProviderType provider) {
        Intent homeIntent = new Intent(AuthActivity.this, HomeActivity.class);
        homeIntent.putExtra("email", email);
        homeIntent.putExtra("provider", provider.name());
        startActivity(homeIntent);
    }
}