package oogasalad.engine.view.util;

import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.LevelDisplay;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.ObjectImage;
import oogasalad.engine.view.util.ViewObjectToImageConverter;
import oogasalad.fileparser.records.FrameData;

class ViewObjectToImageConverterTest extends ApplicationTest {

  private ViewObjectToImageConverter converter;
  private ViewState viewState;
  private GameManagerAPI gameManager;

  @Override
  public void start(Stage primary) throws Exception {
    converter   = new ViewObjectToImageConverter();
    gameManager = new DefaultGameManager();
    viewState   = new ViewState(primary, gameManager, new DefaultView(primary, gameManager));
  }

  @BeforeEach
  void setUp() {
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testRealConverterClearsMapAfterLevelSwitch() throws Exception {
    // 1) start the game
    Runnable start = new ButtonActionFactory(viewState).startGame();
    Platform.runLater(start);
    WaitForAsyncUtils.waitForFxEvents();

    // 2) grab the GameDisplay → LevelDisplay via reflection
    Field dvField = ViewState.class.getDeclaredField("myDefaultView");
    dvField.setAccessible(true);
    DefaultView dv = (DefaultView) dvField.get(viewState);

    Field cdField = DefaultView.class.getDeclaredField("currentDisplay");
    cdField.setAccessible(true);
    Object gameDisp = cdField.get(dv);
    assertTrue(gameDisp instanceof GameDisplay);

    Field ldField = GameDisplay.class.getDeclaredField("myLevelView");
    ldField.setAccessible(true);
    Object levelView = ldField.get(gameDisp);
    assertTrue(levelView instanceof LevelDisplay);

    // 3) grab the converter from the LevelDisplay
    Field convField = LevelDisplay.class.getDeclaredField("myConverter");
    convField.setAccessible(true);
    ViewObjectToImageConverter realConverter =
        (ViewObjectToImageConverter) convField.get(levelView);

    // 4) seed it with a dummy object
    ImmutableGameObject obj = makeDummy();
    realConverter.convertObjectsToImages(List.of(obj));

    // assert initial size == 1
    Field mapField = ViewObjectToImageConverter.class.getDeclaredField("UUIDToImageMap");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<?,?> mapBefore = (Map<?,?>) mapField.get(realConverter);
    assertEquals(1, mapBefore.size(), "should have 1 entry after first render");

    // 5) switch level and re-render
    Platform.runLater(() -> {
      new ButtonActionFactory(viewState)
          .selectLevel("dinosaurgame", "DinoLevel1.xml")
          .run();
      try {
        gameManager.displayGameObjects();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    WaitForAsyncUtils.waitForFxEvents();

    // 6) check that the same converter’s map is now empty
    @SuppressWarnings("unchecked")
    Map<?,?> mapAfter = (Map<?,?>) mapField.get(realConverter);
    assertEquals(0, mapAfter.size(),
        "Expected LevelDisplay’s converter to have cleared its map after level switch");
  }


  private ImmutableGameObject makeDummy() throws FileNotFoundException {
    UUID uuid = UUID.randomUUID();
    FrameData frame = new FrameData("hello", 0,0,1,1);
    Sprite sprite = new Sprite(
        Map.of("hello", frame),
        frame,
        Map.of(),
        0,0,
        // <-- use your actual sprite-path here:
        new File("data/gameData/gameSpriteSheetData/doodlejump/blackhole.xml"),
        0.0,
        false
    );
    HitBox hb = new HitBox(0, 0, frame.width(), frame.height());
    return new Entity(uuid, "hello", 0,0,0, hb, sprite, null, null, null);
  }
}