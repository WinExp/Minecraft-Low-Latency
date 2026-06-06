# Low Latency

此模组受 NVIDIA Reflex 启发，旨在当 GPU 满载且 CPU 不满载时降低输入延迟。

当 GPU 满载且 CPU 不满载时，帧队列将会堆积，输入延迟将会上升。

此模组通过历史 CPU 与 GPU 渲染耗时估测输入延迟，并在输入事件前让 CPU 等待，尽可能确保帧队列长度始终为 1，从而降低输入延迟。

**本项目处于早期阶段，可能会导致帧率下降。你可以在配置菜单中将“等待时间偏移”设为 -1 以缓解此问题，但这也会增加输入延迟。**

你可以在配置屏幕中开启/关闭低延迟功能。

### 规划

- [ ] 自动修正等待时间

#### 配置屏幕

![配置屏幕](https://raw.githubusercontent.com/WinExp/Minecraft-Low-Latency/refs/heads/master/docs/images/config_screen.png)

#### F3 调试屏幕

![F3 屏幕](https://raw.githubusercontent.com/WinExp/Minecraft-Low-Latency/refs/heads/master/docs/images/debug_screen.png)
