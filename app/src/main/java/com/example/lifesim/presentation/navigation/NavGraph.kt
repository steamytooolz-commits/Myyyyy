package com.example.lifesim.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifesim.presentation.ui.screens.*
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.GameViewModel
import com.example.lifesim.presentation.viewmodel.Intent
import com.example.lifesim.presentation.viewmodel.Screen
import com.example.lifesim.presentation.viewmodel.UiState

object NavRoutes {
    const val DASHBOARD = "dashboard"
    const val ACTIVITIES = "activities"
    const val RELATIONSHIPS = "relationships"
    const val CAREER = "career"
    const val ASSETS = "assets"
    const val DYNASTY = "dynasty"
    const val PRISON = "prison"
    const val MILITARY = "military"
    const val POLITICAL = "political"
    const val EDUCATION = "education"
    const val REAL_ESTATE = "real_estate"
    const val INVESTMENT = "investment"
    const val HOBBIES = "hobbies"
    const val PETS = "pets"
    const val MEDICAL = "medical"
    const val SETTINGS = "settings"
}

private data class NavigationItem(val icon: ImageVector, val label: String, val screen: Screen, val route: String)

private fun getNavigationItems(): List<NavigationItem> = listOf(
    NavigationItem(Icons.Rounded.Home, "Life", Screen.DASHBOARD, NavRoutes.DASHBOARD),
    NavigationItem(Icons.Rounded.FitnessCenter, "Active", Screen.ACTIVITIES, NavRoutes.ACTIVITIES),
    NavigationItem(Icons.Rounded.Favorite, "Social", Screen.RELATIONSHIPS, NavRoutes.RELATIONSHIPS),
    NavigationItem(Icons.Rounded.Work, "Career", Screen.CAREER, NavRoutes.CAREER),
    NavigationItem(Icons.Rounded.AccountBalanceWallet, "Assets", Screen.ASSETS, NavRoutes.ASSETS),
    NavigationItem(Icons.Rounded.AccountTree, "Dynasty", Screen.DYNASTY, NavRoutes.DYNASTY),
    NavigationItem(Icons.Rounded.Lock, "Prison", Screen.PRISON, NavRoutes.PRISON),
    NavigationItem(Icons.Rounded.Shield, "Military", Screen.MILITARY, NavRoutes.MILITARY),
    NavigationItem(Icons.Rounded.HowToVote, "Politics", Screen.POLITICAL, NavRoutes.POLITICAL),
    NavigationItem(Icons.Rounded.School, "School", Screen.EDUCATION, NavRoutes.EDUCATION),
    NavigationItem(Icons.Rounded.House, "Estate", Screen.REAL_ESTATE, NavRoutes.REAL_ESTATE),
    NavigationItem(Icons.Rounded.TrendingUp, "Invest", Screen.INVESTMENT, NavRoutes.INVESTMENT),
    NavigationItem(Icons.Rounded.Palette, "Hobbies", Screen.HOBBIES, NavRoutes.HOBBIES),
    NavigationItem(Icons.Rounded.Pets, "Pets", Screen.PETS, NavRoutes.PETS),
    NavigationItem(Icons.Rounded.LocalHospital, "Health", Screen.MEDICAL, NavRoutes.MEDICAL)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HubCard(item: NavigationItem, isActive: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Accent.copy(alpha = 0.15f) else BackgroundCard
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isActive) BorderStroke(1.dp, Accent) else null,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                item.icon,
                contentDescription = item.label,
                tint = if (isActive) Accent else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                item.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) Accent else TextPrimary,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    uiState: UiState,
    onScreenChange: (Screen) -> Unit,
    onAgeUp: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= 600.dp
        val hasLivingCharacter = uiState.character != null && uiState.character.isAlive

        if (isTablet && hasLivingCharacter) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Navigation Rail on tablet
                NavigationRail(
                    containerColor = BackgroundSurface,
                    modifier = Modifier
                        .width(88.dp)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Accent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.AutoAwesome,
                                contentDescription = "Aeterna",
                                tint = Accent,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Age Up Button directly inside rail
                        IconButton(
                            onClick = onAgeUp,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Accent)
                        ) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Age Up",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        getNavigationItems().forEach { item ->
                            val selected = uiState.currentScreen == item.screen
                            NavigationRailItem(
                                selected = selected,
                                onClick = { onScreenChange(item.screen) },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) Accent else TextSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Accent else TextSecondary,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Accent,
                                    unselectedIconColor = TextSecondary,
                                    indicatorColor = BackgroundCard
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(BackgroundCard)
                )

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = uiState.character?.let { "${it.firstName} ${it.lastName}" } ?: "Aeterna",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    uiState.character?.let { c ->
                                        if (c.isAlive) {
                                            Text(
                                                text = "Year ${uiState.currentYear} · Age ${(uiState.currentYear - (c.dateOfBirth / 31557600000L).toInt()).coerceAtLeast(0)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                if (uiState.character != null && uiState.character.isAlive) {
                                    Text(
                                        text = "$ ${String.format("%,.0f", uiState.character?.cash ?: 0.0)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Gold,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 16.dp)
                                    )
                                }
                                IconButton(onClick = { onScreenChange(Screen.SETTINGS) }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Settings,
                                        contentDescription = "Settings",
                                        tint = TextPrimary
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = BackgroundSurface,
                                titleContentColor = TextPrimary
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { padding ->
                    content(padding)
                }
            }
        } else {
            // Mobile layout (or tablet without a living character)
            var showHubSheet by remember { mutableStateOf(false) }

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = uiState.character?.let { "${it.firstName} ${it.lastName}" } ?: "Aeterna",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                uiState.character?.let { c ->
                                    if (c.isAlive) {
                                        Text(
                                            text = "Year ${uiState.currentYear} · Age ${(uiState.currentYear - (c.dateOfBirth / 31557600000L).toInt()).coerceAtLeast(0)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        },
                        actions = {
                            if (uiState.character != null && uiState.character.isAlive) {
                                Text(
                                    text = "$ ${String.format("%,.0f", uiState.character?.cash ?: 0.0)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            }
                            IconButton(onClick = { onScreenChange(Screen.SETTINGS) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = TextPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BackgroundSurface,
                            titleContentColor = TextPrimary
                        )
                    )
                },
                bottomBar = {
                    if (hasLivingCharacter) {
                        Surface(
                            color = BackgroundSurface,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBar(
                                containerColor = BackgroundSurface,
                                tonalElevation = 8.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                            ) {
                                val mainItems = listOf(
                                    NavigationItem(Icons.Rounded.Home, "Life", Screen.DASHBOARD, NavRoutes.DASHBOARD),
                                    NavigationItem(Icons.Rounded.FitnessCenter, "Active", Screen.ACTIVITIES, NavRoutes.ACTIVITIES),
                                    NavigationItem(Icons.Rounded.Favorite, "Social", Screen.RELATIONSHIPS, NavRoutes.RELATIONSHIPS),
                                    NavigationItem(Icons.Rounded.AccountBalanceWallet, "Assets", Screen.ASSETS, NavRoutes.ASSETS)
                                )

                                mainItems.forEach { item ->
                                    val selected = uiState.currentScreen == item.screen
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = { onScreenChange(item.screen) },
                                        icon = {
                                            Icon(
                                                item.icon,
                                                contentDescription = item.label,
                                                tint = if (selected) Accent else TextSecondary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        },
                                        label = {
                                            Text(
                                                item.label,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) Accent else TextSecondary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Accent,
                                            unselectedIconColor = TextSecondary,
                                            indicatorColor = BackgroundCard
                                        )
                                    )
                                }

                                val isOtherScreenActive = uiState.currentScreen !in listOf(Screen.DASHBOARD, Screen.ACTIVITIES, Screen.RELATIONSHIPS, Screen.ASSETS)
                                NavigationBarItem(
                                    selected = isOtherScreenActive,
                                    onClick = { showHubSheet = true },
                                    icon = {
                                        Icon(
                                            Icons.Rounded.Apps,
                                            contentDescription = "More Systems",
                                            tint = if (isOtherScreenActive) Accent else TextSecondary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            "More",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isOtherScreenActive) Accent else TextSecondary,
                                            fontWeight = if (isOtherScreenActive) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Accent,
                                        unselectedIconColor = TextSecondary,
                                        indicatorColor = BackgroundCard
                                    )
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { padding ->
                content(padding)

                if (showHubSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showHubSheet = false },
                        containerColor = BackgroundSurface,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = TextSecondary.copy(alpha = 0.5f)) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 32.dp, top = 8.dp)
                        ) {
                            Text(
                                "Life Systems",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            val secondaryItems = getNavigationItems().filter {
                                it.screen !in listOf(Screen.DASHBOARD, Screen.ACTIVITIES, Screen.RELATIONSHIPS, Screen.ASSETS)
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val chunked = secondaryItems.chunked(3)
                                chunked.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        rowItems.forEach { item ->
                                            Box(modifier = Modifier.weight(1f)) {
                                                HubCard(item = item, isActive = uiState.currentScreen == item.screen) {
                                                    showHubSheet = false
                                                    onScreenChange(item.screen)
                                                }
                                            }
                                        }
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    // Event modal overlay (shown on top of any screen)
    if (uiState.showEvent && uiState.currentEvent != null) {
        EventModalScreen(
            event = uiState.currentEvent!!,
            onChoice = { choiceIndex ->
                viewModel.processIntent(Intent.MakeChoice(choiceIndex, uiState.currentEvent!!))
            },
            onDismiss = { viewModel.processIntent(Intent.DismissEvent) }
        )
    }

    val onScreenChange: (Screen) -> Unit = { screen ->
        val hasLivingCharacter = uiState.character != null && uiState.character!!.isAlive
        if (!hasLivingCharacter) {
            if (screen == Screen.DASHBOARD || screen == Screen.SETTINGS) {
                viewModel.processIntent(Intent.ChangeScreen(screen))
                val route = if (screen == Screen.DASHBOARD) NavRoutes.DASHBOARD else NavRoutes.SETTINGS
                navController.navigate(route) {
                    popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        } else {
            viewModel.processIntent(Intent.ChangeScreen(screen))
            val route = when (screen) {
                Screen.DASHBOARD -> NavRoutes.DASHBOARD
                Screen.ACTIVITIES -> NavRoutes.ACTIVITIES
                Screen.RELATIONSHIPS -> NavRoutes.RELATIONSHIPS
                Screen.CAREER -> NavRoutes.CAREER
                Screen.ASSETS -> NavRoutes.ASSETS
                Screen.DYNASTY -> NavRoutes.DYNASTY
                Screen.PRISON -> NavRoutes.PRISON
                Screen.MILITARY -> NavRoutes.MILITARY
                Screen.POLITICAL -> NavRoutes.POLITICAL
                Screen.EDUCATION -> NavRoutes.EDUCATION
                Screen.REAL_ESTATE -> NavRoutes.REAL_ESTATE
                Screen.INVESTMENT -> NavRoutes.INVESTMENT
                Screen.HOBBIES -> NavRoutes.HOBBIES
                Screen.PETS -> NavRoutes.PETS
                Screen.MEDICAL -> NavRoutes.MEDICAL
                Screen.SETTINGS -> NavRoutes.SETTINGS
            }
            navController.navigate(route) {
                popUpTo(NavRoutes.DASHBOARD) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    MainLayout(
        uiState = uiState,
        onScreenChange = onScreenChange,
        onAgeUp = { viewModel.processIntent(Intent.AgeUp) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.DASHBOARD,
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            composable(NavRoutes.DASHBOARD) {
                DashboardScreen(
                    uiState = uiState,
                    onStartNewGame = { viewModel.processIntent(Intent.StartNewGame) },
                    onAgeUp = { viewModel.processIntent(Intent.AgeUp) },
                    onScreenChange = onScreenChange
                )
            }

            composable(NavRoutes.ACTIVITIES) {
                ActivityScreen(
                    uiState = uiState,
                    onActivity = { activityId ->
                        viewModel.processIntent(Intent.PerformActivity(activityId))
                    }
                )
            }

            composable(NavRoutes.RELATIONSHIPS) {
                RelationshipsScreen(
                    uiState = uiState,
                    onInteraction = { relId, type ->
                        viewModel.processIntent(Intent.RelationshipInteraction(relId, type))
                    }
                )
            }

            composable(NavRoutes.CAREER) {
                CareerScreen(uiState = uiState)
            }

            composable(NavRoutes.ASSETS) {
                AssetsScreen(uiState = uiState)
            }

            composable(NavRoutes.DYNASTY) {
                DynastyScreen(uiState = uiState)
            }

            composable(NavRoutes.PRISON) {
                PrisonScreen(
                    uiState = uiState,
                    onPrisonAction = { action ->
                        viewModel.processIntent(Intent.PrisonAction(action))
                    }
                )
            }

            composable(NavRoutes.MILITARY) {
                MilitaryScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.MilitaryAction(action))
                    }
                )
            }

            composable(NavRoutes.POLITICAL) {
                PoliticalScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.PoliticalAction(action))
                    }
                )
            }

            composable(NavRoutes.EDUCATION) {
                EducationScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.EducationAction(action))
                    }
                )
            }

            composable(NavRoutes.REAL_ESTATE) {
                RealEstateScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.RealEstateAction(action))
                    }
                )
            }

            composable(NavRoutes.INVESTMENT) {
                InvestmentScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.InvestmentAction(action))
                    }
                )
            }

            composable(NavRoutes.HOBBIES) {
                HobbyScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.PerformActivity(action))
                    }
                )
            }

            composable(NavRoutes.PETS) {
                PetScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.PerformActivity(action))
                    }
                )
            }

            composable(NavRoutes.MEDICAL) {
                MedicalScreen(
                    uiState = uiState,
                    onAction = { action ->
                        viewModel.processIntent(Intent.PerformActivity(action))
                    }
                )
            }

            composable(NavRoutes.SETTINGS) {
                SettingsScreen(
                    settingsManager = viewModel.settingsManager,
                    apiService = viewModel.apiService,
                    onBack = { onScreenChange(Screen.DASHBOARD) }
                )
            }
        }
    }
}

