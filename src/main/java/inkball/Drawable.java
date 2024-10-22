package inkball;

/**
 * An interface representing drawable objects that can be rendered on the screen.
 */
public interface Drawable {

    /**
     * Draws the object on the screen using the provided App instance.
     *
     * @param app the App object used to render the drawable object
     */
    void draw(App app);
}
