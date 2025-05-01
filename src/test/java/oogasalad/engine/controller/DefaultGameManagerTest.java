package oogasalad.engine.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.ViewAPI;
import oogasalad.exceptions.ViewInitializationException;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import javafx.stage.Stage;
import java.lang.reflect.Field;
import java.util.UUID;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.LevelDisplay;

public class DefaultGameManagerTest extends ApplicationTest {

  private ViewState viewState;
  private GameManagerAPI gameManager;
  private DefaultView defaultView;

  @Override
  public void start(Stage stage) throws ViewInitializationException, FileNotFoundException {
    // Initialize ViewState and GameManager so that ApplicationTest sets up JavaFX
    gameManager = new DefaultGameManager();
    defaultView = new DefaultView(stage, gameManager);
    viewState = new ViewState(stage, gameManager, defaultView);

  }

  @BeforeEach
  void setUp() {
    // Make sure we're on the JavaFX thread before each test
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testObjectImagePersistsAfterLevelSwitch_shouldFail() throws Exception {
    // 1. Add a dummy object and its image
    FrameData marioFrame = new FrameData("Mario Paused", 1, 1, 2, 4);
    Map<String, FrameData> frameMap = new HashMap<>();
    frameMap.put("Mario Paused", marioFrame);
    Map<String, AnimationData> animationMap = new HashMap<>();

    Sprite expectedSprite = new Sprite(frameMap, marioFrame, animationMap, 0, 0, new File("dd"), 90.0, false);
    UUID testUUID = UUID.fromString("f1010102-aaaa-bbbb-cccc-000000000001");
    ImmutableGameObject dummy = new Entity(
        testUUID, "100", 0, 0, 0,
        null, expectedSprite, null, null, null
    );
    // this will add its ImageView to the LevelDisplay once GameDisplay is set
    gameManager.addGameObjectImage(dummy);

    // 2. Kick off startGame() via ButtonActionFactory (the only allowed caller of setDisplay)
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    Runnable startGame = factory.startGame();
    // run it on the FX thread
    WaitForAsyncUtils.asyncFx(startGame);
    WaitForAsyncUtils.waitForFxEvents();

    // 3. Now switch level
    gameManager.selectGame("data/gameData/levels/dinosaurgame/DinoLevel1.xml");
    WaitForAsyncUtils.waitForFxEvents();

    // 4. Reflectively grab the LevelDisplay
    // 4a. DefaultGameManager.myView → DefaultView.currentDisplay → GameDisplay.myLevelView
    Field viewField = DefaultGameManager.class.getDeclaredField("myView");
    viewField.setAccessible(true);
    Object defaultView = viewField.get(gameManager);

    Field dispField = defaultView.getClass().getDeclaredField("currentDisplay");
    dispField.setAccessible(true);
    Object gameDisplay = dispField.get(defaultView);
    assertTrue(gameDisplay instanceof GameDisplay);

    Field levelViewField = GameDisplay.class.getDeclaredField("myLevelView");
    levelViewField.setAccessible(true);
    LevelDisplay levelView = (LevelDisplay) levelViewField.get(gameDisplay);

    // 5. Assert the old object's ImageView is still present (this is the failing behavior)
    boolean stillThere = levelView.getChildren().stream()
        .anyMatch(node -> testUUID.toString().equals(node.getId()));

    assertTrue(stillThere,
        "FAIL: Expected the old object's image to remain, but it was removed.");
  }
}
