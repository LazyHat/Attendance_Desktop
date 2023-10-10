package nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

sealed class Page {
    data object Main : Page()
    data object LogIn : Page()
    data class Lesson(val id: UInt) : Page()
}

interface NavController {
    @Composable
    fun collectCurrentPageAsState(): State<Page>
    fun navigateUp()
    fun navigate(page: Page)
}

class NavControllerInstance(val initialPage: Page) : NavController {
    private val pages = MutableStateFlow(listOf(initialPage))

    @Composable
    override fun collectCurrentPageAsState(): State<Page> = pages.map { it.last() }.collectAsState(initialPage)

    override fun navigateUp() {
        pages.update { it.dropLast(1) }
    }

    override fun navigate(page: Page) {
        pages.update { it + page }
        pages.value
    }
}