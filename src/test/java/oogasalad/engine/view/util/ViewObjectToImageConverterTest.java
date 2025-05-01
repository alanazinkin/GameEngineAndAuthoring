package oogasalad.engine.view.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.DataFormatException;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.Sprite;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.LevelDisplay;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.engine.view.ObjectImage;
import oogasalad.fileparser.records.FrameData;

class ViewObjectToImageConverterTest extends ApplicationTest {

  private ViewState viewState;
  private GameManagerAPI gameManager;

  @Override
  public void start(Stage stage) throws Exception {
    // set up real application pieces
    gameManager = new DefaultGameManager();
    DefaultView defaultView = new DefaultView(stage, gameManager);
    viewState = new ViewState(stage, gameManager, defaultView);
  }

  @BeforeEach
  void setUp() {
    // flush any pending JavaFX events
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  void testConverterClearedAfterLevelSwitch() throws Exception {
    // 1) start the game
    Runnable start = new ButtonActionFactory(viewState).startGame();
    Platform.runLater(start);
    WaitForAsyncUtils.waitForFxEvents();

    // 2) reflectively grab DefaultView, then the Scene
    Field dvField = ViewState.class.getDeclaredField("myDefaultView");
    dvField.setAccessible(true);
    DefaultView dv = (DefaultView) dvField.get(viewState);
    Scene scene = dv.getCurrentScene();
    assertNotNull(scene);

    // 3) lookup the GameDisplay by its CSS ID
    Parent root = scene.getRoot();
    GameDisplay gameDisp = (GameDisplay) root.lookup("#game-display");
    assertNotNull(gameDisp, "GameDisplay should have been added");

    // 3) grab its LevelDisplay child
    LevelDisplay lvlDisp = gameDisp.getChildrenUnmodifiable().stream()
        .filter(n -> n instanceof LevelDisplay)
        .map(n -> (LevelDisplay)n)
        .findFirst()
        .orElseThrow(() -> new AssertionError("LevelDisplay not found"));

    // 4) reflectively grab the converter inside LevelDisplay
    Field convField = LevelDisplay.class.getDeclaredField("myConverter");
    convField.setAccessible(true);
    Object converter = convField.get(lvlDisp);
    assertNotNull(converter, "The LevelDisplay's converter must be non-null");

    // 5) render a dummy object through the real converter
    ImmutableGameObject dummy = makeDummy();
    // invoke convertObjectsToImages on the real converter
    converter.getClass()
        .getMethod("convertObjectsToImages", List.class)
        .invoke(converter, List.of(dummy));

    // 6) inspect its internal map size == 1
    Field mapField = converter.getClass().getDeclaredField("UUIDToImageMap");
    mapField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<?,?> before = (Map<?,?>) mapField.get(converter);
    assertEquals(1, before.size(), "Converter map should have 1 entry after rendering dummy");

    // 7) select a new level and re-render
    Platform.runLater(() -> {
      try {
        gameManager
            .selectGame("data/gameData/levels/dinosaurgame/DinoLevel1.xml");
      } catch (DataFormatException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (LayerParseException e) {
        throw new RuntimeException(e);
      } catch (LevelDataParseException e) {
        throw new RuntimeException(e);
      } catch (PropertyParsingException e) {
        throw new RuntimeException(e);
      } catch (SpriteParseException e) {
        throw new RuntimeException(e);
      } catch (EventParseException e) {
        throw new RuntimeException(e);
      } catch (HitBoxParseException e) {
        throw new RuntimeException(e);
      } catch (BlueprintParseException e) {
        throw new RuntimeException(e);
      } catch (GameObjectParseException e) {
        throw new RuntimeException(e);
      }
      try {
        gameManager.displayGameObjects();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
    WaitForAsyncUtils.waitForFxEvents();

    // 8) now the same converter’s map must be cleared
    @SuppressWarnings("unchecked")
    Map<?,?> after = (Map<?,?>) mapField.get(converter);
    assertEquals(0, after.size(),
        "Converter map should be empty after level switch + displayGameObjects()");

    // ... rest remains exactly as before ...
  }

  private ImmutableGameObject makeDummy() throws FileNotFoundException {
    UUID uuid = UUID.randomUUID();
    FrameData frame = new FrameData("F", 0, 0, 10, 10);
    Sprite sprite = new Sprite(
        Map.of("F", frame),
        frame,
        Map.of(),
        0, 0,
        new File("data/gameData/gameSpriteSheetData/doodlejump/blackhole.xml"),
        0.0,
        false
    );
    HitBox hb = new HitBox(0, 0, frame.width(), frame.height());
    return new Entity(uuid, "dummy", 0, 0, 0, hb, sprite, null, null, null);
  }
}
