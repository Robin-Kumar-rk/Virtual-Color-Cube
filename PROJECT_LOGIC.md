# Project Logic & Algorithms

This document provides a technical explanation of the core logic, data structures, and algorithms used in the Color Cube project. It focuses on the internal mechanics of the cube simulation, rotation logic, and 3D rendering mathematics.

## 1. Data Structure: Cube Representation

The core state of the Rubik's Cube is represented in `CubeModel.java` using a single **one-dimensional character array** of size 54.

-   **Variable**: `private char[] facelets`
-   **Size**: 54 (6 faces × 9 stickers per face)
-   **Mapping**: The array stores characters representing colors ('W', 'R', 'G', 'Y', 'O', 'B'). Code order is typically index 0-53.

The indices are mapped to faces as follows:

-   **0 - 8**: Up (U)
-   **9 - 17**: Right (R)
-   **18 - 26**: Front (F)
-   **27 - 35**: Down (D)
-   **36 - 44**: Left (L)
-   **45 - 53**: Back (B)

## 2. Rotation Algorithm (`CubeModel.java`)

Rotations are handled mathematically by permuting the indices of this array. A single face rotation (e.g., "U" move) consists of two distinct steps:

### A. Surface Rotation (`rotateFaceSurface`)
The 9 stickers on the face itself must rotate 90 degrees clockwise.
-   **Logic**: The elements at indices `0, 1, 2, 5, 8, 7, 6, 3` are shifted cyclically.
-   **Mapping**:
    -   Corner to Corner: `0 -> 2 -> 8 -> 6 -> 0`
    -   Edge to Edge: `1 -> 5 -> 7 -> 3 -> 1`
    -   Center (`4`) remains unchanged.

### B. Adjacent Layer Rotation (`rotateAdjacentLayers`)
The 12 stickers on the 4 adjacent faces that touch the rotating face must also shift cyclically.
-   **Data**: A 2D array `int[][] adj` defines the 4 sets of 3 indices that form the "ring" around the rotating face.
-   **Example (U Face)**:
    -   F Top Row (`18, 19, 20`) moves to L Top Row.
    -   L Top Row moves to B Top Row.
    -   B Top Row moves to R Top Row.
    -   R Top Row moves to F Top Row.
-   **Algorithm**:
    1.  Save the first set (`adj[0]`) into a temporary buffer.
    2.  Shift `adj[3]` into `adj[2]`, `adj[2]` into `adj[1]`, `adj[1]` into `adj[0]` (depending on rotation direction).
    3.  Restore temp into the last slot.

## 3. 3D Rendering & Projection Math (`Cube3DPanel.java`)

The application renders a 3D view using standard Java 2D (`Graphics2D`) by manually calculating 3D geometry and projecting it to 2D screen coordinates.

### A. Coordinate System
-   **Cubies**: The cube is constructed from 27 individual "cubie" blocks.
-   **Space**: Coordinates (x, y, z) range from -1 to 1.
    -   `x`: Left (-1) to Right (+1)
    -   `y`: Top (-1) to Bottom (+1)
    -   `z`: Back (-1) to Front (+1)

### B. Rotation Matrices (`project` method)
To simulate camera rotation, every 3D point `P(x,y,z)` is transformed using rotation matrices for **Pitch** (rotation around X-axis) and **Yaw** (rotation around Y-axis).

**1. Pitch Rotation (Around X):**
```
y' = y * cos(pitch) - z * sin(pitch)
z' = y * sin(pitch) + z * cos(pitch)
```

**2. Yaw Rotation (Around Y):**
```
x'' = x * cos(yaw) + z' * sin(yaw)
z'' = -x * sin(yaw) + z' * cos(yaw)
```

The final `(x'', y')` coordinates are scaled by a zoom factor (`scale`) and centered on the screen (`cx`, `cy`) to get the drawing coordinates.

### C. Occlusion: Painter's Algorithm
To ensure 3D depth is rendered correctly (front faces cover back faces), the application uses the **Painter's Algorithm**.
1.  All faces (quads) of all 27 cubies are generated.
2.  They are added to a list `List<FaceletQuad> quads`.
3.  The list is **sorted** by their transformed Z-depth (average Z coordinate after rotation).
4.  Quads are drawn in order from farthest (negative Z) to nearest (positive Z).

## 4. Animation Logic

Smooth animations are achieved by interpolating rotations over time.
-   **Timer**: A `javax.swing.Timer` ticks every 15ms.
-   **Interpolation**: `animProgress` goes from 0.0 to 1.0.
-   **Transform**: During animation, vertices of the moving cubies are transformed by an additional rotation matrix corresponding to the active move (e.g., rotating the top layer 90 degrees * `animProgress`).

## 5. Scramble Generation (`Scrambler.java`)

Random scrambles are generated to ensure a legal state.
-   **Algorithm**: Generates a sequence of 20 random moves.
-   **Constraint**: It checks `do-while (face == prevFace)` to prevent the same face from being turned twice in a row (e.g., "R R" is simplified to "R2", but for simplicity, we just pick a different face).
