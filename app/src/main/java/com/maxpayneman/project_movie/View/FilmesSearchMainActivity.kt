package com.maxpayneman.project_movie.View

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.maxpayneman.aulayt_8.databinding.ActivityFilmesSearchMainBinding
import com.maxpayneman.project_movie.Model.Filme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class FilmesSearchMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilmesSearchMainBinding
    private val Api_Key = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5ZmM1YzgyYjMxNGMwNjNmNDdmZDkyOGU1NzE1NzkxMiIsInN1YiI6IjY1MWNiNDQ1OTY3Y2M3MzQyNWYxZjYxMiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.WAXOM3qxERSbv6SCsygStOwygDjtI4G-WLsOfbUciYI"
    private var listaFilmes = ArrayList<Filme>()
    private  var pos =-1;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilmesSearchMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaFilmes)

        binding.listView.adapter = adapter;

        binding.buscarFilme.setOnClickListener {
            listaFilmes.clear()
            CoroutineScope(Dispatchers.IO).launch {

                val nameFilme = binding.editFilmeName.text.toString()

                val client = OkHttpClient()

                val request = Request.Builder()
                    .url("https://api.themoviedb.org/3/search/movie?query=${nameFilme}&language=pt-BR&page=1&include_adult=false")
                    .get()
                    .addHeader("accept", "application/json")
                    .addHeader(
                        "Authorization",
                        "Bearer $Api_Key"
                    )
                    .build()

                val response = client.newCall(request).execute()

                val responseData = response.body?.string()


                responseData?.let {
                    val jsonObject = JSONObject(it)
                    val resultsArray = jsonObject.getJSONArray("results")

                    for (i in 0 until resultsArray.length()) {
                        val filme = resultsArray.getJSONObject(i)
                        val titulo = filme.getString("title")
                        val ano = filme.getString("release_date")
                        val descricao = filme.getString("overview")
                        val id = filme.getInt("id")

                        val detalhesRequest = Request.Builder()
                            .url("https://api.themoviedb.org/3/movie/$id?language=pt-BR")
                            .get()
                            .addHeader("accept", "application/json")
                            .addHeader(
                                "Authorization",
                                "Bearer $Api_Key"
                            )
                            .build()



                        val detalhesResponse = client.newCall(detalhesRequest).execute()
                        val detalhesData = detalhesResponse.body?.string()

                        detalhesData?.let {
                            val detalhesJson = JSONObject(it)
                            val imagemPath = detalhesJson.getString("poster_path")
                            val imagemUrl = "https://image.tmdb.org/t/p/w500$imagemPath"

                            // Adiciona o filme à lista com o URL da imagem
                            listaFilmes.add(Filme(id, titulo, ano, imagemUrl,descricao))

                        }
                        runOnUiThread {
                            adapter.notifyDataSetChanged()
                        }
                        binding.listView.setOnItemClickListener { _, _, position, _ ->

                            val nome = listaFilmes.get(position).nome
                            val data = listaFilmes.get(position).Data
                            val img = listaFilmes.get(position).imageUrl
                            val descricao = listaFilmes.get(position).descricao
                            pos = position;

                            val i = Intent(applicationContext, FilmeSelecionadoMainActivity::class.java)

                            i.putExtra("filmename", nome)
                            i.putExtra("filmedata", data)
                            i.putExtra("filmeimg", img)
                            i.putExtra("filmedesc", descricao)
                            startActivity(i)

                        }

                    }

                }
            }
        }
        binding.back.setOnClickListener {
            finish()
        }
    }
}