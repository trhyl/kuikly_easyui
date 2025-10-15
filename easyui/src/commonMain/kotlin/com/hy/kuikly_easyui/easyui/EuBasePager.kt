package com.hy.kuikly_easyui.easyui

import com.hy.kuikly_easyui.easyui.base.BridgeModule
import com.hy.kuikly_easyui.easyui.theme.ThemeContext
import com.hy.kuikly_easyui.easyui.theme.ThemeManager
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.manager.PagerManager
import com.tencent.kuikly.core.module.BackPressModule
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.module.NotifyModule
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.PageData
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.CallbackRef
import com.tencent.kuikly.core.utils.urlParams

/**
 * 外部模块管理器
 */
object EuExternalModules {
    /**
     * 全局 Module
     * @note 只需要注册一次即可，后续可以直接通过 getModule 获取
     */
    private val global = hashMapOf<String, Module>()

    /**
     * 延迟注册的模块，在页面创建时才会加载、绑定
     */
    private val latent = hashSetOf<() -> Map<String, Module>>()

    fun injectLatentModule(func: () -> Map<String, Module>) {
        latent.add(func)
    }

    fun registerGlobalModule(moduleName: String, module: Module) {
        global[moduleName] = module
    }

    fun removeGlobalModule(moduleName: String): Module? {
        return global.remove(moduleName)
    }

    fun getAllGlobalModules(): Map<String, Module> {
        return global.toMap()
    }

    fun getLatentModules(): Map<String, Module> {
        val modules = hashMapOf<String, Module>()
        for (func in latent) {
            modules.putAll(func())
        }
        return modules
    }
}

val curPageData: PageData
    get() = PagerManager.getCurrentPager().pageData

val curRouter: RouterModule
    get() = PagerManager.getCurrentPager().acquireModule(RouterModule.MODULE_NAME)

/**
 * 当前EasyUI 基础页面实例
 * @note 需要保证当前页面继承自「EuBasePager」
 */
val curEuPage: EuBasePager
    get() = PagerManager.getCurrentPager() as EuBasePager

abstract class EuBasePager : Pager() {
    var euColor by observable(ThemeManager.getColor())
    val themeCtx: ThemeContext = ThemeContext()
    val notify by lazy(LazyThreadSafetyMode.NONE) {
        acquireModule<NotifyModule>(NotifyModule.MODULE_NAME)
    }

    private lateinit var themeChangedRef: CallbackRef

    open fun getLatentModules(): (() -> Map<String, Module>)? {
        return {
            val latentModules = hashMapOf<String, Module>()
            latentModules[BridgeModule.MODULE_NAME] = BridgeModule()
            latentModules
        }
    }

    /**
     * 页面脚手架
     */
    var scaffold: EuScaffoldViewAttr? = null

    override fun createExternalModules(): Map<String, Module>? {
        val externalModules = hashMapOf<String, Module>()
        externalModules.putAll(EuExternalModules.getAllGlobalModules())
        externalModules.putAll(EuExternalModules.getLatentModules())
        val latentModule = getLatentModules()
        if (latentModule != null) {
            externalModules.putAll(latentModule.invoke())
        }
        return externalModules
    }

    override fun created() {
        super.created()
        this.themeCtx.pageData = this.pageData
        this.themeCtx.setup()
        themeChangedRef =
            acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).addNotify(
                ThemeManager.COLOR_MODE_EVENT
            ) { data ->
                ThemeManager.receiveSystemMode(data?.optInt("mode") ?: -1)
                euColor = ThemeManager.getColor()
            }
        euSetup()
        euAddNotify()
    }

    override fun onReceivePagerEvent(pagerEvent: String, eventData: JSONObject) {
        super.onReceivePagerEvent(pagerEvent, eventData)
        /**
         * 安卓/鸿蒙设备屏幕边缘滑动返回拦截事件
         * 安卓侧修改代码：
         * ```
         *     override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
         *         if (event?.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
         *             if (kuiklyRenderViewDelegator.onBackPressed()) {
         *                 return true
         *             }
         *         }
         *         return super.dispatchKeyEvent(event)
         *     }
         * ```
         * 鸿蒙侧修改代码：
         * ```
         *   onBackPress(): boolean | void {
         *     if (this.kuiklyViewDelegate) {
         *       // send back press event and wait for consumed result synchronously
         *       return this.kuiklyController?.onBackPress();
         *     }
         *     return false;
         *   }
         * ```
         * @return true: 拦截事件，不执行默认返回逻辑
         */
        if (pagerEvent == "onBackPressed") {
            val isIntercept = euOnBackPressed()
            KLog.i("Kuikly", "拦截屏幕手势返回事件: $isIntercept")
            acquireModule<BackPressModule>(BackPressModule.MODULE_NAME).backHandle(isIntercept)
        } else {
            KLog.i("Kuikly", "收到页面事件: $pagerEvent, 数据：$eventData")
        }
    }

    /**
     * 安卓/鸿蒙设备屏幕边缘滑动返回拦截事件
     * @return true: 拦截事件，不执行默认返回逻辑
     */
    open fun euOnBackPressed(): Boolean {
        return false
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        ThemeManager.notifyCallback = {
            acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).postNotify(
                ThemeManager.THEME_CHANGED_EVENT,
                JSONObject()
            )
        }
    }

    override fun pageWillDestroy() {
        this.euRemoveNotify()
        super.pageWillDestroy()
        KLog.i("Kuikly", "${this.pageName} 页面即将销毁")
    }

    override fun viewDestroyed() {
        super.viewDestroyed()
        acquireModule<NotifyModule>(NotifyModule.MODULE_NAME).removeNotify(
            ThemeManager.COLOR_MODE_EVENT,
            themeChangedRef
        )
    }

    override fun isNightMode(): Boolean {
        return themeCtx.isNightModel
    }

    override fun themeDidChanged(data: JSONObject) {
        super.themeDidChanged(data)
        themeCtx.themeDidChanged(data)
    }

    /**
     * 实现「必要」初始化，不可影响页面加载延迟
     */
    open fun euSetup() {
    }

    /**
     * 按需通过「notify」注册通知，此处注册的「notify」必须在「euRemoveNotify」移除
     */
    open fun euAddNotify() {

    }

    /**
     * 移除「euAddNotify」注册的「notify」
     */
    open fun euRemoveNotify() {
    }

    open fun jumpRouter(router: String) {
        val params = urlParams("pageName=$router")
        val pageData = JSONObject()
        params.forEach {
            pageData.put(it.key, it.value)
        }
        val pageName = pageData.optString("pageName")
        KLog.i("Kuikly", "跳转路由2：$pageName 参数:$pageData")
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(pageName, pageData)
    }

    open fun openPage(pageName: String, pageData: JSONObject? = null) {
        val data = pageData ?: JSONObject()
        KLog.i("Kuikly", "跳转路由1：$pageName 参数:$data")
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).openPage(pageName, data)
    }

    open fun closePage() {
        acquireModule<RouterModule>(RouterModule.MODULE_NAME).closePage()
    }
}