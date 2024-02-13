import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FilmeSelecionadoViewModel : ViewModel() {
    private val user = FirebaseAuth.getInstance().currentUser
    private val db = FirebaseFirestore.getInstance()
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String>
        get() = _toastMessage
    // Função para definir mensagens
    private fun definirMensagem(mensagem: String) {
        _toastMessage.value = mensagem
    }


    fun adicionarFilme(nome: String, descricao: String, imagemUrl: String) {
        user?.uid?.let { uid ->
            val filmesUsuarioRef =
                db.collection("FilmesUsuario").document(uid).collection("MeusFilmes")

            // Verifique se o nome já existe na coleção
            filmesUsuarioRef
                .whereEqualTo("nome", nome)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (task.result?.isEmpty == true) {
                            // O nome não existe, então você pode adicionar o filme
                            user.uid.let { uid ->
                                val filmeRef =
                                    db.collection("FilmesUsuario").document(uid)
                                        .collection("MeusFilmes").document()
                                val MeusFilmes = hashMapOf(
                                    "id" to filmeRef.id,
                                    "nome" to nome,
                                    "descricao" to descricao,
                                    "imagUrl" to imagemUrl,

                                    )

                                filmeRef
                                    .set(MeusFilmes)
                                    .addOnCompleteListener {
                                        definirMensagem("Filme Adicionado!!")
                                    }
                                    .addOnFailureListener { e ->
                                        definirMensagem("Erro ao adicionar filme: $e")
                                    }
                            }
                        } else {
                            // O nome já existe, faça algo aqui (por exemplo, exiba uma mensagem)
                            definirMensagem("Esse filme já existe em sua lista.")
                        }
                    } else {
                        definirMensagem("Erro ao verificar o nome do filme: ${task.exception}")
                    }
                }
        }
    }

    fun deletarFilme(uidFilme: String) {
        user?.uid?.let { uidUsuario ->
            val filmeRef = db.collection("FilmesUsuario").document(uidUsuario)
                .collection("MeusFilmes").document(uidFilme)

            filmeRef
                .delete()
                .addOnCompleteListener {
                    _toastMessage.value = "Filme deletado com sucesso!!"

                }
                .addOnFailureListener { e ->
                    _toastMessage.value = "Erro ao deletar filme: $e"
                }
        }
    }
}
