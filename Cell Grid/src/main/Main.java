package main;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * @version 1.15.1
 * @author RODRIGO TEIXEIRA
 *
 */

public class Main extends JFrame implements KeyListener, MouseListener {
	private static final long serialVersionUID = 1L;

	public static final int WINDOW_SIZE = 500;
	public static final int CELL_SIZE = 50;
	public static final int CELL_COUNT = WINDOW_SIZE / CELL_SIZE + 1;
	public static int MAP_SIZE = 100;
	@SuppressWarnings("unused")
	private static int tick = 0;
	/**
	 * Changes the FPS and TPS of the game.<br>
	 * <br>
	 * The physics will break if you use anything other than 60<br>
	 * (my monitor is 60 Hz anyways)
	 */
	public static final int FPS = 60; // 0x3c or 0b111100 or 0o74

	public static final String FILE_TYPE = ".png";
	public static final String SIZE_FILE_TYPE = ".size";

	public static final int OFFSET_X = 8;
	public static final int OFFSET_Y = 32;
	public static final int WINDOW_OFFSET_X = 16;
	public static final int WINDOW_OFFSET_Y = 40;

	private static final int DELAY = 1000 / FPS;
	private static final int SPACEMENT = 300 / FPS;
	private static final double SPACEMENT_Y = 60 / FPS;

	public static final Random rand = new Random();

	public static int PLAYER_X = 0;
	public static int PLAYER_Y = 0;

	public static int CAMERA_X = 0;

	public static int ANCHOR_X = Integer.MIN_VALUE;

	public static int checkpointX = 0;
	public static int checkpointY = 0;

	public static final int PLAYER_POS = (WINDOW_SIZE - CELL_SIZE) >>> 1;

	public static final int PLAYER_HITBOX_X = (int) (CELL_SIZE * 0.0d);
	public static final int PLAYER_HITBOX_Y = (int) (CELL_SIZE * 0.1d);
	public static final int PLAYER_HITBOX_WIDTH = (int) (CELL_SIZE * 0.6d);
	public static final int PLAYER_HITBOX_HEIGHT = (int) (CELL_SIZE * 0.9d);

	public static boolean CREATIVE_MODE = false;

	public static int LEVEL = 1;

	public static LinkedList<Cell> cells = new LinkedList<>();

	private static Image playerSprite;

	public static int[][] map; // index in the "tiles" array

	public static final String[] tileFileNames = { "black", // non-solid black
			"white", // solid white
			"white", // non-solid white (fake block)
			"spike", // spike
			"black", // invisible block
			"flag", // flag
			"checkpoint", // checkpoint
			"checkpoint 2", // collected checkpoint
			"black", // invisible spike
			"gelado", // GELADO (ice cream)
			"enemy", "enemy frame 1", "enemy frame 2", // enemy + frames 1 and 2,
			"checkpoint", // anchor block
			"checkpoint 2", // collected anchor
			"all", // block
			"mushroom", "mushroom left", "mushroom right", // mushroom sprite
			"health v3" // health bar sprite (boss fight)
	};

	public static final boolean[] solid = { false, true, false, false, true, false, false, false, false, false, false,
			false, false, false, false, true, false, false, false, false };
	public static final boolean[] deadly = { false, false, false, true, false, false, false, false, true, false, false,
			false, false, false, false, false, false, false, false, false };
	public static final boolean[] collectable = { false, false, false, false, false, true, true, false, false, true,
			false, false, false, true, false, false, false, false, false, false };
	public static Image[] tiles;

	public static final String[] guiFileNames = { "text down zoomed", "You Win", "low%", "100%", "creative%" };
	public static Image[] guis; // GUI images
	public static Point[] guiPoses; // GUI positions
	public static Dimension[] guiSizes; // GUI sizes
	public int currentGui = -1;
	private static int[] nextGui = { -1, 1, 2, 3, 4 };
	private static boolean[] isWinScreen = {false, true, true, true, true};

	public static final char[] fontChars = "0123456789-".toCharArray();
	public static Image[] fontImages;

	public static LinkedList<Entity> entities = new LinkedList<>();

	private static final int LOW_PERCENT_MAX_AMOUNT = 36;
	private static final int ONE_HUNDRED_PERCENT_MIN_AMOUNT = 81;

	private int wPressed = 0;
	private int aPressed = 0;
	private int sPressed = 0;
	private int dPressed = 0;

