package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.theme.Constant
import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.base.event.appearPercentage
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.FooterRefresh
import com.tencent.kuikly.core.views.FooterRefreshEndState
import com.tencent.kuikly.core.views.FooterRefreshState
import com.tencent.kuikly.core.views.FooterRefreshView
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.ImageView
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.ListView
import com.tencent.kuikly.core.views.Refresh
import com.tencent.kuikly.core.views.RefreshView
import com.tencent.kuikly.core.views.RefreshViewState
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

enum class EuFooterEndState {
    HIDE, SHOW, SUCCESS, FAILURE, NO_MORE_DATA,
}

class EuRefreshController {
    /**
     * 是否展示下拉刷新
     */
    var enablePullDown: Boolean by observable(true)

    /**
     * 是否展示上拉加载更多
     * @note 默认不展示下拉刷新，只有当数据列表展示后选择是否要展示上拉加载更多
     */
    var enablePullUp: Boolean by observable(false)
    private var listRefresher: ViewRef<RefreshView>? = null
    private var listFooter: ViewRef<FooterRefreshView>? = null
    private var listRef: ViewRef<ListView<*, *>>? = null
    fun bindRefresher(refresher: ViewRef<RefreshView>) {
        this.listRefresher = refresher
    }

    fun bindFooter(footer: ViewRef<FooterRefreshView>) {
        this.listFooter = footer
    }

    fun bindList(list: ViewRef<ListView<*, *>>) {
        this.listRef = list
    }

    /**
     * 手动刷新
     */
    fun manualRefresh() {
        this.listRefresher?.view?.beginRefresh()
    }

    private var initialRefreshFlag = false
    fun initialRefresh() {
        if (initialRefreshFlag) return
        initialRefreshFlag = true
        manualRefresh()
    }

    /**
     * 结束下拉刷新
     */
    fun endRefresh(): EuRefreshController {
        listRefresher?.view?.endRefresh()
        listFooter?.view?.resetRefreshState()
        return this
    }

    /**
     * 更新「上拉加载」的状态
     */
    fun updateEndState(state: EuFooterEndState): EuRefreshController {
        when (state) {
            EuFooterEndState.HIDE -> {
                enablePullUp = false
            }

            EuFooterEndState.SHOW, EuFooterEndState.FAILURE, EuFooterEndState.SUCCESS -> {
                enablePullUp = true
                listFooter?.view?.endRefresh(FooterRefreshEndState.SUCCESS)
            }

            EuFooterEndState.NO_MORE_DATA -> {
                enablePullUp = true
                listFooter?.view?.endRefresh(FooterRefreshEndState.NONE_MORE_DATA)
            }
        }
        return this
    }
}

class EuRefreshView : EuComposeView<EuRefreshViewAttr, EuRefreshViewEvent>() {
    private var refreshIdx: Int = 0
    private var loadingIdx: Int = 0
    private var controller = EuRefreshController()
    private var refreshing by observable(false)
    private var loading by observable(false)
    private var pullDownRotate by observable(0f)
    private var pullDownRotateStatic: Float = 0f
    private var noMoreData: Boolean by observable(false)
    private var getControllerIfNeed = false

    override fun viewDidLoad() {
        super.viewDidLoad()
        if (this.attr.pullDown == null) {
            this.controller.enablePullDown = false
        }
        if (this.attr.pullUp == null && this.controller.enablePullUp) {
            this.controller.enablePullUp = false
        }
    }

    override fun createEvent(): EuRefreshViewEvent {
        return EuRefreshViewEvent()
    }

    override fun createAttr(): EuRefreshViewAttr {
        return EuRefreshViewAttr()
    }

    private fun listBuilder(): ViewBuilder {
        return this.attr.listBuilder
    }

