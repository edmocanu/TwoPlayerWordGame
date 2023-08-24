import java.io.IOException;

public class WordGameDemo {
    public static void main(String[] args) throws IOException {
        GameVisualiser game = new GameVisualiser();
        game.setUp();
        game.runGameLoop();
    }
}