	private int leftPressed = 0;
	private int upPressed = 0;
	private int rightPressed = 0;
	private int downPressed = 0;

	private int collectedGelados = 0;
	private boolean enteredCreative = false;
	
	private static double dy = 0;

	private static final int DEFAULT_HP = 5;
	private static int hp = DEFAULT_HP;

	private static int invulnerable = 0;

	public Main() {

		if (tileFileNames.length != solid.length || solid.length != deadly.length
				|| solid.length != collectable.length) {
			System.out.println(tileFileNames.length);
			System.out.println(solid.length);
			System.out.println(deadly.length);
			System.out.println(collectable.length);
			throw new RuntimeException("assert equal size failed");
		}

		setupSprites();
		setupWindow();
		setupCells();
		setupMap();
		reloadLevel();

		long time;

		while (true) {
			time = System.currentTimeMillis();
			if (currentGui < 0)
				playerTick();
			tickCells();
			if (currentGui < 0 && !CREATIVE_MODE) {
				tickEntities();
				checkDeath();
				checkColectables();
			}
			// repaint();
			paint(getGraphics());
			try {
				long timeOut = DELAY - System.currentTimeMillis() + time;
				if (timeOut > 0)
					Thread.sleep(timeOut);
			} catch (InterruptedException e) {
				System.out.println("ERROR WHILE WAITING BEETWEEN FRAMES");
				e.printStackTrace();
				System.exit(1);
			}
			tick++;
		}
	}

	private void tickEntities() {
		for (Object entity : entities) {
			((Entity) entity).tick();
		}
	}

	private void checkColectables() {
		for (Cell cell : cells) {
			if (!collectable[map[cell.idxX][cell.idxY]])
				continue;
			if (cell.checkFor(PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS, Cell.CHECKPOINT)) {
				if ((remainingGelados(cell.idxX)))
					return;
				checkpointX = cell.idxX * CELL_SIZE - PLAYER_POS;
				checkpointY = cell.idxY * CELL_SIZE - PLAYER_POS;
				map[cell.idxX][cell.idxY] = Cell.COLLECTED_CHECKPOINT;
				return;
			}
			if (cell.checkFor(PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS, Cell.FLAG)) {
				checkpointX = 0;
				checkpointY = 0;
				LEVEL++;
				reloadLevel();
				return;
			}
			if (cell.checkFor(PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS, Cell.GELADO)) {
				map[cell.idxX][cell.idxY] = Cell.BLACK;
				collectedGelados++;
				return;
			}
			if (cell.checkFor(PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS, Cell.ANCHOR)) {
				checkpointX = (cell.idxX + 1) * CELL_SIZE - PLAYER_POS;
				checkpointY = (PLAYER_Y - PLAYER_Y % CELL_SIZE) + (CELL_SIZE >>> 1);
				map[cell.idxX][cell.idxY] = Cell.COLLECTED_ANCHOR;
				ANCHOR_X = PLAYER_X + PLAYER_POS + CELL_SIZE;
				PLAYER_X += CELL_SIZE << 1;
				return;
			}
		}

	}

	private void reloadLevel() {
		load("src\\levels\\level " + LEVEL + ".save");
	}

	private void checkDeath() {
		for (Cell cell : cells) {
			if (cell.deadColliding(PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS)) {
				respawn();
				return;
			}
		}
		for (Entity entity : entities) {
			if (PLAYER_X + PLAYER_HITBOX_X + PLAYER_HITBOX_WIDTH + PLAYER_POS < entity.x) {
				continue;
			}
			if (PLAYER_Y + PLAYER_HITBOX_Y + PLAYER_HITBOX_WIDTH + PLAYER_POS < entity.y) {
				continue;
			}
			if (PLAYER_X + PLAYER_POS > entity.x + entity.w) {
				continue;
			}
			if (PLAYER_Y + PLAYER_POS > entity.y + entity.h) {
				continue;
			}

			if (PLAYER_Y + PLAYER_POS + PLAYER_HITBOX_HEIGHT + PLAYER_HITBOX_Y > entity.y && dy > 1) {
				entity.stepedByPlayer();
			} else {
				entity.touchedByPlayer();
			}
		}

	}

	private void tickCells() {
		for (Cell cell : cells) {
			cell.tick();
		}
	}

