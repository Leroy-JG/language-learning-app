# Carte du code (générée par `tools/carte_du_code.py` — ne pas éditer à la main)

Pour lire une fonction : `Read` avec `offset` = ligne et `limit` ≈ 60, plutôt que le fichier entier.
Les fichiers `app/` ne se compilent que sur GitHub Actions.

- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/AnswerMatcher.kt` (132 l.) — AnswerMatcher:14
- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/ArabicScript.kt` (98 l.) — ArabicScript:6
- `core/src/main/kotlin/io/github/leroyjg/arabe/arabic/Markup.kt` (64 l.) — Markup:7
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Audio.kt` (83 l.) — AudioNeed:7, Audio:9
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/ContentLoader.kt` (130 l.) — ContentSource:8, FileContentSource:16, ContentIssue:28, Course:32, Level:57, Chapter:61, Activity:72, LoadResult:82, ContentLoader:85
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/CourseIndex.kt` (89 l.) — Position:6, CourseIndex:16
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Icons.kt` (22 l.) — Icons:7
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Model.kt` (407 l.) — CourseFile:12, Voice:22, LevelFile:25, ChapterFile:34, BiText:46, Section:49, ItemKind:52, Gender:58, Item:64, ActivityType:82, ActivityFile:100, ScreenContext:111, Txt:119, Screen:122, Exercise:127, PresentScreen:135, TipScreen:139, McqMode:142, McqScreen:151, MatchMode:162, MatchScreen:170, Gap:180, FillGapScreen:184, OrderScreen:197, TypingAsk:209, TypingScreen:217, Statement:230, TrueFalseScreen:234, QuestionScreen:245, MultiScreen:261, SortCard:275, SortScreen:285, SpotScreen:297, BuildScreen:310, MemoryMode:320, MemoryScreen:328, Speaker:338, DialogueLine:341, DialogueScreen:345, Block:354, ParagraphBlock:358, ExampleBlock:362, TableBlock:366, NoteTone:369, NoteBlock:385, CompareRow:389, CompareBlock:393, Part:397, BreakdownBlock:402, GalleryBlock:407
- `core/src/main/kotlin/io/github/leroyjg/arabe/content/Validator.kt` (639 l.) — ValidationReport:7, Validator:22
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/ActivitySession.kt` (73 l.) — Outcome:10, ActivitySession:29
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/CourseProgress.kt` (41 l.) — LessonState:9, CourseProgress:17
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/Exercises.kt` (197 l.) — PreparedExercise:26, PreparedMcq:28, PreparedMatch:33, PreparedFillGap:37, PreparedOrder:51, PreparedTyping:56, PreparedTrueFalse:60, PreparedQuestion:64, PreparedMulti:71, PreparedSort:78, PreparedSpot:82, PreparedBuild:88, MemoryCard:97, MemoryFace:99, PreparedMemory:101, ExerciseFactory:107
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/TestScoring.kt` (45 l.) — TestScoring:14
- `core/src/main/kotlin/io/github/leroyjg/arabe/engine/TimeEstimate.kt` (55 l.) — TimeEstimate:28
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/Fsrs.kt` (116 l.) — Grade:10, Memory:30, Fsrs:42
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/ReviewCard.kt` (103 l.) — CardKind:4, ReviewCard:21, Mastery:33, ReviewScheduler:45
- `core/src/main/kotlin/io/github/leroyjg/arabe/review/ReviewPlanner.kt` (222 l.) — ReviewExercise:19, GradedKey:22, Completion:25, ReviewRules:28, ReviewPlanner:100
- `core/src/main/kotlin/io/github/leroyjg/arabe/tools/ContentTool.kt` (84 l.) — main:17, courseDirs:29, audioRoot:32, validate:34, ManifestEntry:57, audioManifest:65
- `app/src/main/java/io/github/leroyjg/arabe/ArabeApp.kt` (35 l.) — ArabeApp:16, AppContainer:27
- `app/src/main/java/io/github/leroyjg/arabe/MainActivity.kt` (105 l.) — MainActivity:32, MainRoute:77, ActivityRoute:80, ReviewRoute:84, AppNavHost:87
- `app/src/main/java/io/github/leroyjg/arabe/audio/AudioPlayer.kt` (157 l.) — Sound:22, AudioPlayer:31, PhoneSpeech:89
- `app/src/main/java/io/github/leroyjg/arabe/audio/SoundEffects.kt` (72 l.) — SoundEffects:17, Haptics:55, rememberHaptics:68
- `app/src/main/java/io/github/leroyjg/arabe/data/Backup.kt` (120 l.) — BackupFile:11, BackupResult:22, BackupCard:34, BackupLog:47, BackupSaved:50, BackupManager:53
- `app/src/main/java/io/github/leroyjg/arabe/data/ContentRepository.kt` (52 l.) — AssetContentSource:19, ContentState:29, ContentRepository:36
- `app/src/main/java/io/github/leroyjg/arabe/data/ProgressDatabase.kt` (127 l.) — ActivityResultEntity:29, CompletionRow:42, ProgressDao:49, ProgressDatabase:84, ProgressRepository:96
- `app/src/main/java/io/github/leroyjg/arabe/data/ReviewDatabase.kt` (227 l.) — ReviewCardEntity:39, ReviewLogEntity:53, SavedItemEntity:65, ReviewDao:71, ReviewDatabase:128, ReviewCardEntity.toCard:139, ReviewCard.toEntity:146, ReviewRepository:159
- `app/src/main/java/io/github/leroyjg/arabe/data/Settings.kt` (87 l.) — ThemeMode:8, Settings:11, SettingsRepository:31
- `app/src/main/java/io/github/leroyjg/arabe/ui/Common.kt` (70 l.) — WithContent:37, ActivityType.label:54, ActivityType.icon:64
- `app/src/main/java/io/github/leroyjg/arabe/ui/Decor.kt` (95 l.) — AppIcons:30, DrawScope.eightPointStar:57, MasteryBars:78
- `app/src/main/java/io/github/leroyjg/arabe/ui/GenderTag.kt` (78 l.) — GenderIcons:31, GenderTag:56
- `app/src/main/java/io/github/leroyjg/arabe/ui/IconImages.kt` (98 l.) — IconImages:7
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/ActivityScreen.kt` (693 l.) — ReviewKind:130, ActivityScreen:137, ReviewSessionScreen:171, ReviewPlanHolder:224, planReview:226, NothingToReview:260, Player:274, PlayerTopBar:329, ComboSlot:348, ComboBadge:358, ScreenBody:377, BottomArea:412, BigButton:457, FeedbackPanel:468, EndScreen:538, StatTile:672, NoVoiceWarning:684
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Celebration.kt` (73 l.) — Piece:18, Confetti:32
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Games.kt` (411 l.) — SortContent:76, sortSound:166, SortCardText:173, CategoryLabel:182, GameInstruction:188, SpotContent:196, BuildContent:235, MemoryContent:305, MemoryTile:364
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/PlayerViewModel.kt` (499 l.) — Feedback:54, SessionKind:72, ActivityResult:79, PlayerViewModel:119
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/ScreenContents.kt` (872 l.) — Playback:101, Instruction:107, AutoPlay:113, SpokenArabic:125, PresentContent:135, InfoCard:185, TipContent:198, NoteBox:215, CompareView:241, CompareSide:261, BreakdownView:277, GalleryView:309, ExampleCard:336, TipTable:355, TableRow:364, ContextView:385, DialogueReminder:400, playLines:416, DialogueContent:431, DialogueLines:459, McqContent:495, choiceState:527, QuestionContent:537, MultiContent:557, TxtView:581, TrueFalseContent:593, TypingContent:621, FillGapContent:679, RtlFlow:722, OrderContent:735, MatchContent:774, matchState:837, MatchTile:845, AnswerMatcher.Remark.label:867
- `app/src/main/java/io/github/leroyjg/arabe/ui/activity/Widgets.kt` (184 l.) — SpeakerButton:62, ContentIcon:76, ChoiceState:82, ChoiceCard:85, Modifier.answerAnimation:121, TokenChip:148, BigPlayButton:175
- `app/src/main/java/io/github/leroyjg/arabe/ui/home/HomeScreen.kt` (555 l.) — levelOf:90, LearnScreen:98, LevelHeader:170, durationText:216, ProgressWithBadge:228, ChapterCard:257, ChapterRing:337, ChapterPath:370, LessonRow:410, LessonNode:445, TypePill:491, NextPill:503, CheckpointCard:515
- `app/src/main/java/io/github/leroyjg/arabe/ui/main/MainScreen.kt` (100 l.) — Tab:37, MainScreen:45
- `app/src/main/java/io/github/leroyjg/arabe/ui/review/ReviewScreen.kt` (738 l.) — VocabularyFilter:110, ReviewScreen:126, WordEntry:212, VocabularyTab:215, normalize:344, DueBanner:348, ChartEntry:364, MasteryChart:368, Hatching:410, SavedRow:423, WordRow:444, nextReviewText:521, WordThumb:532, NewChip:552, StartButton:559, EmptyState:573, GrammarTab:588, topicSummary:667, TopicRow:670, TopicSheet:691
- `app/src/main/java/io/github/leroyjg/arabe/ui/settings/SettingsScreen.kt` (340 l.) — SettingsScreen:66, Section:279, SwitchRow:294, <T>:308, ActionRow:326, InfoRow:335
- `app/src/main/java/io/github/leroyjg/arabe/ui/text/ArabicText.kt` (139 l.) — ReadingPrefs:30, displayArabic:40, ArabicText:48, arabicStyle:67, frenchSpacing:85, richText:90, RichText:112
- `app/src/main/java/io/github/leroyjg/arabe/ui/theme/Theme.kt` (157 l.) — Brand:28, AppColors:36, ArabeTheme:144, AppTheme:154
