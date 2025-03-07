package com.andonilaramagallon.pistapadel;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

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
        cerrarSesion();
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

        emailTextView.setText(email);
        providerTextView.setText(provider);
    }

    /**
     * Metodo para definir la acción del metodo Cerrar Sesión.
     * Cierra la sesión del usuario conectado mediante el metodo signOut del servicio FirebaseAuth.
     */
    private void cerrarSesion() {
        Button signOutButton = findViewById(R.id.signOutButton);

        signOutButton.setOnClickListener(v -> {
            // Obtener una instancia de FirebaseAuth y llamar al metodo signOut para cerrar la sesión
            FirebaseAuth.getInstance().signOut();

            //TODO: Queda pendiente borrar este método deprecado. Mirar el tema de la navegación entre fragmentos, mediante el archivo res/navigation/nav_graph.xml
            //onBackPressed();

            // Terminar la actividad actual y volver a la anterior
            finish();
        });
    }
}