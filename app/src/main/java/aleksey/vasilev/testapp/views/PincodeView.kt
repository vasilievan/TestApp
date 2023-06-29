package aleksey.vasilev.testapp.views

import aleksey.vasilev.testapp.BuildConfig
import aleksey.vasilev.testapp.R
import aleksey.vasilev.testapp.model.Globals.localDataStorage
import aleksey.vasilev.testapp.model.Globals.viewModel
import aleksey.vasilev.testapp.model.PincodeState
import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

object PincodeView {
    @Composable
    fun PincodeView(navController: NavHostController) {
        val currentPincode =
            localDataStorage[stringResource(id = R.string.pincode), String::class.java]!!
        if (currentPincode.isEmpty()) {
            CreatePincode(navController)
        } else {
            FillInPincode(navController)
        }
    }

    @Composable
    fun CreatePincode(navController: NavHostController) {
        val context = LocalContext.current
        var pincodeState by remember {
            mutableStateOf(PincodeState.NOT_YET)
        }
        var savedPincode by remember {
            mutableStateOf(context.getString(R.string.empty))
        }
        var pincode by remember {
            mutableStateOf(context.getString(R.string.empty))
        }
        var pincodePrompt by remember {
            mutableStateOf(context.getString(R.string.come_up_with_pin_code))
        }
        val buttonFraction = 0.15f

        LaunchedEffect(pincodeState) {
            when (pincodeState) {
                PincodeState.CORRECT -> viewModel.viewModelScope.launch {
                    delay(1000)
                    navController.navigate(context.getString(R.string.home_view))
                }

                PincodeState.INCORRECT -> viewModel.viewModelScope.launch(Dispatchers.IO) {
                    delay(1000)
                    pincodeState = PincodeState.NOT_YET
                }

                else -> Unit
            }
        }

        LaunchedEffect(pincodeState) {
            pincodePrompt = when (pincodeState) {
                PincodeState.NOT_YET -> context.getString(R.string.come_up_with_pin_code)
                PincodeState.CORRECT -> context.getString(R.string.match)
                PincodeState.INCORRECT -> context.getString(R.string.no_match)
                PincodeState.SAVED -> context.getString(R.string.repeat_pin_code)
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = pincodePrompt, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.fillMaxHeight(0.015f))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(0.3f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        items(4) {
                            Icon(
                                Icons.Default.Circle,
                                null,
                                tint = getIconTint(it, pincode),
                                modifier = Modifier.fillMaxSize(0.025f)
                            )
                        }
                    }
                }
                Text(context.getString(R.string.empty), style = MaterialTheme.typography.bodyMedium)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(3) {
                            val topIndex = it
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(3) {
                                    val currentNumber = 3 * topIndex + it + 1
                                    Button(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (pincodeState == PincodeState.NOT_YET || pincodeState == PincodeState.SAVED) {
                                                managePincode(context,
                                                    currentNumber.toString(),
                                                    pincode,
                                                    savedPincode,
                                                    { savedPincode = it },
                                                    { pincode = it },
                                                    { pincodeState = it })
                                            }
                                        },
                                        shape = CircleShape,
                                    ) {
                                        Text(
                                            text = "$currentNumber",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                item {
                                    Button(
                                        enabled = false,
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {},
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = colorResource(
                                                id = R.color.transparent
                                            )
                                        )
                                    ) {}
                                }
                                item {
                                    Button(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (pincodeState == PincodeState.NOT_YET || pincodeState == PincodeState.SAVED) {
                                                managePincode(context,
                                                    context.getString(R.string.zero),
                                                    pincode,
                                                    savedPincode,
                                                    { savedPincode = it },
                                                    { pincode = it },
                                                    { pincodeState = it })
                                            }
                                        },
                                        shape = CircleShape,
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.zero),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                item {
                                    IconButton(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (pincode.isNotEmpty() &&
                                                (pincodeState == PincodeState.NOT_YET || pincodeState == PincodeState.SAVED)
                                            ) {
                                                pincode = pincode.dropLast(1)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .height((LocalConfiguration.current.screenWidthDp * 0.1).dp)
                                                .width((LocalConfiguration.current.screenWidthDp * 0.1).dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    @Composable
    fun FillInPincode(navController: NavHostController) {
        val context = LocalContext.current
        val dataFormat = SimpleDateFormat(" HH:mm:ss")
        var pincode by remember {
            mutableStateOf(context.getString(R.string.empty))
        }
        var pincodePrompt by remember {
            mutableStateOf(context.getString(R.string.input_pincode))
        }
        var pincodeState by remember {
            mutableStateOf(PincodeState.NOT_YET)
        }
        var accessDenied by remember {
            mutableStateOf(false)
        }

        val buttonFraction = 0.15f

        LaunchedEffect(Unit) {
            if (System.currentTimeMillis() < localDataStorage[context.getString(R.string.block_till), Long::class.java]!!) {
                accessDenied = true
            }
        }

        LaunchedEffect(pincodeState) {
            when (pincodeState) {
                PincodeState.CORRECT -> {
                    viewModel.viewModelScope.launch {
                        delay(1000)
                        navController.navigate(context.getString(R.string.home_view))
                    }
                }

                PincodeState.INCORRECT -> {
                    manageMismatch(context) { accessDenied = true }
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        delay(1000)
                        pincodeState = PincodeState.NOT_YET
                    }
                }

                else -> Unit
            }
        }

        LaunchedEffect(pincodeState) {
            pincodePrompt = when (pincodeState) {
                PincodeState.NOT_YET -> context.getString(R.string.input_pincode)
                PincodeState.CORRECT -> context.getString(R.string.welcome)
                PincodeState.INCORRECT -> context.getString(R.string.incorrect_pincode)
                else -> context.getString(R.string.empty)
            }
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = pincodePrompt, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.fillMaxHeight(0.015f))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(0.3f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        items(4) {
                            Icon(
                                Icons.Default.Circle,
                                null,
                                tint = getIconTint(it, pincode),
                                modifier = Modifier.fillMaxSize(0.025f)
                            )
                        }
                    }
                }
                Text(
                    if (accessDenied) context.getString(R.string.pincode_blocked) + dataFormat.format(
                        Date(localDataStorage[context.getString(R.string.block_till), Long::class.java]!!)
                    ) else context.getString(R.string.empty),
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .fillMaxHeight(0.5f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        items(3) {
                            val topIndex = it
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                items(3) {
                                    val currentNumber = 3 * topIndex + it + 1
                                    Button(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (System.currentTimeMillis() > localDataStorage[context.getString(
                                                    R.string.block_till
                                                ), Long::class.java]!! && pincodeState == PincodeState.NOT_YET
                                            ) {
                                                accessDenied = false
                                                verifyPincode(context,
                                                    currentNumber.toString(),
                                                    pincode,
                                                    { pincode = it },
                                                    { pincodeState = it })
                                            }
                                        },
                                        shape = CircleShape,
                                    ) {
                                        Text(
                                            text = "$currentNumber",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                item {
                                    Button(
                                        enabled = false,
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {},
                                        shape = CircleShape,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContainerColor = colorResource(
                                                id = R.color.transparent
                                            )
                                        )
                                    ) {}
                                }
                                item {
                                    Button(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (System.currentTimeMillis() > localDataStorage[context.getString(
                                                    R.string.block_till
                                                ), Long::class.java]!! && pincodeState == PincodeState.NOT_YET
                                            ) {
                                                accessDenied = false
                                                verifyPincode(context,
                                                    context.getString(R.string.zero),
                                                    pincode,
                                                    { pincode = it },
                                                    { pincodeState = it })
                                            }
                                        },
                                        shape = CircleShape,
                                    ) {
                                        Text(
                                            text = stringResource(id = R.string.zero),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                                item {
                                    IconButton(
                                        modifier = Modifier
                                            .height((LocalConfiguration.current.screenWidthDp * buttonFraction).dp)
                                            .width((LocalConfiguration.current.screenWidthDp * buttonFraction).dp),
                                        onClick = {
                                            if (pincode.isNotEmpty() && pincodeState == PincodeState.NOT_YET) {
                                                pincode = pincode.dropLast(1)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Backspace,
                                            null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .height((LocalConfiguration.current.screenWidthDp * 0.1).dp)
                                                .width((LocalConfiguration.current.screenWidthDp * 0.1).dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private fun manageMismatch(context: Context, setAccessDenied: () -> Unit) {
        val previousMismatchCounterValue =
            localDataStorage[context.getString(R.string.mismatch_counter), Int::class.java]!!
        localDataStorage[context.getString(R.string.mismatch_counter)] =
            previousMismatchCounterValue + 1
        if (previousMismatchCounterValue + 1 == BuildConfig.Tries) {
            localDataStorage[context.getString(R.string.block_till)] =
                System.currentTimeMillis() + BuildConfig.Penalty
            localDataStorage[context.getString(R.string.mismatch_counter)] = 0
            setAccessDenied()
        }
    }

    private fun verifyPincode(
        context: Context,
        currentNumber: String,
        pincode: String,
        setPincode: (String) -> Unit,
        setPincodeState: (PincodeState) -> Unit
    ) {
        val currentPincode = pincode + currentNumber
        if (pincode.length < 4) {
            setPincode(currentPincode)
        }
        if (currentPincode.length == 4) {
            if (localDataStorage[context.getString(R.string.pincode), String::class.java] == currentPincode) {
                setPincodeState(PincodeState.CORRECT)
            } else {
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    delay(500)
                    setPincode(context.getString(R.string.empty))
                }
                setPincodeState(PincodeState.INCORRECT)
            }
        }
    }

    private fun managePincode(
        context: Context,
        currentNumber: String,
        pincode: String,
        savedPincode: String,
        setSavedPincode: (String) -> Unit,
        setPincode: (String) -> Unit,
        setPincodeState: (PincodeState) -> Unit
    ) {
        val currentPincode = pincode + currentNumber
        if (pincode.length < 4) {
            setPincode(currentPincode)
        }
        if (currentPincode.length == 4) {
            if (savedPincode.isEmpty()) {
                setSavedPincode(currentPincode)
                setPincodeState(PincodeState.SAVED)
                viewModel.viewModelScope.launch(Dispatchers.IO) {
                    delay(500)
                    setPincode(context.getString(R.string.empty))
                }
            } else {
                if (savedPincode == currentPincode) {
                    localDataStorage[context.getString(R.string.pincode)] = savedPincode
                    setPincodeState(PincodeState.CORRECT)
                } else {
                    viewModel.viewModelScope.launch(Dispatchers.IO) {
                        delay(500)
                        setSavedPincode(context.getString(R.string.empty))
                        setPincode(context.getString(R.string.empty))
                    }
                    setPincodeState(PincodeState.INCORRECT)
                }
            }
        }
    }

    @Composable
    private fun getIconTint(number: Int, pincode: String): Color {
        return if (pincode.length >= number + 1) {
            MaterialTheme.colorScheme.primary
        } else Color.LightGray
    }
}