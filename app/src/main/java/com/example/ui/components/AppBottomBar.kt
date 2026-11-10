package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.preferences.UserRole

enum class MainNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    DASHBOARD(
        title = "Beranda",
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "bottom_nav_dashboard"
    ),
    STUDENTS(
        title = "Siswa",
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People,
        testTag = "bottom_nav_students"
    ),
    MATERIALS(
        title = "Materi",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "bottom_nav_materials"
    ),
    ASSESSMENTS(
        title = "Tugas & Kuis",
        selectedIcon = Icons.Filled.Assignment,
        unselectedIcon = Icons.Outlined.Assignment,
        testTag = "bottom_nav_assessments"
    ),
    REPORTS(
        title = "Nilai",
        selectedIcon = Icons.Filled.Assessment,
        unselectedIcon = Icons.Outlined.Assessment,
        testTag = "bottom_nav_reports"
    )
}

@Composable
fun AppBottomBar(
    currentTab: MainNavTab,
    role: UserRole,
    onTabSelected: (MainNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        MainNavTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            // For student role, label could be "Kelas Saya" instead of "Siswa"
            val displayTitle = if (tab == MainNavTab.STUDENTS && role == UserRole.SISWA) "Kelas" else tab.title

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = displayTitle
                    )
                },
                label = { Text(displayTitle) },
                alwaysShowLabel = true,
                modifier = Modifier.testTag(tab.testTag)
            )
        }
    }
}
