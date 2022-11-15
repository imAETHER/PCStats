# <p align="center">PC Stats<br><img src="https://aetherclient.com/uploads/imf44/2a8cfc720a.png"></p>


# 🟢 Usage
Either build it yourself or go to [releases](https://github.com/imAETHER/PCStats/releases) and download it.

This project uses OSHI to get processor temperatures, OSHI uses [OpenHardwareMonitor](https://openhardwaremonitor.org/) to get these, download and run it in the background while you use the RPC to get accurate readings.<br>

- Place the jar in a folder
- Open CMD/Windows Terminal(looks better) and execute the jar with the command:
`java -jar PCStats.jar`
- It will automatically create config.json in the current directory and will ask you which gpu you want to use **(for the display name only)**
- Will auto save the config when you close the program by doing CTRL + C
- You can manually edit the config to change selected gpu, show uptime and change the RPC images/gifs

# 💻 Supported platforms
All platforms since its java & its using OSHI (haven't tested tho), for now GPU usage and temperature **only works with NVIDIA gpus that have nvidia-smi**.
