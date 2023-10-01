package screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import repo.MainRepository

enum class LoginStatus {
    Idle,
    Loading,
    Failed
}

@Composable
fun LoginScreen(onSuccess: () -> Unit) {
    val mainRepository: MainRepository = koinInject()
    val scope = rememberCoroutineScope { Dispatchers.IO }
    val focusManager = LocalFocusManager.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(LoginStatus.Idle) }

    fun logIn() {
        scope.launch {
            status = LoginStatus.Loading
            mainRepository.logIn(username, password).let {
                status = if (it) {
                    onSuccess()
                    LoginStatus.Idle
                } else
                    LoginStatus.Failed
            }
        }
    }
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Log In", fontSize = 20.sp)
        Spacer(Modifier.height(20.dp).fillMaxWidth())
        TextField(
            value = username,
            onValueChange = {
                username = it
                status = LoginStatus.Idle
            },
            label = { Text("username") },
            singleLine = true, maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )
        TextField(
            value = password,
            onValueChange = {
                password = it
                status = LoginStatus.Idle
            },
            label = { Text("password") },
            singleLine = true, maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    logIn()
                }
            )
        )
        if (status != LoginStatus.Idle) {
            Text("status: ${status.name}")
        }
        Button({ logIn() }) {
            Text("Log In")
        }
    }
}