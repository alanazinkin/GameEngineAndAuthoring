package oogasalad.engine.controller;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;

import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.LevelDisplay;
import oogasalad.engine.view.factory.ButtonActionFactory;

public class DefaultGameManagerTest extends ApplicationTest {

  private ViewState viewState;
  private GameManagerAPI gameManager;

  @Override
  public void start(Stage stage) throws Exception {
    // Initialize JavaFX, DefaultView, GameManager, and ViewState
    gameManager = new DefaultGameManager();
    DefaultView defaultView = new DefaultView(stage, gameManager);
    viewState = new ViewState(stage, gameManager, defaultView);
  }

  @BeforeEach
  void setUp() {
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testLevelSwitchAccumulatesNodes_shouldFailAfterFix() throws Exception {
    // 1️⃣ Start the game
    Runnable start = new ButtonActionFactory(viewState).startGame();
    Platform.runLater(start);
    WaitForAsyncUtils.waitForFxEvents();

    // 2️⃣ Grab initial node count
    LevelDisplay before = extractLevelDisplay();
    int initialCount = before.getChildren().size();

    // 3️⃣ Switch to a new level
    gameManager.selectGame("data/gameData/levels/dinosaurgame/DinoLevel1.xml");
    WaitForAsyncUtils.waitForFxEvents();

    // 4️⃣ Grab post‐switch node count
    LevelDisplay after = extractLevelDisplay();
    int afterCount = after.getChildren().size();

    // TODAY this passes (afterCount > initialCount because nothing was cleared).
    // ONCE FIXED it will fail (afterCount should be ≤ initialCount).
    assertTrue(afterCount > initialCount,
        String.format("Expected more nodes after switch (got %d > %d); will fail once cleanup works.",
            afterCount, initialCount));
  }

  // -----------------------
// Helper (same as before)
// -----------------------
  private LevelDisplay extractLevelDisplay() throws Exception {
    Field dvField = ViewState.class.getDeclaredField("myDefaultView");
    dvField.setAccessible(true);
    DefaultView dv = (DefaultView) dvField.get(viewState);

    Field cdField = DefaultView.class.getDeclaredField("currentDisplay");
    cdField.setAccessible(true);
    Object gd = cdField.get(dv);
    assertInstanceOf(GameDisplay.class, gd);

    Field lvField = GameDisplay.class.getDeclaredField("myLevelView");
    lvField.setAccessible(true);
    return (LevelDisplay) lvField.get(gd);
  }


}

