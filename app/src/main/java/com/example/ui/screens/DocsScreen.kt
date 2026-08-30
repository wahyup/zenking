package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DocItem(
    val title: String,
    val description: String,
    val codeSnippet: String,
    val tag: String
)

@Composable
fun DocsScreen(
    modifier: Modifier = Modifier
) {
    val docs = listOf(
        DocItem(
            title = "BoxLayout (Responsive Flow)",
            description = "Arranges children either vertically or horizontally with proportional size_hint weights.",
            codeSnippet = "from kivy.uix.boxlayout import BoxLayout\n\nlayout = BoxLayout(orientation='vertical', padding=12, spacing=8)\nlayout.add_widget(Button(text='Submit', size_hint=(1, 0.2)))",
            tag = "Layout"
        ),
        DocItem(
            title = "GridLayout (Table & Dashboard Grid)",
            description = "Arranges widgets in fixed columns or rows. Ideal for calculators, game boards, or metric tiles.",
            codeSnippet = "from kivy.uix.gridlayout import GridLayout\n\ngrid = GridLayout(cols=2, rows=2, spacing=10)\ngrid.add_widget(Label(text='Metric 1'))\ngrid.add_widget(Label(text='Metric 2'))",
            tag = "Layout"
        ),
        DocItem(
            title = "Slider & Reactive Binding",
            description = "Interactive slider with min, max, step, and value listener.",
            codeSnippet = "slider = Slider(min=0, max=100, value=50, step=1)\nslider.bind(on_value=lambda inst, val: print(f'Slider: {val}'))",
            tag = "Widget"
        ),
        DocItem(
            title = "Kivy Clock Loop (60 FPS Game / Animation)",
            description = "Schedules recurring callbacks for physics, timers, animations, or dynamic counters.",
            codeSnippet = "from kivy.clock import Clock\n\ndef tick(dt):\n    # dt is delta time in seconds (~0.016s for 60fps)\n    pass\n\nClock.schedule_interval(tick, 1.0 / 60.0)",
            tag = "Clock"
        ),
        DocItem(
            title = "Kivy Canvas Graphics & Shapes",
            description = "Draws hardware-accelerated shapes (Color, Rectangle, Ellipse, Line) inside widgets.",
            codeSnippet = "from kivy.graphics import Color, Ellipse, Rectangle\n\nwith widget.canvas:\n    Color(0.1, 0.9, 0.5, 1)\n    ball = Ellipse(pos=(100, 100), size=(50, 50))",
            tag = "Graphics"
        ),
        DocItem(
            title = "Kivy Language (.kv) Rules",
            description = "Declarative separation of UI layout and event bindings.",
            codeSnippet = "<MyLayout>:\n    orientation: 'vertical'\n    padding: 16\n    Button:\n        text: 'Save'\n        on_press: root.save_data()",
            tag = "KV Lang"
        ),
        DocItem(
            title = "ScreenManager & Transitions",
            description = "Multi-screen navigation stack with slide or fade transitions.",
            codeSnippet = "from kivy.uix.screenmanager import ScreenManager, Screen\n\nsm = ScreenManager()\nsm.add_widget(HomeScreen(name='home'))\nsm.current = 'home'",
            tag = "Navigation"
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Column {
                Text(
                    text = "📚 Kivy & Python 3 Cheatsheet",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "API reference, widgets, and rapid responsive patterns",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(docs) { doc ->
                DocCard(doc)
            }
        }
    }
}

@Composable
private fun DocCard(doc: DocItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = doc.title,
                    color = Color(0xFF38BDF8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = Color(0x3310B981),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = doc.tag,
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = doc.description,
                color = Color(0xFFE2E8F0),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Code block
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070A10)),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = doc.codeSnippet,
                    color = Color(0xFFA7F3D0),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    lineHeight = 16.sp
                )
            }
        }
    }
}
