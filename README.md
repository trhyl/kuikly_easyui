# Kuikly EasyUI 组件库

[example](./example.jpg)

## 📚 基础组件

### 1. EuBasePager - 基础页面

**功能描述**: 所有页面的基类，提供主题切换、模块管理、路由跳转等核心功能。

**主要特性**:
- 自动主题切换支持
- 模块生命周期管理
- 路由跳转封装
- 返回键拦截处理

```kotlin
@Page("example_page")
class ExamplePage : EuBasePager() {
    override fun body(): ViewBuilder {
        return {
            // 页面内容
        }
    }
    
    override fun euSetup() {
        // 页面初始化逻辑
    }
    
    override fun euOnBackPressed(): Boolean {
        // 自定义返回逻辑
        return true // 拦截返回事件
    }
}
```

### 2. EuScaffold - 页面脚手架

**功能描述**: 页面布局容器，提供导航栏、内容区域、状态管理的统一布局。

**主要特性**:
- 导航栏管理
- 内容状态切换
- 弹窗管理
- 主题适配

```kotlin
EuScaffold {
    attr {
        // 导航栏配置
        navBar = EuNavBarController(
            text = "页面标题",
            showBack = true,
            actions = listOf(
                IEuNavAction(
                    textView = {
                        attr {
                            text("操作")
                            color(euColor.p1)
                        }
                    },
                    callback = {
                        // 操作回调
                    }
                )
            )
        )
        
        // 内容构建器
        builder = EuContentBuilder(
            business = {
                // 正常业务内容
            },
            initialization = {
                // 加载状态内容
            },
            netError = {
                // 网络错误内容
            }
        )
        
        // 状态控制器
        status = contentStatusController
    }
}
```

### 3. EuRefresher - 下拉刷新

**功能描述**: 支持下拉刷新和上拉加载的列表容器组件。

**主要特性**:
- 下拉刷新
- 上拉加载更多
- 状态管理
- 自定义刷新UI

```kotlin
EuRefresher {
    attr {
        // 下拉刷新配置
        pullDown = EuRefreshPullDown(
            pullingText = "松手刷新",
            initialRefresh = true,
            refresh = { controller ->
                // 刷新逻辑
                setTimeout(2000) {
                    // 模拟网络请求
                    controller.endRefresh()
                        .updateEndState(EuFooterEndState.SHOW)
                }
            }
        )
        
        // 上拉加载配置
        pullUp = EuRefreshPullUp(
            loading = { controller ->
                // 加载更多逻辑
                setTimeout(1000) {
                    // 模拟加载更多
                    if (dataList.size >= 50) {
                        controller.updateEndState(EuFooterEndState.NO_MORE_DATA)
                    } else {
                        controller.updateEndState(EuFooterEndState.SUCCESS)
                    }
                }
            }
        )
        
        // 列表内容构建
        listBuilder = {
            vforIndex({ dataList }) { item, index, _ ->
                // 列表项UI
                View {
                    attr {
                        backgroundColor(euColor.w1)
                        padding(16f)
                        margin(bottom = 8f)
                    }
                    Text {
                        attr {
                            text("列表项 $index")
                        }
                    }
                }
            }
        }
    }
}
```

### 4. EuComposeView - 基础视图

**功能描述**: 所有自定义组件的基类，提供主题切换和生命周期管理。

```kotlin
class CustomView : EuComposeView<CustomAttr, CustomEvent>() {
    override fun createAttr(): CustomAttr {
        return CustomAttr()
    }
    
    override fun createEvent(): CustomEvent {
        return CustomEvent()
    }
    
    override fun body(): ViewBuilder {
        return {
            View {
                attr {
                    backgroundColor(euColor.w1)
                    padding(16f)
                }
                Text {
                    attr {
                        text("自定义组件")
                        color(euColor.b1)
                    }
                }
            }
        }
    }
}
```

## 🚀 快速开始

### 1. 创建基础页面

```kotlin
@Page("home_page")
class HomePage : EuBasePager() {
    private val statusController = EuContentStatusController(
        defaultStatus = EuContentStatus.INITIALIZATION
    )
    
    override fun body(): ViewBuilder {
        return {
            EuScaffold {
                attr {
                    navBar = EuNavBarController(
                        text = "首页",
                        showBack = false
                    )
                    
                    status = statusController
                    
                    builder = EuContentBuilder(
                        business = {
                            // 主要内容
                            View {
                                attr {
                                    flex(1f)
                                    padding(16f)
                                }
                                Text {
                                    attr {
                                        text("欢迎使用 EasyUI")
                                        fontSize(24f)
                                        color(euColor.b1)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    override fun pageDidAppear() {
        super.pageDidAppear()
        // 模拟数据加载
        setTimeout(1000) {
            statusController.updateStatus(EuContentStatus.BUSINESS)
        }
    }
}
```

### 2. 创建列表页面

