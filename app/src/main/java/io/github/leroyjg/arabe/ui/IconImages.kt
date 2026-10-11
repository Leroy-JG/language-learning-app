package io.github.leroyjg.arabe.ui

import androidx.annotation.DrawableRes
import io.github.leroyjg.arabe.R

/** Images des icônes du contenu (OpenMoji, CC BY-SA 4.0). Un test vérifie que chaque nom en a une. */
object IconImages {
    val byName: Map<String, Int> = mapOf(
        "house" to R.drawable.icon_house,
        "mosque" to R.drawable.icon_mosque,
        "door" to R.drawable.icon_door,
        "book" to R.drawable.icon_book,
        "pen" to R.drawable.icon_pen,
        "key" to R.drawable.icon_key,
        "bed" to R.drawable.icon_bed,
        "chair" to R.drawable.icon_chair,
        "shirt" to R.drawable.icon_shirt,
        "star" to R.drawable.icon_star,
        "doctor" to R.drawable.icon_doctor,
        "boy" to R.drawable.icon_boy,
        "man" to R.drawable.icon_man,
        "student" to R.drawable.icon_student,
        "teacher" to R.drawable.icon_teacher,
        "dog" to R.drawable.icon_dog,
        "cat" to R.drawable.icon_cat,
        "rooster" to R.drawable.icon_rooster,
        "donkey" to R.drawable.icon_donkey,
        "horse" to R.drawable.icon_horse,
        "camel" to R.drawable.icon_camel,
        "rock" to R.drawable.icon_rock,
        "milk" to R.drawable.icon_milk,
        "moon" to R.drawable.icon_moon,
        "sun" to R.drawable.icon_sun,
        "water" to R.drawable.icon_water,
        "bread" to R.drawable.icon_bread,
        "meal" to R.drawable.icon_meal,
        "meat" to R.drawable.icon_meat,
        "fish" to R.drawable.icon_fish,
        "flower" to R.drawable.icon_flower,
        "eye" to R.drawable.icon_eye,
        "mouth" to R.drawable.icon_mouth,
        "hand" to R.drawable.icon_hand,
    )

    @DrawableRes
    fun of(name: String?): Int? = name?.let { byName[it] }
}
