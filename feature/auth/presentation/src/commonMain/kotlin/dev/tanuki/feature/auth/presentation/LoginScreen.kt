package dev.tanuki.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.tanuki.core.presentation.ObserveAsEvents
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
        Text(
            text = "GitLab Mobile",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "Review merge requests on the go.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        if (state.isAuthenticating) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        } else {
            Button(onClick = { onAction(LoginAction.OnLoginClick) }) {
                Text("Sign in with GitLab")
            }
        }

        state.error?.let { error ->
            Text(
                text = error.asString(),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
