package dev.tanuki.feature.auth.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
import dev.tanuki.feature.auth.presentation.resources.Res
import dev.tanuki.feature.auth.presentation.resources.tanuki_logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoot(
    onLoggedIn: () -> Unit,
    onLaunchOAuth: (url: String) -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is LoginEvent.LaunchOAuth -> onLaunchOAuth(event.url)
            LoginEvent.LoggedIn -> onLoggedIn()
            is LoginEvent.ShowError -> Unit // surfaced via state.error
        }
    }

    LoginScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.tanuki_logo),
            contentDescription = "Tanuki",
            modifier = Modifier.size(96.dp).padding(bottom = 12.dp),
        )
        Text(text = "Tanuki", style = MaterialTheme.typography.headlineMedium)
        Text(
            text = "Your GitLab projects on the go.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        if (state.isAuthenticating) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else if (state.tokenMode) {
            TokenLogin(state = state, onAction = onAction)
        } else {
            Button(onClick = { onAction(LoginAction.OnLoginClick) }) {
                Text("Sign in with GitLab")
            }
            TextButton(onClick = { onAction(LoginAction.OnUseTokenClick) }) {
                Text("Use a personal access token")
            }
        }

        state.error?.let { error ->
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

@Composable
private fun TokenLogin(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    OutlinedTextField(
        value = state.instanceUrl,
        onValueChange = { onAction(LoginAction.OnInstanceChange(it)) },
        label = { Text("GitLab instance") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.token,
        onValueChange = { onAction(LoginAction.OnTokenChange(it)) },
        label = { Text("Personal access token") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
    )
    Text(
        text = "Create one in GitLab → Settings → Access Tokens with the 'api' or 'read_api' scope.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp),
    )
    Button(
        onClick = { onAction(LoginAction.OnSubmitToken) },
        enabled = state.canSubmitToken,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Text("Continue")
    }
    TextButton(onClick = { onAction(LoginAction.OnUseOAuthClick) }) {
        Text("Back to one-tap sign in")
    }
}
