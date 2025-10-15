package com.hy.kuikly_easyui.easyui.theme

import com.tencent.kuikly.core.base.Color

data class ThemeColors(
    val p1: Color,
    val p1_5: Color,
    val p1_15: Color,
    val p1_30: Color,
    val p2: Color,
    val p3: Color,
    var w1: Color,
    var w1_0: Color,
    val b1: Color,
    val b2: Color,
    val b3: Color,
    val b10: Color,
    var s1: Color,
    var s2: Color,
    var y2: Color,
)

/**
 * 亮色模式
 */
val lightColorScheme = ThemeColors(
    p1 = Color(0xff426EFF),
    p1_5 = Color(0x0d6783FF),
    p1_15 = Color(0x42426EFF),
    p1_30 = Color(0x4d426EFF),
    p2 = Color(0xff577EFF),
    p3 = Color(0xff809DFF),
    w1 = Color(0xffFFFFFF),
    w1_0 = Color(0x00ffffff),
    b1 = Color(0xff222222),
    b2 = Color(0xff4E5366),
    b3 = Color(0xff9399AD),
    b10 = Color(0xffF2F3F7),
    s1 = Color(0xff5C6A99),
    y2 = Color(0xffFDF6F0),
    s2 = Color(0xffF5F7FA),
)

/**
 * 暗色模式
 */
val darkColorScheme = ThemeColors(
    p1 = Color(0xff3559D4),  // 原#426EFF → 明度↓20% 饱和度↑5%[1,5](@ref)
    p1_5 = Color(0x0d3559D4),  // 降明度+提饱和，保持品牌辨识[2](@ref)
    p1_15 = Color(0x423559D4),  // 同上，用于遮罩层等半透明效果[5](@ref)
    p1_30 = Color(0x4d3559D4),  // 同上，用于遮罩层等半透明效果[5](@ref)
    p2 = Color(0xff4A6BEB),  // 保持色相，明度阶梯式降低
    p3 = Color(0xff6D8AFF),  // 避免过亮刺眼[3](@ref)
    w1 = Color(0xff1A1F30),  // 纯白→深蓝灰（避免强对比）
    w1_0 = Color(0x001A1F30),
    b1 = Color(0xffE4E7F2),  // 原#222222→提亮为浅灰（文字主色）[4](@ref)
    b2 = Color(0xffB8BFD9),  // 中灰文本
    b3 = Color(0xff7A849F),  // 次弱文本
    b10 = Color(0xff121826), // 深色基底（原F2F3F7→降明度70%）[3](@ref)
    s1 = Color(0xff4A5A8C),  // 降明度+增饱和度
    y2 = Color(0xff2A2118),  // 原暖白→深棕调背景
    s2 = Color(0xff1A2235),  // 原浅灰→转深蓝灰
)