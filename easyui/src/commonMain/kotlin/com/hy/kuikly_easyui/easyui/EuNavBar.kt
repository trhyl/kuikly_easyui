package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.theme.Constant
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.directives.velseif
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.layout.FlexDirection
import com.tencent.kuikly.core.module.IModuleAccessor
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.pager.PageData
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.TextView
import com.tencent.kuikly.core.views.View

/**
 * 导航条右侧按钮配置
 * @param callback 点击事件
 * @param gap 与右侧元素的间距，默认 16dp
 * @param icon 按钮图标（与文案互斥）
 */
class IEuNavAction(
    val callback: (() -> Unit)? = null,
    hide: Boolean = false,
    gap: Float = 16f,
    icon: String? = null,
    val textView: (TextView.() -> Unit)? = null,
) {
    var icon: String? by observable(icon)
    var gap: Float by observable(gap)
    var hide: Boolean by observable(hide)
}

/**
 * @param text 页面标题（内置默认文字样式），优先级高于「titleView」
 * @param titleView 标题视图配置，如果不设置则展示默认标题配置
 * @param backAction 返回按钮点击事件，如果为空，则默认执行路由pop 操作关闭当前页面
 * @param topSafeArea 是否需要适配刘海屏，默认为true
 * @param hideNavBar 是否隐藏导航条（不包含刘海屏），默认为false
 * @param showBack 是否显示返回按钮，默认为true
 * @param builder 自定义导航条布局，图层位于默认元素（返回按钮、默认标题、右侧点击按钮）底部
 * @param leading 导航条左侧按钮配置列表
 * @param actions 导航条右侧按钮配置列表
 */
class EuNavBarController(
    var builder: ViewBuilder? = null,
    var text: String? = null,
    var titleView: (TextView.() -> Unit)? = null,
    var backAction: (() -> Unit)? = null,
    topSafeArea: Boolean = true,
    hideNavBar: Boolean = false,
    showBack: Boolean = true,
    var leading: List<IEuNavAction> = emptyList(),
    var actions: List<IEuNavAction> = emptyList()
) {
    var topSafeArea: Boolean by observable(topSafeArea)
    var hideNavBar: Boolean by observable(hideNavBar)
    var showBack: Boolean by observable(showBack)

    internal fun clickBackAction(ctx: IModuleAccessor) {
        if (this.backAction != null) {
            this.backAction?.invoke()
        } else {
            ctx.getModule<RouterModule>(RouterModule.MODULE_NAME)?.closePage()
        }
    }
}

internal class EuNavBarView : EuComposeView<EuNavBarViewAttr, EuNavBarViewEvent>() {
    var attributes: EuNavBarViewAttr? = null

    lateinit var pgData: PageData

    override fun created() {
        super.created()
        this.pgData = pagerData
    }

    override fun createEvent(): EuNavBarViewEvent {
        return EuNavBarViewEvent()
    }

