package com.andonilaramagallon.pistapadel;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MainActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics; // Declarar instancia de FireBaseAnalitycs

    /**
     * Método0 que se ejecuta cuando se crea la actividad.
     * @param savedInstanceState Estado previo de la actividad, si lo hay.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Inicializar la instancia de FireBaseAnalitycs.
        // Permite que la aplicación registre eventos y datos analíticos que se envían a la consola de Firebase.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

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

    }
}