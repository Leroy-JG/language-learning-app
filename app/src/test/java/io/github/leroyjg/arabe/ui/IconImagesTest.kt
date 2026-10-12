package io.github.leroyjg.arabe.ui

import io.github.leroyjg.arabe.content.Icons
import kotlin.test.Test
import kotlin.test.assertEquals

class IconImagesTest {
    @Test
    fun `chaque icône du format a une image dans l'app`() {
        assertEquals(Icons.NAMES, IconImages.byName.keys)
    }
}
