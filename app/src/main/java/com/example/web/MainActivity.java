package com.example.web;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import retrofit2.http.GET;
import retrofit2.http.Path;

public class MainActivity extends AppCompatActivity {

    private GitHubService gitHubService;
    private EditText usernameEditText;
    private TextView resultTextView;
    private Button fetchReposButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializa la vista
        usernameEditText = findViewById(R.id.usernameEditText);
        resultTextView = findViewById(R.id.resultTextView);
        fetchReposButton = findViewById(R.id.fetchReposButton);

        // Configura Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gitHubService = retrofit.create(GitHubService.class);

        // Configura el botón para realizar la búsqueda de repositorios
        fetchReposButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                if (!username.isEmpty()) {
                    fetchRepos(username);
                } else {
                    resultTextView.setText("Por favor, ingresa un nombre de usuario.");
                }
            }
        });
    }

    private void fetchRepos(String username) {
        Call<List<Repo>> call = gitHubService.listRepos(username);
        call.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                if (!response.isSuccessful()) {
                    resultTextView.setText("Error: " + response.code());
                    return;
                }

                List<Repo> repos = response.body();
                if (repos == null || repos.isEmpty()) {
                    resultTextView.setText("No se encontraron repositorios para este usuario.");
                    return;
                }

                StringBuilder builder = new StringBuilder();
                int n = 1;
                builder.append("--- REPOSITORIOS de ' ").append(username).append(" ' ---\n\n");
                for (Repo repo : repos) {
                    builder.append("·Nombre: ").append(repo.getName()).append("\n")
                            .append("·Descripción: ").append(repo.getDescription() != null ? repo.getDescription() : "Sin descripción").append("\n")
                            .append("·Nº estrellas: ").append(repo.getStargazers_count()).append("\n\n");
                }

                resultTextView.setText(builder.toString());
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable t) {
                resultTextView.setText("Error al cargar los repositorios.");
            }
        });
    }

    // Interfaz de Retrofit para acceder a la API de GitHub
    public interface GitHubService {
        @GET("users/{username}/repos")
        Call<List<Repo>> listRepos(@Path("username") String username);
    }

    // Modelo para los repositorios
    public static class Repo {
        private String name;
        private String description;
        private int stargazers_count;
        private String html_url;

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getStargazers_count() {
            return stargazers_count;
        }
    }
}
