package nav

import androidx.compose.runtime.mutableStateListOf

sealed class Page {
    data object Main : Page()
    data object LogIn : Page()
    data class Lesson(val id: UInt) : Page()
}

interface NavController {
    val currentPage: Page
    fun navigateUp()
    fun navigate(page: Page)
}

class NavControllerInstance(initialPage: Page) : NavController {
    private val pages = mutableStateListOf(initialPage)
    override val currentPage: Page
        get() = pages.last()

    override fun navigateUp() {
        pages.removeLast()
    }

    override fun navigate(page: Page) {
        pages.add(page)
    }
}