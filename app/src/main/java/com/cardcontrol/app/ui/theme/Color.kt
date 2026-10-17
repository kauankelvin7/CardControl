package com.cardcontrol.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.cardcontrol.app.ui.designsystem.CcGrafiteDark
import com.cardcontrol.app.ui.designsystem.CcSemanticsDark

// Mapeamento dos tokens legados para os valores oficiais do Design System Vitrine (Grafite)
val AppBackground = CcGrafiteDark.background
val AppBackgroundTop = Color(0xFF171C1D)
val GlassSurface = Color(0xFF1C2222).copy(alpha = 0.85f)

// Core Accents
val PrimaryAccent = CcGrafiteDark.primary       // #72D5E1
val SecondaryAccent = CcGrafiteDark.secondary   // #A8CDD2

// Semantic Colors
val SemanticSuccess = CcSemanticsDark.success   // #8FD79E
val SemanticWarning = CcSemanticsDark.warning   // #EAC078
val SemanticDanger = CcSemanticsDark.error      // #F2B8B5

// Neutral Text Hierarchy
val TextPrimary = CcGrafiteDark.onBackground    // #DDE4E5
val TextSecondary = CcGrafiteDark.onSurfaceVariant // #B5C4C6
val TextMuted = CcGrafiteDark.outline           // #808E90

// Glass Reflections & Specular Highlights adaptados para bordas sutis Vitrine (B4.3)
val GlassBorderTopLeft = Color.White.copy(alpha = 0.10f)
val GlassBorderMid = Color.White.copy(alpha = 0.05f)
val GlassBorderBottomRight = Color.Transparent
val GlassTrackBackground = Color.White.copy(alpha = 0.06f)
