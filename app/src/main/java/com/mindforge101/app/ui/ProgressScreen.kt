package com.mindforge101.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.data.AchievementEntity
import com.mindforge101.app.data.ProfileEntity
import com.mindforge101.app.data.SessionEntity
import com.mindforge101.app.domain.Achievement
import com.mindforge101.app.domain.Achievements
import com.mindforge101.app.domain.Category

private val Green = Color(0xFF22A06B)
private val Amber = Color(0xFFE59A1F)
private val Red = Color(0xFFD9483B)

private val BadgeColors = listOf(
    Color(0xFF3F7FEF) to Color(0xFF1F55C7),
    Color(0xFF9B7BEF) to Color(0xFF6D4BD1),
    Color(0xFF2DBE8B) to Color(0xFF0F8F6E),
    Color(0xFFF7B84A) to Color(0xFFE08A12),
    Color(0xFFEF7A9B) to Color(0xFFC03A68),
)

@Composable
fun ProgressScreen(profile: ProfileEntity?, sessions: List<SessionEntity>, unlocked: List<AchievementEntity>) {
    val scores = categoryScores(sessions)
    val unlockedIds = unlocked.map { it.id }.toSet()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader("Progress", "Track your growth and brain performance", "🏆")
        RankCard(profile?.totalXp ?: 0, profile?.streakDays ?: 0)
        CategoryCard(scores)
        AchievementsCard(unlockedIds)
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.Top) {
            Text("ⓘ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "Note: Games are for practice and fun, not a medical or IQ measure.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CategoryCard(scores: Map<Category, Int>) {
    CardSurface {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(BlueSoft), contentAlignment = Alignment.Center) {
                    Text("▂▅▇", fontSize = 14.sp, color = Blue, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Category Performance", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("Your recent accuracy in each category", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(14.dp))
            Category.entries.chunked(5).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { c -> CategoryTile(c, scores[c], Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun CategoryTile(c: Category, score: Int?, modifier: Modifier) {
    val t = tintFor(c)
    val color = when {
        score == null -> MaterialTheme.colorScheme.outline
        score >= 80 -> Green
        score >= 60 -> Amber
        else -> Red
    }
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(t.bg.copy(alpha = 0.55f))
            .semantics { contentDescription = "${c.label}: ${score?.let { "$it percent" } ?: "not played"}" }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.height(26.dp), contentAlignment = Alignment.Center) {
            Text(t.glyph, fontSize = 20.sp, color = t.fg, fontWeight = FontWeight.Bold, maxLines = 1)
        }
        Text(
            if (c == Category.PROBLEM) "Problem\nSolving" else c.label,
            fontSize = 11.sp, lineHeight = 13.sp, textAlign = TextAlign.Center, minLines = 2, maxLines = 2,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(score?.let { "$it%" } ?: "–", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(Modifier.fillMaxWidth(((score ?: 0) / 100f).coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(color))
        }
    }
}

@Composable
private fun AchievementsCard(unlockedIds: Set<String>) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val sorted = Achievements.all.sortedByDescending { it.id in unlockedIds }
    CardSurface {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(BlueSoft), contentAlignment = Alignment.Center) {
                    Text("★", fontSize = 20.sp, color = Blue)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("Achievements", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("${unlockedIds.size} of ${Achievements.all.size} Unlocked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Row(
                    Modifier.clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), CircleShape)
                        .clickable(onClickLabel = if (expanded) "Show fewer achievements" else "Show all achievements") { expanded = !expanded }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) { Text(if (expanded) "Show Less" else "View All  ›", fontSize = 13.sp) }
            }
            Spacer(Modifier.height(14.dp))
            if (!expanded) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    sorted.take(5).forEachIndexed { i, a -> BadgeTile(a, a.id in unlockedIds, i, Modifier.weight(1f)) }
                }
            } else {
                sorted.forEachIndexed { i, a -> AchievementRow(a, a.id in unlockedIds, i) }
            }
        }
    }
}

@Composable
private fun Badge(index: Int, got: Boolean, size: Int) {
    val (a, b) = BadgeColors[index % BadgeColors.size]
    Box(
        Modifier.size(size.dp).clip(RoundedCornerShape((size / 3).dp))
            .background(if (got) Brush.linearGradient(listOf(a, b)) else Brush.linearGradient(listOf(Color(0xFFC9CBD3), Color(0xFF9EA2AF)))),
        contentAlignment = Alignment.Center,
    ) { Text(if (got) "★" else "🔒", fontSize = (size / 2.4f).sp, color = Color.White) }
}

@Composable
private fun BadgeTile(a: Achievement, got: Boolean, index: Int, modifier: Modifier) {
    Column(
        modifier.semantics { contentDescription = "${a.title}, ${if (got) "unlocked" else "locked"}. ${a.description}" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Badge(index, got, 56)
        Spacer(Modifier.height(6.dp))
        Text(
            a.title, fontSize = 12.sp, lineHeight = 14.sp, textAlign = TextAlign.Center, minLines = 2, maxLines = 2,
            overflow = TextOverflow.Ellipsis, fontWeight = if (got) FontWeight.SemiBold else FontWeight.Normal,
            color = if (got) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(if (got) "✓" else " ", color = Green, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun AchievementRow(a: Achievement, got: Boolean, index: Int) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Badge(index, got, 44)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(a.title, fontWeight = FontWeight.SemiBold, color = if (got) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(a.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(if (got) "✓ Earned" else "Locked", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = if (got) Green else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
