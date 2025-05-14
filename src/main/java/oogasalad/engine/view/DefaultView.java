package oogasalad.engine.view;

import java.awt.Point;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.engine.view.camera.TrackerCamera;
import oogasalad.engine.view.screen.SplashScreen;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.ViewInitializationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the current view that a user sees. It implements the GameAppView API
 *
 * @author Aksel Bell
 */
public class DefaultView implements ViewAPI {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private static final int LEVEL_WIDTH = Integer.parseInt(
      resourceManager.getConfig("engine.controller.level", "LevelWidth"));
  private static final int LEVEL_HEIGHT = Integer.parseInt(
      resourceManager.getConfig("engine.controller.level", "LevelHeight"));

  private Display currentDisplay;
  private Scene currentScene;
  private final Stage currentStage;
  private final GameManagerAPI gameManager;
  private final List<KeyCode> currentInputs;
  private final List<KeyCode> releasedInputs;
  private Camera myCamera;
  private Point mousePosition;

  /**
   * Constructor to initialize the GameAppView with a Stage reference.
   */
  public DefaultView(Stage stage, GameManagerAPI gameManager) throws ViewInitializationException {
    this.currentStage = stage;
    this.gameManager = gameManager;
    this.myCamera = new TrackerCamera();
    currentScene = new Scene(new Group(), LEVEL_WIDTH, LEVEL_HEIGHT);
    currentInputs = new ArrayList<>();
    releasedInputs = new ArrayList<>();
  }

  /**
   * @see DefaultView#initialize()
   */
  @Override
  public void initialize() throws ViewInitializationException, FileNotFoundException {
    ViewState currentState = new ViewState(currentStage, gameManager, this);
    SplashScreen splashScreen = new SplashScreen(currentState);

    int width = splashScreen.getSplashWidth();
    int height = splashScreen.getSplashHeight();
    currentDisplay = splashScreen;

    currentScene = new Scene(currentDisplay, width, height);
    currentScene.setOnMouseMoved(e -> {
      this.mousePosition = new Point((int) e.getSceneX(), (int) e.getSceneY());
    });
  }

  /**
   * @see DefaultView#renderGameObjects(List, Camera)
   */
  @Override
  public void renderGameObjects(List<ImmutableGameObject> gameObjects, Camera camera)
      throws RenderingException, FileNotFoundException {
    myCamera = camera;
    currentDisplay.renderGameObjects(gameObjects);
    currentDisplay.shiftNode(myCamera);
  }

  /**
   * @see ViewAPI#getCurrentInputs()
   */
  public List<KeyCode> getCurrentInputs() throws InputException {
    return Collections.unmodifiableList(currentInputs);
  }

  /**
   * return current mouse position
   */
  @Override
  public Point getMousePosition() {
    if (mousePosition == null) {
      return new Point(0, 0);
    }
    return mousePosition;
  }

  /**
   * @see ViewAPI@getReleasedInputs()
   */
  public List<KeyCode> getReleasedInputs() throws InputException {
    return Collections.unmodifiableList(releasedInputs);
  }

  /**
   * Clears the released‑keys list so each release only fires once.
   */
  public void clearReleasedInputs() {
    releasedInputs.clear();
  }

  /**
   * @return the current game scene. Public method because the controller must call it to set the
   * scene initially.
   */
  public Scene getCurrentScene() {
    return currentScene;
  }

  /**
   * Set the current display.
   */
  public void setCurrentDisplay(Display display) {
    currentDisplay = display;
    currentScene.setRoot(currentDisplay);
  }

  /**
   * removes an object image from the level display scene
   *
   * @param gameObject the game object to remove
   */
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    currentDisplay.removeGameObjectImage(gameObject);
  }

  /**
   * adds object image to the level display scene
   *
   * @param gameObject
   */
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    currentDisplay.addGameObjectImage(gameObject);
  }

  /**
   * renders a player's statistics within the HUD display
   *
   * @param player the player game object
   */
  public void renderPlayerStats(ImmutableGameObject player) {
    currentDisplay.renderPlayerStats(player);
  }

  /**
   * Package protected method that allows frontend to trigger key pressed in input list.
   *
   * @param key pressed key.
   */
  public void pressKey(KeyCode key) {
    if (!currentInputs.contains(key)) {
      currentInputs.add(key);
    }
  }

  /**
   * Package protected method that allows frontend to trigger key released in input list.
   *
   * @param key released key.
   */
  public void releaseKey(KeyCode key) {
    currentInputs.remove(key);
    if (!releasedInputs.contains(key)) {
      releasedInputs.add(key);
    }
  }

  /**
   * Renders the overlap screen when the game ends
   *
   * @param gameWon whether the game has been won
   */
  public void renderEndGameScreen(boolean gameWon) {
    LOG.info("Game is over: " + gameWon);
    currentDisplay.renderEndGameScreen(gameWon);
  }


  public void removeObjectImages() {
    currentDisplay.removeObjectImages();
  }
}