	private void setupSprites() {
		try {
			System.out.println("src\\main\\Sprite v2" + FILE_TYPE);
			playerSprite = ImageIO.read(new File("src\\main\\Sprite v2" + FILE_TYPE));
		} catch (FileNotFoundException fnfe) {
			System.out.println("ERROR WHILE IMPLORTING PLAYER SPRITE -> FILE DOEN'T EXIST !!!");
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("ERROR IMPORTING PLAYER SPRITE");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			tiles = new Image[tileFileNames.length];
			for (int i = 0; i < tiles.length; i++) {
				System.out.println("textures\\" + tileFileNames[i] + FILE_TYPE);
				tiles[i] = ImageIO.read(new File("textures\\" + tileFileNames[i] + FILE_TYPE));
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("ERROR WHILE IMPLORTING TILE SPRITES -> FILE DOEN'T EXIST !!!");
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("ERROR IMPORTING TILE SPRITES");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			guis = new Image[guiFileNames.length];
			for (int i = 0; i < guis.length; i++) {
				System.out.println("guis\\" + guiFileNames[i] + FILE_TYPE);
				guis[i] = ImageIO.read(new File("src\\guis\\" + guiFileNames[i] + FILE_TYPE));
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("ERROR WHILE IMPLORTING GUI SPRITES -> FILE DOEN'T EXIST !!!");
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("ERROR IMPORTING GUI SPRITES");
			e.printStackTrace();
			System.exit(1);
		}
		try {
			guiSizes = new Dimension[guiFileNames.length];
			guiPoses = new Point[guiFileNames.length];
			for (int i = 0; i < guis.length; i++) {
				System.out.println("guis\\" + guiFileNames[i] + SIZE_FILE_TYPE);
				FileInputStream in = new FileInputStream("src\\guis\\" + guiFileNames[i] + SIZE_FILE_TYPE);
				byte[] bytes = in.readNBytes(8);
				int[] ints = new int[bytes.length];
				for (int j = 0; j < bytes.length; j++) {
					ints[j] = ((int) bytes[j]) & 0xff;
					System.out.println("ints[" + j + "] = " + ints[j] + ";");
				}
				in.close();
				guiSizes[i] = new Dimension(ints[0] * 256 + ints[1], ints[2] * 256 + ints[3]);
				guiPoses[i] = new Point(ints[4] * 256 + ints[5] + OFFSET_X, ints[6] * 256 + ints[7] + OFFSET_Y);
				System.out.println("pos[" + i + "] = " + guiPoses[i] + ";size[" + i + "] = " + guiSizes[i] + ";");
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("ERROR WHILE IMPLORTING GUI SIZES -> FILE DOEN'T EXIST !!!");
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			System.out.println("ERROR IMPORTING GUI SIZES");
			e.printStackTrace();
			System.exit(1);
		}
		int i = 0;
		try {
			fontImages = new Image[fontChars.length];
			while (i < fontChars.length) {
				String local = "font\\" + fontChars[i] + FILE_TYPE;
				System.out.println(local);
				fontImages[i++] = ImageIO.read(new File(local));
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println(
					"ERROR WHILE IMPORTING FONT -> FILE font\\" + fontChars[i] + FILE_TYPE + " DOESN'T EXIST !!!");
			fnfe.printStackTrace();
			System.exit(1);
		} catch (IOException ioe) {
			System.out.println("ERROR WHILE IMPORTING FONT");
			ioe.printStackTrace();
			System.exit(1);
		}
	}

	private void setupMap() {
		map = new int[MAP_SIZE][MAP_SIZE];
		for (int i = 0; i < MAP_SIZE; i++) {
			for (int j = 0; j < MAP_SIZE; j++) {
				map[i][j] = Cell.BLACK;
			}
		}

	}

	private void playerTick() {
		int up = wPressed | upPressed;
		int left = aPressed | leftPressed;
		int down = sPressed | downPressed;
		int right = dPressed | rightPressed;

		int dx = SPACEMENT * (right - left);
		PLAYER_X += dx;

		if (CREATIVE_MODE) {
			PLAYER_Y += SPACEMENT * (down - up);
			CAMERA_X = PLAYER_X;
			return;
		}

		if (colliding()) {
			PLAYER_X -= dx;
		}
		PLAYER_Y += dy * SPACEMENT_Y;
		if (colliding()) {
			if (dy >= 0) {
				PLAYER_Y -= PLAYER_Y % CELL_SIZE - (CELL_SIZE >>> 1) + 1;
				if (colliding())
					PLAYER_Y -= CELL_SIZE;
				if (up == 1) {
					dy = -14;
				} else {
					dy = -1;
				}
			} else {
				PLAYER_Y -= dy * SPACEMENT_Y;
				dy = -SPACEMENT_Y;
			}
		}
		dy += SPACEMENT_Y;
		if (dy > 20)
			dy = 20; // TERMINAL VELOCITY
		CAMERA_X = PLAYER_X;
		if (ANCHOR_X > Integer.MIN_VALUE) {
			CAMERA_X = ANCHOR_X;
		}
		if (CAMERA_X > PLAYER_X + PLAYER_POS + PLAYER_HITBOX_X) {
			CAMERA_X = PLAYER_X + PLAYER_POS + PLAYER_HITBOX_X;
		}

		invulnerable--;
		if (invulnerable < 0) {
			invulnerable = 0;
		}

	}

	private boolean colliding() {
		for (Cell cell : cells) {
			if (cell.colliding(PLAYER_POS + PLAYER_HITBOX_X + PLAYER_X - CAMERA_X, PLAYER_POS + PLAYER_HITBOX_Y,
					PLAYER_HITBOX_WIDTH, PLAYER_HITBOX_HEIGHT)) {
				return true;
			}
		}
		return false;
	}

	private void setupWindow() {
		setSize(WINDOW_SIZE + WINDOW_OFFSET_X, WINDOW_SIZE + WINDOW_OFFSET_Y);
		setTitle("SCOLLER");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setIconImage(playerSprite);
		addKeyListener(this);
		addMouseListener(this);
		setVisible(true);
	}

	private void setupCells() {
		int x = 0;
		for (int i = 0; i < CELL_COUNT; i++) {
			int y = 0;
			for (int j = 0; j < CELL_COUNT; j++) {
				cells.add(new Cell(x, y, i, j));
				y += CELL_SIZE;
			}
			x += CELL_SIZE;
		}
	}

	public static void main(String[] args) {
		new Main();
	}

	public void paint(Graphics g) {
		BufferedImage bufferImage = new BufferedImage(WINDOW_SIZE, WINDOW_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics buffer = bufferImage.getGraphics();
		buffer.clearRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
		for (Cell cell : cells) {
			cell.showBorder(buffer);
		}
		for (Cell cell : cells) {
			cell.show(buffer);
		}
		for (Entity entity : entities) {
			entity.show(buffer);
		}
		if (invulnerable % 10 < 5)
			buffer.drawImage(playerSprite, PLAYER_POS + PLAYER_X - CAMERA_X, PLAYER_POS, CELL_SIZE, CELL_SIZE, null);

		if (currentGui >= 0) {
			buffer.drawImage(guis[currentGui], guiPoses[currentGui].x, guiPoses[currentGui].y,
					guiSizes[currentGui].width, guiSizes[currentGui].height, null);
			if(isWinScreen[currentGui]) {
				writeNumber(buffer, (int) ((double)tick/60.0d), CELL_SIZE*String.valueOf((int) ((double)tick/60.0d)).length(), 0, CELL_SIZE, CELL_SIZE);
			}
		}
		if (ANCHOR_X > Integer.MIN_VALUE) {
			final int spacement = 5;
			final int size = CELL_SIZE >>> 1;
			for (int i = 0; i < hp; i++) {
				buffer.drawImage(tiles[Cell.HEALTH], i * size + spacement, spacement, size, size, null);
			}
		}

		writeNumber(buffer, collectedGelados, WINDOW_SIZE, 0, CELL_SIZE, CELL_SIZE);
		
		g.drawImage(bufferImage, OFFSET_X, OFFSET_Y, null);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case 37:
			leftPressed = 1;
			break;
		case 38:
			upPressed = 1;
			break;
		case 39:
			rightPressed = 1;
			break;
		case 40:
			downPressed = 1;
			break;
		case 87: // w
			wPressed = 1;
			break;
		case 65: // a
			aPressed = 1;
			break;
		case 83: // s
			sPressed = 1;
			break;
		case 68: // d
			dPressed = 1;
			break;
		case 71: // g -> save
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("save.save"));
				out.writeObject(map);
				out.close();
			} catch (IOException fnfe) {
				System.out.println("ERROR SAVING");
				fnfe.printStackTrace();
				System.exit(1);
			}
			break;
		case 76: // l -> load
			checkpointX = 0; // reset checkpoints
			checkpointY = 0;
			ANCHOR_X = Integer.MIN_VALUE;
			load("save.save");
			break;
		case 82: // r -> re-spawn / reload
			respawn();
			ANCHOR_X = Integer.MIN_VALUE;
			break;
		case 80: // DEBUG p
			System.out.println("[DEBUG KEY PRESSED]");
			for (Cell cell : cells) {
				System.out.println("[DEBUG] x: " + (cell.x - PLAYER_X) + " y: " + (cell.y - PLAYER_Y) + " idxX: "
						+ cell.idxX + " idxY: " + cell.idxY);
			}
			break;
		case 67: // DEBUG c
			CREATIVE_MODE = !CREATIVE_MODE;
			enteredCreative = true;
			dy = 0;
			break;
		case 78: // DEBUG n
			checkpointX = PLAYER_X;
			checkpointY = PLAYER_Y;
			break;
		case 88: // DEBUG x
			Scanner scan = new Scanner(System.in);
			System.out.println("What level to load?? ");
			String str = scan.nextLine();
			if (!str.endsWith(".save"))
				str += ".save";
			load(str);
			break;
		case 27: // ESC
			System.exit(0);
		case 32: // Space
			currentGui = currentGui == -1 ? -1 : nextGui[currentGui];
			break;
		}

	}

	private void load(String fileName) {
		System.out.println("trying to load " + fileName);
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(fileName));
			map = (int[][]) in.readObject();
			MAP_SIZE = map.length;
			assert map.length == map[0].length;
			in.close();
		} catch (FileNotFoundException fnfe) {
			System.err.print("failed");
			System.out.println(" to open file \"" + fileName + "\"\nso YOU WIN GUI will be openned.");
			currentGui = enteredCreative? 4: collectedGelados <= LOW_PERCENT_MAX_AMOUNT ? 2
					: collectedGelados >= ONE_HUNDRED_PERCENT_MIN_AMOUNT ? 3 : 1;
		} catch (IOException | ClassNotFoundException | ClassCastException ioe) {
			System.out.println("ERROR LOADING LEVEL");
			ioe.printStackTrace();
			System.exit(1);
		}
		// collectedGelados = 0;
		respawn();

	}

