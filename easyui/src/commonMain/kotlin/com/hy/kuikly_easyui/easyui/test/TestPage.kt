package com.hy.kuikly_easyui.easyui.test

import com.hy.kuikly_easyui.easyui.EuBasePager
import com.hy.kuikly_easyui.easyui.EuComposeView
import com.hy.kuikly_easyui.easyui.EuContentBuilder
import com.hy.kuikly_easyui.easyui.EuContentStatus
import com.hy.kuikly_easyui.easyui.EuContentStatusController
import com.hy.kuikly_easyui.easyui.EuFooterEndState
import com.hy.kuikly_easyui.easyui.EuNavBarController
import com.hy.kuikly_easyui.easyui.EuPlaceConfig
import com.hy.kuikly_easyui.easyui.EuPlaceholderController
import com.hy.kuikly_easyui.easyui.EuRefreshController
import com.hy.kuikly_easyui.easyui.EuRefreshPullDown
import com.hy.kuikly_easyui.easyui.EuRefreshPullUp
import com.hy.kuikly_easyui.easyui.EuRefresher
import com.hy.kuikly_easyui.easyui.EuScaffold
import com.hy.kuikly_easyui.easyui.EuScaffoldViewAttr
import com.hy.kuikly_easyui.easyui.IEuNavAction
import com.hy.kuikly_easyui.easyui.theme.ThemeManager
import com.hy.kuikly_easyui.easyui.theme.ThemeScheme
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.base.event.didAppear
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vforIndex
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.compose.Button

class EasyUITest {
    companion object Companion {
        const val ROUTE_NAME = "router"
    }
}

@Page(EasyUITest.ROUTE_NAME)
internal class TestPage : EuBasePager() {
    private val data by observableList<Number>()
    private var control: EuRefreshController? = null

    private val status =
        EuContentStatusController(defaultStatus = EuContentStatus.INITIALIZATION)
    private val listStatus: EuContentStatusController =
        EuContentStatusController(EuContentStatus.INITIALIZATION)
    private var operRef: ViewRef<OperationTileView>? = null

