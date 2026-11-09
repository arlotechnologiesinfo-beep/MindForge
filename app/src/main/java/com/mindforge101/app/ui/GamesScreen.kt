package com.mindforge101.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.data.GameProgressEntity
import com.mindforge101.app.domain.AdaptiveDifficultyEngine
import com.mindforge101.app.domain.Category
import com.mindforge101.app.domain.GameDef
import com.mindforge101.app.games.Catalog

/** Shared page header: brand line, big title, subtitle and a decorative badge on the right. */
@Composable
internal fun PageHeader(title: String, subtitle: String, art: String, trailing: @Composable (() -> Unit)? = null) {
    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text("MindForge 101", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Blue)
            Spacer(Modifier.height(8.dp))
            Text(title, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            trailing?.invoke()
            Box(
                Modifier.padding(top = if (trailing != null) 4.dp else 0.dp).size(72.dp).clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color(0xFF8DB8FF), Color(0xFFDDE9FD)))),
                contentAlignment = Alignment.Center,
            ) { Text(art, fontSize = 34.sp) }
        }
    }
}

@Composable
fun GamesScreen(progress: Map<String, GameProgressEntity>, onPlay: (String) -> Unit) {
    var selected by rememberSaveable { mutableStateOf<Category?>(null) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val q = query.trim()
    val games = Catalog.games.filter {
        (selected == null || it.category == selected) &&
            (q.isEmpty() || it.name.contains(q, ignoreCase = true) || it.blurb.contains(q, ignoreCase = true))
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PageHeader(
                    "Games", "${Catalog.games.size} brain-training games across ${Category.entries.size} categories", "🎮",
                    trailing = {
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable(onClickLabel = if (searching) "Close search" else "Search games") {
                                    searching = !searching
                                    if (!searching) query = ""
                                },
                            contentAlignment = Alignment.Center,
                        ) { Icon(if (searching) Icons.Filled.Close else Icons.Filled.Search, contentDescription = null) }
                    },
                )
                if (searching) SearchField(query) { query = it }
            }
        }
        item {
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { CategoryChip("▦", "All", selected == null) { selected = null } }
                items(Category.entries) { c ->
                    CategoryChip(tintFor(c).glyph, c.label, selected == c) { selected = c }
                }
            }
        }
        if (games.isEmpty()) {
            item {
                Text(
                    "No games match your search.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                )
            }
        }
        items(games.chunked(2), key = { it.first().id }) { pair ->
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { game -> GameCard(game, onPlay, Modifier.weight(1f)) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SearchField(value: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(8.dp))
        Box(Modifier.weight(1f)) {
            if (value.isEmpty()) Text("Search games", color = MaterialTheme.colorScheme.onSurfaceVariant)
            BasicTextField(
                value = value, onValueChange = { onChange(it.take(30)) }, singleLine = true,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
                cursorBrush = SolidColor(Blue),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun CategoryChip(glyph: String, label: String, selected: Boolean, onClick: () -> Unit) {
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Row(
        Modifier.mfClickable(CircleShape, onClick = onClick)
            .background(if (selected) Blue else MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, if (selected) Blue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), CircleShape)
            .semantics { contentDescription = "$label${if (selected) ", selected" else ""}" }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(glyph, color = if (selected) Color.White else Blue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(label, color = fg, fontSize = 15.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
    }
}


/** Square tile: just the game's icon and name. Tapping anywhere opens the game. */
@Composable
private fun GameCard(game: GameDef, onPlay: (String) -> Unit, modifier: Modifier) {
    Surface(
        modifier.aspectRatio(1f)
            .mfClickable(RoundedCornerShape(24.dp), "Open ${game.name}") { onPlay(game.id) }
            .shadow(4.dp, RoundedCornerShape(24.dp), ambientColor = Blue.copy(alpha = 0.12f), spotColor = Blue.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            GameIcon(game, 84.dp)
            Spacer(Modifier.height(12.dp))
            Text(
                game.name, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
