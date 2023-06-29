package aleksey.vasilev.testapp.views

import aleksey.vasilev.testapp.BuildConfig
import aleksey.vasilev.testapp.R
import aleksey.vasilev.testapp.model.Globals.viewModel
import aleksey.vasilev.testapp.model.Record
import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


object HomeView {
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun HomeView(navController: NavHostController) {
        BackHandler(enabled = true) {}

        val internetPermission = rememberMultiplePermissionsState(
            permissions = listOf(
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE
            )
        )
        val context = LocalContext.current
        var isInternetAvailable by remember {
            mutableStateOf(internetPermission.allPermissionsGranted && isInternetAvailable(context))
        }
        val content = rememberMutableStateListOf<Record>()

        LaunchedEffect(isInternetAvailable) {
            if (isInternetAvailable && content.isEmpty()) {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    val recordsResponse = getRecordsResponse(context)
                    if (recordsResponse.status == HttpStatusCode.OK) {
                        val records = getRecords(context, recordsResponse)
                        content.addAll(records)
                    } else {
                        Toast.makeText(
                            context,
                            "${context.getString(R.string.error_occurred)}: ${recordsResponse.status.value}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        if (isInternetAvailable) {
            if (content.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            isInternetAvailable =
                                internetPermission.allPermissionsGranted && isInternetAvailable(
                                    context
                                )
                        }, shape = RoundedCornerShape(20.dp), modifier = Modifier
                            .fillMaxHeight(0.05f)
                            .fillMaxWidth(0.3f)
                    ) {
                        Text(text = stringResource(id = R.string.update))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    items(content.size) {
                        Card(elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                            modifier = Modifier.clickable {
                                val encodedUrl = URLEncoder.encode(
                                    content[it].url,
                                    StandardCharsets.UTF_8.toString()
                                )
                                navController.navigate(
                                    "${context.getString(R.string.record_view)}/{url}/{user}/{title}/{description}"
                                        .replace("{url}", encodedUrl)
                                        .replace("{user}", content[it].user.toString())
                                        .replace("{title}", content[it].title)
                                        .replace("{description}", content[it].description)
                                )
                            }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = content[it].title,
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = content[it].description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Justify
                                    )
                                }

                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.internet_issue),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.fillMaxHeight(0.1f))
                Button(
                    onClick = {
                        isInternetAvailable =
                            internetPermission.allPermissionsGranted && isInternetAvailable(context)
                    }, shape = RoundedCornerShape(20.dp), modifier = Modifier
                        .fillMaxHeight(0.1f)
                        .fillMaxWidth(0.3f)
                ) {
                    Text(text = stringResource(id = R.string.next))
                }
            }
        }
    }

    @Composable
    fun <T : Any> rememberMutableStateListOf(vararg elements: T): SnapshotStateList<T> {
        return rememberSaveable(
            saver = listSaver(
                save = { stateList ->
                    if (stateList.isNotEmpty()) {
                        val first = stateList.first()
                        if (!canBeSaved(first)) {
                            throw IllegalStateException("${first::class}")
                        }
                    }
                    stateList.toList()
                },
                restore = { it.toMutableStateList() }
            )
        ) {
            elements.toList().toMutableStateList()
        }
    }

    private suspend fun getRecordsResponse(context: Context): HttpResponse {
        val client = HttpClient(OkHttp)
        val response = client.get(BuildConfig.Server) {
            parameter(context.getString(R.string.limit), BuildConfig.RecordLimit)
        }
        return response
    }

    private suspend fun getRecords(context: Context, recordsResponse: HttpResponse): List<Record> {
        val body = recordsResponse.body<String>()
        val jsonBody = JSONObject(body)
        val itemType = object : TypeToken<List<Record>>() {}.type
        val productsArray = jsonBody[context.getString(R.string.photos)].toString()
        return Gson().fromJson(productsArray, itemType)
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}