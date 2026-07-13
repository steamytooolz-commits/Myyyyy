package com.example.lifesim.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifesim.presentation.ui.theme.*
import com.example.lifesim.presentation.viewmodel.UiState

data class ActivityItem(val id: String, val name: String, val icon: ImageVector, val color: Color, val statEffects: String)

val defaultActivities = listOf(
    ActivityItem("gym", "Gym", Icons.Rounded.FitnessCenter, HealthGreen, "athleticism+5 energy-8"),
    ActivityItem("study", "Study", Icons.Rounded.MenuBook, SmartsBlue, "smarts+6 energy-5"),
    ActivityItem("meditate", "Meditate", Icons.Rounded.SelfImprovement, KarmaPurple, "stress-8 sanity+3"),
    ActivityItem("socialize", "Socialize", Icons.Rounded.Groups, HappinessYellow, "happiness+5 charisma+2"),
    ActivityItem("work", "Work", Icons.Rounded.Work, ReputationTeal, "cash+5000 energy-15"),
    ActivityItem("party", "Party", Icons.Rounded.Celebration, LooksPink, "happiness+8 energy-12"),
    ActivityItem("travel", "Travel", Icons.Rounded.Flight, Gold, "happiness+10 energy-15"),
    ActivityItem("sleep", "Sleep", Icons.Rounded.Bedtime, Diamond, "stress-5 health+1"),
    ActivityItem("eat", "Eat", Icons.Rounded.Restaurant, StressOrange, "hunger+25 cash-30"),
    ActivityItem("therapy", "Therapy", Icons.Rounded.Psychology, SmartsBlue, "sanity+10 stress-8"),
    ActivityItem("shower", "Shower", Icons.Rounded.WaterDrop, HealthGreen, "hygiene+40 happiness+2"),
    ActivityItem("crime", "Crime", Icons.Rounded.Lock, Error, "karma-5 energy-10"),
    ActivityItem("drink", "Drink", Icons.Rounded.Liquor, StressOrange, "stress-5 cash-40"),
    ActivityItem("gamble", "Gamble", Icons.Rounded.Casino, Gold, "stress-3 energy-5"),
    ActivityItem("lottery", "Lottery", Icons.Rounded.ConfirmationNumber, Gold, "cash-50"),
    ActivityItem("smoke", "Smoke", Icons.Rounded.SmokingRooms, Error, "stress-3 health-3"),
    ActivityItem("rehab", "Rehab", Icons.Rounded.Healing, HealthGreen, "health+5 energy-10"),
    ActivityItem("plastic_surgery", "Plastic Surgery", Icons.Rounded.Face, LooksPink, "looks+20 cash-5000"),
)

val militaryActivities = listOf(
    ActivityItem("boot_camp", "Boot Camp", Icons.Rounded.FitnessCenter, HealthGreen, "athletic+15 discipline+15 health+5"),
    ActivityItem("combat_training", "Combat Training", Icons.Rounded.Gavel, Error, "athletic+5 aggression+8 discipline+5"),
    ActivityItem("military_exercise", "Military Exercise", Icons.Rounded.DirectionsRun, Gold, "athletic+8 discipline+5 energy-8"),
    ActivityItem("guard_duty", "Guard Duty", Icons.Rounded.Visibility, SmartsBlue, "discipline+3 energy-3"),
    ActivityItem("weapons_training", "Weapons Training", Icons.Rounded.TrackChanges, StressOrange, "smarts+3 athletic+5"),
)

val educationActivities = listOf(
    ActivityItem("attend_class", "Attend Class", Icons.Rounded.School, SmartsBlue, "smarts+8 discipline+3 stress+5"),
    ActivityItem("do_homework", "Homework", Icons.Rounded.Assignment, KarmaPurple, "smarts+5 discipline+4 stress+3"),
    ActivityItem("research", "Research", Icons.Rounded.Search, Gold, "smarts+10 creativity+5 stress+4"),
    ActivityItem("join_study_group", "Study Group", Icons.Rounded.Groups, HappinessYellow, "smarts+6 charisma+3"),
    ActivityItem("take_exam", "Take Exam", Icons.Rounded.Quiz, Error, "smarts+12 stress+15 energy-8"),
)

val politicalActivities = listOf(
    ActivityItem("campaign_rally", "Campaign Rally", Icons.Rounded.Campaign, KarmaPurple, "charisma+8 stress+10 cash-2000"),
    ActivityItem("fundraiser", "Fundraiser", Icons.Rounded.AttachMoney, Gold, "charisma+5 reputation+8 cash+5000"),
    ActivityItem("public_speech", "Public Speech", Icons.Rounded.RecordVoiceOver, SmartsBlue, "charisma+10 reputation+6 stress+12"),
    ActivityItem("meet_constituents", "Meet Constituents", Icons.Rounded.Groups, HealthGreen, "charisma+4 empathy+5 reputation+5"),
    ActivityItem("political_debate", "Political Debate", Icons.Rounded.Forum, Error, "smarts+8 charisma+8 reputation+10 stress+12"),
)

