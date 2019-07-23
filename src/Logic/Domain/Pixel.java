package Logic.Domain;

public class Pixel {

    private int red;
    private int green;
    private int blue;

    public Pixel(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public int getRedChannel() {
        return this.red;
    }

    public int getGreenChannel() {
        return this.green;
    }

    public int getBlueChannel() {
        return this.blue;
    }
}