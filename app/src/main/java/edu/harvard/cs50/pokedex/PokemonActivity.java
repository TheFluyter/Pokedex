package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView flavorTextTextView;
    private String url;
    private String speciesURL;
    private RequestQueue requestQueue;
    private Button catch_pokemon_button;
    private String pokemonName;
    private String flavorText;
    private ImageView spriteView;

    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        catch_pokemon_button = findViewById(R.id.caught);
        spriteView = findViewById(R.id.pokemon_sprite);
        settings = getSharedPreferences("USER", Context.MODE_PRIVATE);
        flavorTextTextView = findViewById(R.id.pokemon_flavor_text);

        load();
    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    // Get name and set first letter to upper and the rest to lower case
                    String name = response.getString("name");
                    String upperName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                    nameTextView.setText(upperName);

                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    // Store Pokemon name as string to search for catch-release
                    pokemonName = response.getString("name");

                    // Set correct catch-release text in button
                    if (settings.getBoolean(pokemonName, false)) {
                        catch_pokemon_button.setText("Release");
                    }
                    else {
                        catch_pokemon_button.setText("Catch");
                    }

                    // Download image sprite form URL and set in image view
                    JSONObject spriteEntries = response.getJSONObject("sprites");
                    String spriteURL = spriteEntries.getString("front_default");
                    Picasso.get().load(spriteURL).into(spriteView);

                    // Get URL to flavor species entries and set flavor text entry
                    JSONObject speciesEntries = response.getJSONObject("species");
                    speciesURL = speciesEntries.getString("url");
                    getFlavorText(speciesURL);

                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }

    // Get flavor text method with a separate API call
    public void getFlavorText(String flavorTextURL) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, flavorTextURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RESPONSE", response.toString());

                try {
                    JSONArray flavorTextEntries = response.getJSONArray("flavor_text_entries");

                    for (int i = 0; i < flavorTextEntries.length(); i++) {
                        JSONObject flavorTextEntry = flavorTextEntries.getJSONObject(i);
                        JSONObject language = flavorTextEntry.getJSONObject("language");
                        String languageName = language.getString("name");

                        if (languageName.equals("en")) {
                            flavorText = flavorTextEntry.getString("flavor_text");
                            flavorTextTextView.setText(flavorText);
                            break;

                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Flavor text json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon flavor text details error", error);

            }
        });

        requestQueue.add(request);
    }

    // Method to set catch or release text for catch-button based on caught ArrayList
    public void catchRelease(View view) {

        if (settings.getBoolean(pokemonName, false)) {
            catch_pokemon_button.setText("Catch");
            settings.edit().putBoolean(pokemonName, false).apply();
        }
        else {
            catch_pokemon_button.setText("Release");
            settings.edit().putBoolean(pokemonName, true).apply();
        }
    }
}
