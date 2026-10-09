package com.batterypet.app

import android.content.Context
import android.content.SharedPreferences

/**
 * Shared helpers: preferences, pet/mood drawable mapping, themes, dp conversion.
 */
object PetKit {

    const val PREFS_NAME = "battery_pet_prefs"

    val PETS = listOf("bunny", "cat", "bear", "dino")

    data class Theme(val key: String, val label: String, val outline: Int, val fill: Int)

    val THEMES = listOf(
        Theme("pink", "Pastel Pink", 0xFF5A4A5A.toInt(), 0xFFFF9EBB.toInt()),
        Theme("cream", "Coquette Cream", 0xFF6B4F3A.toInt(), 0xFFFFB3C7.toInt()),
        Theme("lavender", "Lavender Dream", 0xFF5A4A5A.toInt(), 0xFFB79CED.toInt())
    )

    fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun themeOf(key: String?): Theme = THEMES.find { it.key == key } ?: THEMES[0]

    fun petDrawable(pet: String, mood: String): Int = when (pet) {
        "cat" -> when (mood) {
            "happy" -> R.drawable.pet_cat_happy
            "sleepy" -> R.drawable.pet_cat_sleepy
            else -> R.drawable.pet_cat_normal
        }
        "bear" -> when (mood) {
            "happy" -> R.drawable.pet_bear_happy
            "sleepy" -> R.drawable.pet_bear_sleepy
            else -> R.drawable.pet_bear_normal
        }
        "dino" -> when (mood) {
            "happy" -> R.drawable.pet_dino_happy
            "sleepy" -> R.drawable.pet_dino_sleepy
            else -> R.drawable.pet_dino_normal
        }
        else -> when (mood) {
            "happy" -> R.drawable.pet_bunny_happy
            "sleepy" -> R.drawable.pet_bunny_sleepy
            else -> R.drawable.pet_bunny_normal
        }
    }

    /** Drawable for the currently selected pet in its current mood. */
    fun moodDrawable(context: Context): Int {
        val p = prefs(context)
        val pet = p.getString("pet", "bunny") ?: "bunny"
        val level = p.getInt("last_level", 100)
        val charging = p.getBoolean("last_charging", false)
        val mood = when {
            charging -> "happy"
            level <= 20 -> "sleepy"
            else -> "normal"
        }
        return petDrawable(pet, mood)
    }

    fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
