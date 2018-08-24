package application;
	
import java.io.File;
import java.io.IOException;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class Main extends Application {
	public static final double SCREEN_WIDTH = 1000;
	public static final double SCREEN_HEIGHT = 720;
	public static final double SCREEN_BOTTOM = 140;
	public static final double SCREEN_EDGE = 10;
	public static final double BETWEEN_DISTANCE = 5;
	public static final String TRAP_STRING = "There is a trap nearby";
	public static final String CONDUIT_STRING = "There is a conduit nearby";
	public static final String MONSTER_STRING = "There is a monster nearby";
	public static final String CONDUIT_SHOT_STRING = "You disarmed a conduit";
	public static final String CONDUIT_DESTROYED_STRING = "You destroyed a conduit";
	public static final String ALL_CONDUITS_SHOT_STRING = "All conduits were destroyed";
	public static final String MONSTER_SHOT_INVULNERABLE_STRING = "The monster emits a low growl";
	public static final String MONSTER_SHOT_VULNERABLE_STRING = "The monster screams in pain";
	public static final String MONSTER_MAD_STRING = "The monster is angry";
	public static final String FOUND_AMMO_STRING = "You found a bullet";
	public static final String FOG_STRING = "A low fog rolls out over the ground";
	
	int xdest = 0;
	int ydest = 0;
	boolean shooting = false;
	
	boolean lost = false;
	boolean won = false;
	boolean alreadyMadeFile = false;
	long waitUntil = -1;
	boolean rulesOver = false;
	
	@Override
	public void start(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT, Color.web("#232323"));
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
			
			Text infoText = new Text(SCREEN_EDGE, SCREEN_HEIGHT - SCREEN_BOTTOM + 2 * SCREEN_EDGE, "test\ntest\ntest");
			infoText.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
			Text rulesText = new Text(SCREEN_EDGE * 2, SCREEN_EDGE * 4,
					"Disable and stomp on the conduits.\n"
					+ "Shoot the monster.\n"
					+ "Map out the safe territory.\n"
					+ "Use arrow keys to move.\n"
					+ "Use shift plus arrow key to shoot in direction");
			rulesText.setFont(Font.font("Verdana", FontWeight.BOLD, 25));
			rulesText.setFill(Color.WHITE);
			root.getChildren().add(infoText);
			
			Board board = new Board(8, 5, root, infoText);
			board.drawBoard();
			root.getChildren().add(rulesText);
			board.populate(3, 3, 3);
			board.updatePlayer(0, 0);
			
			scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case UP: ydest = -1; break;
					case DOWN: ydest = 1; break;
					case LEFT: xdest = -1; break;
					case RIGHT: xdest = 1; break;
					case SHIFT: shooting = true; break;
					default: break;
					}
				}
			});
			
			scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
				public void handle(KeyEvent event) {
					switch (event.getCode()) {
					case SHIFT: shooting = false; break;
					default: break;
					}
				}
			});
			AnimationTimer timer = new AnimationTimer() {
				public void handle(long now) {
					if ((xdest != 0 || ydest != 0) && !lost && !won) {
						if (!rulesOver) {
							root.getChildren().remove(rulesText);
							rulesOver = true;
						}
						if (!shooting) {
							lost = board.updatePlayer(xdest, ydest);
							if (lost && waitUntil < 0) {
								infoText.setFill(Color.RED);
								waitUntil = now + 1000000000;
							}
						} else {
							lost = board.shoot(xdest, ydest);
						}
						xdest = 0;
						ydest = 0;
					}
					if ((now > waitUntil) && lost) {
						Platform.exit();
						System.exit(0);
					}
					won = board.win();
					if (won) {
						if (!alreadyMadeFile) {
							File dir = new File("soul");
							dir.mkdirs();
							File tmp = new File(dir, "monster.soul");
							try {
								tmp.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							alreadyMadeFile = true;
						}
						infoText.setText("Email \"puppy monkey baby\" to dodo@wpi.edu to notify that you have won!\nBring the file monster.soul to Bancroft Tower at 4:00\non a flash drive to purify it.");
					}
				}
			};
			timer.start();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
