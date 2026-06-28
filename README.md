# Low Latency

[中文](https://github.com/WinExp/Minecraft-Low-Latency/blob/master/README_CN.md)

This mod is designed to reduce input latency when the GPU is full load and the CPU is not, inspired by NVIDIA Reflex.

When the gpu is full load and the CPU is not, a frame queue backlog will build up, the input latency will increase.

This mod estimates input latency based on historical CPU and GPU frame times, and waits before input events to ensure the frame queue will not build up, which reduces input latency.

**This project is in its early stages, which may result in reduced frame rates or increased latency. You can adjust the ‘wait time offset’ in the config menu to alleviate this issue.**

You can enable/disable low latency feature in config menu.

#### Config menu

![Config menu](https://github.com/WinExp/Minecraft-Low-Latency/blob/HEAD/docs/images/config_menu.png?raw=true)

#### F3 debug screen

![F3 debug screen](https://github.com/WinExp/Minecraft-Low-Latency/blob/HEAD/docs/images/debug_screen.png?raw=true)
