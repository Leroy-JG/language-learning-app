# Carte du code (générée par `tools/carte_du_code.py` — ne pas éditer à la main)

Pour lire une fonction : `Read` avec `offset` = ligne et `limit` ≈ 60, plutôt que le fichier entier.
Les fichiers `app/` ne se compilent que sur GitHub Actions.

- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/AnswerMatcher.kt` (132 l.) — AnswerMatcher:14
- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/ArabicScript.kt` (97 l.) — ArabicScript:6
- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/Markup.kt` (64 l.) — Markup:7
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Audio.kt` (79 l.) — AudioNeed:7, Audio:9
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/ContentLoader.kt` (130 l.) — ContentSource:8, FileContentSource:16, ContentIssue:28, Course:32, Level:57, Chapter:61, Activity:72, LoadResult:82, ContentLoader:85
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/CourseIndex.kt` (89 l.) — Position:6, CourseIndex:16
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Icons.kt` (20 l.) — Icons:7
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Model.kt` (381 l.) — CourseFile:12, Voice:22, LevelFile:25, ChapterFile:34, BiText:46, Section:49, ItemKind:52, Gender:58, Item:64, ActivityType:82, ActivityFile:91, ScreenContext:102, Txt:110, Screen:113, Exercise:118, PresentScreen:126, TipScreen:130, McqMode:133, McqScreen:142, MatchMode:153, MatchScreen:161, Gap:171, FillGapScreen:175, OrderScreen:188, TypingAsk:200, TypingScreen:208, Statement:221, TrueFalseScreen:225, QuestionScreen:236, SortCard:249, SortScreen:259, SpotScreen:271, BuildScreen:284, MemoryMode:294, MemoryScreen:302, Speaker:312, DialogueLine:315, DialogueScreen:319, Block:328, ParagraphBlock:332, ExampleBlock:336, TableBlock:340, NoteTone:343, NoteBlock:359, CompareRow:363, CompareBlock:367, Part:371, BreakdownBlock:376, GalleryBlock:381
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Validator.kt` (594 l.) — ValidationReport:7, Validator:22
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/ActivitySession.kt` (72 l.) — Outcome:10, ActivitySession:28
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/CourseProgress.kt` (41 l.) — LessonState:9, CourseProgress:17
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/Exercises.kt` (184 l.) — PreparedExercise:25, PreparedMcq:27, PreparedMatch:32, PreparedFillGap:36, PreparedOrder:50, PreparedTyping:55, PreparedTrueFalse:59, PreparedQuestion:63, PreparedSort:70, PreparedSpot:74, PreparedBuild:80, MemoryCard:89, MemoryFace:91, PreparedMemory:93, ExerciseFactory:99
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/Fsrs.kt` (116 l.) — Grade:10, Memory:30, Fsrs:42
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/ReviewCard.kt` (103 l.) — CardKind:4, ReviewCard:21, Mastery:33, ReviewScheduler:45
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/ReviewPlanner.kt` (222 l.) — ReviewExercise:19, GradedKey:22, Completion:25, ReviewRules:28, ReviewPlanner:100
- `core/src/main/kotlin/io/github/leroyjg/arabe/tools/ContentTool.kt` (84 l.) — main:17, courseDirs:29, audioRoot:32, validate:34, ManifestEntry:57, audioManifest:65
- `app/src/main/java/io/github/leroyjg/arabe/ArabeApp.kt` (35 l.) — ArabeApp:16, AppContainer:27
- `app/src/main/java/io/github/leroyjg/arabe/MainActivity.kt` (105 l.) — MainActivity:32, MainRoute:77, ActivityRoute:80, ReviewRoute:84, AppNavHost:87
- `app/src/main/java/io/github/leroyjg/arabe/audio/AudioPlayer.kt` (157 l.) — Sound:22, AudioPlayer:31, PhoneSpeech:89
- `app/src/main/java/io/github/leroyjg/arabe/audio/SoundEffects.kt` (72 l.) — SoundEffects:17, Haptics:55, rememberHaptics:68
- `app/src/main/java/io/github/leroyjg/arabe/data/Backup.kt` (117 l.) — BackupFile:11, BackupResult:22, BackupCard:34, BackupLog:47, BackupSaved:50, BackupManager:53
- `app/src/main/java/io/github/leroyjg/arabe/data/ContentRepository.kt` (52 l.) — AssetContentSource:19, ContentState:29, ContentRepository:36
- `app/src/main/java/io/github/leroyjg/arabe/data/ProgressDatabase.kt` (127 l.) — ActivityResultEntity:29, CompletionRow:42, ProgressDao:49, ProgressDatabase:84, ProgressRepository:96
- `app/src/main/java/io/github/leroyjg/arabe/data/ReviewDatabase.kt` (227 l.) — ReviewCardEntity:39, ReviewLogEntity:53, SavedItemEntity:65, ReviewDao:71, ReviewDatabase:128, ReviewCardEntity.toCard:139, ReviewCard.toEntity:146, ReviewRepository:159
- `app/src/main/java/io/github/leroyjg/arabe/data/Settings.kt` (87 l.) — ThemeMode:8, Settings:11, SettingsRepository:31
- `app/src/main/java/io/github/leroyjg/arabe/ui/Common.kt` (68 l.) — WithContent:37, ActivityType.label:54, ActivityType.icon:62
- `app/src/main/java/io/github/leroyjg/arabe/ui/Decor.kt` (95 l.) — AppIcons:30, DrawScope.eightPointStar:57, MasteryBars:78
- `app/src/main/java/io/github/leroyjg/arabe/ui/GenderTag.kt` (78 l.) — GenderIcons:31, GenderTag:56
- `app/src/main/java/io/github/leroyjg/arabe/ui/IconImages.kt` (86 l.) — IconImages:7
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/ActivityScreen.kt` (663 l.) — ReviewKind:126, ActivityScreen:133, ReviewSessionScreen:162, ReviewPlanHolder:215, planReview:217, NothingToReview:251, Player:265, PlayerTopBar:320, ComboSlot:339, ComboBadge:349, ScreenBody:368, BottomArea:402, BigButton:447, FeedbackPanel:458, EndScreen:528, StatTile:642, NoVoiceWarning:654
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Celebration.kt` (73 l.) — Piece:18, Confetti:32
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Games.kt` (411 l.) — SortContent:76, sortSound:166, SortCardText:173, CategoryLabel:182, GameInstruction:188, SpotContent:196, BuildContent:235, MemoryContent:305, MemoryTile:364
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/PlayerViewModel.kt` (450 l.) — Feedback:50, SessionKind:68, ActivityResult:75, PlayerViewModel:100
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/ScreenContents.kt` (844 l.) — Playback:100, Instruction:106, AutoPlay:112, SpokenArabic:124, PresentContent:134, InfoCard:184, TipContent:197, NoteBox:214, CompareView:240, CompareSide:260, BreakdownView:276, GalleryView:308, ExampleCard:335, TipTable:354, TableRow:363, ContextView:384, DialogueReminder:399, playLines:415, DialogueContent:430, DialogueLines:458, McqContent:494, choiceState:526, QuestionContent:536, TxtView:553, TrueFalseContent:565, TypingContent:593, FillGapContent:651, RtlFlow:694, OrderContent:707, MatchContent:746, matchState:809, MatchTile:817, AnswerMatcher.Remark.label:839
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Widgets.kt` (184 l.) — SpeakerButton:62, ContentIcon:76, ChoiceState:82, ChoiceCard:85, Modifier.answerAnimation:121, TokenChip:148, BigPlayButton:175
- `app/src/main/java/io/github/leroyjg/arabe/ui/home/HomeScreen.kt` (500 l.) — levelOf:88, LearnScreen:96, LevelHeader:163, ProgressWithBadge:199, ChapterCard:228, ChapterRing:297, ChapterPath:330, LessonRow:365, LessonNode:395, TypePill:441, NextPill:453, CheckpointCard:465
- `app/src/main/java/io/github/leroyjg/arabe/ui/main/MainScreen.kt` (100 l.) — Tab:37, MainScreen:45
- `app/src/main/java/io/github/leroyjg/arabe/ui/review/ReviewScreen.kt` (738 l.) — VocabularyFilter:110, ReviewScreen:126, WordEntry:212, VocabularyTab:215, normalize:344, DueBanner:348, ChartEntry:364, MasteryChart:368, Hatching:410, SavedRow:423, WordRow:444, nextReviewText:521, WordThumb:532, NewChip:552, StartButton:559, EmptyState:573, GrammarTab:588, topicSummary:667, TopicRow:670, TopicSheet:691
- `app/src/main/java/io/github/leroyjg/arabe/ui/settings/SettingsScreen.kt` (340 l.) — SettingsScreen:66, Section:279, SwitchRow:294, <T>:308, ActionRow:326, InfoRow:335
- `app/src/main/java/io/github/leroyjg/arabe/ui/text/ArabicText.kt` (139 l.) — ReadingPrefs:30, displayArabic:40, ArabicText:48, arabicStyle:67, frenchSpacing:85, richText:90, RichText:112
- `app/src/main/java/io/github/leroyjg/arabe/ui/theme/Theme.kt` (157 l.) — Brand:28, AppColors:36, ArabeTheme:144, AppTheme:154
