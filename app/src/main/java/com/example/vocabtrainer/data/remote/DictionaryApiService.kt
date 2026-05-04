package com.example.vocabtrainer.data.remote
import com.example.vocabtrainer.data.remote.model.DictionaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
interface DictionaryApiService {
    @GET("api/v2/entries/en/{word}")
    suspend fun define(@Path("word") word: String): Response<List<DictionaryResponse>>
}
