package com.vladislaviliev.meet.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vladislaviliev.meet.R
import com.vladislaviliev.meet.ui.theme.MeetTheme
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LoginScreen(onLoggedIn: () -> Unit) {
    val vm = koinViewModel<LoginViewModel>()
    val state by vm.state.collectAsStateWithLifecycle()
    if (state is LoginState.Success) {
        onLoggedIn()
        return
    }
    LoginScreen(vm::login, state)
}

@Composable
internal fun LoginScreen(onLoginClicked: (String, String) -> Unit, state: LoginState, modifier: Modifier = Modifier) {
    Surface(modifier) {
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(15.dp)
        ) {
            Contents(onLoginClicked, state)
        }
    }
}

@Composable
private fun BoxScope.Contents(
    onLoginClicked: (String, String) -> Unit, state: LoginState, modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(stringResource(R.string.sign_in_to_your_account), style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
        )

        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onLoginClicked(email, password) }),
            trailingIcon = { PasswordTrailingIcon(passwordVisible) { passwordVisible = !passwordVisible } }
        )

        TextButton({}, Modifier.align(Alignment.CenterHorizontally)) {
            Text(stringResource(R.string.forgot_password))
        }

        Spacer(Modifier.height(16.dp))
        StateIndicator(state)
    }
    Button(
        { onLoginClicked(email, password) },
        Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
    ) { Text(stringResource(R.string.login)) }
}

@Composable
private fun PasswordTrailingIcon(isVisible: Boolean, onClick: () -> Unit) {
    val image = if (isVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
    val description = if (isVisible) stringResource(R.string.hide_password) else stringResource(R.string.show_password)
    IconButton(onClick) { Icon(image, description) }
}

@Composable
private fun StateIndicator(state: LoginState, modifier: Modifier = Modifier) {
    if (state is LoginState.Idle)
        return
    if (state is LoginState.Loading) {
        CircularProgressIndicator(modifier)
        return
    }
    require(state is LoginState.Error)
    Text(state.message, modifier, color = MaterialTheme.colorScheme.error)
}

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun LoginScreenPreview() {
    MeetTheme {
        LoginScreen({ _, _ -> }, LoginState.Idle)
    }
}
