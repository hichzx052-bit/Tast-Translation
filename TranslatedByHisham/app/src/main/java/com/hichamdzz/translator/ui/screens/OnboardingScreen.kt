package com.hichamdzz.translator.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hichamdzz.translator.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(val icon: ImageVector, val title: String, val description: String)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = listOf(
        OnboardingPage(Icons.Default.Translate, "مرحباً بك", "Translated by Hisham\nتطبيق الترجمة الصوتية الفورية بالذكاء الاصطناعي"),
        OnboardingPage(Icons.Default.Mic, "الصلاحيات", "نحتاج إذن المايكروفون والعرض فوق التطبيقات\nلتشغيل الترجمة في الخلفية"),
        OnboardingPage(Icons.Default.TouchApp, "الزر العائم", "اضغط على الزر العائم في أي تطبيق\nللتحكم بالترجمة مباشرة"),
        OnboardingPage(Icons.Default.RocketLaunch, "جاهز!", "ابدأ الآن بترجمة أي محادثة صوتية\nبدقة عالية وأصوات بشرية حقيقية"),
    )
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        // SKIP button
        TextButton(onClick = onComplete, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
            Text("SKIP", color = Accent, fontSize = 16.sp)
        }

        Column(modifier = Modifier.fillMaxSize().padding(top = 60.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
                Column(modifier = Modifier.fillMaxSize().padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Brush.linearGradient(listOf(GradientStart, GradientEnd))), contentAlignment = Alignment.Center) {
                        Icon(pages[page].icon, contentDescription = null, modifier = Modifier.size(60.dp), tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    Text(pages[page].title, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(pages[page].description, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            }
            // Dots
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center) {
                repeat(pages.size) { i ->
                    Box(modifier = Modifier.padding(4.dp).size(if (pagerState.currentPage == i) 12.dp else 8.dp).clip(CircleShape).background(if (pagerState.currentPage == i) Primary else TextSecondary))
                }
            }
            // Button
            Button(
                onClick = { if (pagerState.currentPage < pages.size - 1) scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } else onComplete() },
                modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text(if (pagerState.currentPage < pages.size - 1) "التالي" else "ابدأ الآن", fontSize = 18.sp) }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