    override fun pageDidAppear() {
        super.pageDidAppear()
        setTimeout(1000) {
            this.status.updateStatus(EuContentStatus.BUSINESS)
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            EuScaffold {
                attr {
                    getScaffold = {
                        ctx.scaffold = it
                        ctx.operRef?.view?.attr { scaffold = it }
                    }
                    status = ctx.status
                    navBar = EuNavBarController(
                        text = "EasyUI",
                        actions = listOf(
                            IEuNavAction(
                                textView = {
                                    attr {
                                        text("还原到业务态")
                                        color(ctx.euColor.b2)
                                        fontSize(16f)
                                    }
                                },
                                callback = {
                                    ctx.scaffold?.status?.updateStatus(
                                        EuContentStatus.BUSINESS
                                    )
                                }
                            )
                        )
                    )
                    builder = EuContentBuilder(business = {
                        OperationTile {
                            ref {
                                ctx.operRef = it
                            }
                            attr {
                                listStatus = ctx.listStatus
                                manualRefresh = {
                                    ctx.control?.manualRefresh()
                                }
                                enableRefresh = {
                                    ctx.control?.enablePullDown =
                                        !(ctx.control?.enablePullDown ?: true)
                                }
                                enableLoadMore = {
                                    ctx.control?.enablePullUp =
                                        !(ctx.control?.enablePullUp ?: true)
                                }
                            }
                        }
                        EuRefresher {
                            attr {
                                flex(1f)
                                statusController = ctx.listStatus
                                placeholder = ctx.customPlaceConfig()
                                listBuilder = {
                                    vforIndex({ ctx.data }) { item, _, _ ->
                                        ctx.tileBuilder(item).invoke(this)
                                    }
                                }
                                listTopGap = 8f
                                defaultInitialText = "加载列表"
                                pullDown = EuRefreshPullDown(
                                    pullingText = "松手执行刷新",
                                    refresh = { control ->
                                        ctx.control = control
                                        setTimeout(4000) {
                                            ctx.refreshData()
                                            control.endRefresh()
                                                .updateEndState(EuFooterEndState.SHOW)
                                        }
                                    }
                                )
                                pullUp = EuRefreshPullUp(
                                    loading = { control ->
                                        ctx.control = control
                                        setTimeout(600) {
                                            ctx.requestData()
                                            if (ctx.data.size >= 30) {
                                                control.updateEndState(
                                                    EuFooterEndState.NO_MORE_DATA
                                                )
                                            } else {
                                                control.updateEndState(
                                                    EuFooterEndState.SUCCESS
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                            event {
                                didAppear {
                                    setTimeout(200) {
                                        ctx.listStatus.updateStatus(
                                            EuContentStatus.BUSINESS
                                        )
                                    }
                                    ctx.refreshData()
                                }
                            }
                        }
                    })
                    placeholder = ctx.customPlaceConfig()
                }
                event {
                    contentDidLoad = { _ ->
                        ctx.operRef?.view?.attr { scaffold = ctx.scaffold }
                    }
                }
            }
        }
    }

    private fun refreshData() {
        this.data.clear()
        this.data.addAll(1..5)
    }

    private fun requestData() {
        this.data.addAll(this.data.size..5 + this.data.size)
    }

    private fun customPlaceConfig(): EuPlaceConfig {
        val ctx = this
        return EuPlaceConfig(
            netError = EuPlaceholderController(
                title = "标题",
                desc = "副标题",
                buttonText = "去添加",
                operationAction = {
                    setTimeout(200) {
                        ctx.scaffold?.status?.updateStatus(EuContentStatus.BUSINESS)
                    }
                }
            )
        )
    }

    private fun tileBuilder(index: Number): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    backgroundColor(ctx.euColor.w1)
                    borderRadius(10f)
                    padding(left = 12f, right = 12f, top = 20f, bottom = 16f)
                    margin(left = 8f, right = 8f, bottom = 8f)
                }
                View {
                    attr {
                        flexDirectionRow()
                        justifyContentSpaceBetween()
                    }
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                        }
                        Text {
                            attr {
                                text("XXXXXXXXXXXX")
                                margin(right = 4f)
                                color(ctx.euColor.b1)
                                fontWeight600()
                                fontSize(18f)
                            }
                        }
                        Button {
                            attr {
                                padding(3f)
                                borderRadius(4f)
                                border(Border(1f, BorderStyle.SOLID, ctx.euColor.p1_5))
                                backgroundColor(ctx.euColor.p1_30)
                                titleAttr {
                                    color(ctx.euColor.p1)
                                    fontWeight500()
                                    text("X")
                                    fontSize(10f)
                                }
                            }
                        }
                    }

                    Text {
                        attr {
                            text("XX-XXXX")
                            color(ctx.euColor.p1)
                            fontWeight600()
                            fontSize(16f)
                        }
                    }
                }
                Text {
                    attr {
                        margin(top = 16f)
                        text("XXXX XX XXXXXX $index")
                        color(ctx.euColor.b2)
                        fontWeight400()
                        fontSize(14f)
                    }
                }
                View {
                    attr {
                        margin(top = 8f)
                        flexDirectionRow()
                        flexWrapWrap()
                        height(24f)
                    }
                    for (i in listOf(
                        "XXXXX",
                        "XXXXXXX",
                        "XXXXX",
                        "XXX",
                        "XXXX",
                        "XXXXXX",
                        "XXXXXX",
                    )) {
                        View {
                            attr {
                                borderRadius(4f)
                                backgroundColor(ctx.euColor.s2)
                                padding(left = 8f, right = 8f, top = 6f, bottom = 6f)
                                margin(right = 6f)
                            }
                            Text {
                                attr {
                                    text(i)
                                    color(ctx.euColor.b2)
                                    fontWeight400()
                                    fontSize(12f)
                                }
                            }
                        }
                    }
                }
                View {
                    attr {
                        backgroundColor(ctx.euColor.w1)
                        padding(top = 12f)
                        flexDirectionRow()
                        flex(1f)
                    }

                    View {
                        attr {
                            flex(1f)
                            flexDirectionRow()
                            alignItemsCenter()
                        }
                        Image {
                            attr {
                                size(22f, 22f)
                                borderRadius(12f)
                                backgroundColor(ctx.euColor.p1)
                            }
                        }
                        View {
                            attr {
                                margin(left = 8f)
                                flexDirectionColumn()
                            }
                            Text {
                                attr {
                                    text("XXXXXXX · XXXX")
                                    color(ctx.euColor.b2)
                                    fontWeight400()
                                    fontSize(12f)
                                }
                            }
                            Text {
                                attr {
                                    margin(top = 6f)
                                    text("XXXXXXXXXXXX XXX")
                                    color(ctx.euColor.b3)
                                    fontWeight400()
                                    fontSize(12f)
                                }
                            }
                        }

                        Text {
                            attr {
                                absolutePosition()
                                top(18f)
                                right(0f)
                                text("XX XXXX  X")
                                color(ctx.euColor.b3)
                                fontWeight400()
                                fontSize(12f)
                            }
                        }
                    }
                }

                View {
                    attr {
                        margin(top = 11f)
                        flexDirectionRow()
                        alignItemsCenter()
                    }
                    View {
                        attr {
                            backgroundColor(ctx.euColor.b3)
                            padding(4f)
                            borderRadius(4f)
                        }
                        Text {
                            attr {
                                borderRadius(4f)
                                text("XXXXXX")
                                color(ctx.euColor.p1)
                                fontWeight500()
                                fontSize(12f)
                            }
                        }
                    }
                    Text {
                        attr {
                            margin(left = 4f)
                            text("XXXXXXXXXXXXX")
                            color(ctx.euColor.b2)
                            fontWeight400()
                            fontSize(13f)
                        }
                    }
                }

                event {
                    click {
                        ctx.jumpRouter(EasyUITest.ROUTE_NAME)
                    }
                }
            }
            event {
                //注意:在这里写点击事件，鸿蒙平台不能识别!!!
            }
        }
    }
}

