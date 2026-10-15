package io.github.leroyjg.arabe.engine

import io.github.leroyjg.arabe.content.Activity
import io.github.leroyjg.arabe.content.BuildScreen
import io.github.leroyjg.arabe.content.Chapter
import io.github.leroyjg.arabe.content.DialogueScreen
import io.github.leroyjg.arabe.content.FillGapScreen
import io.github.leroyjg.arabe.content.MatchScreen
import io.github.leroyjg.arabe.content.McqScreen
import io.github.leroyjg.arabe.content.MemoryScreen
import io.github.leroyjg.arabe.content.MultiScreen
import io.github.leroyjg.arabe.content.OrderScreen
import io.github.leroyjg.arabe.content.PresentScreen
import io.github.leroyjg.arabe.content.QuestionScreen
import io.github.leroyjg.arabe.content.Screen
import io.github.leroyjg.arabe.content.SortScreen
import io.github.leroyjg.arabe.content.SpotScreen
import io.github.leroyjg.arabe.content.TipScreen
import io.github.leroyjg.arabe.content.TrueFalseScreen
import io.github.leroyjg.arabe.content.TypingScreen

/**
 * Durée estimée d'une leçon ou d'un chapitre, calculée d'après le contenu :
 * chaque écran a un temps moyen selon son type (lire, écouter, répéter, répondre, se tromper et
 * recommencer), augmenté pour les écrans qui contiennent plusieurs éléments (paires, cartes, répliques…).
 * C'est un ordre de grandeur pour un débutant, pas une mesure.
 */
object TimeEstimate {
    /** Secondes estimées pour un écran. */
    fun seconds(screen: Screen): Int = when (screen) {
        is PresentScreen -> 12
        is TipScreen -> 20 + 8 * screen.blocks.size
        is DialogueScreen -> 15 + 10 * screen.lines.size
        is McqScreen -> 10
        is QuestionScreen -> 12
        is TrueFalseScreen -> 8
        is FillGapScreen -> 12 + 6 * screen.gaps.size
        is OrderScreen -> 20
        is TypingScreen -> 25
        is MultiScreen -> 15
        is MatchScreen -> 8 + 6 * screen.items.size
        is SortScreen -> 8 + 4 * screen.cards.size
        is SpotScreen -> 12
        is BuildScreen -> 20
        is MemoryScreen -> 10 + 8 * screen.items.size
    }

    fun seconds(activity: Activity): Int = activity.screens.sumOf { seconds(it) }

    /** Minutes affichées pour une leçon : arrondies à la minute supérieure, 1 min au minimum. */
    fun minutes(activity: Activity): Int = maxOf((seconds(activity) + 59) / 60, 1)

    /** Minutes affichées pour un chapitre : la somme de celles de ses leçons. */
    fun minutes(chapter: Chapter): Int = chapter.activities.sumOf { minutes(it) }
}