val realEstateActivities = listOf(
    ActivityItem("buy_property", "Buy Property", Icons.Rounded.Home, Gold, "cash-100,000 stress+8"),
    ActivityItem("sell_property", "Sell Property", Icons.Rounded.Sell, HealthGreen, "cash+120,000 happiness+5"),
    ActivityItem("renovate_home", "Renovate", Icons.Rounded.Handyman, SmartsBlue, "cash-5000 happiness+5 stress+5"),
    ActivityItem("hire_contractor", "Hire Contractor", Icons.Rounded.Construction, StressOrange, "cash-2000 stress-5"),
    ActivityItem("property_inspection", "Inspect Property", Icons.Rounded.Search, ReputationTeal, "smarts+3 energy-3"),
)

val investmentActivities = listOf(
    ActivityItem("research_stocks", "Research Stocks", Icons.Rounded.MenuBook, SmartsBlue, "smarts+8 stress+2"),
    ActivityItem("day_trade", "Day Trade", Icons.Rounded.FlashOn, Gold, "smarts+5 stress+10 cash+/-2000"),
    ActivityItem("check_portfolio", "Check Portfolio", Icons.Rounded.TrendingUp, HealthGreen, "stress +/- depending on market"),
    ActivityItem("diversify_investments", "Diversify", Icons.Rounded.AccountBalance, KarmaPurple, "smarts+5 discipline+3"),
)

val romanceActivities = listOf(
    ActivityItem("find_date", "Find Date", Icons.Rounded.Favorite, LooksPink, "happiness+5 energy-10"),
    ActivityItem("dating_app", "Dating App", Icons.Rounded.PhoneIphone, KarmaPurple, "happiness+3 energy-5 cash-10")
)

val familyActivities = listOf(
    ActivityItem("adopt_child", "Adopt Child", Icons.Rounded.ChildCare, HappinessYellow, "happiness+10 cash-25000"),
    ActivityItem("spend_time_family", "Spend Time All", Icons.Rounded.FamilyRestroom, HealthGreen, "happiness+5 energy-5")
)

val medicalActivities = listOf(
    ActivityItem("medical_checkup", "Checkup", Icons.Rounded.LocalHospital, HealthGreen, "health+10 smarts+2 cash-500"),
    ActivityItem("surgery", "Surgery", Icons.Rounded.Medication, Error, "health+50 energy-80 cash-20000"),
    ActivityItem("vaccination", "Vaccination", Icons.Rounded.Vaccines, SmartsBlue, "health+20 energy-5 cash-200"),
    ActivityItem("physical_therapy", "Physical Therapy", Icons.Rounded.Healing, HealthGreen, "health+25 athleticism+5 cash-1000")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityScreen(uiState: UiState, onActivity: (String) -> Unit) {
    Column(Modifier.fillMaxSize().background(BackgroundDark).verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Activities", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp))

        // --- Daily Life ---
        SectionHeader("Daily Life", Icons.Rounded.Favorite)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            defaultActivities.forEach { activity ->
                ActivityCard(activity, onActivity)
            }
        }

        // --- Romance ---
        val age = uiState.character?.dateOfBirth?.let { uiState.currentYear - (it / 31557600000L).toInt() } ?: 0
        if (age >= 16) {
            SectionHeader("Romance & Dating", Icons.Rounded.FavoriteBorder)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                romanceActivities.forEach { activity ->
                    ActivityCard(activity, onActivity)
                }
            }
        }

        // --- Family ---
        if (age >= 21) {
            SectionHeader("Family", Icons.Rounded.FamilyRestroom)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                familyActivities.forEach { activity ->
                    ActivityCard(activity, onActivity)
                }
            }
        }

        // --- Medical ---
        SectionHeader("Medical", Icons.Rounded.LocalHospital)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            medicalActivities.forEach { activity ->
                ActivityCard(activity, onActivity)
            }
        }

        // --- Military ---
        if (uiState.character?.isInMilitary == true) {
            SectionHeader("Military", Icons.Rounded.Shield)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                militaryActivities.forEach { activity ->
                    ActivityCard(activity, onActivity)
                }
            }
        }

        // --- Education ---
        if (uiState.character?.currentEducationId != null) {
            SectionHeader("Education", Icons.Rounded.School)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                educationActivities.forEach { activity ->
                    ActivityCard(activity, onActivity)
                }
            }
        }

        // --- Political ---
        if (uiState.character?.politicalOfficeTitle != null) {
            SectionHeader("Political", Icons.Rounded.HowToVote)
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                politicalActivities.forEach { activity ->
                    ActivityCard(activity, onActivity)
                }
            }
        }

        // --- Real Estate ---
        SectionHeader("Real Estate", Icons.Rounded.House)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            realEstateActivities.forEach { activity ->
                ActivityCard(activity, onActivity)
            }
        }

        // --- Investment ---
        SectionHeader("Investment", Icons.Rounded.TrendingUp)
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            investmentActivities.forEach { activity ->
                ActivityCard(activity, onActivity)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActivityCard(activity: ActivityItem, onActivity: (String) -> Unit) {
    Card(onClick = { onActivity(activity.id) },
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(155.dp)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(activity.icon, null, tint = activity.color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text(activity.name, style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(2.dp))
            Text(activity.statEffects, style = MaterialTheme.typography.labelSmall, color = TextSecondary, maxLines = 1)
        }
    }
}
