package io.github.winter000boy.minimalist.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.winter000boy.minimalist.R
import io.github.winter000boy.minimalist.data.LauncherApp
import io.github.winter000boy.minimalist.data.recentlyInstalled
import io.github.winter000boy.minimalist.data.search
import kotlinx.coroutines.launch

/** The full app list: search on top, names only, A–Z jump bar on the right. */
@Composable
fun AppDrawer(
    apps: List<LauncherApp>,
    homeAppKeys: Set<String>,
    query: String,
    onQueryChange: (String) -> Unit,
    actions: AppActions,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val results = remember(apps, query) { apps.search(query) }
    val recent = remember(apps) { recentlyInstalled(apps) }
    val showRecent = query.isBlank() && recent.isNotEmpty()

    LaunchedEffect(query) { listState.scrollToItem(0) }

    Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            onGo = { results.firstOrNull()?.let(actions.launch) },
        )
        Row(Modifier.weight(1f)) {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (showRecent) {
                    item(key = "recent-label") {
                        Text(
                            text = stringResource(R.string.recently_installed),
                            color = Muted,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 4.dp),
                        )
                    }
                    items(recent, key = { "recent-${it.key}" }) { app ->
                        AppRow(app, isOnHome = app.key in homeAppKeys, actions = actions)
                    }
                    item(key = "recent-divider") {
                        HorizontalDivider(
                            color = Color.White,
                            modifier = Modifier.padding(start = 24.dp, top = 8.dp, bottom = 8.dp),
                        )
                    }
                }
                items(results, key = { it.key }) { app ->
                    AppRow(app, isOnHome = app.key in homeAppKeys, actions = actions)
                }
                if (results.isEmpty() && apps.isNotEmpty()) {
                    item(key = "no-results") {
                        Text(
                            text = stringResource(R.string.no_matching_apps, query.trim()),
                            color = Muted,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        )
                    }
                }
            }

            if (query.isBlank()) {
                // List position of the first app under each letter.
                val letterPositions = remember(results, showRecent) {
                    val offset = if (showRecent) recent.size + 2 else 0
                    buildMap {
                        results.forEachIndexed { i, app -> putIfAbsent(app.indexLetter, i + offset) }
                    }
                }
                AlphabetIndex(
                    letters = letterPositions.keys.toList(),
                    onLetter = { letter ->
                        scope.launch { listState.scrollToItem(letterPositions.getValue(letter)) }
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onGo: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val textStyle = TextStyle(color = Color.White, fontSize = 20.sp, fontFamily = FontFamily.Serif)

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = textStyle,
            cursorBrush = SolidColor(Color.White),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onGo() }),
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            decorationBox = { innerTextField ->
                Column {
                    Box(Modifier.padding(vertical = 10.dp)) {
                        if (query.isEmpty()) {
                            Text(stringResource(R.string.search_hint), style = textStyle, color = Muted)
                        }
                        innerTextField()
                    }
                    HorizontalDivider(color = Color.White)
                }
            },
        )
        if (query.isEmpty()) {
            IconButton(onClick = { focusRequester.requestFocus() }) {
                Icon(painterResource(R.drawable.ic_search), stringResource(R.string.search))
            }
        } else {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(painterResource(R.drawable.ic_close), stringResource(R.string.clear_search))
            }
        }
    }
}

/** Tap or drag along the letters to jump through the list. */
@Composable
private fun AlphabetIndex(letters: List<Char>, onLetter: (Char) -> Unit) {
    if (letters.isEmpty()) return
    val haptics = LocalHapticFeedback.current
    var active by remember { mutableStateOf<Char?>(null) }

    Box(Modifier.fillMaxHeight().width(36.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .heightIn(max = (letters.size * 26).dp)
                .fillMaxHeight()
                .pointerInput(letters) {
                    fun select(y: Float) {
                        val index = (y / size.height * letters.size).toInt().coerceIn(0, letters.lastIndex)
                        val letter = letters[index]
                        if (letter != active) {
                            active = letter
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onLetter(letter)
                        }
                    }
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        down.consume()
                        select(down.position.y)
                        while (true) {
                            val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id }
                            if (change == null || !change.pressed) break
                            change.consume()
                            select(change.position.y)
                        }
                        active = null
                    }
                },
        ) {
            letters.forEach { letter ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = letter.toString(),
                        fontSize = if (letter == active) 18.sp else 13.sp,
                        color = if (letter == active) Color.White else Muted,
                    )
                }
            }
        }
    }
}
