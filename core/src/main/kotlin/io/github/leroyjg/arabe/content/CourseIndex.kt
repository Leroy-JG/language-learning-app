package io.github.leroyjg.arabe.content

import io.github.leroyjg.arabe.arabic.ArabicScript

/** Position d'un écran dans le parcours : index de l'activité dans le cours, puis de l'écran. */
data class Position(val activity: Int, val screen: Int) : Comparable<Position> {
    override fun compareTo(other: Position): Int =
        compareValuesBy(this, other, Position::activity, Position::screen)
}

/**
 * Ordre du parcours : où chaque élément est présenté pour la première fois, où se trouve chaque
 * écran. Sert au validateur (« un exercice n'utilise que des éléments déjà présentés ») et à l'app
 * (distracteurs automatiques).
 */
class CourseIndex(val course: Course) {
    private val screenPositions = HashMap<String, Position>()
    private val firstPresentation = HashMap<String, Position>()
    private val screenActivity = HashMap<String, Activity>()

    init {
        course.activities.forEachIndexed { a, activity ->
            activity.screens.forEachIndexed { s, screen ->
                val pos = Position(a, s)
                screenPositions.putIfAbsent(screen.id, pos)
                screenActivity.putIfAbsent(screen.id, activity)
                if (screen is PresentScreen) firstPresentation.putIfAbsent(screen.item, pos)
            }
        }
    }

    fun position(screenId: String): Position? = screenPositions[screenId]
    fun activityOf(screenId: String): Activity? = screenActivity[screenId]
    fun firstPresentation(itemId: String): Position? = firstPresentation[itemId]

    /** L'élément a-t-il été présenté avant l'écran à [pos] ? */
    fun presentedBefore(itemId: String, pos: Position): Boolean =
        firstPresentation[itemId]?.let { it < pos } ?: false

    /**
     * Candidats aux distracteurs automatiques d'un QCM, par ordre de priorité : déjà présentés,
     * même sorte (mot / phrase), sans doublon de traduction ni de texte arabe (ni de texte qui ne
     * diffère que par le démonstratif, voir [ArabicScript.distractorKey]), d'abord dans la même
     * activité, puis le même chapitre, puis le même niveau. Chaque palier est une liste distincte,
     * que l'app mélange.
     */
    fun distractorTiers(target: Item, screenId: String): List<List<Item>> {
        val pos = position(screenId) ?: return emptyList()
        val activity = activityOf(screenId) ?: return emptyList()
        val chapter = course.chapter(activity.chapterId) ?: return emptyList()
        val levelId = chapter.levelId

        val activityTier = mutableListOf<Item>()
        val chapterTier = mutableListOf<Item>()
        val levelTier = mutableListOf<Item>()
        val candidates = firstPresentation.entries
            .filter { it.value < pos }
            .sortedBy { it.value }
            .mapNotNull { course.item(it.key) }
        for (item in candidates) {
            if (item.id == target.id || item.kind != target.kind) continue
            val itemChapter = course.chapterOfItem(item.id) ?: continue
            if (itemChapter.levelId != levelId) continue
            val presentedIn = course.activities[firstPresentation.getValue(item.id).activity]
            when {
                presentedIn.id == activity.id -> activityTier += item
                itemChapter.id == chapter.id -> chapterTier += item
                else -> levelTier += item
            }
        }
        // Sans doublon de traduction ni de texte arabe, ni entre eux ni avec la bonne réponse ;
        // en cas de doublon, l'élément du palier le plus prioritaire est gardé.
        val seenFr = mutableSetOf(target.fr)
        val seenAr = mutableSetOf(ArabicScript.distractorKey(target.ar))
        return listOf(activityTier, chapterTier, levelTier)
            .map { tier ->
                tier.filter { item ->
                    val letters = ArabicScript.distractorKey(item.ar)
                    (item.fr !in seenFr && letters !in seenAr).also {
                        if (it) {
                            seenFr += item.fr
                            seenAr += letters
                        }
                    }
                }
            }
            .filter { it.isNotEmpty() }
    }
}
