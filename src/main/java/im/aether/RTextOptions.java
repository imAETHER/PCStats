package im.aether;

public enum RTextOptions {
    CPUStats("CPU - Usage + Temp"),
    GPUStats("GPU - Usage + Temp"),
    Custom("Custom");

    private final String display;
    RTextOptions(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }
}
