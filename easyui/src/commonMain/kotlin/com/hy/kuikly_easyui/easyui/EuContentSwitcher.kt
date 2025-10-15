package com.hy.kuikly_easyui.easyui

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.event.didAppear
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.View

enum class EuContentStatus(val code: Int, val desc: String) {
    Unknown(-1, "容器待加载，请勿使用该状态"), INITIALIZATION(
        0,
        "初始化"
    ),
    BUSINESS(1, "业务正常"), NET_ERROR(1001, "网络链接失败"), BIZ_ERROR(
        1002,
        "业务逻辑异常"
    ),
    DATA_EMPTY(1003, "数据为空"),
}

/**
 * 内容状态控制器
 * @param defaultStatus 默认「业务正常-BUSINESS」状态，这个需要注意，如果初始状态为加载态，可以默认改为「初始化-INITIALIZATION」
 */
class EuContentStatusController(
    defaultStatus: EuContentStatus = EuContentStatus.BUSINESS,
) {
    private var status: EuContentStatus by observable(defaultStatus)
    var latestStatus: EuContentStatus by observable(EuContentStatus.Unknown)
    val mStatus: EuContentStatus
        get() = this.status

    fun updateStatus(status: EuContentStatus) {
        if (status.code == this.status.code) return
        this.latestStatus = this.status
        this.status = status
        KLog.i(
            "EasyUI",
            "容器状态：${this.latestStatus.desc} => ${this.status.desc}"
        )
    }
}

/**
 * @param initialization 初始化界面，为空时，默认加载 loading
 * @param initialization 网络异常数据界面，为空时，展示默认「异常状态」缺省视图
 * @param bizError 业务异常界面，为空时，展示默认「业务异常」缺省视图
 * @param dataEmpty 数据为空界面，为空时，展示默认「数据为空」缺省视图
 * @param business 业务正常界面
 */
class EuContentBuilder(
    var initialization: ViewBuilder? = null,
    var netError: ViewBuilder? = null,
    var bizError: ViewBuilder? = null,
    var dataEmpty: ViewBuilder? = null,
    var business: ViewBuilder
)

internal class EuContentSwitcherView :
    EuComposeView<EuContentSwitcherViewAttr, EuContentSwitcherViewEvent>() {
    var attribute: EuContentSwitcherViewAttr? = null;
    override fun createEvent(): EuContentSwitcherViewEvent {
        return EuContentSwitcherViewEvent()
    }

    override fun createAttr(): EuContentSwitcherViewAttr {
        return attribute ?: EuContentSwitcherViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    flexDirectionColumn()
                    flex(1f)
                    if (!ctx.attr.setBackgroundColor) {
                        backgroundColor(Color.TRANSPARENT)
                    }
                }
                vif({ ctx.attr.controller.mStatus != ctx.attr.controller.latestStatus }) {
                    InEuContentSwitcher {
                        attr {
                            flex(1f)
                            alignItemsStretch()
                            controller = ctx.attr.controller
                            builder = ctx.attr.builder
                            placeholder = ctx.attr.placeholder
                            defaultInitialText = ctx.attr.defaultInitialText
                        }
                        event {
                            viewDidLoad = ctx.event.viewDidLoad
                            didAppear {
                                ctx.event.didAppear?.invoke()
                            }
                        }
                    }
                }
            }
        }
    }
}

class EuContentSwitcherViewAttr : ComposeAttr() {
    var placeholder: EuPlaceConfig? = null
    lateinit var controller: EuContentStatusController
    lateinit var builder: EuContentBuilder

    /**
     * 是否已经绑定背景色
     */
    var setBackgroundColor: Boolean = false

    /**
     * 默认加载状态的 loading 的文案，默认没有
     */
    var defaultInitialText: String? = null
}

internal class EuContentSwitcherViewEvent : ComposeEvent() {
    var viewDidLoad: ((controller: EuContentStatusController) -> Unit)? = null
    var didAppear: (() -> Unit)? = null
}

internal fun ViewContainer<*, *>.EuContentSwitcher(
    init: EuContentSwitcherView.() -> Unit,
    attar: EuContentSwitcherViewAttr? = null
) {
    val a = EuContentSwitcherView()
    a.attribute = attar
    addChild(a, init)
}

internal class InEuContentSwitcherView :
    EuComposeView<InEuContentSwitcherViewAttr, InEuContentSwitcherViewEvent>() {
    override fun createEvent(): InEuContentSwitcherViewEvent {
        return InEuContentSwitcherViewEvent()
    }

    override fun createAttr(): InEuContentSwitcherViewAttr {
        return InEuContentSwitcherViewAttr()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        this.event.viewDidLoad?.invoke(this.attr.controller)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        val builder = when (ctx.attr.controller.mStatus) {
            EuContentStatus.BUSINESS -> ctx.attr.builder.business
            EuContentStatus.INITIALIZATION -> ctx.attr.builder.initialization
                ?: {
                    attr {
                        flex(1f)
                        allCenter()
                    }
                    EuToast {
                        attr {
                            text = ctx.attr.defaultInitialText
                        }
                    }
                }

            EuContentStatus.NET_ERROR -> ctx.attr.builder.netError ?: {
                vif({ ctx.attr.placeholder?.netError }) {
                    EuPlaceholder {
                        attr {
                            controller = ctx.attr.placeholder?.netError!!
                        }
                    }
                }
                velse {
                    EuPlaceholder {
                        attr {
                            controller = EuPlaceholderController(
                                type = EuPlaceholderType.NO_NETWORK,
                                title = "网络开小差",
                            )
                        }
                    }
                }
            }

            EuContentStatus.BIZ_ERROR -> ctx.attr.builder.bizError ?: {
                vif({ ctx.attr.placeholder?.bizError }) {
                    EuPlaceholder {
                        attr {
                            controller = ctx.attr.placeholder?.bizError!!
                        }
                    }
                }
                velse {
                    EuPlaceholder {
                        attr {
                            controller = EuPlaceholderController(
                                type = EuPlaceholderType.NO_DATA,
                                title = "业务数据异常",
                            )
                        }
                    }
                }
            }

            EuContentStatus.DATA_EMPTY -> ctx.attr.builder.dataEmpty ?: {
                vif({ ctx.attr.placeholder?.dataEmpty }) {
                    EuPlaceholder {
                        attr {
                            controller = ctx.attr.placeholder?.dataEmpty!!
                        }
                    }
                }
                velse {
                    EuPlaceholder {
                        attr {
                            controller = EuPlaceholderController(
                                type = EuPlaceholderType.NO_DATA,
                                title = "内容为空",
                            )
                        }
                    }
                }
            }

            EuContentStatus.Unknown -> null
        }
        return builder ?: {}
    }
}

internal class InEuContentSwitcherViewAttr : ComposeAttr() {
    var placeholder: EuPlaceConfig? = null

    /**
     * 默认加载状态的 loading 的文案，默认没有
     */
    var defaultInitialText: String? = null
    lateinit var controller: EuContentStatusController
    lateinit var builder: EuContentBuilder
}

internal class InEuContentSwitcherViewEvent : ComposeEvent() {
    var viewDidLoad: ((controller: EuContentStatusController) -> Unit)? = null
}

internal fun ViewContainer<*, *>.InEuContentSwitcher(init: InEuContentSwitcherView.() -> Unit) {
    addChild(InEuContentSwitcherView(), init)
}