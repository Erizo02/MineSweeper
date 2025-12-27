package com.javarush.games.minesweeper;


import com.javarush.engine.cell.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MinesweeperGame extends Game {

    private static final String MINE = "\uD83D\uDCA3";
    private static final String FLAG = "\uD83D\uDEA9";

    @Override
    public void initialize() {
        difficultySelector();
        setScreenSize(SIDE, SIDE);
        createGame();
    }

    @Override
    public void onMouseLeftClick(int x, int y) {
        if (isGameStopped) {
            restart();
        } else {
            openTile(x, y);
        }
    }

    @Override
    public void onMouseRightClick(int x, int y) {
        markTile(x, y);
    }

    private static int SIDE;
    private GameObject[][] gameField;
    private int countMinesOnField;
    private int countFlags;
    private boolean isGameStopped;
    private int countClosedTiles;
    private int score;
    private int percentage;

    private void createGame() {
        gameField = new GameObject[SIDE][SIDE];
        countClosedTiles = SIDE * SIDE;

        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                setCellValue(y, x, "");

                int randomNumber = getRandomNumber(10);
                boolean isMine = randomNumber < percentage;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
                setCellColor(x, y, Color.GREEN);
            }
        }
        countMineNeighbors();
        countFlags = countMinesOnField;
    }

    private List<GameObject> getNeighbors(GameObject gameObject) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void countMineNeighbors() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                GameObject gameObject = gameField[y][x];
                if (!gameObject.isMine) {
                    for (GameObject neighbor : getNeighbors(gameObject)) {
                        if (neighbor.isMine) {
                            gameObject.countMineNeighbors++;
                        }
                    }
                }
            }
        }
    }

    private void openTile(int x, int y) {
        GameObject gameObject = gameField[y][x];

        if (gameObject.isOpen || gameObject.isFlag || isGameStopped) {
            return;
        }

        if (!gameObject.isOpen) {
            gameObject.isOpen = true;
            countClosedTiles--;
            if (!gameObject.isMine) {
                score += 5;
            }
            setScore(score);
        }

        gameObject.isOpen = true;
        setCellColor(gameObject.x, gameObject.y, Color.YELLOW);

        if (gameObject.isMine) {
            setCellValue(gameObject.x, gameObject.y, MINE);
            setCellValueEx(gameObject.x, gameObject.y, Color.RED, MINE);
            gameOver();
        } else if (gameObject.countMineNeighbors == 0) {
            setCellValue(gameObject.x, gameObject.y, "");
            for (GameObject neighbor : getNeighbors(gameObject)) {
                if (!neighbor.isOpen) {
                    openTile(neighbor.x, neighbor.y);
                }
            }
        } else {
            setCellNumber(gameObject.x, gameObject.y, gameObject.countMineNeighbors);
        }

        if (countClosedTiles == countMinesOnField && !gameObject.isMine) {
            win();
        }
    }

    private void markTile(int x, int y) {
        GameObject gameObject = gameField[y][x];

        if (gameObject.isOpen || countFlags == 0 && !gameObject.isFlag || isGameStopped == true) {
            return;
        }
        if (!gameObject.isFlag) {
            gameObject.isFlag = true;
            countFlags--;
            setCellValue(gameObject.x, gameObject.y, FLAG);
            setCellColor(gameObject.x, gameObject.y, Color.ORANGE);
        } else {
            gameObject.isFlag = false;
            countFlags++;
            setCellValue(gameObject.x, gameObject.y, "");
            setCellColor(gameObject.x, gameObject.y, Color.GREEN);
        }
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.WHITE, "GAME OVER", Color.RED, 50);
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(Color.WHITE, "YOU WON", Color.LIGHTGREEN, 50);
    }

    private void restart() {
        isGameStopped = false;
        countClosedTiles = SIDE * SIDE;
        score = 0;
        countMinesOnField = 0;
        setScore(score);

        createGame();
    }

    private void difficultySelector() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select Your Game Difficulty:");
        int fieldSize = 0;

        while (fieldSize <= 0) {
            System.out.print("Field Size (x by x): ");
            fieldSize = scanner.nextInt();

            if (fieldSize <= 0) {
                System.out.println("Please Input A Whole Positive Number");
            }
        }
        SIDE = fieldSize;

        int mines = 0;
        while (mines <= 0 || mines > 9) {
            System.out.print("Percentage Of Mines On Field (1 = 10%, 2 = 20% ... 9 = 90%: ");
            mines = scanner.nextInt();

            if (mines <= 0 || mines > 9) {
                System.out.println("Please Input A Number Between 1 And 9");
            }
        }
        percentage = mines;
    }
}