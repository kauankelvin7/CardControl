package com.cardcontrol.app.ui.theme

import androidx.compose.ui.graphics.Color

// Design System Tokens — Liquid Glass Edition (Linear / Raycast / Fintech Premium)
val AppBackground = Color(0xFF060913)
val AppBackgroundTop = Color(0xFF0F172A)
val GlassSurface = Color(0xFF0F172A).copy(alpha = 0.65f)

// Core Accents
val PrimaryAccent = Color(0xFF38BDF8) // Sky Blue — CTA primário e indicadores
val SecondaryAccent = Color(0xFF6366F1) // Indigo — Gradientes e barras de progresso

// Semantic Colors
val SemanticSuccess = Color(0xFF34D399) // Emerald — Valores disponíveis/abatidos
val SemanticWarning = Color(0xFFFBBF24) // Amber — Sublimite próximo ao teto / Alertas
val SemanticDanger = Color(0xFFF43F5E) // Rose — Ações destrutivas e confirmação

// Neutral Text Hierarchy
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Glass Reflections & Specular Highlights
val GlassBorderTopLeft = Color.White.copy(alpha = 0.22f)
val GlassBorderMid = Color.White.copy(alpha = 0.05f)
val GlassBorderBottomRight = Color.Transparent
val GlassTrackBackground = Color.White.copy(alpha = 0.08f)