```kotlin
@Page("list_page")
class ListPage : EuBasePager() {
    private val dataList by observableList<String>()
    private val statusController = EuContentStatusController()
    
    override fun body(): ViewBuilder {
        return {
            EuScaffold {
                attr {
                    navBar = EuNavBarController(
                        text = "列表页",
                        showBack = true
                    )
                    
                    status = statusController
                    
                    builder = EuContentBuilder(
                        business = {
                            EuRefresher {
                                attr {
                                    statusController = statusController
                                    
                                    pullDown = EuRefreshPullDown(
                                        refresh = { controller ->
                                            loadData()
                                            controller.endRefresh()
                                        }
                                    )
                                    
                                    listBuilder = {
                                        vforIndex({ dataList }) { item, index, _ ->
                                            View {
                                                attr {
                                                    backgroundColor(euColor.w1)
                                                    padding(16f)
                                                    margin(bottom = 8f)
                                                }
                                                Text {
                                                    attr {
                                                        text(item)
                                                        color(euColor.b1)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun loadData() {
        dataList.clear()
        dataList.addAll(listOf("项目1", "项目2", "项目3"))
    }
}
```

## 综合示例页面

```kotlin
@Page("demo_page")
class DemoPage : EuBasePager() {
    private val statusController = EuContentStatusController(
        defaultStatus = EuContentStatus.INITIALIZATION
    )
    private val listController = EuRefreshController()
    private val dataList by observableList<DemoItem>()
    
    data class DemoItem(
        val id: String,
        val title: String,
        val description: String
    )
    
    override fun body(): ViewBuilder {
        return {
            EuScaffold {
                attr {
                    navBar = EuNavBarController(
                        text = "EasyUI Demo",
                        actions = listOf(
                            IEuNavAction(
                                textView = {
                                    attr {
                                        text("主题")
                                        color(euColor.p1)
                                    }
                                },
                                callback = {
                                    toggleTheme()
                                }
                            )
                        )
                    )
                    
                    status = statusController
                    
                    builder = EuContentBuilder(
                        initialization = {
                            EuToast {
                                attr {
                                    text = "加载中..."
                                }
                            }
                        },
                        business = {
                            EuRefresher {
                                attr {
                                    statusController = statusController
                                    
                                    pullDown = EuRefreshPullDown(
                                        pullingText = "下拉刷新",
                                        refresh = { controller ->
                                            refreshData()
                                            controller.endRefresh()
                                                .updateEndState(EuFooterEndState.SHOW)
                                        }
                                    )
                                    
                                    pullUp = EuRefreshPullUp(
                                        loading = { controller ->
                                            loadMoreData()
                                            if (dataList.size >= 20) {
                                                controller.updateEndState(EuFooterEndState.NO_MORE_DATA)
                                            } else {
                                                controller.updateEndState(EuFooterEndState.SUCCESS)
                                            }
                                        }
                                    )
                                    
                                    listBuilder = {
                                        vforIndex({ dataList }) { item, index, _ ->
                                            createListItem(item, index)
                                        }
                                    }
                                }
                            }
                        },
                        netError = {
                            EuPlaceholder {
                                attr {
                                    controller = EuPlaceholderController(
                                        type = EuPlaceholderType.NO_NETWORK,
                                        title = "网络异常",
                                        desc = "请检查网络连接",
                                        buttonText = "重试",
                                        operationAction = {
                                            statusController.updateStatus(EuContentStatus.INITIALIZATION)
                                            loadData()
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
    
    private fun createListItem(item: DemoItem, index: Int): ViewBuilder {
        return {
            View {
                attr {
                    backgroundColor(euColor.w1)
                    borderRadius(8f)
                    padding(16f)
                    margin(left = 16f, right = 16f, bottom = 12f)
                }
                
                Text {
                    attr {
                        text(item.title)
                        fontSize(18f)
                        fontWeight600()
                        color(euColor.b1)
                    }
                }
                
                Text {
                    attr {
                        text(item.description)
                        fontSize(14f)
                        color(euColor.b2)
                        margin(top = 8f)
                    }
                }
                
                event {
                    click {
                        // 点击事件处理
                        curEuPage.bridgeModule.toast("点击了: ${item.title}")
                    }
                }
            }
        }
    }
    
    private fun loadData() {
        setTimeout(1000) {
            dataList.clear()
            dataList.addAll(generateDemoData())
            statusController.updateStatus(EuContentStatus.BUSINESS)
        }
    }
    
    private fun refreshData() {
        setTimeout(1500) {
            dataList.clear()
            dataList.addAll(generateDemoData())
        }
    }
    
    private fun loadMoreData() {
        setTimeout(1000) {
            val startIndex = dataList.size
            dataList.addAll(generateMoreData(startIndex))
        }
    }
    
    private fun generateDemoData(): List<DemoItem> {
        return (1..5).map { index ->
            DemoItem(
                id = "item_$index",
                title = "示例项目 $index",
                description = "这是第 $index 个示例项目的描述信息"
            )
        }
    }
    
    private fun generateMoreData(startIndex: Int): List<DemoItem> {
        return (startIndex + 1..startIndex + 5).map { index ->
            DemoItem(
                id = "item_$index",
                title = "示例项目 $index",
                description = "这是第 $index 个示例项目的描述信息"
            )
        }
    }
    
    private fun toggleTheme() {
        val currentScheme = if (ThemeManager.getColor() == lightColorScheme) {
            ThemeScheme.DARK
        } else {
            ThemeScheme.LIGHT
        }
        ThemeManager.changeColorScheme(currentScheme)
    }
    
    override fun pageDidAppear() {
        super.pageDidAppear()
        loadData()
    }
}
```
