package oogasalad.engine.view.util;

import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.view.DefaultView;
import oogasalad.fileparser.records.HitBoxData;
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
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.util.ViewObjectToImageConverter;
import oogasalad.fileparser.records.FrameData;
import oogasalad.engine.view.ObjectImage;

class ViewObjectToImageConverterTest extends ApplicationTest {

  private ViewObjectToImageConverter converter;
  private ViewState viewState;
  private GameManagerAPI gameManager;

  @Override
  public void start(Stage primary) throws Exception {
    // This runs on the FX thread
    converter = new ViewObjectToImageConverter();
    gameManager = new DefaultGameManager();
    viewState = new ViewState(primary, gameManager, new DefaultView(primary, gameManager));
  }

  @BeforeEach
  void setUp() {
    // ensure any pending FX events complete before each test
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testConverterMapClearedAfterSelectLevelAndDisplay() throws Exception {
    // 1️⃣ Start the game so GameDisplay (and its converter usage) is initialized
    Runnable start = new ButtonActionFactory(viewState).startGame();
    Platform.runLater(start);
    WaitForAsyncUtils.waitForFxEvents();

    // 2️⃣ Convert a dummy object → map size should be 1
    ImmutableGameObject obj = makeDummy();
    converter.convertObjectsToImages(List.of(obj));
    assertMapSize(converter, 1, "after initial conversion");

    // 3️⃣ Simulate “Select Level” and then force displayGameObjects()
    Platform.runLater(() -> {
      ButtonActionFactory factory = new ButtonActionFactory(viewState);
      factory.selectLevel("dinosaurgame", "DinoLevel1.xml").run();
      try {
        gameManager.displayGameObjects();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    WaitForAsyncUtils.waitForFxEvents();

    // 4️⃣ Now the converter’s map must have been cleared ⇒ size == 0
    assertMapSize(converter, 0, "after selectLevel + displayGameObjects()");
  }

  private void assertMapSize(ViewObjectToImageConverter conv, int expected, String phase)
      throws Exception {
    Field mapField = ViewObjectToImageConverter.class
        .getDeclaredField("UUIDToImageMap");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<UUID, ObjectImage> internalMap =
        (Map<UUID, ObjectImage>) mapField.get(conv);
    assertEquals(
        expected,
        internalMap.size(),
        "Expected converter map size " + expected + " " + phase
    );
  }

  private ImmutableGameObject makeDummy() throws FileNotFoundException {
    UUID uuid = UUID.randomUUID();               // always valid
    FrameData frame = new FrameData("hello", 0,0,1,1);
    Sprite sprite = new Sprite(
        Map.of("hello", frame),
        frame,
        Map.of(),
        0,0,
        new File("data/gameData/gameSpriteSheetData/doodlejump/blackhole.xml"),
        0.0,
        false
    );
    HitBox hbData = new HitBox(
        /* x = */ 0,
        /* y = */ 0,
        /* width = */ 10,
        /* height = */ 10
    );
    // If your Entity constructor expects a HitBox object instead of HitBoxData, you can wrap this:

    return new Entity(
        uuid,
        "hello",
        /* xPos= */ 0,
        /* yPos= */ 0,
        /* rotation= */ 0,
        hbData,         // ← now non-null
        sprite,
        /* stringProps= */ null,
        /* doubleProps= */ null,
        /* displayedProps= */ null
    );
  }
}
