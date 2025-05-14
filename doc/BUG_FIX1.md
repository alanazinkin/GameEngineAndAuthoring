## Description

Selecting a new level from within a given level does not remove previous object images, but removes
the back-end object.

## Expected Behavior

Objects are expected to be removed from the front-end when the user selects a new level from within
another level

## Current Behavior

Any game objects from previously selected levels remain in the view, but are not interactable.

## Steps to Reproduce

Provide detailed steps for reproducing the issue.

1. Set up a game and create a Game Manager Instance
2. Create a new Immutable Game Object
2. Call gameManager.addGameObjectImage(ImmutableGameObject gameObject) with a specific UUID
2. Call the GameManager select level method
2. Then user reflection to grab the LevelView scene and assert that the object node is still in the scene

## Failure Logs

N/A

## Hypothesis for Fixing the Bug

* TEST: I will add a game object to the scene, call the select level method, and assert that the
  scene still contains the original object image
* CODE: to fix this bug, I must remove the object image from the image map and create a new method
  called removeObjectImage, which removes the object image from the map and removes it from the
  level view scene