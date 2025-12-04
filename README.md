# 3x3 Color Cube - Virtual Practice

A desktop application for practicing 3x3 Rubik's Cube solves.

## Prerequisites

- **Java Development Kit (JDK) 21** or higher.

## Setup Instructions

### 1. SQLite JDBC Driver

This project uses SQLite for saving progress. You need to download the SQLite JDBC driver jar file.
# 3x3 Color Cube - Virtual Practice

A desktop application for practicing 3x3 Rubik's Cube solves.

## Prerequisites

- **Java Development Kit (JDK) 21** or higher.

## Setup Instructions

### 1. SQLite JDBC Driver

This project uses SQLite for saving progress. You need to download the SQLite JDBC driver jar file.

1.  Download the JAR from: [https://github.com/xerial/sqlite-jdbc/releases](https://github.com/xerial/sqlite-jdbc/releases) (e.g., `sqlite-jdbc-3.46.0.0.jar`).
2.  Place the downloaded `.jar` file in the `lib` folder of this project. (Create the `lib` folder if it doesn't exist).

## How to Run

### From an IDE (IntelliJ, Eclipse, VS Code)

1.  Open the project folder.
2.  Add the `lib/sqlite-jdbc-xxx.jar` to your project's **Classpath** or **Libraries**.
3.  Run `src/main/java/com/colorcube/Main.java`.

### From Command Line

1.  Compile the code:
    ```bash
    javac -d bin -cp "lib/*" src/main/java/com/colorcube/*.java src/main/java/com/colorcube/**/*.java
    ```

2.  Run the application:
    ```bash
    java -cp "bin;lib/*" com.colorcube.Main
    ```
    *(Note: On Linux/Mac, use `:` instead of `;` in the classpath)*

## Features

- **3D Visualization**: Interactive 3D cube that can be rotated and zoomed.
- **Solid Cubies**: Realistic rendering with 27 individual cubies and black internal faces.
- **Move Animation**: Smooth animations for face rotations.
- **2D Net View**: Real-time flattened view of the cube state.
- **Scramble Generator**: Generates random legal scrambles for practice.
- **Persistence**: Save and load your practice sessions.
- **Keyboard Controls**: Standard notation support (R, L, U, D, F, B) and Undo (Ctrl+Z).

## Controls

- **Mouse**: Drag to rotate view, Scroll to zoom.
- **Keyboard**:
    - `R`, `L`, `U`, `D`, `F`, `B`: Clockwise moves.
    - `Shift` + Key: Counter-clockwise moves.
    - `Ctrl + Z`: Undo.
- **Buttons**:
    - **New Scramble**: Scramble the cube.
    - **Reset**: Return to solved state.
    - **Save Progress**: Save current state.
    - **Load**: Load a saved session.
