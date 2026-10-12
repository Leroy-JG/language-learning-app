package io.github.leroyjg.arabe.content

import io.github.leroyjg.arabe.arabic.ArabicScript
import io.github.leroyjg.arabe.arabic.Markup

/** Résultat de la validation d'un cours (docs/content-format.md §11). */
class ValidationReport(
    val errors: List<ContentIssue>,
    val warnings: List<ContentIssue>,
    val itemCount: Int,
    val screensByType: Map<String, Int>,
    val audioNeeds: Int,
) {
    val ok: Boolean get() = errors.isEmpty()
    val screenCount: Int get() = screensByType.values.sum()
}

/**
 * Vérifie un cours complet. [audioExists] dit si un fichier audio existe (chemin relatif aux assets) ;
 * null tant qu'aucun audio n'a été généré : les audios manquants sont alors un simple avertissement.
 */
class Validator(
    private val source: ContentSource,
    private val audioExists: ((String) -> Boolean)? = null,
) {
    fun validate(courseDir: String): ValidationReport {
        val load = ContentLoader(source).load(courseDir)
        val run = Run(courseDir)
        run.errors += load.issues
        load.course?.let { run.check(it) }
        return ValidationReport(
            errors = run.errors,
            warnings = run.warnings,
            itemCount = load.course?.itemsById?.size ?: 0,
            screensByType = run.screensByType.toSortedMap(),
            audioNeeds = run.audioNeeds,
        )
    }

    private inner class Run(val courseDir: String) {
        val errors = mutableListOf<ContentIssue>()
        val warnings = mutableListOf<ContentIssue>()
        val screensByType = mutableMapOf<String, Int>()
        var audioNeeds = 0

        private val allIds = mutableSetOf<String>()
        private val arabicTexts = mutableListOf<Pair<String, String>>()
        private lateinit var course: Course
        private lateinit var index: CourseIndex

        fun err(where: String, message: String) {
            errors += ContentIssue(where, message)
        }

        fun warn(where: String, message: String) {
            warnings += ContentIssue(where, message)
        }

        fun check(course: Course) {
            this.course = course
            this.index = CourseIndex(course)
            checkCourse()
            for (level in course.levels) checkLevel(level)
            checkDemonstratives()
            checkAudio()
        }

        // ---------- textes ----------

        /** Texte arabe ; [vocalized] exige des harakat sur chaque mot de deux lettres ou plus. */
        fun ar(where: String, text: String, vocalized: Boolean = true) {
            ArabicScript.problems(text).forEach { err(where, it) }
            if (vocalized) {
                for (word in ArabicScript.nfc(text).split(' ')) {
                    if (word.count { ArabicScript.isLetter(it) } >= 2 && word.none { ArabicScript.isHarakah(it) }) {
                        err(where, "mot non vocalisé « $word » dans « $text »")
                    }
                }
            }
            arabicTexts += where to text
        }

        /** Texte français, éventuellement avec `{ar|…}` et `**gras**`. */
        fun fr(where: String, text: String, vocalized: Boolean = true) {
            if (text.isBlank()) err(where, "texte vide")
            val parsed = Markup.parse(text)
            parsed.errors.forEach { err(where, it) }
            parsed.segments.filter { it.arabic }.forEach { ar(where, it.text, vocalized) }
        }

        fun id(where: String, id: String) {
            if (!allIds.add(id)) err(where, "identifiant en double : $id")
        }

        // ---------- cours, niveaux, chapitres ----------

        fun checkCourse() {
            val f = course.file
            if (f.formatVersion != SUPPORTED_FORMAT_VERSION) err("course", "formatVersion ${f.formatVersion} inconnu")
            if (f.id != courseDir) err("course", "id « ${f.id} » différent du dossier « $courseDir »")
            fr("course.title", f.title)
            if (f.voices.isEmpty()) err("course", "aucune voix")
            for ((key, v) in f.voices) {
                if (!CODE.matches(key)) err("course.voices", "clé de voix mal formée : $key")
                if (v.provider != "edge") err("course.voices.$key", "fournisseur inconnu : ${v.provider}")
                if (v.name.isBlank()) err("course.voices.$key", "nom de voix vide")
                if (!RATE.matches(v.rate)) err("course.voices.$key", "vitesse mal formée : ${v.rate} (ex. -10%)")
            }
            if (f.defaultVoice !in f.voices) err("course", "defaultVoice inconnue : ${f.defaultVoice}")
            if (f.levels.isEmpty()) err("course", "aucun niveau")
            duplicates(f.levels).forEach { err("course", "niveau listé deux fois : $it") }
        }

        fun checkLevel(level: Level) {
            val f = level.file
            val w = level.dir
            if (f.id != level.dir) err(w, "id « ${f.id} » différent du dossier")
            if (!CODE.matches(f.id)) err(w, "id mal formé")
            id(w, f.id)
            fr("$w.title", f.title)
            f.description?.let { fr("$w.description", it) }
            f.source?.let { fr("$w.source", it) }
            duplicates(f.chapters).forEach { err(w, "chapitre listé deux fois : $it") }
            var lastNumber = 0
            for (chapter in level.chapters) {
                if (chapter.file.number <= lastNumber) err(chapter.id, "numéro ${chapter.file.number} non croissant")
                lastNumber = chapter.file.number
                checkChapter(level, chapter)
            }
        }

        fun checkChapter(level: Level, chapter: Chapter) {
            val f = chapter.file
            val w = "${level.id}.${chapter.dir}"
            if (f.id != w) err(w, "id attendu : $w")
            if (!CODE.matches(chapter.dir)) err(w, "dossier mal nommé")
            id(w, f.id)
            ar("$w.title.ar", f.title.ar)
            fr("$w.title.fr", f.title.fr)
            f.description?.let { fr("$w.description", it) }
            f.source?.let { fr("$w.source", it) }
            if (f.sections.isEmpty()) err(w, "aucune partie")
            val codes = mutableListOf<String>()
            f.sections.forEachIndexed { i, s ->
                fr("$w.sections[$i].title", s.title)
                if (s.activities.isEmpty()) err("$w.sections[$i]", "partie sans activité")
                codes += s.activities
            }
            duplicates(codes).forEach { err(w, "activité listée deux fois : $it") }
            codes.filterNot { CODE.matches(it) }.forEach { err(w, "code d'activité mal formé : $it") }
            val dir = "$courseDir/${level.dir}/${chapter.dir}"
            source.list(dir)?.let { files ->
                val unlisted = files.filter { it.endsWith(".json") }.map { it.removeSuffix(".json") }
                    .filterNot { it == "chapter" || it == "items" || it in codes }
                if (unlisted.isNotEmpty()) err(w, "fichiers d'activité non listés : $unlisted")
            }
            if (chapter.activities.lastOrNull()?.file?.type != ActivityType.QUIZ) {
                warn(w, "la dernière activité n'est pas un quiz")
            }
            checkItems(chapter)
            for (activity in chapter.activities) checkActivity(activity)
        }

        fun checkItems(chapter: Chapter) {
            val frSeen = mutableMapOf<String, String>()
            for (item in chapter.items) {
                val w = item.id
                if (!ITEM_ID.matches(item.id)) err(w, "id d'élément mal formé")
                val expectedPrefix = if (item.kind == ItemKind.WORD) "w:" else "p:"
                if (!item.id.startsWith(expectedPrefix)) err(w, "« kind » incohérent avec le préfixe de l'id")
                id(w, item.id)
                ar("$w.ar", item.ar)
                fr("$w.fr", item.fr)
                frSeen.put(item.fr, item.id)?.let { err(w, "traduction identique à celle de $it") }
                item.plural?.let { ar("$w.plural", it) }
                item.icon?.let { if (it !in Icons.NAMES) err(w, "icône inconnue : $it") }
                item.note?.let { fr("$w.note", it) }
                item.example?.let {
                    ar("$w.example.ar", it.ar)
                    fr("$w.example.fr", it.fr)
                }
                item.voice?.let { if (it !in course.file.voices) err(w, "voix inconnue : $it") }
                item.tts?.let { ar("$w.tts", it) }
                item.audioFile?.let {
                    if (!MANUAL_AUDIO.matches(it)) err(w, "audioFile doit être de la forme audio/manual/<nom>.mp3")
                    else if (audioExists != null && !audioExists.invoke(it)) err(w, "enregistrement introuvable : $it")
                }
                val first = index.firstPresentation(item.id)
                if (first == null) {
                    err(w, "élément jamais présenté (aucun écran « present »)")
                } else if (course.activities[first.activity].chapterId != chapter.id) {
                    err(w, "élément présenté pour la première fois hors de son chapitre")
                }
            }
        }

        // ---------- activités et écrans ----------

        fun checkActivity(activity: Activity) {
            val f = activity.file
            val w = f.id
            if (f.id != "${activity.chapterId}.${activity.code}") err(w, "id différent du fichier ${activity.code}.json")
            id(w, f.id)
            fr("$w.title", f.title)
            f.icon?.let { if (it !in Icons.NAMES) err(w, "icône inconnue : $it") }
            if (f.screens.isEmpty()) err(w, "aucun écran")
            if (f.screens.none { it is Exercise }) warn(w, "aucun exercice")
            for (screen in f.screens) checkScreen(activity, screen)
        }

        fun checkScreen(activity: Activity, screen: Screen) {
            val w = screen.id
            val prefix = "${activity.id}#"
            if (!screen.id.startsWith(prefix) || !CODE.matches(screen.id.removePrefix(prefix))) err(w, "id d'écran mal formé")
            id(w, screen.id)
            val pos = index.position(screen.id) ?: return
            screensByType.merge(typeName(screen), 1, Int::plus)
            if (screen is Exercise) checkCommon(screen, pos)
            when (screen) {
                is PresentScreen -> {
                    if (course.item(screen.item) == null) err(w, "élément inconnu : ${screen.item}")
                    else if (index.firstPresentation(screen.item) != pos) warn(w, "${screen.item} déjà présenté")
                }
                is TipScreen -> checkTip(w, screen, pos)
                is McqScreen -> checkMcq(w, screen, pos)
                is MatchScreen -> checkMatch(w, screen, pos)
                is FillGapScreen -> checkFillGap(w, screen)
                is OrderScreen -> checkOrder(w, screen)
                is TypingScreen -> checkTyping(w, screen, pos)
                is TrueFalseScreen -> {
                    fr("$w.statement.fr", screen.statement.fr)
                    screen.statement.ar?.let { ar("$w.statement.ar", it) }
                }
                is QuestionScreen -> checkQuestion(w, screen)
                is DialogueScreen -> checkDialogue(w, screen)
                is SortScreen -> checkSort(w, screen, pos)
                is SpotScreen -> checkSpot(w, screen)
                is BuildScreen -> checkBuild(w, screen, pos)
                is MemoryScreen -> checkMemory(w, screen, pos)
            }
        }

        fun checkCommon(screen: Exercise, pos: Position) {
            val w = screen.id
            screen.instruction?.let { fr("$w.instruction", it) }
            screen.explain?.let { fr("$w.explain", it) }
            val c = screen.context ?: return
            if (listOfNotNull(c.icon, c.item, c.dialogue).size != 1) err(w, "context doit avoir exactement un champ")
            c.icon?.let { if (it !in Icons.NAMES) err(w, "icône inconnue : $it") }
            c.item?.let { presented(w, it, pos) }
            c.dialogue?.let { d ->
                val dPos = index.position(d)
                val dScreen = index.activityOf(d)?.screens?.firstOrNull { it.id == d }
                if (dPos == null || dScreen !is DialogueScreen || dPos >= pos) err(w, "dialogue inconnu ou pas encore vu : $d")
            }
        }

        /** L'élément existe et a été présenté avant [pos]. */
        fun presented(w: String, itemId: String, pos: Position): Item? {
            val item = course.item(itemId)
            if (item == null) {
                err(w, "élément inconnu : $itemId")
                return null
            }
            if (!index.presentedBefore(itemId, pos)) err(w, "élément pas encore présenté : $itemId")
            return item
        }

        fun checkTip(w: String, screen: TipScreen, pos: Position) {
            fr("$w.title", screen.title, vocalized = false)
            if (screen.blocks.isEmpty()) err(w, "astuce sans contenu")
            screen.blocks.forEachIndexed { i, b ->
                val bw = "$w.blocks[$i]"
                when (b) {
                    is ParagraphBlock -> fr(bw, b.text, vocalized = false)
                    is NoteBlock -> fr(bw, b.text, vocalized = false)
                    is CompareBlock -> {
                        b.title?.let { fr("$bw.title", it, vocalized = false) }
                        if (b.rows.size !in 1..6) err(bw, "il faut 1 à 6 lignes")
                        b.rows.forEachIndexed { r, row ->
                            ar("$bw.rows[$r].from.ar", row.from.ar)
                            fr("$bw.rows[$r].from.fr", row.from.fr, vocalized = false)
                            ar("$bw.rows[$r].to.ar", row.to.ar)
                            fr("$bw.rows[$r].to.fr", row.to.fr, vocalized = false)
                            row.label?.let { fr("$bw.rows[$r].label", it, vocalized = false) }
                        }
                    }
                    is BreakdownBlock -> {
                        ar("$bw.ar", b.ar)
                        fr("$bw.fr", b.fr, vocalized = false)
                        if (b.parts.size !in 2..6) err(bw, "il faut 2 à 6 morceaux")
                        b.parts.forEachIndexed { k, part ->
                            ar("$bw.parts[$k].ar", part.ar, vocalized = false)
                            fr("$bw.parts[$k].fr", part.fr, vocalized = false)
                        }
                        // Les morceaux, mis bout à bout, doivent redonner exactement le mot ou la phrase.
                        if (b.parts.joinToString("") { letters(it.ar) } != letters(b.ar)) {
                            err(bw, "les morceaux ne redonnent pas « ${b.ar} »")
                        }
                    }
                    is GalleryBlock -> {
                        b.title?.let { fr("$bw.title", it, vocalized = false) }
                        if (b.items.size !in 2..9) err(bw, "il faut 2 à 9 éléments")
                        duplicates(b.items).forEach { err(bw, "élément en double : $it") }
                        b.items.forEach { presented(bw, it, pos) }
                    }
                    is ExampleBlock -> {
                        ar("$bw.ar", b.ar)
                        fr("$bw.fr", b.fr, vocalized = false)
                    }
                    is TableBlock -> {
                        if (b.header.isEmpty() || b.rows.isEmpty()) err(bw, "tableau vide")
                        b.header.forEach { fr("$bw.header", it, vocalized = false) }
                        b.rows.forEach { row ->
                            if (row.size != b.header.size) err(bw, "ligne de ${row.size} cellules au lieu de ${b.header.size}")
                            row.forEach { fr("$bw.rows", it, vocalized = false) }
                        }
                    }
                }
            }
        }

        fun checkMcq(w: String, screen: McqScreen, pos: Position) {
            val target = presented(w, screen.item, pos) ?: return
            if (screen.mode == McqMode.ICON_AR && target.icon == null) err(w, "mode icon-ar sur un élément sans icône")
            val explicit = screen.distractors
            if (explicit != null) {
                if (explicit.size !in 2..3) err(w, "il faut 2 ou 3 distracteurs")
                val shown = mutableListOf(target)
                for (d in explicit) {
                    val item = presented(w, d, pos) ?: continue
                    if (shown.any { it.id == item.id || it.fr == item.fr || letters(it.ar) == letters(item.ar) }) {
                        err(w, "distracteur en double ou équivalent à la réponse : $d")
                    }
                    // En traduction, هٰذَا et ذٰلِكَ se rendent souvent tous deux par « c'est » : un choix qui ne
                    // diffère de la réponse que par le démonstratif pourrait aussi être juste.
                    val translating = screen.mode == McqMode.AR_FR || screen.mode == McqMode.FR_AR
                    if (translating && ArabicScript.distractorKey(target.ar) == ArabicScript.distractorKey(item.ar)) {
                        err(w, "distracteur qui ne diffère de la réponse que par le démonstratif : $d")
                    }
                    shown += item
                }
            } else if (index.distractorTiers(target, screen.id).sumOf { it.size } < 2) {
                err(w, "moins de 2 candidats pour les distracteurs automatiques")
            }
        }

        fun checkMatch(w: String, screen: MatchScreen, pos: Position) {
            if (screen.items.size !in 3..5) err(w, "il faut 3 à 5 éléments")
            duplicates(screen.items).forEach { err(w, "élément en double : $it") }
            val items = screen.items.mapNotNull { presented(w, it, pos) }
            if (items.map { letters(it.ar) }.toSet().size != items.size) err(w, "deux textes arabes identiques")
            if (items.map { it.fr }.toSet().size != items.size) err(w, "deux traductions identiques")
            if (screen.mode == MatchMode.SING_PLUR) {
                if (items.any { it.plural == null }) {
                    err(w, "sing-plur avec un élément sans pluriel")
                } else {
                    val all = items.map { letters(it.ar) } + items.map { letters(it.plural!!) }
                    if (all.toSet().size != all.size) err(w, "singuliers et pluriels pas tous différents")
                }
            }
        }

        fun checkFillGap(w: String, screen: FillGapScreen) {
            fr("$w.fr", screen.fr)
            val placeholders = PLACEHOLDER.findAll(screen.sentence).map { it.groupValues[1].toInt() }.toList()
            if (screen.gaps.isEmpty() || placeholders.sorted() != screen.gaps.indices.toList()) {
                err(w, "trous {n} incohérents avec gaps")
            }
            ar("$w.sentence", PLACEHOLDER.replace(screen.sentence, "بِ"), vocalized = false)
            screen.options.forEach { ar("$w.options", it) }
            if (screen.options.map { ArabicScript.withoutHarakat(it) }.toSet().size != screen.options.size) {
                err(w, "options identiques (au moins sans harakat)")
            }
            if (screen.options.size <= screen.gaps.size) err(w, "pas assez d'options : aucun choix possible")
            screen.gaps.forEachIndexed { i, g ->
                if (g.answers.isEmpty()) err(w, "trou $i sans réponse")
                g.answers.filterNot { it in screen.options }.forEach { err(w, "réponse absente des options : $it") }
            }
            if (screen.gaps.all { it.answers.isNotEmpty() }) ar("$w.phrase complète", Audio.fillGapSentence(screen))
        }

        fun checkOrder(w: String, screen: OrderScreen) {
            fr("$w.fr", screen.fr)
            if (screen.answers.isEmpty()) {
                err(w, "answers vide")
                return
            }
            val base = screen.answers.first()
            if (base.size < 2) err(w, "il faut au moins 2 étiquettes")
            val baseCounts = base.groupingBy { it }.eachCount()
            for (answer in screen.answers) {
                if (answer.groupingBy { it }.eachCount() != baseCounts) err(w, "les réponses n'utilisent pas les mêmes étiquettes")
            }
            duplicates(screen.answers.map { it.joinToString("|") }).forEach { err(w, "réponse en double") }
            val distractors = screen.distractors.orEmpty()
            for (token in base.toSet() + distractors) {
                ar("$w.étiquette", token)
                if (' ' in token) err(w, "étiquette avec espace : « $token »")
            }
            distractors.filter { it in base }.forEach { err(w, "distracteur identique à une étiquette : $it") }
            val tokens = base.toSet() + distractors
            if (tokens.map { ArabicScript.withoutHarakat(it) }.toSet().size != tokens.size) {
                err(w, "étiquettes qui ne diffèrent que par les harakat")
            }
            screen.punctuation?.let { if (it !in setOf(".", "؟", "!")) err(w, "ponctuation finale inattendue : $it") }
        }

        fun checkTyping(w: String, screen: TypingScreen, pos: Position) {
            if ((screen.item == null) == (screen.answers == null)) err(w, "il faut item OU answers")
            if ((screen.question != null) != (screen.ask == TypingAsk.QUESTION)) {
                err(w, "question présent si et seulement si ask vaut question")
            }
            screen.question?.let { ar("$w.question", it) }
            screen.item?.let {
                presented(w, it, pos)
                if (screen.fr != null) err(w, "fr inutile avec item")
            }
            screen.answers?.let { answers ->
                if (answers.isEmpty()) err(w, "answers vide")
                if (screen.fr == null) err(w, "fr obligatoire avec answers") else fr("$w.fr", screen.fr)
                answers.forEach { ar("$w.answers", it) }
                if (answers.map { letters(it) }.toSet().size != answers.size) err(w, "réponses en double")
            }
        }

        fun checkQuestion(w: String, screen: QuestionScreen) {
            txt("$w.prompt", screen.prompt)
            val keys = screen.choices.mapIndexed { i, c -> txt("$w.choices[$i]", c) }
            if (screen.choices.size !in 2..4) err(w, "il faut 2 à 4 choix")
            if (keys.filterNotNull().toSet().size != keys.size) err(w, "choix en double (au moins sans harakat)")
            if (screen.answer !in screen.choices.indices) err(w, "answer hors bornes")
        }

        /** Texte d'un choix (fr ou ar) ; renvoie une clé de comparaison. */
        fun txt(where: String, t: Txt): String? {
            if (listOfNotNull(t.fr, t.ar).size != 1) {
                err(where, "il faut exactement un champ fr ou ar")
                return null
            }
            return t.ar?.let {
                ar(where, it)
                "ar:" + letters(it)
            } ?: run {
                fr(where, t.fr!!)
                "fr:" + t.fr
            }
        }

        fun checkSort(w: String, screen: SortScreen, pos: Position) {
            if (screen.categories.size !in 2..3) err(w, "il faut 2 ou 3 catégories")
            val keys = screen.categories.mapIndexed { i, c -> txt("$w.categories[$i]", c) }
            if (keys.filterNotNull().toSet().size != keys.size) err(w, "catégories en double")
            if (screen.cards.size !in 3..8) err(w, "il faut 3 à 8 cartes")
            val cardKeys = screen.cards.mapIndexed { i, c ->
                val cw = "$w.cards[$i]"
                if (c.category !in screen.categories.indices) err(cw, "catégorie hors bornes")
                if (listOfNotNull(c.item, c.ar, c.fr).size != 1) {
                    err(cw, "il faut exactement un champ item, ar ou fr")
                    null
                } else {
                    c.item?.let { presented(cw, it, pos)?.let { item -> "ar:" + letters(item.ar) } }
                        ?: txt(cw, Txt(fr = c.fr, ar = c.ar))
                }
            }
            if (cardKeys.filterNotNull().toSet().size != cardKeys.size) err(w, "cartes en double")
            screen.categories.indices.filter { k -> screen.cards.none { it.category == k } }
                .forEach { err(w, "catégorie $it sans carte") }
        }

        fun checkSpot(w: String, screen: SpotScreen) {
            fr("$w.instruction", screen.instruction)
            screen.fr?.let { fr("$w.fr", it) }
            if (screen.words.size !in 2..12) err(w, "il faut 2 à 12 mots")
            screen.words.forEach { if (' ' in it || it.isBlank()) err(w, "mot vide ou avec espace : « $it »") }
            ar("$w.words", Audio.spotSentence(screen))
            if (screen.answers.isEmpty()) err(w, "answers vide")
            duplicates(screen.answers.map { it.toString() }).forEach { err(w, "réponse en double : $it") }
            screen.answers.filterNot { it in screen.words.indices }.forEach { err(w, "réponse hors bornes : $it") }
            if (screen.answers.toSet().size >= screen.words.size) err(w, "tous les mots sont des réponses")
        }

        fun checkBuild(w: String, screen: BuildScreen, pos: Position) {
            val item = presented(w, screen.item, pos) ?: return
            if (' ' in item.ar.trim()) err(w, "un seul mot à écrire, sans espace")
            val n = Audio.buildLetters(item.ar).length
            if (n !in 2..9) err(w, "il faut un mot de 2 à 9 lettres")
            if (screen.extra.size > 3) err(w, "3 lettres en plus au maximum")
            screen.extra.forEach { x ->
                if (x.length != 1 || !ArabicScript.isLetter(x[0])) err(w, "lettre en plus invalide : « $x »")
            }
        }

        fun checkMemory(w: String, screen: MemoryScreen, pos: Position) {
            if (screen.items.size !in 3..6) err(w, "il faut 3 à 6 éléments")
            duplicates(screen.items).forEach { err(w, "élément en double : $it") }
            val items = screen.items.mapNotNull { presented(w, it, pos) }
            if (items.map { letters(it.ar) }.toSet().size != items.size) err(w, "deux textes arabes identiques")
            if (items.map { it.fr }.toSet().size != items.size) err(w, "deux traductions identiques")
            if (screen.mode == MemoryMode.ICON_AR) {
                items.filter { it.icon == null }.forEach { err(w, "icon-ar avec un élément sans icône : ${it.id}") }
                if (items.mapNotNull { it.icon }.toSet().size != items.size) err(w, "deux icônes identiques")
            }
        }

        fun checkDialogue(w: String, screen: DialogueScreen) {
            fr("$w.title", screen.title)
            screen.intro?.let { fr("$w.intro", it) }
            if (screen.speakers.isEmpty()) err(w, "aucun locuteur")
            for ((key, sp) in screen.speakers) {
                if (sp.name.isBlank()) err(w, "locuteur $key sans nom")
                if (sp.voice !in course.file.voices) err(w, "voix inconnue : ${sp.voice}")
            }
            if (screen.lines.isEmpty()) err(w, "dialogue vide")
            screen.lines.forEachIndexed { i, line ->
                if (line.speaker !in screen.speakers) err("$w.lines[$i]", "locuteur inconnu : ${line.speaker}")
                ar("$w.lines[$i].ar", line.ar)
                fr("$w.lines[$i].fr", line.fr)
            }
        }

        // ---------- contrôles globaux ----------

        /** Pas de هٰذَا / ذٰلِكَ directement devant un nom marqué féminin (le féminin vient plus tard). */
        fun checkDemonstratives() {
            fun bare(s: String) = ArabicScript.lettersOnly(s)
            val feminine = course.itemsById.values.filter { it.gender == Gender.FEMININE }.map { bare(it.ar) }.toSet()
            val demonstratives = setOf("هذا", "ذلك", "اهذا", "اذلك", "أهذا", "أذلك", "وهذا", "وذلك")
            for ((where, text) in arabicTexts) {
                val words = ArabicScript.nfc(text).split(' ').map { bare(it) }
                for ((a, b) in words.zipWithNext()) {
                    if (a in demonstratives && (b in feminine || (b.startsWith("ال") && b.drop(2) in feminine))) {
                        err(where, "démonstratif masculin devant un nom féminin : « $text »")
                    }
                }
            }
        }

        fun checkAudio() {
            val needs = Audio.needs(course)
            audioNeeds = needs.size
            if (audioExists == null) {
                warn("audio", "aucun audio généré pour l'instant (${needs.size} attendus)")
                return
            }
            val missing = needs.map { Audio.path(course, it) }.filterNot { audioExists.invoke(it) }
            if (missing.isNotEmpty()) err("audio", "${missing.size} audio(s) manquant(s), ex. ${missing.take(3)}")
        }

        private fun letters(s: String) = ArabicScript.lettersOnly(s)
    }

    private companion object {
        val CODE = Regex("[a-z0-9-]+")
        val ITEM_ID = Regex("[wp]:[a-z0-9-]+")
        val RATE = Regex("[+-]\\d{1,3}%")
        val MANUAL_AUDIO = Regex("audio/manual/[a-z0-9-]+\\.mp3")
        val PLACEHOLDER = Regex("\\{(\\d+)\\}")

        fun duplicates(list: List<String>): Set<String> =
            list.groupingBy { it }.eachCount().filterValues { it > 1 }.keys

        fun typeName(screen: Screen): String = when (screen) {
            is PresentScreen -> "present"
            is TipScreen -> "tip"
            is McqScreen -> "mcq"
            is MatchScreen -> "match"
            is FillGapScreen -> "fill_gap"
            is OrderScreen -> "order"
            is TypingScreen -> "typing"
            is TrueFalseScreen -> "true_false"
            is QuestionScreen -> "question"
            is DialogueScreen -> "dialogue"
            is SortScreen -> "sort"
            is SpotScreen -> "spot"
            is BuildScreen -> "build"
            is MemoryScreen -> "memory"
        }
    }
}
