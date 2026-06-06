# Low Latency

[中文](README_CN.md)

This mod is designed to reduce input latency when the GPU is full load and the CPU is not, inspired by NVIDIA Reflex.

When the gpu is full load and the CPU is not, a frame queue backlog will build up, the input latency will increase.

This mod estimates input latency based on historical CPU and GPU frame times, and waits before input events to ensure the frame queue will not build up, which reduces input latency.

**This project is in its early stages and may cause your frame rate to drop. You can set the 'Wait time offset' to -1 in the config menu to alleviate the problem, but it will increase input latency.**

You can enable/disable low latency feature in config screen.

### Roadmap

- [ ] Autocorrect wait time

#### Config screen

![Config screen](https://raw.githubusercontent.com/WinExp/Minecraft-Low-Latency/refs/heads/master/docs/images/config_screen.png)

#### F3 Debug screen

![F3 Debug screen](https://raw.githubusercontent.com/WinExp/Minecraft-Low-Latency/refs/heads/master/docs/images/debug_screen.png)
