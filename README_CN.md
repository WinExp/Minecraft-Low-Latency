# Low Latency

此模组受 NVIDIA Reflex 启发，旨在当 GPU 满载且 CPU 不满载时降低输入延迟。

当 GPU 满载且 CPU 不满载时，帧队列将会堆积，输入延迟将会上升。

此模组通过历史 CPU 与 GPU 渲染耗时估测输入延迟，并在输入事件前让 CPU 等待，尽可能确保帧队列不会堆积，从而降低输入延迟。

**本项目处于早期阶段，可能会导致帧率下降或延迟上升。你可以在配置菜单中调整“等待时间偏移”来缓解此问题。**

你可以在配置菜单中开启/关闭低延迟功能。

#### 配置菜单

![配置菜单](https://github.com/WinExp/Minecraft-Low-Latency/blob/HEAD/docs/images/config_menu.png?raw=true)

#### F3 调试屏幕

![F3 屏幕](https://github.com/WinExp/Minecraft-Low-Latency/blob/HEAD/docs/images/debug_screen.png?raw=true)
