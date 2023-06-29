package aleksey.vasilev.testapp.system

import aleksey.vasilev.testapp.R
import aleksey.vasilev.testapp.model.Globals.localDataStorage
import aleksey.vasilev.testapp.model.Globals.viewModel
import aleksey.vasilev.testapp.model.LocalDataStorage
import aleksey.vasilev.testapp.ui.theme.TestAppTheme
import aleksey.vasilev.testapp.views.HomeView.HomeView
import aleksey.vasilev.testapp.views.PincodeView.PincodeView
import aleksey.vasilev.testapp.views.RecordView.RecordView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val testAppViewModel: TestAppViewModel by viewModels()
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        localDataStorage = LocalDataStorage(this)
        viewModel = testAppViewModel
        setContent {
            val navController = rememberNavController()
            this@MainActivity.navController = navController
            TestAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = this@MainActivity.getString(R.string.pincode_view)
                    ) {
                        composable(this@MainActivity.getString(R.string.pincode_view)) {
                            PincodeView(
                                navController
                            )
                        }
                        composable(this@MainActivity.getString(R.string.home_view)) {
                            HomeView(
                                navController
                            )
                        }
                        composable("${this@MainActivity.getString(R.string.record_view)}/{url}/{user}/{title}/{description}") {
                            val url =
                                it.arguments?.getString(this@MainActivity.getString(R.string.url))
                                    ?: this@MainActivity.getString(
                                        R.string.empty
                                    )
                            val user =
                                it.arguments?.getString(this@MainActivity.getString(R.string.user))
                                    ?: this@MainActivity.getString(R.string.empty)
                            val title =
                                it.arguments?.getString(this@MainActivity.getString(R.string.title))
                                    ?: this@MainActivity.getString(R.string.empty)
                            val description =
                                it.arguments?.getString(this@MainActivity.getString(R.string.description))
                                    ?: this@MainActivity.getString(R.string.empty)
                            RecordView(navController, url, user, title, description)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        localDataStorage[this.getString(R.string.last_visit)] = System.currentTimeMillis()
    }

    override fun onStart() {
        if (this::navController.isInitialized) {
            val timeDifference =
                System.currentTimeMillis() - localDataStorage[this.getString(R.string.last_visit), Long::class.java]!!
            if (timeDifference > 60_000) {
                this@MainActivity.navController.navigate(this.getString(R.string.pincode_view))
            }
        }
        super.onStart()
    }
}