	@Override
	public void keyReleased(KeyEvent e) {
		switch (e.getKeyCode()) {
		case 37:
			leftPressed = 0;
			break;
		case 38:
			upPressed = 0;
			break;
		case 39:
			rightPressed = 0;
			break;
		case 40:
			downPressed = 0;
			break;
		case 87: // w
			wPressed = 0;
			break;
		case 65: // a
			aPressed = 0;
			break;
		case 83: // s
			sPressed = 0;
			break;
		case 68: // d
			dPressed = 0;
			break;
		}

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX() - OFFSET_X;
		int y = e.getY() - OFFSET_Y;
		for (Cell cell : cells) {
			cell.mousePressed(x, y, e.isControlDown());
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	public static void die() {
		if (invulnerable > 0) {
			return;
		}
		hp--;
		invulnerable = FPS;
		if (hp > 0) {
			return;
		}
		respawn(false);
	}

	private static void respawn() {
		respawn(true);
	}

	private static void respawn(boolean clearEntities) {
		PLAYER_X = checkpointX;
		PLAYER_Y = checkpointY;
		dy = -SPACEMENT_Y;
		hp = DEFAULT_HP;
		invulnerable = 0;
		for (Cell cell : cells) {
			cell.respawn();
		}
		if (clearEntities) {
			entities.clear();
		} else {
			for (Entity entity : entities) {
				entity.hp = entity.DEFAULT_HP;
			}
		}
	}

	private boolean remainingGelados(int x) {
		for (int i = 0; i <= x; i++) {
			for (int j = 0; j < MAP_SIZE; j++) {
				if (map[i][j] == Cell.GELADO) {
					return true;
				} else if (map[i][j] == Cell.CHECKPOINT) {
					return false;
				}
			}
		}
		return false;

	}

	public void writeNumber(Graphics g, int n, int x, int y, int w, int h) {
		char[] chars = Integer.toString(n).toCharArray();
		x -= w * chars.length;
		int i = 0;
		if (n < 0) {
			g.drawImage(fontImages[fontImages.length - 1], x, y, w, h, null); // "-" sign is in the end of the array.
			x += w;
			i = 1;
		}
		while (i < chars.length) {
			g.drawImage(fontImages[(int) chars[i] - 48], x, y, w, h, null); // 48 is the ASCII number for '0'
			x += w;
			i++;
		}
	}

}