    private fun footerBuilder(): ViewBuilder {
        val ctx = this
        return {
            vif({ ctx.controller.enablePullUp }) {
                FooterRefresh {
                    ref {
                        ctx.controller.bindFooter(it)
                    }
                    attr {
                        allCenter()
                        flexDirectionRow()
                        height(ctx.attr.pullUp?.height ?: 60f)
                    }
                    event {
                        refreshStateDidChange {
                            when (it) {
                                FooterRefreshState.NONE_MORE_DATA -> {
                                    ctx.noMoreData = true
                                    ctx.loading = false
                                }

                                FooterRefreshState.IDLE -> {
                                    ctx.noMoreData = false
                                    ctx.loading = false
                                }

                                FooterRefreshState.REFRESHING -> {
                                    if (!ctx.loading) {
                                        KLog.d(
                                            "Kuikly",
                                            "[EuRefresh] ${ctx.hashCode()}loading第${ctx.loadingIdx++}次"
                                        )
                                        ctx.noMoreData = false
                                        ctx.loading = true
                                        ctx.attr.pullUp?.loading?.invoke(ctx.controller)
                                    }
                                }

                                FooterRefreshState.FAILURE -> {
                                    ctx.noMoreData = false
                                    ctx.loading = false
                                }
                            }
                        }
                    }
                    vif({ ctx.noMoreData }) {
                        Text {
                            attr {
                                text(ctx.attr.pullUp?.noMoreText ?: "没有更多数据了...")
                                fontSize(12f)
                                color(ctx.euColor.b3)
                                fontFamily(Constant.TXT_FONT_FAMILY)
                            }
                        }
                    }
                    velse {
                        Image {
                            attr {
                                src(ImageUri.commonAssets("ic_common_pull_loading.png"))
                                size(width = 18f, height = 18f)
                                transform(
                                    rotate = Rotate(if (ctx.loading) 360f else 0f)
                                )
                                animate(
                                    animation = Animation.linear(0.7f)
                                        .repeatForever(ctx.loading),
                                    value = ctx.loading,
                                )
                            }
                        }
                        Text {
                            attr {
                                margin(left = 3f)
                                text("正在加载...")
                                fontSize(12f)
                                color(ctx.euColor.b3)
                                fontFamily(Constant.TXT_FONT_FAMILY)
                            }
                        }
                    }
                }
            }
        }
    }

    private var refreshImageRef: ViewRef<ImageView>? = null
    fun rotate() {
        val ctx = this
        this.refreshImageRef?.view?.animateToAttr(Animation.linear(0.7f), attrBlock = {
            ctx.pullDownRotateStatic += 360f
            transform(rotate = Rotate(ctx.pullDownRotateStatic))
        }, completion = {
            if (ctx.refreshing) {
                ctx.rotate()
            }
        })
    }

