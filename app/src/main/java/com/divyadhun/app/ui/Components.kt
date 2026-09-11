package com.divyadhun.app.ui
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.divyadhun.app.ui.theme.*
@Composable fun DevotionalArtwork(modifier: Modifier = Modifier) {
 val gold = LocalDevotionalColors.current.gold
 Canvas(modifier.size(104.dp)) {
  val w = size.width; val h = size.height
  drawCircle(gold.copy(alpha = 0.16f), w * .47f, style = Stroke(1.dp.toPx()))
  val petal = Path().apply { moveTo(w*.5f,h*.16f); cubicTo(w*.18f,h*.47f,w*.38f,h*.63f,w*.5f,h*.7f); cubicTo(w*.62f,h*.63f,w*.82f,h*.47f,w*.5f,h*.16f) }
  drawPath(petal,gold,style=Stroke(2.dp.toPx()))
  drawArc(gold,0f,180f,false,topLeft=Offset(w*.2f,h*.47f),size=androidx.compose.ui.geometry.Size(w*.6f,h*.38f),style=Stroke(2.dp.toPx()))
 }
}
@Composable fun SectionHeader(title: String) { Text(title, style = MaterialTheme.typography.titleLarge) }
@Composable fun EmptyState(title: String, body: String) {
 Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f), shape = MaterialTheme.shapes.large) {
  Column(Modifier.fillMaxWidth().padding(Space.lg), verticalArrangement = Arrangement.spacedBy(Space.sm)) {
   Text(title, style = MaterialTheme.typography.titleLarge)
   Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
 }
}
@Composable fun DivyaButton(label: String, onClick: () -> Unit) {
 Button(onClick, modifier = Modifier.heightIn(min = 52.dp), contentPadding = PaddingValues(horizontal = Space.lg, vertical = Space.md)) { Text(label) }
}

