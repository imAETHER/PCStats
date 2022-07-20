# <p align="center">PC Stats<br><img src="https://aetherclient.com/uploads/imf44/2a8cfc720a.png"></p>

# 📄 Config
You need to place `config.json` in the same directory as the jar.<br>
This is an example config, you can use it if you want:
```
{
  "appId": "995501904685715476",
  "smallImage": "https://aetherclient.com/images/gato.png",
  "largeImage": "https://c.tenor.com/hEwfEcj2R60AAAAd/laptop-smoking.gif",
  "showPCActiveTime": false
}
```

# 🟢 Usage
Either build it yourself or go to [releases](https://github.com/imAETHER/PCStats/releases) and download it.

This project uses OSHI to get processor temperatures, OSHI uses [OpenHardwareMonitor](https://github.com/openhardwaremonitor/openhardwaremonitor) to get these, download and run it in the background while you use the RPC to get accurate readings.<br>


# 💻 Supported platforms
All platforms since its java & its using OSHI (haven't tested tho), for now GPU usage and temperature only works with NVIDIA gpus that have nvidia-smi.
