package com.quicktrim.ai.ui.fillerwords

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.BottomSheetNavigatorSheetState
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.quicktrim.MainViewModel
import com.quicktrim.ai.ui.Routes
import com.quicktrim.ai.ui.theme.QuicktrimandroidTheme

@OptIn(ExperimentalMaterialNavigationApi::class)
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
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Add Filler Words",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
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
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            submit()
                            navigator.popBackStack()
                        }
                    ) {
                        Text("SUBMIT")
                    }
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(fillerWords.toList()) { fillerWord ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = fillerWord,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.fillMaxWidth(0.6f)
                            )

                            IconButton(
                                onClick = {
                                    mainViewModel.onRemoveFillerWord(fillerWord)
                                },
                                modifier = Modifier
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "remove"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}