internal class OperationTileView :
    EuComposeView<OperationTileAttr, ComposeEvent>() {

    private var expand by observable(false)

    override fun createEvent(): ComposeEvent {
        return ComposeEvent()
    }

    override fun createAttr(): OperationTileAttr {
        return OperationTileAttr()
    }

    private fun expandBuilder(): ViewBuilder {
        val ctx = this
        return {
            for (i in listOf(
                EuContentStatus.INITIALIZATION,
                EuContentStatus.BUSINESS,
                EuContentStatus.NET_ERROR,
                EuContentStatus.BIZ_ERROR,
                EuContentStatus.DATA_EMPTY
            )) {
                Button {
                    attr {
                        margin(top = 3f, left = 5f)
                        padding(5f)
                        backgroundColor(ctx.euColor.s1)
                        titleAttr {
                            text("切页面状态->${i.desc}")
                            color(ctx.euColor.w1)
                        }
                    }
                    event {
                        click {
                            ctx.attr.scaffold?.status?.updateStatus(i)
                        }
                    }
                }
            }
            for (i in listOf(
                EuContentStatus.INITIALIZATION,
                EuContentStatus.BUSINESS,
                EuContentStatus.NET_ERROR,
                EuContentStatus.BIZ_ERROR,
                EuContentStatus.DATA_EMPTY
            )) {
                Button {
                    attr {
                        margin(top = 3f, left = 5f)
                        padding(5f)
                        backgroundColor(ctx.euColor.s1)
                        titleAttr {
                            text("切列表状态->${i.desc}")
                            color(ctx.euColor.w1)
                        }
                    }
                    event {
                        click {
                            ctx.attr.listStatus?.updateStatus(i)
                        }
                    }
                }
            }
            for (i in listOf(
                "「♻️收起测试面板」",
                "隐藏或展示导航条",
                "切换暗黑模式",
                "切换光亮模式",
                "手动刷新列表",
                "启用或关闭下拉刷新",
                "启用或关闭上拉加载",
            )) {
                Button {
                    event {
                        click {
                            when (i) {
                                "「♻️收起测试面板」" -> {
                                    ctx.expand = false
                                }

                                "隐藏或展示导航条" -> {
                                    ctx.attr.scaffold?.navBar?.hideNavBar =
                                        !(ctx.attr.scaffold?.navBar?.hideNavBar ?: false)
                                }

                                "切换暗黑模式" -> {
                                    ThemeManager.changeColorScheme(ThemeScheme.DARK)
                                }

                                "切换光亮模式" -> {
                                    ThemeManager.changeColorScheme(ThemeScheme.LIGHT)
                                }

                                "手动刷新列表" -> {
                                    ctx.attr.manualRefresh?.invoke()
                                }

                                "启用或关闭下拉刷新" -> {
                                    ctx.attr.enableRefresh?.invoke()
                                }

                                "启用或关闭上拉加载" -> {
                                    ctx.attr.enableLoadMore?.invoke()
                                }
                            }
                        }
                    }
                    attr {
                        margin(top = 3f, left = 5f)
                        padding(5f)
                        backgroundColor(ctx.euColor.s1)
                        titleAttr {
                            text(i)
                            color(ctx.euColor.w1)
                        }
                    }
                }
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(ctx.euColor.y2)
                padding(10f)
                flexWrapWrap()
                flexDirectionRow()
            }
            vif({ ctx.expand }) {
                ctx.expandBuilder().invoke(this)
            }
            velse {
                Button {
                    attr {
                        padding(5f)
                        backgroundColor(ctx.euColor.s1)
                        titleAttr {
                            text("「♻️展开测试面板」")
                            color(ctx.euColor.w1)
                        }
                    }
                    event {
                        click {
                            ctx.expand = true
                        }
                    }
                }
            }
        }
    }
}

internal class OperationTileAttr : ComposeAttr() {
    var listStatus: EuContentStatusController? = null
    var scaffold: EuScaffoldViewAttr? = null
    var manualRefresh: (() -> Unit)? = null
    var enableRefresh: (() -> Unit)? = null
    var enableLoadMore: (() -> Unit)? = null
}

internal fun ViewContainer<*, *>.OperationTile(init: OperationTileView.() -> Unit) {
    addChild(OperationTileView(), init)
}