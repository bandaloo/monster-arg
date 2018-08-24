package application;

import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.Random;
import java.lang.Math;

public class Board {
	Random random = new Random();
	int[] xcoords = {1, 1, 0, -1, -1, -1, 0, 1};
	int[] ycoords = {0, -1, -1, -1, 0, 1, 1, 1};
	double blockWidth;
	double blockHeight;
	int[][] boardArray;
	Rectangle[][] blockArray;
	int playerX;
	int playerY;
	int monsterX;
	int monsterY;
	int width;
	int height;
	Group blocks = new Group();
	Circle playerShape = new Circle(10);
	BorderPane root;
	Text infoText;
	String infoTextString = "";
	int numConduits;
	boolean fog = false;
	int monsterStun = 0;
	int ammo = 10000;
	boolean win = false;
	int unfogCount = 0;
	int unfogGoal;
	Color nothingColor= Color.web("#232323");
	Color trapColor = Color.web("#5d37a8");
	Color conduitColor = Color.web("#d89427");
	Color unfogColor = Color.web("#2bdb92");
	Color monsterBloodColor = Color.web("#871e0c");
	
	Color destroyedColor = Color.web("#d6a431");
	
	Stop[] fogStops = new Stop[] {new Stop(0, Color.web("#dbdbdb")), new Stop(1, Color.web("#878787"))};
	LinearGradient fogGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, fogStops);
	
	public Board(int width, int height, BorderPane root, Text infoText) {
		this.root = root;
		this.infoText = infoText;
		boardArray = new int[width][height];
		blockArray = new Rectangle[width][height];
		this.width = width;
		this.height = height;
		playerX = width / 2;
		playerY = height / 2;
		blockWidth = (Main.SCREEN_WIDTH - Main.SCREEN_EDGE * 2) / width;
		blockHeight = (Main.SCREEN_HEIGHT - Main.SCREEN_BOTTOM - Main.SCREEN_EDGE * 2) / height;
		playerShape.setFill(Color.web("red"));
		root.getChildren().add(blocks);
		root.getChildren().add(playerShape);
	}
	
	public boolean numNear(int num) {
		for (int i = 0; i < 8; i++) {
			if (isInside(xcoords[i], ycoords[i], playerX, playerY)) {
				if (boardArray[xcoords[i] + playerX][ycoords[i] + playerY] == num) {
					switch (num) {
					case 1:
						if (!fog) {
							infoTextString += Main.TRAP_STRING + "\n";
						}
						break;
					case 2:
						infoTextString += Main.CONDUIT_STRING + "\n";
						break;
					default:
						break;
					}
					return true;
				}
			}
		}
		return false;
	}
	
	public void populate(int numTraps, int numConduits, int numBullets) {
		do {
			monsterX = random.nextInt(width);
			monsterY = random.nextInt(height);
		} while (monsterX != playerX && monsterY != playerY);
		
		populateNum(1, numTraps);
		populateNum(2, numConduits);
		populateNum(3, numBullets);
		this.numConduits = numConduits;
		unfogGoal = width * height - numTraps - 2;
		
		// print board contents for testing
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				System.out.print(boardArray[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public void drawBoard() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double blockX = Main.SCREEN_EDGE + i * blockWidth + 0.5 * Main.BETWEEN_DISTANCE;
				double blockY = Main.SCREEN_EDGE + j * blockHeight + 0.5 * Main.BETWEEN_DISTANCE;
				Rectangle blockRectangle = new Rectangle(blockX, blockY, blockWidth - Main.BETWEEN_DISTANCE, blockHeight - Main.BETWEEN_DISTANCE);
				blockArray[i][j] = blockRectangle;
				blocks.getChildren().add(blockRectangle);
			}
		}
	}
	
	public boolean updatePlayer(int xdest, int ydest) {
		if (isInside(xdest, ydest, playerX, playerY)) {
			boolean lost = false;
			infoTextString = "";
			playerX += xdest;
			playerY += ydest;
			if (fog && boardArray[playerX][playerY] != 1 && boardArray[playerX][playerY] != -1) {
				blockArray[playerX][playerY].setFill(unfogColor);
				unfogCount++;
				boardArray[playerX][playerY] = -1;
				System.out.println(unfogCount);
				if (unfogCount == unfogGoal) {
					win = true;
				}
			}
			
			switch (boardArray[playerX][playerY]) {
			case 1:
				System.out.println("trapped");
				infoTextString = "YOU HAVE FALLEN ONTO A TRAP" + "\n";
				infoText.setText(infoTextString);
				lost = true;
				break;
			case 2:
				System.out.println("conduited");
				infoTextString = "YOU HAVE BEEN DESTROYED BY A CONDUIT" + "\n";
				infoText.setText(infoTextString);
				lost = true;
				break;
			case 3:
				//System.out.println("ammo");
				//infoTextString += "You picked up a bullet";
				boardArray[playerX][playerY] = 0;
				break;
			case 4:
				System.out.println("conduit destroyed");
				infoTextString += Main.CONDUIT_DESTROYED_STRING + "\n";
				numConduits--;
				boardArray[playerX][playerY] = 0;
				System.out.println(numConduits);
				if (numConduits == 0) {
					infoTextString += Main.ALL_CONDUITS_SHOT_STRING + "\n";
				}
				break;
			default:
				break;
			}
			
			double playerXPos = Main.SCREEN_EDGE + playerX * blockWidth + 0.5 * blockWidth;
			double playerYPos = Main.SCREEN_EDGE + playerY * blockHeight + 0.5 * blockHeight;
			
			playerShape.setCenterX(playerXPos);
			playerShape.setCenterY(playerYPos);
			
			if (lost) return true;

			Color color1 = nothingColor;
			Color color2 = nothingColor;
			
			if (numNear(1)) color1 = trapColor;
			if (numNear(2)) color2 = conduitColor;
			
			Stop[] stops = new Stop[] {new Stop(0, color1), new Stop(1, color2)};
			LinearGradient gradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
			if (!fog) blockArray[playerX][playerY].setFill(gradient);
			
			if (Math.abs(monsterX - playerX) <= 1 && Math.abs(monsterY - playerY) <= 1) {
				infoTextString += Main.MONSTER_STRING + "\n";
			}
			if (endMove() == true) return true;
		}
		return false;
	}
	public boolean shoot(int xdest, int ydest) {
		if (ammo == 0) {
			infoText.setText("Out of bullets\n");
			return false;
		}
		//ammo--;
		if (isInside(xdest, ydest, playerX, playerY)) {
			infoTextString = "";
			if (boardArray[playerX + xdest][playerY + ydest] == 2) {
				boardArray[playerX + xdest][playerY + ydest] = 4;
				infoTextString += Main.CONDUIT_SHOT_STRING + "\n";
				blockArray[playerX + xdest][playerY + ydest].setFill(destroyedColor);
			}
			int xmove = xdest;
			int ymove = ydest;
			
			while (isInside(xmove, ymove, playerX, playerY)) {
				if (playerX + xmove == monsterX && playerY + ymove == monsterY) {
					if (numConduits == 0) {
						infoTextString += Main.MONSTER_SHOT_VULNERABLE_STRING + "\n";
						if (!fog) {
							infoTextString += Main.MONSTER_MAD_STRING + "\n";
							infoTextString += Main.FOG_STRING + "\n";
							fog = true;
							fogOut();
						} else {
							monsterStun = 15;
						}
						blockArray[monsterX][monsterY].setFill(monsterBloodColor);
					} else {
						infoTextString += Main.MONSTER_SHOT_INVULNERABLE_STRING + "\n";
					}
				}
				xmove += xdest;
				ymove += ydest;
			}
			return endMove();
		}
		return false;
	}
	
	public boolean isInside(int dx, int dy, int x, int y) {
		return (dx + x < width) && (dx + x >= 0) && (dy + y < height) && (dy + y >= 0);
	}
	
	private void populateNum(int num, int amount) {
		for (int i = 0; i < amount; i++) {
			int randomX = random.nextInt(width);
			int randomY = random.nextInt(height);
			if (boardArray[randomX][randomY] == 0 && !(randomX == playerX && randomY == playerY)) {
				boardArray[randomX][randomY] = num;
			} else i--;
		}
	}
	
	private void updateMonster() {
		// monster is stunned
		if (monsterStun > 0) {
			monsterStun--;
			return;
		}
		// monster sits still when all conduits are first destroyed
		if (numConduits == 0 && !fog) return;
		int xdest, ydest;
		// if the fog is out follow the player
		if (fog) {
			if (playerX > monsterX) xdest = 1;
			else if (playerX < monsterX) xdest = -1;
			else xdest = 0;
			if (playerY > monsterY) ydest = 1;
			else if (playerY < monsterY) ydest = -1;
			else ydest = 0;
		} else {
			xdest = random.nextInt(3) - 1;
			ydest = random.nextInt(3) - 1;
		}
		// will avoid stepping into player when fog is false
		if (isInside(xdest, ydest, monsterX, monsterY) && (!(monsterX + xdest == playerX && monsterY + ydest == playerY) || fog)) {
			monsterX += xdest;
			monsterY += ydest;
		}
		System.out.println("Monster position: " + monsterX + " " + monsterY);
	}
	
	private boolean endMove() {
		infoText.setText(infoTextString);
		updateMonster();
		// monster moves twice when the fog is out
		//if (fog) updateMonster();
		if (playerX == monsterX && playerY == monsterY) {
			infoTextString = "YOU BEEN DEVOURED BY THE MONSTER" + "\n";
			infoText.setText(infoTextString);
			return true;
		}
		return false;
	}
	
	private void fogOut() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				blockArray[i][j].setFill(fogGradient);
			}
		}
	}
	
	public boolean win() {
		return win;
	}
}
