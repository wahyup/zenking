package com.example.data.local

object DefaultProjects {
    val list = listOf(
        KivyProjectEntity(
            id = 1,
            name = "Responsive Dashboard",
            description = "Modern responsive Kivy dashboard with grid cards, live sliders, and reactive metric widgets.",
            category = "Responsive UI",
            pythonCode = """# Kivy Python 3 - Responsive Metric Dashboard
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.slider import Slider
from kivy.uix.switch import Switch
from kivy.uix.progressbar import ProgressBar
import math

class DashboardApp(App):
    def build(self):
        self.title = "Kivy Responsive Dashboard"
        self.efficiency = 85
        self.energy_mode = True
        
        # Root layout with vertical orientation
        root = BoxLayout(orientation='vertical', padding=16, spacing=14)
        
        # Header banner
        header = BoxLayout(orientation='vertical', size_hint=(1, 0.18), spacing=4)
        title_lbl = Label(text="⚡ Python 3 + Kivy Studio", font_size=22, color=[0.2, 0.9, 0.6, 1], bold=True)
        sub_lbl = Label(text="Real-time Responsive UI Engine", font_size=13, color=[0.7, 0.8, 0.9, 0.8])
        header.add_widget(title_lbl)
        header.add_widget(sub_lbl)
        root.add_widget(header)
        
        # Metrics Grid (2x2)
        grid = GridLayout(cols=2, rows=2, spacing=12, size_hint=(1, 0.45))
        
        # Card 1: Performance
        self.lbl_perf = Label(text="🚀 Performance\n85%", font_size=16, halign='center', color=[1, 1, 1, 1])
        grid.add_widget(self.lbl_perf)
        
        # Card 2: Memory
        self.lbl_mem = Label(text="💾 RAM Usage\n142 MB", font_size=16, halign='center', color=[0.4, 0.8, 1, 1])
        grid.add_widget(self.lbl_mem)
        
        # Card 3: Frame Rate
        self.lbl_fps = Label(text="🎯 Target Rate\n60 FPS", font_size=16, halign='center', color=[1, 0.8, 0.2, 1])
        grid.add_widget(self.lbl_fps)
        
        # Card 4: Status
        self.lbl_status = Label(text="🟢 Active State\nOptimized", font_size=16, halign='center', color=[0.3, 0.9, 0.4, 1])
        grid.add_widget(self.lbl_status)
        root.add_widget(grid)
        
        # Interactive Controls
        ctrl_box = BoxLayout(orientation='vertical', size_hint=(1, 0.25), spacing=8)
        
        # Slider for CPU/Efficiency load
        slider_row = BoxLayout(orientation='horizontal', spacing=10, size_hint=(1, 0.5))
        slider_lbl = Label(text="Adjust Load:", size_hint=(0.35, 1), font_size=14)
        self.slider = Slider(min=10, max=100, value=85, step=1, size_hint=(0.65, 1))
        self.slider.bind(on_value=self.on_slider_change)
        slider_row.add_widget(slider_lbl)
        slider_row.add_widget(self.slider)
        ctrl_box.add_widget(slider_row)
        
        # Progress Bar
        self.progress = ProgressBar(max=100, value=85, size_hint=(1, 0.2))
        ctrl_box.add_widget(self.progress)
        root.add_widget(ctrl_box)
        
        # Action Buttons
        btn_box = BoxLayout(orientation='horizontal', spacing=10, size_hint=(1, 0.12))
        btn_boost = Button(text="⚡ Boost System", background_color=[0.1, 0.6, 0.9, 1], font_size=15)
        btn_boost.bind(on_press=self.boost_system)
        
        btn_reset = Button(text="🔄 Reset", background_color=[0.8, 0.2, 0.3, 1], font_size=15)
        btn_reset.bind(on_press=self.reset_system)
        
        btn_box.add_widget(btn_boost)
        btn_box.add_widget(btn_reset)
        root.add_widget(btn_box)
        
        return root

    def on_slider_change(self, instance, value):
        val = int(value)
        self.efficiency = val
        self.progress.value = val
        self.lbl_perf.text = f"🚀 Performance\n{val}%"
        self.lbl_mem.text = f"💾 RAM Usage\n{int(100 + val * 0.8)} MB"
        print(f"[Dashboard] Load adjusted: {val}%")

    def boost_system(self, instance):
        self.slider.value = 100
        self.lbl_status.text = "🔥 Turbo State\nMax Power"
        print("[Dashboard] System Boost Activated!")

    def reset_system(self, instance):
        self.slider.value = 50
        self.lbl_status.text = "🟢 Active State\nBalanced"
        print("[Dashboard] System Reset to Default")

if __name__ == '__main__':
    DashboardApp().run()
""",
            kvCode = """# Optional KV definitions for styling
<DashboardCard@Label>:
    font_size: 16
    color: 1, 1, 1, 1
    halign: 'center'
""",
            isFavorite = true,
            isTemplate = true,
            tags = "dashboard,responsive,sliders,kivy"
        ),
        KivyProjectEntity(
            id = 2,
            name = "Kivy Game & Clock Loop",
            description = "Interactive physics animation & game loop using Kivy Canvas, Ellipse, Rectangle, and Clock.",
            category = "Games & Canvas",
            pythonCode = """# Kivy Interactive Canvas & Clock Animation
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.widget import Widget
from kivy.clock import Clock
from kivy.graphics import Color, Ellipse, Rectangle
import random
import math

class GameCanvas(Widget):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.ball_x = 150
        self.ball_y = 200
        self.vel_x = 4.5
        self.vel_y = 5.2
        self.ball_radius = 24
        self.score = 0
        self.is_running = True
        
        # Initialize canvas shapes
        with self.canvas:
            # Player / Ball color (Vibrant Emerald)
            self.col = Color(0.1, 0.9, 0.5, 1)
            self.ball = Ellipse(pos=(self.ball_x, self.ball_y), size=(self.ball_radius * 2, self.ball_radius * 2))
            
            # Obstacle rectangle
            Color(0.9, 0.4, 0.2, 0.8)
            self.paddle = Rectangle(pos=(100, 30), size=(120, 18))

    def update(self, dt):
        if not self.is_running:
            return
            
        # Update physics
        self.ball_x += self.vel_x
        self.ball_y += self.vel_y
        
        # Wall bounce
        max_w = 340
        max_h = 360
        
        if self.ball_x <= 10 or self.ball_x >= max_w - self.ball_radius * 2:
            self.vel_x = -self.vel_x
            self.score += 1
            
        if self.ball_y <= 20 or self.ball_y >= max_h - self.ball_radius * 2:
            self.vel_y = -self.vel_y
            self.score += 1
            
        # Update graphics position
        self.ball.pos = (self.ball_x, self.ball_y)

    def jump(self):
        self.vel_y = 7.0
        self.vel_x = random.choice([-5.0, 5.0])
        print(f"[Game] Jump! Score: {self.score}")

class GameApp(App):
    def build(self):
        self.title = "Kivy Canvas Game"
        root = BoxLayout(orientation='vertical', padding=12, spacing=10)
        
        # Score header
        self.score_lbl = Label(text="🏆 Score: 0 | 60 FPS Canvas", font_size=18, color=[1, 0.9, 0.2, 1], size_hint=(1, 0.12))
        root.add_widget(self.score_lbl)
        
        # Interactive Canvas
        self.game = GameCanvas(size_hint=(1, 0.72))
        root.add_widget(self.game)
        
        # Controls
        ctrl = BoxLayout(orientation='horizontal', spacing=10, size_hint=(1, 0.16))
        btn_jump = Button(text="🚀 Tap to Jump / Boost", background_color=[0.1, 0.7, 0.4, 1], font_size=16)
        btn_jump.bind(on_press=self.on_tap)
        
        btn_toggle = Button(text="⏯️ Pause/Resume", background_color=[0.3, 0.4, 0.9, 1], font_size=14)
        btn_toggle.bind(on_press=self.toggle_pause)
        
        ctrl.add_widget(btn_jump)
        ctrl.add_widget(btn_toggle)
        root.add_widget(ctrl)
        
        # Schedule 60 FPS loop
        Clock.schedule_interval(self.tick, 1.0 / 60.0)
        return root

    def tick(self, dt):
        self.game.update(dt)
        self.score_lbl.text = f"🏆 Score: {self.game.score} | Position: ({int(self.game.ball_x)}, {int(self.game.ball_y)})"

    def on_tap(self, instance):
        self.game.jump()

    def toggle_pause(self, instance):
        self.game.is_running = not self.game.is_running
        print(f"[Game] Running state: {self.game.is_running}")

if __name__ == '__main__':
    GameApp().run()
""",
            kvCode = "",
            isFavorite = true,
            isTemplate = true,
            tags = "game,canvas,clock,animation,physics"
        ),
        KivyProjectEntity(
            id = 3,
            name = "Responsive Calculator",
            description = "Fast, responsive mathematical calculator built with Kivy GridLayout and Python expression parser.",
            category = "Responsive UI",
            pythonCode = """# Kivy Responsive Calculator
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.gridlayout import GridLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
import math

class CalculatorApp(App):
    def build(self):
        self.title = "Kivy Responsive Calculator"
        self.formula = ""
        
        root = BoxLayout(orientation='vertical', padding=14, spacing=10)
        
        # Display Screen (Sub-formula + Big result)
        display_box = BoxLayout(orientation='vertical', size_hint=(1, 0.28), padding=12)
        self.history_lbl = Label(text="", font_size=14, color=[0.6, 0.7, 0.8, 1], halign='right')
        self.result_lbl = Label(text="0", font_size=32, color=[1, 1, 1, 1], bold=True, halign='right')
        display_box.add_widget(self.history_lbl)
        display_box.add_widget(self.result_lbl)
        root.add_widget(display_box)
        
        # Buttons Grid (4 columns)
        grid = GridLayout(cols=4, spacing=8, size_hint=(1, 0.72))
        
        buttons = [
            ('C', [0.8, 0.2, 0.3, 1]), ('(', [0.2, 0.4, 0.6, 1]), (')', [0.2, 0.4, 0.6, 1]), ('/', [0.9, 0.6, 0.1, 1]),
            ('7', [0.2, 0.25, 0.35, 1]), ('8', [0.2, 0.25, 0.35, 1]), ('9', [0.2, 0.25, 0.35, 1]), ('*', [0.9, 0.6, 0.1, 1]),
            ('4', [0.2, 0.25, 0.35, 1]), ('5', [0.2, 0.25, 0.35, 1]), ('6', [0.2, 0.25, 0.35, 1]), ('-', [0.9, 0.6, 0.1, 1]),
            ('1', [0.2, 0.25, 0.35, 1]), ('2', [0.2, 0.25, 0.35, 1]), ('3', [0.2, 0.25, 0.35, 1]), ('+', [0.9, 0.6, 0.1, 1]),
            ('0', [0.2, 0.25, 0.35, 1]), ('.', [0.2, 0.25, 0.35, 1]), ('⌫', [0.5, 0.3, 0.3, 1]), ('=', [0.1, 0.8, 0.5, 1])
        ]
        
        for text, color in buttons:
            btn = Button(text=text, background_color=color, font_size=20, bold=True)
            btn.bind(on_press=self.on_button_press)
            grid.add_widget(btn)
            
        root.add_widget(grid)
        return root

    def on_button_press(self, instance):
        text = instance.text
        
        if text == 'C':
            self.formula = ""
            self.history_lbl.text = ""
            self.result_lbl.text = "0"
        elif text == '⌫':
            self.formula = self.formula[:-1]
            self.result_lbl.text = self.formula if self.formula else "0"
        elif text == '=':
            try:
                # Safe evaluation in Python 3
                expr = self.formula.replace('^', '**')
                res = eval(expr)
                if isinstance(res, float) and res.is_integer():
                    res = int(res)
                self.history_lbl.text = f"{self.formula} ="
                self.result_lbl.text = str(res)
                self.formula = str(res)
                print(f"[Calc] Calculated: {expr} = {res}")
            except Exception as e:
                self.result_lbl.text = "Error"
                print(f"[Calc] Evaluation error: {e}")
        else:
            if self.result_lbl.text == "0" and text not in '+-*/.':
                self.formula = text
            else:
                self.formula += text
            self.result_lbl.text = self.formula

if __name__ == '__main__':
    CalculatorApp().run()
""",
            kvCode = "",
            isFavorite = true,
            isTemplate = true,
            tags = "calculator,gridlayout,eval,math"
        ),
        KivyProjectEntity(
            id = 4,
            name = "KV Lang Reactive Form",
            description = "Declarative Kivy (.kv) language form with dynamic data binding, TextInput, Switch, and verification.",
            category = "Forms & Controls",
            pythonCode = """# Kivy Reactive Form with KV Language Binding
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.lang import Builder

class UserFormApp(App):
    def build(self):
        self.title = "Kivy Reactive Form"
        return MainLayout()

class MainLayout(BoxLayout):
    def submit_form(self):
        name = self.ids.name_input.text
        email = self.ids.email_input.text
        notifications = self.ids.notif_switch.active
        tier = self.ids.tier_slider.value
        
        if not name or not email:
            self.ids.status_label.text = "⚠️ Please fill in Name & Email!"
            self.ids.status_label.color = [0.9, 0.3, 0.3, 1]
            return
            
        summary = f"✅ Registered: {name} ({email})\nAlerts: {'ON' if notifications else 'OFF'} | Tier: {int(tier)}"
        self.ids.status_label.text = summary
        self.ids.status_label.color = [0.2, 0.9, 0.5, 1]
        print(f"[Form] Submitted: {name}, {email}, Notifications={notifications}, Tier={int(tier)}")

    def clear_form(self):
        self.ids.name_input.text = ""
        self.ids.email_input.text = ""
        self.ids.notif_switch.active = True
        self.ids.tier_slider.value = 1
        self.ids.status_label.text = "Form reset."
        self.ids.status_label.color = [0.7, 0.8, 0.9, 1]

if __name__ == '__main__':
    UserFormApp().run()
""",
            kvCode = """# Kivy Language Definition (.kv)
<MainLayout>:
    orientation: 'vertical'
    padding: 16
    spacing: 12

    Label:
        text: '📋 Kivy Profile Setup'
        font_size: 22
        size_hint: 1, 0.12
        color: 0.2, 0.9, 0.6, 1
        bold: True

    BoxLayout:
        orientation: 'vertical'
        size_hint: 1, 0.18
        spacing: 4
        Label:
            text: 'Full Name:'
            font_size: 14
            color: 0.8, 0.9, 1, 1
        TextInput:
            id: name_input
            hint_text: 'Enter your name'
            multiline: False

    BoxLayout:
        orientation: 'vertical'
        size_hint: 1, 0.18
        spacing: 4
        Label:
            text: 'Email Address:'
            font_size: 14
            color: 0.8, 0.9, 1, 1
        TextInput:
            id: email_input
            hint_text: 'user@example.com'
            multiline: False

    BoxLayout:
        orientation: 'horizontal'
        size_hint: 1, 0.14
        spacing: 8
        Label:
            text: 'Enable Notifications:'
            font_size: 14
        Switch:
            id: notif_switch
            active: True

    BoxLayout:
        orientation: 'horizontal'
        size_hint: 1, 0.14
        spacing: 8
        Label:
            text: 'Priority Level (' + str(int(tier_slider.value)) + '):'
            font_size: 14
        Slider:
            id: tier_slider
            min: 1
            max: 5
            value: 3
            step: 1

    Label:
        id: status_label
        text: 'Ready to submit.'
        font_size: 14
        size_hint: 1, 0.14
        color: 0.7, 0.8, 0.9, 1

    BoxLayout:
        orientation: 'horizontal'
        size_hint: 1, 0.12
        spacing: 10
        Button:
            text: '💾 Save Profile'
            background_color: 0.1, 0.7, 0.4, 1
            on_press: root.submit_form()
        Button:
            text: '🧹 Clear'
            background_color: 0.7, 0.2, 0.3, 1
            on_press: root.clear_form()
""",
            isFavorite = true,
            isTemplate = true,
            tags = "kvlang,forms,inputs,binding"
        ),
        KivyProjectEntity(
            id = 5,
            name = "Multi-Screen Navigation",
            description = "ScreenManager with Slide Transitions between Home, Analytics, and Settings screens.",
            category = "Navigation",
            pythonCode = """# Kivy ScreenManager & Navigation Transitions
from kivy.app import App
from kivy.uix.screenmanager import ScreenManager, Screen
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.switch import Switch
from kivy.uix.slider import Slider

class HomeScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.name = 'home'
        box = BoxLayout(orientation='vertical', padding=16, spacing=14)
        
        box.add_widget(Label(text="🏠 Home Screen", font_size=24, bold=True, color=[0.3, 0.8, 1, 1], size_hint=(1, 0.2)))
        box.add_widget(Label(text="Welcome to Kivy ScreenManager!\nNavigate seamlessly with transitions.", halign='center', font_size=15, size_hint=(1, 0.4)))
        
        btn_nav_analytics = Button(text="📊 Go to Analytics", background_color=[0.2, 0.7, 0.5, 1], size_hint=(1, 0.2))
        btn_nav_analytics.bind(on_press=self.go_analytics)
        
        btn_nav_settings = Button(text="⚙️ Open Settings", background_color=[0.5, 0.3, 0.8, 1], size_hint=(1, 0.2))
        btn_nav_settings.bind(on_press=self.go_settings)
        
        box.add_widget(btn_nav_analytics)
        box.add_widget(btn_nav_settings)
        self.add_widget(box)

    def go_analytics(self, instance):
        self.manager.current = 'analytics'

    def go_settings(self, instance):
        self.manager.current = 'settings'

class AnalyticsScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.name = 'analytics'
        box = BoxLayout(orientation='vertical', padding=16, spacing=14)
        
        box.add_widget(Label(text="📊 Analytics & Stats", font_size=22, bold=True, color=[0.2, 0.9, 0.6, 1], size_hint=(1, 0.2)))
        box.add_widget(Label(text="📈 Monthly Growth: +48%\n⚡ Speed Score: 99/100\n👥 Active Sessions: 1,420", halign='center', font_size=16, size_hint=(1, 0.5)))
        
        btn_back = Button(text="⬅️ Back to Home", background_color=[0.2, 0.4, 0.6, 1], size_hint=(1, 0.2))
        btn_back.bind(on_press=self.go_home)
        box.add_widget(btn_back)
        self.add_widget(box)

    def go_home(self, instance):
        self.manager.current = 'home'

class SettingsScreen(Screen):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.name = 'settings'
        box = BoxLayout(orientation='vertical', padding=16, spacing=14)
        
        box.add_widget(Label(text="⚙️ Preferences", font_size=22, bold=True, color=[0.8, 0.5, 1, 1], size_hint=(1, 0.2)))
        
        row1 = BoxLayout(orientation='horizontal', size_hint=(1, 0.2))
        row1.add_widget(Label(text="Dark Theme:"))
        row1.add_widget(Switch(active=True))
        box.add_widget(row1)
        
        row2 = BoxLayout(orientation='horizontal', size_hint=(1, 0.2))
        row2.add_widget(Label(text="Volume:"))
        row2.add_widget(Slider(min=0, max=100, value=75))
        box.add_widget(row2)
        
        btn_back = Button(text="⬅️ Back to Home", background_color=[0.2, 0.4, 0.6, 1], size_hint=(1, 0.2))
        btn_back.bind(on_press=self.go_home)
        box.add_widget(btn_back)
        self.add_widget(box)

    def go_home(self, instance):
        self.manager.current = 'home'

class NavigationApp(App):
    def build(self):
        self.title = "Kivy Multi-Screen Navigation"
        sm = ScreenManager()
        sm.add_widget(HomeScreen())
        sm.add_widget(AnalyticsScreen())
        sm.add_widget(SettingsScreen())
        return sm

if __name__ == '__main__':
    NavigationApp().run()
""",
            kvCode = "",
            isFavorite = false,
            isTemplate = true,
            tags = "screenmanager,navigation,screens,transitions"
        ),
        KivyProjectEntity(
            id = 6,
            name = "Interactive Drawing Canvas",
            description = "Multi-color touch drawing pad using Kivy Canvas, Color palette, and dynamic stroke width.",
            category = "Games & Canvas",
            pythonCode = """# Kivy Touch Drawing & Paint Canvas
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.label import Label
from kivy.uix.slider import Slider
from kivy.uix.widget import Widget
from kivy.graphics import Color, Line, Ellipse

class PaintWidget(Widget):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.current_color = [0.1, 0.9, 0.6, 1]
        self.stroke_width = 4.0
        self.lines = []

    def set_color(self, rgba):
        self.current_color = rgba
        print(f"[Paint] Color changed to {rgba}")

    def set_stroke(self, width):
        self.stroke_width = width

    def clear(self):
        self.canvas.clear()
        self.lines.clear()
        print("[Paint] Canvas cleared.")

class PaintApp(App):
    def build(self):
        self.title = "Kivy Drawing Canvas"
        root = BoxLayout(orientation='vertical', padding=10, spacing=8)
        
        # Header
        hdr = Label(text="🎨 Touch & Draw Canvas (Kivy Graphics)", font_size=17, color=[0.3, 0.8, 1, 1], size_hint=(1, 0.1))
        root.add_widget(hdr)
        
        # Canvas Area
        self.painter = PaintWidget(size_hint=(1, 0.68))
        root.add_widget(self.painter)
        
        # Palette & Controls
        ctrl = BoxLayout(orientation='vertical', size_hint=(1, 0.22), spacing=6)
        
        # Color buttons row
        palette = BoxLayout(orientation='horizontal', spacing=8, size_hint=(1, 0.5))
        
        colors = [
            ("Emerald", [0.1, 0.9, 0.5, 1]),
            ("Cyan", [0.1, 0.8, 1.0, 1]),
            ("Amber", [1.0, 0.7, 0.1, 1]),
            ("Coral", [1.0, 0.3, 0.4, 1]),
            ("Purple", [0.8, 0.4, 1.0, 1])
        ]
        
        for name, col in colors:
            btn = Button(text=name, background_color=col, font_size=12)
            btn.bind(on_press=lambda inst, c=col: self.painter.set_color(c))
            palette.add_widget(btn)
            
        ctrl.add_widget(palette)
        
        # Actions row
        actions = BoxLayout(orientation='horizontal', spacing=8, size_hint=(1, 0.5))
        
        btn_clear = Button(text="🧹 Clear Canvas", background_color=[0.8, 0.2, 0.3, 1], size_hint=(0.4, 1), font_size=13)
        btn_clear.bind(on_press=lambda inst: self.painter.clear())
        
        stroke_lbl = Label(text="Stroke:", size_hint=(0.2, 1), font_size=12)
        slider_stroke = Slider(min=2, max=16, value=4, size_hint=(0.4, 1))
        slider_stroke.bind(on_value=lambda inst, val: self.painter.set_stroke(val))
        
        actions.add_widget(btn_clear)
        actions.add_widget(stroke_lbl)
        actions.add_widget(slider_stroke)
        ctrl.add_widget(actions)
        
        root.add_widget(ctrl)
        return root

if __name__ == '__main__':
    PaintApp().run()
""",
            kvCode = "",
            isFavorite = false,
            isTemplate = true,
            tags = "drawing,canvas,touch,paint,kivy"
        )
    )
}
