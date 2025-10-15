package com.hy.kuikly_easyui.easyui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.AlertDialog

class EuScaffoldController() {}

class EuScaffoldView :
    EuComposeView<EuScaffoldViewAttr, EuScaffoldViewEvent>() {
    private var scaffold: EuScaffoldViewAttr? = null

    override fun createEvent(): EuScaffoldViewEvent {
        return EuScaffoldViewEvent()
    }

    override fun createAttr(): EuScaffoldViewAttr {
        scaffold = EuScaffoldViewAttr()
        return scaffold!!
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        this.attr.getScaffold?.invoke(scaffold)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            ref {
                ctx.attr.getScaffoldRef?.invoke()
            }
            attr {
                flex(1f)
                flexDirectionColumn()
                alignItemsStretch()
                if (!ctx.attr.setBackgroundColor) {
                    backgroundColor(ctx.euColor.b10)
                }
            }
            EuNavBar(
                {
                    attr {
                        zIndex(1)
                        controller = ctx.attr.navBar
                    }
                }, attr = ctx.attr.navbarAttr
            )
            EuContentSwitcher(
                {
                    attr {
                        zIndex(2)
                        flex(1f)
                        controller = ctx.attr.status
                        builder = ctx.attr.builder
                        placeholder = ctx.attr.placeholder
                    }
                    event {
                        viewDidLoad = ctx.event.contentDidLoad
                    }
                },
                ctx.attr.statusAttr
            )
            ctx.AlertDialogBuilder().invoke(this)
        }
    }

    private fun AlertDialogBuilder(): ViewBuilder {
        val ctx = this
        return {
            AlertDialog {
                attr {
                    showAlert(ctx.attr.alert.showAlert)
                    title(ctx.attr.alert.title)
                    message(ctx.attr.alert.message)
                    actionButtons(*(ctx.attr.alert.actionButtons))
                    inWindow(true)
                }
                event {
                    clickActionButton { index ->
                        val close =
                            ctx.attr.alert.actionButtonClick?.invoke(index)
                                ?: true
                        if (close) {
                            ctx.attr.alert.showAlert = false
                        }
                    }
                    willDismiss {
                        val close = ctx.attr.alert.willDismiss?.invoke() ?: true
                        if (close) {
                            ctx.attr.alert.showAlert = false
                        }
                    }
                }
            }
        }
    }
}

class ESAlertOptions {
    var showAlert by observable(false)
    var title by observable("")
    var message by observable("")
    private var _actionButtons by observableList<String>()

    var actionButtons: Array<String>
        get() = _actionButtons.toTypedArray()
        set(value) {
            _actionButtons.clear()
            _actionButtons.addAll(value)
        }

    /**
     * 根据index进行确认点击了哪一个button处理对应事件(index值和actionButtons传入button的下标一致)
     * @return 返回true则关闭弹框，否则不关闭
     */
    var actionButtonClick: ((index: Int) -> Boolean)? = null

    /**
     * 按下系统返回按键或右滑返回时触发，返回true则关闭弹框
     */
    var willDismiss: (() -> Boolean)? = null
}

class EuScaffoldViewAttr : ComposeAttr() {
    val alert: ESAlertOptions = ESAlertOptions()

    /**
     * 脚手架容器控制器(主容器)
     */
    var container: EuScaffoldController = EuScaffoldController()

    /**
     * 导航条控制器(导航条)
     */
    var navBar: EuNavBarController = EuNavBarController()

    /**
     * 内容容器状态控制器(内容区)
     */
    var status: EuContentStatusController = EuContentStatusController()

    /**
     * 缺省图配置
     * @note 如果为空，则显示默认配置
     */
    var placeholder: EuPlaceConfig? = null
        set(value) {
            if (value?.netError != null) {
                value.netError?.type = EuPlaceholderType.NO_NETWORK
            }
            if (value?.dataEmpty != null) {
                value.dataEmpty?.type = EuPlaceholderType.NO_DATA
            }
            field = value
        }

    /**
     * 页面加载 builder
     * @note 必须实现！！
     */
    lateinit var builder: EuContentBuilder

    /**
     * 获取脚手架控制器
     * @note 如果「Pager」使用「EuScaffold」搭建页面，需要在此时机内绑定脚手架属性
     */
    var getScaffold: ((scaffold: EuScaffoldViewAttr?) -> Unit)? = null

    var getScaffoldRef: (() -> Unit)? = null

    /**
     * 绑定容器背景色
     * @note 默认背景色为 primary color
     */
    fun containerBgColor(color: Color): ComposeAttr {
        StyleConst.BACKGROUND_COLOR with color.toString()
        setBackgroundColor = true
        return this
    }

    /**
     * 绑定状态容器背景色
     * @note 默认背景色为透明色
     */
    fun statusBgColor(color: Color): ComposeAttr {
        statusAttr.setProp(StyleConst.BACKGROUND_COLOR, color.toString())
        statusAttr.setBackgroundColor = true
        return statusAttr
    }

    internal var setBackgroundColor: Boolean = false

    /**
     * 设置导航条背景色
     */
    fun navBarBgColor(color: Color): EuScaffoldViewAttr {
        navbarAttr.setProp(StyleConst.BACKGROUND_COLOR, color.toString())
        navbarAttr.setNavBarBackgroundColor = true
        return this
    }

    internal val statusAttr: EuContentSwitcherViewAttr =
        EuContentSwitcherViewAttr()
    internal val navbarAttr: EuNavBarViewAttr = EuNavBarViewAttr()
}

class EuScaffoldViewEvent : ComposeEvent() {
    var contentDidLoad: ((controller: EuContentStatusController) -> Unit)? =
        null
}

fun ViewContainer<*, *>.EuScaffold(init: EuScaffoldView.() -> Unit) {
    addChild(EuScaffoldView(), init)
}