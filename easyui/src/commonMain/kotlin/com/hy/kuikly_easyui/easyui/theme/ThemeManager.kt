package com.hy.kuikly_easyui.easyui.theme

import com.hy.kuikly_easyui.easyui.base.bridgeModule
import com.hy.kuikly_easyui.easyui.curEuPage
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.PageData
import com.tencent.kuikly.core.reactive.handler.observable

data class Theme(
    var colors: ThemeColors,
)

enum class ThemeColorMode(val raw: Int) {
    NOTE_SET(-1),
    DARK(0),
    LIGHT(1)
}

class ThemeContext {
    private var nightMode: ThemeScheme? by observable(null)
    var pageData: PageData? = null

    internal fun setup() {
        if (nightMode == null) {
            val isNight =
                pageData?.params?.optBoolean(IS_NIGHT_MODE_KEY) ?: false
            handleNightMode(isNight)
        }
    }

    internal fun themeDidChanged(data: JSONObject) {
        val isNight = data.optBoolean(IS_NIGHT_MODE_KEY)
        handleNightMode(isNight)

    }

    private fun handleNightMode(isNight: Boolean) {
        nightMode = if (isNight) ThemeScheme.DARK else ThemeScheme.LIGHT
    }

    /**
     * 是否为夜间模式，true 是夜间模式
     */
    val isNightModel: Boolean
        get() {
            return nightMode == ThemeScheme.DARK;
        }

    companion object {
        const val IS_NIGHT_MODE_KEY = "isNightMode"
    }
}

enum class ThemeType {
    COLOR
}

enum class ThemeScheme {
    LIGHT,
    DARK
}

object ThemeManager {
    var notifyCallback: (() -> Unit)? = null
    const val THEME_CHANGED_EVENT = "theme_changed"
    const val COLOR_MODE_EVENT = "system.colorMode"
    private val theme: Theme = Theme(
        colors = lightColorScheme
    )

    private val colorSchemes = mapOf(
        ThemeScheme.LIGHT to lightColorScheme,
        ThemeScheme.DARK to darkColorScheme
    )

    var systemMode: Int = -1

    fun getColor() = theme.colors.copy()

    fun receiveSystemMode(mode: Int) {
        this.systemMode = mode
        when (mode) {
            ThemeColorMode.NOTE_SET.raw -> {
                this.changeColorScheme(ThemeScheme.LIGHT)
            }
            ThemeColorMode.DARK.raw -> {
                this.changeColorScheme(ThemeScheme.DARK)
            }
            ThemeColorMode.LIGHT.raw -> {
                this.changeColorScheme(ThemeScheme.LIGHT)
            }
        }
    }

    fun changeColorScheme(scheme: ThemeScheme) {
        changeTheme(ThemeType.COLOR, scheme)
        curEuPage.bridgeModule.statusBar(scheme == ThemeScheme.LIGHT)
        notifyCallback?.invoke()
    }

    private fun changeTheme(type: ThemeType, scheme: ThemeScheme) {
        when (type) {
            ThemeType.COLOR -> {
                this.theme.colors = colorSchemes[scheme]!!
            }
        }
    }
}