    private fun businessBuilder(): ViewBuilder {
        val ctx = this
        return {
            List {
                ref {
                    ctx.controller.bindList(it)
                }
                attr {
                    flex(1f)
                }
                event {
                    scroll { params ->
                        if (params.offsetY <= 0) {
                            ctx.pullDownRotate = (params.offsetY * -1.6f)
                        }
                    }
                    appearPercentage { per ->
                        if (ctx.attr.pullDown?.initialRefresh == true && per >= 0.9f) {
                            ctx.controller.initialRefresh()
                        }
                    }
                }
                Refresh {
                    ref {
                        ctx.controller.bindRefresher(it)
                        if (ctx.attr.getController != null && !ctx.getControllerIfNeed) {
                            ctx.getControllerIfNeed = true
                            ctx.attr.getController?.invoke(ctx.controller)
                        }
                    }
                    attr {
                        refreshEnable = ctx.controller.enablePullDown
                        flexDirectionColumn()
                        justifyContentCenter()
                        alignItemsCenter()
                        height(ctx.attr.pullDown?.height ?: 56f)
                    }
                    event {
                        refreshStateDidChange { it ->
                            if (!ctx.controller.enablePullDown) {
                                return@refreshStateDidChange
                            }
                            when (it) {
                                RefreshViewState.PULLING -> {
                                    KLog.d(
                                        "Kuikly",
                                        "[EuRefresh] ${ctx.hashCode()} refresh PULLING 第${ctx.refreshIdx} 转动角度${ctx.pullDownRotate}次"
                                    )
                                    ctx.refreshing = false
                                }

                                // 刷新结束理解变化状态为 idle
                                RefreshViewState.IDLE -> {
                                    KLog.d(
                                        "Kuikly",
                                        "[EuRefresh] ${ctx.hashCode()} refresh IDLE 第${ctx.refreshIdx}次 转动角度${ctx.pullDownRotate}"
                                    )
                                    ctx.pullDownRotate = 0f
                                    ctx.refreshing = false
                                }

                                RefreshViewState.REFRESHING -> {
                                    if (!ctx.refreshing) {
                                        ctx.pullDownRotateStatic = ctx.pullDownRotate
                                        ctx.refreshing = true
                                        ctx.rotate()
                                        ctx.attr.pullDown?.refresh?.invoke(ctx.controller)
                                        KLog.d(
                                            "Kuikly",
                                            "[EuRefresh] ${ctx.hashCode()} refresh第${ctx.refreshIdx++}次 转动角度${ctx.pullDownRotateStatic}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                    vif({ ctx.controller.enablePullDown }) {
                        Image {
                            ref {
                                ctx.refreshImageRef = it
                            }
                            attr {
                                src(ImageUri.commonAssets("ic_common_refresh_loading.png"))
                                size(width = 18f, height = 18f)
                                if (!ctx.refreshing) {
                                    transform(rotate = Rotate(ctx.pullDownRotate))
                                }
                            }
                        }

                        if (ctx.attr.pullDown?.pullingText != null) {
                            Text {
                                attr {
                                    text("${ctx.attr.pullDown?.pullingText}")
                                    color(ctx.euColor.b3)
                                    fontSize(12f)
                                    height(16f)
                                    margin(top = 7f)
                                    fontFamily(Constant.TXT_FONT_FAMILY)
                                }
                            }
                        }
                    }
                }
                if (ctx.attr.listTopGap != null) {
                    View {
                        attr {
                            height(ctx.attr.listTopGap!!)
                        }
                    }
                }
                ctx.listBuilder().invoke(this)
                if (ctx.attr.listBottomGap != null) {
                    View {
                        attr {
                            height(ctx.attr.listBottomGap!!)
                        }
                    }
                }
                ctx.footerBuilder().invoke(this)
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            EuContentSwitcher(
                {
                    attr {
                        flex(1f)
                        controller = ctx.attr.statusController
                        builder = EuContentBuilder(
                            initialization = ctx.attr.initialization,
                            netError = ctx.attr.netError,
                            bizError = ctx.attr.bizError,
                            dataEmpty = ctx.attr.dataEmpty,
                            business = {
                                ctx.businessBuilder().invoke(this)
                            })
                        placeholder = ctx.attr.placeholder
                        defaultInitialText = ctx.attr.defaultInitialText
                    }
                })
        }
    }
}

/**
 * 上拉加载配置
 * @param height 上拉加载的高度
 * @param pullingText 上拉时的提示文字
 * @param refreshText 加载时的提示文字
 * @param noMoreText 没有更多数据时的提示文字
 * @param loading 上拉加载时的回调
 */
class EuRefreshPullUp(
    var height: Float? = null,
    var pullingText: String? = null,
    var refreshText: String? = null,
    var noMoreText: String? = null,
    var loading: ((controller: EuRefreshController) -> Unit)? = null,
)

/**
 * 下拉刷新配置
 * @param height 下拉刷新的高度
 * @param pullingText 下拉刷新时的提示文字
 * @param initialRefresh 是否初始化时自动刷新
 * @param refresh 下拉刷新时的回调
 */
class EuRefreshPullDown(
    var height: Float? = null,
    var pullingText: String? = null,
    val initialRefresh: Boolean? = null,
    var refresh: ((controller: EuRefreshController) -> Unit)? = null,
)

/**
 * 下拉刷新、上拉加载组件属性
 * @param pullDown 下拉刷新配置
 * @param pullUp 上拉加载配置
 * @param listTopGap 列表顶部间距
 * @param listBottomGap 列表底部间距
 */
class EuRefreshViewAttr : ComposeAttr() {
    var pullDown: EuRefreshPullDown? = null
    var pullUp: EuRefreshPullUp? = null
    lateinit var listBuilder: ViewBuilder
    var listTopGap: Float? = null
    var listBottomGap: Float? = null

    /**
     * 获取控制器，只触发一次有效
     */
    var getController: ((controller: EuRefreshController) -> Unit)? = null
    var statusController: EuContentStatusController = EuContentStatusController()

    /**
     * 状态内容
     * - 初始化状态内容
     * - 网络错误状态内容
     * - 业务错误状态内容
     * - 数据为空状态内容
     */
    var initialization: ViewBuilder? = null
    var netError: ViewBuilder? = null
    var bizError: ViewBuilder? = null
    var dataEmpty: ViewBuilder? = null

    /**
     * 默认加载状态的 loading 的文案，默认没有
     */
    var defaultInitialText: String? = null

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
}

class EuRefreshViewEvent : ComposeEvent() {

}

fun ViewContainer<*, *>.EuRefresher(init: EuRefreshView.() -> Unit) {
    addChild(EuRefreshView(), init)
}