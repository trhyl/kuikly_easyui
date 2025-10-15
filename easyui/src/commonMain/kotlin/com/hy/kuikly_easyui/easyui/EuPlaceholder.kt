package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.theme.Constant
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View
import com.tencent.kuikly.core.views.compose.Button

/**
 * 缺生图占位类型
 */
enum class EuPlaceholderType(val code: Int, val desc: String) {
    NO_DATA(0, "无内容"),
    NO_NETWORK(1, "无网络"),
}

/**
 * 缺省图配置
 * @param netError 无网络/加载失败
 * @param bizError 业务错误
 * @param dataEmpty 无内容/无结果
 */
class EuPlaceConfig(
    var netError: EuPlaceholderController? = null,
    var bizError: EuPlaceholderController? = null,
    var dataEmpty: EuPlaceholderController? = null,
)

/**
 * 缺省页控制器
 * @param type 缺省图类型
 * @param title 标题，为空不展示
 * @param desc 描述，为空不展示
 * @param buttonText 操作按钮文案，为空时不展示「操作按钮」
 * @param imageUri 自定义图片资源，为空使用该类型的默认图
 * @param operationAction 操作按钮点击事件，为空时不展示「操作按钮」
 */
class EuPlaceholderController(
    type: EuPlaceholderType = EuPlaceholderType.NO_DATA,
    title: String? = null,
    desc: String? = null,
    buttonText: String? = null,
    var imageUri: ImageUri? = null,
    var operationAction: (() -> Unit)? = null
) {
    var type: EuPlaceholderType by observable(type)
    var title: String? by observable(title)
    var desc: String? by observable(desc)
    var buttonText: String? by observable(buttonText)

    val imageUrlRes: ImageUri
        get() {
            if (imageUri != null) {
                return imageUri!!
            }
            return when (type) {
                EuPlaceholderType.NO_DATA -> ImageUri.commonAssets("ic_common_place_no_data.png")
                EuPlaceholderType.NO_NETWORK -> ImageUri.commonAssets("ic_common_place_no_network.png")
                else -> ImageUri.commonAssets("ic_common_place_no_data.png")
            }
        }
}

internal class EuPlaceholderView : EuComposeView<EuPlaceholderViewAttr, EuPlaceholderViewEvent>() {

    override fun createEvent(): EuPlaceholderViewEvent {
        return EuPlaceholderViewEvent()
    }

    override fun createAttr(): EuPlaceholderViewAttr {
        return EuPlaceholderViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flexDirectionColumn()
                flex(1f)
                alignItemsCenter()
                justifyContentCenter()
            }
            Image {
                attr {
                    src(ctx.attr.controller.imageUrlRes)
                    width(160f)
                    height(160f)
                }
            }
            vif({ ctx.attr.controller.title?.isNotEmpty() }) {
                View {
                    attr {
                        padding(left = 56f, right = 56f)
                    }
                    Text {
                        attr {
                            ctx.attr.controller.title?.let { text(it) }
                            margin(top = 16f)
                            fontSize(16f)
                            fontWeight400()
                            color(ctx.euColor.b1)
                            textAlignCenter()
                            fontWeight400()
                            fontFamily(Constant.TXT_FONT_FAMILY)
                        }
                    }
                }
            }
            vif({ ctx.attr.controller.desc?.isNotEmpty() }) {
                View {
                    attr {
                        padding(left = 56f, right = 56f)
                    }
                    Text {
                        attr {
                            ctx.attr.controller.desc?.let { text(it) }
                            margin(top = 8f)
                            fontSize(14f)
                            fontWeight400()
                            color(ctx.euColor.b3)
                            textAlignCenter()
                            fontWeight400()
                            fontFamily(Constant.TXT_FONT_FAMILY)
                        }
                    }
                }
            }
            vif({ ((ctx.attr.controller.buttonText?.isNotEmpty() == true) && (ctx.attr.controller.operationAction != null)) }) {
                Button {
                    attr {
                        margin(top = 20f)
                        padding(left = 16f, right = 16f)
                        height(36f)
                        backgroundColor(ctx.euColor.p1)
                        borderRadius(18f)
                        titleAttr {
                            lines(1)
                            color(ctx.euColor.w1)
                            text(ctx.attr.controller.buttonText!!)
                            fontSize(14f)
                            fontWeight500()
                            fontFamily(Constant.TXT_FONT_FAMILY)
                        }
                    }
                    event {
                        click { ctx.attr.controller.operationAction?.invoke() }
                    }
                }
            }
        }
    }
}


internal class EuPlaceholderViewAttr : ComposeAttr() {
    var controller: EuPlaceholderController = EuPlaceholderController()
}

internal class EuPlaceholderViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.EuPlaceholder(init: EuPlaceholderView.() -> Unit) {
    addChild(EuPlaceholderView(), init)
}