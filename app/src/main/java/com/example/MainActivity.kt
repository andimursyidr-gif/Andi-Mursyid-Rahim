package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.preferences.UserRole
import com.example.ui.SmartLearningViewModel
import com.example.ui.announcements.AnnouncementDialog
import com.example.ui.assessments.AssessmentsScreen
import com.example.ui.attendance.AttendanceTrackerDialog
import com.example.ui.classes.ClassManagementDialog
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.MainNavTab
import com.example.ui.components.RoleSwitchDialog
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.grades.GradeAndReportScreen
import com.example.ui.materials.LearningMaterialsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.students.ExcelImportScreen
import com.example.ui.students.StudentManagementScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    SPLASH,
    MAIN,
    EXCEL_IMPORT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private val viewModel: SmartLearningViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val session by viewModel.session.collectAsStateWithLifecycle()
            val classes by viewModel.classes.collectAsStateWithLifecycle()
            val students by viewModel.students.collectAsStateWithLifecycle()
            val videos by viewModel.videos.collectAsStateWithLifecycle()
            val modules by viewModel.modules.collectAsStateWithLifecycle()
            val mediaList by viewModel.mediaList.collectAsStateWithLifecycle()
            val worksheets by viewModel.worksheets.collectAsStateWithLifecycle()
            val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
            val allAttempts by viewModel.allAttempts.collectAsStateWithLifecycle()
            val announcements by viewModel.announcements.collectAsStateWithLifecycle()
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            var currentScreen by remember { mutableStateOf(AppScreen.SPLASH) }
            var currentNavTab by remember { mutableStateOf(MainNavTab.DASHBOARD) }

            // Dialog states
            var showClassManagementDialog by remember { mutableStateOf(false) }
            var showAttendanceDialog by remember { mutableStateOf(false) }
            var showAnnouncementDialog by remember { mutableStateOf(false) }
            var showRoleDialog by remember { mutableStateOf(false) }

            MyApplicationTheme(darkTheme = session.isDarkMode) {
                when (currentScreen) {
                    AppScreen.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = { currentScreen = AppScreen.MAIN }
                        )
                    }

                    AppScreen.EXCEL_IMPORT -> {
                        BackHandler { currentScreen = AppScreen.MAIN }
                        ExcelImportScreen(
                            viewModel = viewModel,
                            classes = classes,
                            onBack = { currentScreen = AppScreen.MAIN }
                        )
                    }

                    AppScreen.SETTINGS -> {
                        BackHandler { currentScreen = AppScreen.MAIN }
                        SettingsScreen(
                            session = session,
                            settings = settings,
                            viewModel = viewModel,
                            onBack = { currentScreen = AppScreen.MAIN },
                            onOpenRoleSwitch = { showRoleDialog = true }
                        )
                    }

                    AppScreen.MAIN -> {
                        BackHandler(enabled = currentNavTab != MainNavTab.DASHBOARD) {
                            currentNavTab = MainNavTab.DASHBOARD
                        }

                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                AppTopBar(
                                    session = session,
                                    students = students,
                                    announcementCount = announcements.size,
                                    onRoleSwitch = { role -> viewModel.switchRole(role) },
                                    onSelectStudent = { s -> viewModel.selectStudent(s) },
                                    onAnnouncementsClick = { showAnnouncementDialog = true },
                                    onSettingsClick = { currentScreen = AppScreen.SETTINGS },
                                    onToggleOffline = { viewModel.toggleOfflineMode(it) }
                                )
                            },
                            bottomBar = {
                                AppBottomBar(
                                    currentTab = currentNavTab,
                                    role = session.role,
                                    onTabSelected = { currentNavTab = it }
                                )
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentNavTab) {
                                    MainNavTab.DASHBOARD -> {
                                        DashboardScreen(
                                            session = session,
                                            classes = classes,
                                            students = students,
                                            videos = videos,
                                            modules = modules,
                                            worksheets = worksheets,
                                            quizzes = quizzes,
                                            announcements = announcements,
                                            settings = settings,
                                            onNavigateTab = { currentNavTab = it },
                                            onOpenImportExcel = { currentScreen = AppScreen.EXCEL_IMPORT },
                                            onOpenCreateQuiz = { currentNavTab = MainNavTab.ASSESSMENTS },
                                            onOpenCreateWorksheet = { currentNavTab = MainNavTab.ASSESSMENTS },
                                            onOpenAttendance = { showAttendanceDialog = true }
                                        )
                                    }

                                    MainNavTab.STUDENTS -> {
                                        StudentManagementScreen(
                                            viewModel = viewModel,
                                            session = session,
                                            classes = classes,
                                            students = students,
                                            onOpenImportExcel = { currentScreen = AppScreen.EXCEL_IMPORT }
                                        )
                                    }

                                    MainNavTab.MATERIALS -> {
                                        LearningMaterialsScreen(
                                            viewModel = viewModel,
                                            session = session,
                                            classes = classes,
                                            videos = videos,
                                            modules = modules,
                                            mediaList = mediaList
                                        )
                                    }

                                    MainNavTab.ASSESSMENTS -> {
                                        AssessmentsScreen(
                                            viewModel = viewModel,
                                            session = session,
                                            classes = classes,
                                            worksheets = worksheets,
                                            quizzes = quizzes
                                        )
                                    }

                                    MainNavTab.REPORTS -> {
                                        GradeAndReportScreen(
                                            viewModel = viewModel,
                                            session = session,
                                            classes = classes,
                                            students = students,
                                            allAttempts = allAttempts,
                                            onOpenAttendance = { showAttendanceDialog = true }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Global Dialogs
                if (showClassManagementDialog) {
                    ClassManagementDialog(
                        classes = classes,
                        students = students,
                        onDismiss = { showClassManagementDialog = false },
                        onAddClass = { name, year, semester, teacher, desc ->
                            viewModel.addClass(name, year, semester, teacher, desc)
                        },
                        onDeleteClass = { c -> viewModel.deleteClass(c) }
                    )
                }

                if (showAttendanceDialog) {
                    AttendanceTrackerDialog(
                        classes = classes,
                        students = students,
                        viewModel = viewModel,
                        onDismiss = { showAttendanceDialog = false }
                    )
                }

                if (showAnnouncementDialog) {
                    AnnouncementDialog(
                        session = session,
                        classes = classes,
                        announcements = announcements,
                        onDismiss = { showAnnouncementDialog = false },
                        onAddAnnouncement = { title, content, author, cId, cName, priority ->
                            viewModel.addAnnouncement(title, content, author, cId, cName, priority)
                        },
                        onDeleteAnnouncement = { a -> viewModel.deleteAnnouncement(a) }
                    )
                }

                if (showRoleDialog) {
                    RoleSwitchDialog(
                        currentRole = session.role,
                        students = students,
                        currentStudentId = session.currentStudentId,
                        onDismiss = { showRoleDialog = false },
                        onSelectRole = { role ->
                            viewModel.switchRole(role)
                            showRoleDialog = false
                        },
                        onSelectStudent = { student ->
                            viewModel.selectStudent(student)
                            viewModel.switchRole(UserRole.SISWA)
                            showRoleDialog = false
                        }
                    )
                }
            }
        }
    }
}
