# oogasalad

## It's Thyme to Eat: SALAD

## Billy McCune, Alana Zinkin

This project implements an authoring environment and player for multiple related games.

### Timeline

* Start Date: 3/25/25

* Finish Date: 4/27/25

* Hours Spent: 25 hours/week across ~5 weeks

### Attributions

* Resources used for learning (including AI assistance)
    * [SOLID Design Principles](https://www.digitalocean.com/community/conceptual-articles/s-o-l-i-d-the-first-five-principles-of-object-oriented-design#single-responsibility-principle)
      used to understand design principles
    * [Refactoring Guru](https://refactoring.guru/design-patterns/factory-method) used for
      understanding design patterns - specifically factory
    * [Undoing Git commits](https://stackoverflow.com/questions/22682870/how-can-i-undo-pushed-commits-using-git)
* Resources used directly (including AI assistance)
    * ChatGPT and Claude AI assisted in writing code

### Running the Program

* Main class: Main.java

* Data files needed:
    * Level Data: selected level/game file within the doc folder which is separated by game type
    * Resource Properties files (config, css, gameIcons, i18n, server, and shared folders)

* Interesting data files:
   * Celeste Game
   * Editor Prefabricated data: editorData/prefabricatedData/prefab.xml

* Key/Mouse inputs:
    * Key and Mouse inputs are entirely decided by the user within the level file, allowing for
      total flexibility

### Configuring OpenAI API Key

The chat assistant feature requires an OpenAI API key. You can configure it in one of the following
ways (in order of precedence):

1. **Environment Variable**: Set the `OPENAI_API_KEY` environment variable
   ```bash
   export OPENAI_API_KEY=your_key_here
   ```

2. **Java System Property**: Pass the API key as a system property
   ```bash
   java -DOPENAI_API_KEY=your_key_here -jar oogasalad.jar
   ```

3. **Config File**: Add your API key to `config.properties` in the resources directory
   ```properties
   OPENAI_API_KEY=your_key_here
   ```

4. **Dot Env File**: Create a `.env` file in the project root
   ```
   OPENAI_API_KEY=your_key_here
   ```

For security reasons, options #1 and #2 are recommended for production use.

### Notes/Assumptions

* Assumptions or Simplifications:
    * Assumed that each level was discrete and that users would play one level at a time rather than
      progressing through a series of continuous levels. This simplified the process of level
      progression by removing the need to store the next level within each file.
   * Assumed that Editor and Engine would only be connected through the level files themselves rather
     than directly at runtime. This assumption led us to create two versions of GameObject data,
     GameObject for the Engine and EditorObject for the Editor. This resulted in
     duplicated code and unnecessary extra conversions and more work.

* Known Bugs:

* Features implemented:
   * Users can play a variety of games and create their own games within the editor

* Features unimplemented:
   * Users cannot select prefabricated events
   * Events must be recreated for each object rather than applied from a shared event system

* Noteworthy Features:
   * Networked players
   * AI ChatBot assistant
   * Prefabricated Editor objects
   * Editor Sprite Sheet Parser

### Assignment Impressions

* This assignment challenged our team to find a balance between creating an entirely customizable,
  flexible game and using common design patterns and abstraction hierarchies
* It was challenging to predict that the Engine and Editor would share extensive properties rather
  than simply sharing data through the level files
* The project was the largest team most of us had ever worked on. A large portion of the
  project was dedicated to learning Agile framework, teamwork, communication skills, and
  collaboration.
* This assignment was more complicated than we had previously assumed during our sprint planning,
  but each teammate was able to learn deeply about game design, data formatting, API usage,
  abstraction, externalizing data, encapsulation, and inheritance


