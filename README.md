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

- **3D Visualization**: Interactive 3D cube with smooth rotation and zoom.
- **Visual Polish**: Solid cubies with a "shine" effect and realistic internal faces.
- **Move Animation**: Smooth animations for all face rotations.
- **2D Net View**: Real-time flattened view with distinct face borders for easier orientation.
- **Scramble Generator**: Generates random legal scrambles for practice.
- **Persistence**: 
    - Save and Load practice sessions using SQLite.
    - Toggleable "Saved Progress" pane for better screen space management.
- **App Layout**: Optimized layout with 60/40 split for 3D/2D views.
- **Custom Controls**: 
    - Global keyboard shortcuts (R, L, U, D, F, B).
    - Customizable keys: Click any face in 2D view to assign a custom key.
    - Key bindings are persisted across restarts.

## Controls

- **Mouse**: 
    - Left Click & Drag: Rotate 3D view.
    - Scroll: Zoom in/out.
- **Keyboard**:
    - `R`, `L`, `U`, `D`, `F`, `B`: Clockwise moves.
    - `Ctrl + Z`: Undo last move (Anti-Clockwise).
- **UI Interaction**:
    - **New Scramble**: Apply a random 20-move scramble.
    - **Reset**: Return to solved state.
    - **Save Progress**: Save current state as a new session.
    - **Saved Progress (Toggle)**: Show/Hide the saved sessions list on the right.
    - **Load/Delete**: Use buttons in the saved list to manage sessions.
    - **Assign Keys**: Click center of any face in 2D view to set a custom key.