    override fun createAttr(): EuNavBarViewAttr {
        return attributes ?: EuNavBarViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                if (!ctx.attr.setNavBarBackgroundColor && ctx.attributes != null) {
                    backgroundColor(ctx.euColor.w1)
                }
            }
            View {
                attr {
                    height(if (ctx.attr.controller.topSafeArea) ctx.pgData.statusBarHeight else 0f)
                }
            }
            vif({ !ctx.attr.controller.hideNavBar }) {
                View {
                    attr {
                        height(ctx.pgData.navigationBarHeight - ctx.pgData.statusBarHeight)
                        flex(1f)
                        allCenter()
                        backgroundColor(Color.TRANSPARENT)
                    }
                    EuNavStack {
                        attr {
                            backgroundColor(Color.TRANSPARENT)
                            height(ctx.pgData.navigationBarHeight - ctx.pgData.statusBarHeight)
                            controller = ctx.attr.controller
                            absolutePositionAllZero()
                        }
                    }

                    vif({ ctx.attr.controller.showBack && ctx.attr.controller.leading.isEmpty() }) {
                        Image {
                            attr {
                                size(20f, 20f)
                                src(ImageUri.commonAssets("ic_back.png"))
                                resizeContain()
                                absolutePosition(
                                    left = 16f,
                                    bottom = ((ctx.pgData.navigationBarHeight - ctx.pgData.statusBarHeight) - 20f) / 2f
                                )
                                tintColor(ctx.euColor.b1)
                            }
                            event {
                                click {
                                    ctx.attr.controller.clickBackAction(ctx)
                                }
                            }
                        }
                    }

                    vif({ ctx.attr.controller.titleView }) {
                        Text(ctx.attr.controller.titleView!!)
                    }
                    vif({ ctx.attr.controller.text != null && ctx.attr.controller.titleView == null }) {
                        Text {
                            attr {
                                text(ctx.attr.controller.text!!)
                                fontSize(18f)
                                fontWeight500()
                                fontFamily(Constant.TXT_FONT_FAMILY)
                                color(ctx.euColor.b1)
                            }
                        }
                    }

                    // 导航条 Leading 自定义
                    View {
                        attr {
                            backgroundColor(Color.TRANSPARENT)
                            absolutePosition(left = 0f, top = 0f, bottom = 0f)
                            flexDirection(FlexDirection.ROW)
                            alignItemsCenter()
                        }
                        for (item in ctx.attr.controller.leading) {
                            vif({ !item.hide }) {
                                View {
                                    vif({ item.icon != null }) {
                                        Image {
                                            attr {
                                                src(item.icon!!)
                                                size(width = 24f, height = 24f)
                                                margin(left = item.gap)
                                            }
                                        }
                                    }
                                    velseif({ item.textView != null }) {
                                        View {
                                            attr {
                                                margin(left = item.gap)
                                                padding(top = 10f, bottom = 10f)
                                                flexDirection(FlexDirection.ROW_REVERSE)
                                            }
                                            Text(item.textView!!)
                                        }
                                    }
                                    event {
                                        click {
                                            item.callback?.invoke()
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 导航条 Actions 自定义
                    View {
                        attr {
                            backgroundColor(Color.TRANSPARENT)
                            absolutePosition(right = 0f, top = 0f, bottom = 0f)
                            flexDirection(FlexDirection.ROW_REVERSE)
                            alignItemsCenter()
                        }
                        for (item in ctx.attr.controller.actions.reversed()) {
                            vif({ !item.hide }) {
                                View {
                                    vif({ item.icon != null }) {
                                        Image {
                                            attr {
                                                src(item.icon!!)
                                                size(width = 24f, height = 24f)
                                                margin(right = item.gap)
                                            }
                                        }
                                    }
                                    velseif({ item.textView != null }) {
                                        View {
                                            attr {
                                                margin(right = item.gap)
                                                padding(top = 10f, bottom = 10f)
                                                flexDirection(FlexDirection.ROW_REVERSE)
                                            }
                                            Text(item.textView!!)
                                        }
                                    }
                                    event {
                                        click {
                                            item.callback?.invoke()
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

internal class EuNavBarViewAttr : ComposeAttr() {
    var controller: EuNavBarController = EuNavBarController()

    internal var setNavBarBackgroundColor: Boolean = false
}

internal class EuNavBarViewEvent : ComposeEvent() {}

internal fun ViewContainer<*, *>.EuNavBar(
    init: EuNavBarView.() -> Unit,
    attr: EuNavBarViewAttr? = null
) {
    val a = EuNavBarView()
    a.attributes = attr
    addChild(a, init)
}

internal class EuNavStackView : EuComposeView<EuNavStackAttr, ComposeEvent>() {
    override fun createAttr(): EuNavStackAttr {
        return EuNavStackAttr()
    }

    override fun createEvent(): ComposeEvent {
        return ComposeEvent()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return ctx.attr.controller.builder ?: {}
    }
}

internal class EuNavStackAttr : ComposeAttr() {
    lateinit var controller: EuNavBarController
}

internal fun ViewContainer<*, *>.EuNavStack(init: EuNavStackView.() -> Unit) {
    addChild(EuNavStackView(), init)
}