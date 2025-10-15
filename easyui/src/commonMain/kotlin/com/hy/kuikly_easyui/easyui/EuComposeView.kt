package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.theme.ThemeManager
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ComposeView
import com.tencent.kuikly.core.module.NotifyModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.CallbackRef

/**
 * 所有 UIView 的基类，提供主题切换功能。
 */
abstract class EuComposeView<A : ComposeAttr, E : ComposeEvent> : ComposeView<A, E>() {
    var euColor by observable(ThemeManager.getColor())

    private lateinit var themeChangedRef: CallbackRef

    override fun created() {
        super.created()
        themeChangedRef = acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).addNotify(
            ThemeManager.THEME_CHANGED_EVENT
        ) { _ ->
            euColor = ThemeManager.getColor()
        }
    }

    override fun viewDestroyed() {
        super.viewDestroyed()
        acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).removeNotify(
            ThemeManager.THEME_CHANGED_EVENT,
            themeChangedRef
        )
    }
}