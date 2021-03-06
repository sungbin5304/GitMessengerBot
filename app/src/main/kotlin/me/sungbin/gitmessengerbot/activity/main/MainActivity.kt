/*
 * GitMessengerBot © 2021 지성빈 & 구환. all rights reserved.
 * GitMessengerBot license is under the GPL-3.0.
 *
 * [MainActivity.kt] created by Ji Sungbin on 21. 5. 31. 오후 11:12.
 *
 * Please see: https://github.com/GitMessengerBot/GitMessengerBot-Android/blob/master/LICENSE.
 */

package me.sungbin.gitmessengerbot.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import me.sungbin.gitmessengerbot.R
import me.sungbin.gitmessengerbot.activity.main.script.ScriptContent
import me.sungbin.gitmessengerbot.activity.main.script.ScriptItem
import me.sungbin.gitmessengerbot.activity.main.script.ScriptLang
import me.sungbin.gitmessengerbot.activity.main.script.compiler.CompileResult
import me.sungbin.gitmessengerbot.activity.main.script.compiler.repo.ScriptCompiler
import me.sungbin.gitmessengerbot.bot.Bot
import me.sungbin.gitmessengerbot.bot.StackManager
import me.sungbin.gitmessengerbot.bot.debug.Debug
import me.sungbin.gitmessengerbot.service.BackgroundService
import me.sungbin.gitmessengerbot.theme.MaterialTheme
import me.sungbin.gitmessengerbot.theme.SystemUiController
import me.sungbin.gitmessengerbot.theme.colors
import me.sungbin.gitmessengerbot.ui.fancybottombar.FancyBottomBar
import me.sungbin.gitmessengerbot.ui.fancybottombar.FancyColors
import me.sungbin.gitmessengerbot.ui.fancybottombar.FancyItem
import me.sungbin.gitmessengerbot.util.config.StringConfig
import me.sungbin.gitmessengerbot.util.extension.toast

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var scriptCompiler: ScriptCompiler

    private var tab by mutableStateOf(Tab.Script)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Bot.app.value.power) {
            startService(Intent(this, BackgroundService::class.java))
        }

        SystemUiController(window).run {
            setStatusBarColor(colors.primary)
            setNavigationBarColor(Color.White)
        }

        lifecycleScope.launchWhenCreated {
            if (StackManager.v8[StringConfig.ScriptEvalId] == null) {
                scriptCompiler.process(
                    applicationContext,
                    ScriptItem(
                        id = StringConfig.ScriptEvalId,
                        name = "",
                        lang = ScriptLang.JavaScript,
                        power = false,
                        compiled = false,
                        lastRun = ""
                    )
                ).collect { result ->
                    when (result) {
                        is CompileResult.Success -> toast(
                            this@MainActivity,
                            getString(R.string.main_toast_eval_loaded)
                        )
                        is CompileResult.Error -> toast(
                            this@MainActivity,
                            getString(R.string.main_toast_eval_load_fail, result.exception.message)
                        )
                    }
                }
            }
        }

        setContent {
            MaterialTheme {
                Main()
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun Main() {
        val scriptAddDialogVisible = remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.primary)
        ) {
            Crossfade(
                targetState = tab,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 60.dp)
            ) { index ->
                when (index) {
                    Tab.Script -> ScriptContent(
                        activity = this@MainActivity,
                        compiler = scriptCompiler,
                        scriptAddDialogVisible = scriptAddDialogVisible
                    )
                    Tab.Debug -> Debug()
                    else -> Text("TODO")
                }
            }
            Footer(scriptAddDialogVisible)
        }
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
    @Composable
    private fun Footer(scriptAddDialogVisible: MutableState<Boolean>) {
        val items = listOf(
            FancyItem(icon = R.drawable.ic_round_script_24, id = 0),
            FancyItem(icon = R.drawable.ic_round_debug_24, id = 1),
            FancyItem(icon = R.drawable.ic_round_github_24, id = 2),
            FancyItem(icon = R.drawable.ic_round_settings_24, id = 3)
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            FancyBottomBar(
                fancyColors = FancyColors(primary = colors.primary),
                items = items
            ) { tab = id }
            AnimatedVisibility(
                visible = tab == Tab.Script,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .padding(bottom = 35.dp)
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable {
                            scriptAddDialogVisible.value = true
                        },
                    color = colors.primary,
                    elevation = 2.dp
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_add_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed() // 닫기 확인 todo
    }
}
