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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mindforge101.app.data.ProfileEntity
import com.mindforge101.app.data.Settings

private val AvatarColors = listOf(
    Color(0xFFD6E6FD) to Color(0xFF2F6FE4),
    Color(0xFFEADFFB) to Color(0xFF8B5CD6),
    Color(0xFFD5F2E5) to Color(0xFF1F9D6B),
    Color(0xFFFDE5CF) to Color(0xFFE08A2B),
    Color(0xFFFBDDE6) to Color(0xFFD14B78),
)

@Composable
fun ProfileScreen(
    profiles: List<ProfileEntity>,
    active: ProfileEntity?,
    settings: Settings,
    vm: AppViewModel,
) {
    var nameDialog by remember { mutableStateOf<ProfileEntity?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf<ProfileEntity?>(null) }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader("Profile", "Manage your players and settings", "👥")

        CardSurface {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionIcon("☻", Blue, BlueSoft)
                    Spacer(Modifier.width(10.dp))
                    Text("Players on this device", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.clip(CircleShape).background(Blue)
                        .clickable(onClickLabel = "Add a player") { adding = true }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Add Player", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(end = 4.dp)) {
                    items(profiles, key = { it.id }) { p ->
                        val index = profiles.indexOf(p)
                        PlayerCard(
                            name = p.name, index = index, current = p.id == active?.id, canDelete = profiles.size > 1,
                            onSelect = { vm.switchProfile(p.id) },
                            onRename = { nameDialog = p },
                            onDelete = { deleting = p },
                        )
                    }
                }
            }
        }

        CardSurface {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionIcon("⚙", Blue, BlueSoft)
                    Spacer(Modifier.width(10.dp))
                    Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingToggle("Tт", "Large text", "Make text larger and easier to read", settings.largeText, Color(0xFFE1E9FB), Blue, vm::setLargeText)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingToggle("◌", "Reduce motion", "Minimize animations and visual effects", settings.reduceMotion, Color(0xFFEADFFB), Color(0xFF7B4FD6), vm::setReduceMotion)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                SettingToggle("📳", "Vibrate on wrong answers", "Get a gentle vibration when your answer is wrong", settings.haptics, Color(0xFFD5F2E5), Color(0xFF1F9D6B), vm::setHaptics)
            }
        }

        CardSurface {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionIcon("⛨", Blue, BlueSoft)
                    Spacer(Modifier.width(10.dp))
                    Text("About & Privacy", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                InfoRow("📵", "Works fully offline", "No internet, no accounts, no ads", Color(0xFFFBDDE6), Color(0xFFD14B78))
                InfoRow("🔒", "Your data stays on this device", "All progress is stored locally", Color(0xFFD5F2E5), Color(0xFF1F9D6B))
                InfoRow("🗑", "Data is removed on uninstall", "All your data is deleted when the app is uninstalled", Color(0xFFFDE5CF), Color(0xFFE08A2B))
                InfoRow("ⓘ", "For practice and fun", "Games are for practice and fun, not a medical or IQ measure", Color(0xFFD6E6FD), Blue, last = true)
            }
        }
    }

    if (adding) {
        NameDialog("Add player", "", onDismiss = { adding = false }) {
            vm.addProfile(it)
            adding = false
        }
    }
    nameDialog?.let { p ->
        NameDialog("Rename player", p.name, onDismiss = { nameDialog = null }) {
            vm.renameProfile(p.id, it)
            nameDialog = null
        }
    }
    deleting?.let { p ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete ${p.name}?") },
            text = { Text("This removes their progress, scores and achievements from this device. It can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteProfile(p.id)
                    deleting = null
                }) { Text("Delete player") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep player") } },
        )
    }
}

@Composable
private fun SectionIcon(glyph: String, fg: Color, bg: Color) {
    Box(Modifier.size(40.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
        Text(glyph, fontSize = 20.sp, color = fg)
    }
}

@Composable
private fun PlayerCard(
    name: String, index: Int, current: Boolean, canDelete: Boolean,
    onSelect: () -> Unit, onRename: () -> Unit, onDelete: () -> Unit,
) {
    val (bg, fg) = AvatarColors[index % AvatarColors.size]
    var menu by remember { mutableStateOf(false) }
    Box(
        Modifier.width(100.dp).clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(if (current) 2.dp else 1.dp, if (current) Blue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
            .clickable(onClickLabel = "Switch to $name", onClick = onSelect)
            .semantics { stateDescription = if (current) "Current player" else "Not selected" },
    ) {
        Column(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 12.dp, start = 6.dp, end = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(bg), contentAlignment = Alignment.Center) {
                Text("👤", fontSize = 26.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            if (current) {
                Text(
                    "Current", fontSize = 11.sp, color = Blue, fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clip(CircleShape).background(BlueSoft).padding(horizontal = 10.dp, vertical = 2.dp),
                )
            } else {
                Spacer(Modifier.height(18.dp))
            }
        }
        Box(Modifier.align(Alignment.TopEnd)) {
            Box(
                Modifier.size(36.dp).clip(CircleShape)
                    .clickable(onClickLabel = "Options for $name") { menu = true }
                    .semantics { contentDescription = "Options for $name" },
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.MoreVert, contentDescription = null, modifier = Modifier.size(18.dp), tint = fg.copy(alpha = 0.9f)) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; onRename() })
                if (canDelete) DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
            }
        }
    }
}

@Composable
private fun SettingToggle(
    glyph: String, title: String, desc: String, value: Boolean,
    bg: Color, fg: Color, onChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClickLabel = title) { onChange(!value) }.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
            Text(glyph, fontSize = 20.sp, color = fg, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = value, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = Blue, checkedThumbColor = Color.White),
        )
    }
}

@Composable
private fun InfoRow(glyph: String, title: String, desc: String, bg: Color, fg: Color, last: Boolean = false) {
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(bg), contentAlignment = Alignment.Center) {
            Text(glyph, fontSize = 20.sp, color = fg, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
