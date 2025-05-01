package oogasalad.engine.view.util;

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
  void testConverterMapClearedAfterSelectLevelAndDisplay() throws Exception {
    // 1️⃣ Start the game
    Runnable start = new ButtonActionFactory(viewState).startGame();
    Platform.runLater(start);
    WaitForAsyncUtils.waitForFxEvents();

    // 2️⃣ Convert a dummy object → map size should be 1
    ImmutableGameObject obj = makeDummy();
    converter.convertObjectsToImages(List.of(obj));

    Field mapField = ViewObjectToImageConverter.class
        .getDeclaredField("UUIDToImageMap");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, ObjectImage> initialMap =
        (Map<String, ObjectImage>) mapField.get(converter);
    assertEquals(1, initialMap.size(), "Expected 1 entry after first conversion");

    // 3️⃣ Simulate “Select Level” and then force displayGameObjects()
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

    // 4️⃣ Grab the same map and assert it’s now empty
    @SuppressWarnings("unchecked")
    Map<String, ObjectImage> postMap =
        (Map<String, ObjectImage>) mapField.get(converter);
    assertEquals(
        0,
        postMap.size(),
        "Expected converter map to be cleared after selectLevel + displayGameObjects()"
    );
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
