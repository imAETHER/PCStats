package im.aether;

/**
 * config.json gets deserialized into an instance of this class.
 */
public class Config {
    public String appId = "995501904685715476";
    public String smallImage = "https://aetherclient.com/images/gato.png";
    public String largeImage = "https://c.tenor.com/hEwfEcj2R60AAAAd/laptop-smoking.gif";
    public String topText = "...";
    public String bottomText = "...";
    public boolean showPCActiveTime;
    public String displayGpu = "";

    // Getters
    public String getAppId() {
        return appId;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public String getTopText() {
        return topText;
    }

    public String getBottomText() {
        return bottomText;
    }

    public boolean isShowPCActiveTime() {
        return showPCActiveTime;
    }

    public String getDisplayGpu() {
        return displayGpu;
    }

    // Setters (Optional, you may not need setters for all fields)
    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setSmallImage(String smallImage) {
        this.smallImage = smallImage;
    }

    public void setLargeImage(String largeImage) {
        this.largeImage = largeImage;
    }

    public void setTopText(String topText) {
        this.topText = topText;
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText;
    }

    public void setShowPCActiveTime(boolean showPCActiveTime) {
        this.showPCActiveTime = showPCActiveTime;
    }

    public void setDisplayGpu(String displayGpu) {
        this.displayGpu = displayGpu;
    }
}
