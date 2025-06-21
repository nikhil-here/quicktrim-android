package com.quicktrim.ai.ui.fillerwords

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.quicktrim.MainViewModel
import com.quicktrim.ai.R
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateFillerWordsScreen(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    navigator: NavController
) {
    QuicktrimandroidTheme {
        Surface {
            val fillerWords by mainViewModel.fillerWords.collectAsStateWithLifecycle()
            var userText by remember {
                mutableStateOf(TextFieldValue(""))
            }

            fun submit() {
                if (!userText.text.isEmpty()) {
                    mainViewModel.onAddFillerWord(userText.text)
                    userText = TextFieldValue("")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(top = 56.dp, bottom = 16.dp),
                    text = "Add Filler Words",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(2f),
                        value = userText,
                        onValueChange = {
                            userText = it
                        },
                        maxLines = 1,
                        minLines = 1,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                submit()
                                navigator.popBackStack()
                            }
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        )
                    )
                    Spacer(Modifier.width(6.dp))
                    FilledIconButton(
                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize()),
                        onClick = {
                            submit()
                        },
                        colors =  IconButtonDefaults.filledIconButtonColors(),
                        shape = IconButtonDefaults.mediumRoundShape
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = stringResource(R.string.cd_filler_words),
                            modifier = Modifier.size(IconButtonDefaults.mediumIconSize),
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize().background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(6.dp)
                    )
                        .padding(bottom = 56.dp),
                ) {
                    items(fillerWords.toList()) { fillerWord ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fillerWord,
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )

                            IconButton(
                                onClick = {
                                    mainViewModel.onRemoveFillerWord(fillerWord)
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}