package aleksey.vasilev.testapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Record(
    @SerializedName("description") val description: String,
    @SerializedName("url") val url: String,
    @SerializedName("title") val title: String,
    @SerializedName("id") val id: Int,
    @SerializedName("user") val user: Int,
): Serializable