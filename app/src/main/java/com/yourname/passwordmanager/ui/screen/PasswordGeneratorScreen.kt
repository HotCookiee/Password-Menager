package com.yourname.passwordmanager.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.yourname.passwordmanager.ui.viewmodel.PasswordViewModel

@Composable
fun PasswordGeneratorScreen(
    viewModel: PasswordViewModel,
    onNavigateBack: () -> Unit,
    onPasswordGenerated: (String) -> Unit = {}
) {
    var length by remember { mutableIntStateOf(16) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }
    var generatedPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        // Header - убрали лишние отступы
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                "Password Generator",
                style = MaterialTheme.typography.headlineSmall
            )
            Button(
                onClick = {
                    generatedPassword = viewModel.generatePassword(
                        length, includeUppercase, includeLowercase, includeNumbers, includeSymbols
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Generate")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Generated Password Card
        if (generatedPassword.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Your New Password:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        generatedPassword,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        Button(
                            onClick = { onPasswordGenerated(generatedPassword) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Use This")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { /* Copy to clipboard */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Settings Card - добавим отступ если пароль не сгенерирован
        else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Password Settings",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Length: $length characters", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = length.toFloat(),
                    onValueChange = { length = it.toInt() },
                    valueRange = 8f..32f,
                    steps = 24
                )

                Spacer(modifier = Modifier.height(16.dp))

                PasswordOption(
                    text = "Uppercase Letters (A-Z)",
                    checked = includeUppercase,
                    onCheckedChange = { includeUppercase = it }
                )
                PasswordOption(
                    text = "Lowercase Letters (a-z)",
                    checked = includeLowercase,
                    onCheckedChange = { includeLowercase = it }
                )
                PasswordOption(
                    text = "Numbers (0-9)",
                    checked = includeNumbers,
                    onCheckedChange = { includeNumbers = it }
                )
                PasswordOption(
                    text = "Symbols (!@#$%)",
                    checked = includeSymbols,
                    onCheckedChange = { includeSymbols = it }
                )
            }
        }
    }
}

@Composable
private fun PasswordOption(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}