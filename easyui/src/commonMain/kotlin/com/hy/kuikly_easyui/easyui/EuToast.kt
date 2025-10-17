package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.theme.Constant
import com.tencent.kuikly.core.base.Animation
import com.tencent.kuikly.core.base.ComposeAttr
import com.tencent.kuikly.core.base.ComposeEvent
import com.tencent.kuikly.core.base.Rotate
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.base.event.didAppear
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text

internal class EuToastView : EuComposeView<EuToastViewAttr, EuToastViewEvent>() {
    private var loading by observable(false)

    override fun createEvent(): EuToastViewEvent {
        return EuToastViewEvent()
    }

    override fun createAttr(): EuToastViewAttr {
        return EuToastViewAttr()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(ctx.euColor.b1)
                borderRadius(10f)
                opacity(0.9f)
                minSize(68f, (if (ctx.attr.text.isNullOrEmpty()) 68f else 102f))
                flexDirectionColumn()
                alignItemsCenter()
            }

            Image {
                event {
                    didAppear {
                        ctx.loading = true
                    }
                }
                attr {
                    src(ImageUri.commonAssets("ic_common_pull_loading.png"))
                    size(width = 27f, height = 27f)
                    margin(top = (if (ctx.attr.text.isNullOrEmpty()) 20.5f else 22.5f))
                    transform(rotate = Rotate(if (ctx.loading) 360f else 0f))
                    animate(animation = ctx.rotate(), value = ctx.loading)
                }
            }
            vif({ ctx.attr.text?.isNotEmpty() }) {
                Text {
                    attr {
                        margin(10f, left = 20f, right = 20f)
                        text(ctx.attr.text!!)
                        color(ctx.euColor.w1)
                        height(22f)
                        fontSize(16f)
                        fontFamily(Constant.TXT_FONT_FAMILY)
                        fontWeight400()
                    }
                }
            }
        }
    }

    private fun rotate(): Animation {
        val rotate = Animation.linear(0.7f)
        rotate.repeatForever(true)
        return rotate
    }
}


internal class EuToastViewAttr : ComposeAttr() {
    var text: String? = null
}

internal class EuToastViewEvent : ComposeEvent() {

}

internal fun ViewContainer<*, *>.EuToast(init: EuToastView.() -> Unit) {
    addChild(EuToastView(), init)
}