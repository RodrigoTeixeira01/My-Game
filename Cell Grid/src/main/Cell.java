package main;

import java.awt.Color;
import java.awt.Graphics;

public class Cell {

	public static boolean SHOW_HITBOXES = false;
	public static int BLACK = 0;
	public static int WHITE = 1;
	public static int FAKE = 2;
	public static int SPIKE = 3;
	public static int INVISIBLE = 4;
	public static int FLAG = 5;
	public static int CHECKPOINT = 6;
	public static int COLLECTED_CHECKPOINT = 7;
	public static int INVISIBLE_SPIKE = 8;
	public static int GELADO = 9;
	public static int ENEMY = 10;
	public static int ANCHOR = 13;
	public static int COLLECTED_ANCHOR = 14;
	public static int BORDER_BLOCK = 15;
	public static int MUSHROOM = 16;
	public static int HEALTH = 19;
	// COMING SOON :)

	int x;
	int y;
	int idxX;
	int idxY;

	public Cell(int x, int y, int idxX, int idxY) {
		this.x = x;
		this.y = y;
		this.idxX = idxX;
		this.idxY = idxY;
	}

	public void showBorder(Graphics g) {
		if (Main.tileFileNames[Main.map[idxX][idxY]] == Main.tileFileNames[WHITE]) {
			g.drawImage(Main.tiles[BORDER_BLOCK], x - Main.CAMERA_X - (Main.CELL_SIZE >>> 3),
					y - Main.PLAYER_Y - (Main.CELL_SIZE >>> 3), Main.CELL_SIZE + (Main.CELL_SIZE >>> 2),
					Main.CELL_SIZE + (Main.CELL_SIZE >>> 2), null);
		}
	}

	public void show(Graphics g) {
		if (Main.tileFileNames[Main.map[idxX][idxY]] != Main.tileFileNames[BLACK]) {
			g.drawImage(Main.tiles[Main.map[idxX][idxY]], x - Main.CAMERA_X, y - Main.PLAYER_Y, Main.CELL_SIZE,
					Main.CELL_SIZE, null);
		}

		// HITBOX :)
		if(SHOW_HITBOXES)
		if (Main.deadly[Main.map[idxX][idxY]]) {
			g.setColor(Color.RED);
			g.drawRect(x - Main.CAMERA_X, y - Main.PLAYER_Y + (Main.CELL_SIZE >>> 1), Main.CELL_SIZE,
					Main.CELL_SIZE >>> 1);
		} else if (Main.solid[Main.map[idxX][idxY]]) {
			g.setColor(Color.BLUE);
			g.drawRect(x - Main.CAMERA_X, y - Main.PLAYER_Y, Main.CELL_SIZE, Main.CELL_SIZE);
		} else if (Main.collectable[Main.map[idxX][idxY]]) {
			g.setColor(Color.GREEN);
			g.drawRect(x - Main.CAMERA_X, y - Main.PLAYER_Y, Main.CELL_SIZE, Main.CELL_SIZE);
		}
	}

	public void tick() {
		int x = this.x - Main.CAMERA_X;
		int y = this.y - Main.PLAYER_Y;
		if (x < (0 - Main.CELL_SIZE)) {
			this.x += Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxX += Main.CELL_COUNT;
		} else if (x > Main.WINDOW_SIZE) {
			this.x -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxX -= Main.CELL_COUNT;
		}
		if (y < (0 - Main.CELL_SIZE)) {
			this.y += Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxY += Main.CELL_COUNT;
		} else if (y > Main.WINDOW_SIZE) {
			this.y -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxY -= Main.CELL_COUNT;
		}

		if (idxX < 0) {
			idxX += Main.MAP_SIZE;
		} else if (idxX >= Main.MAP_SIZE) {
			idxX -= Main.MAP_SIZE;
		}

		if (idxY < 0) {
			idxY += Main.MAP_SIZE;
		} else if (idxY >= Main.MAP_SIZE) {
			idxY -= Main.MAP_SIZE;
		}

		if (Main.CREATIVE_MODE)
			return;

		if (Main.map[idxX][idxY] == ENEMY) {
			Main.map[idxX][idxY] = BLACK;
			Main.entities.add(new Boss(this.x, this.y, this.idxX, this.idxY));
		} else if (Main.map[idxX][idxY] == MUSHROOM) {
			Main.map[idxX][idxY] = BLACK;
			Main.entities.add(new Mushroom(this.x, this.y, this.idxX, this.idxY));
		}

	}

	public void respawn() {
		int x = this.x - Main.CAMERA_X;
		int y = this.y - Main.PLAYER_Y;
		while (x < (0 - Main.CELL_SIZE)) {
			this.x += Main.WINDOW_SIZE + Main.CELL_SIZE;
			x += Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxX += Main.CELL_COUNT;
		}
		while (x > Main.WINDOW_SIZE + Main.CELL_SIZE) {
			this.x -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			x -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxX -= Main.CELL_COUNT;
		}
		while (y < (0 - Main.CELL_SIZE)) {
			this.y += Main.WINDOW_SIZE + Main.CELL_SIZE;
			y += Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxY += Main.CELL_COUNT;
		}
		while (y > Main.WINDOW_SIZE + Main.CELL_SIZE) {
			this.y -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			y -= Main.WINDOW_SIZE + Main.CELL_SIZE;
			idxY -= Main.CELL_COUNT;
		}

		if (idxX < 0) {
			idxX = Main.MAP_SIZE - idxX;
			idxX %= Main.MAP_SIZE;
			idxX = Main.MAP_SIZE - idxX;
		}
		if (idxX >= Main.MAP_SIZE) {
			idxX %= Main.MAP_SIZE;
		}

		if (idxY < 0) {
			idxY = Main.MAP_SIZE - idxY;
			idxY %= Main.MAP_SIZE;
			idxY = Main.MAP_SIZE - idxY;
		}
		if (idxY >= Main.MAP_SIZE) {
			idxY %= Main.MAP_SIZE;
		}

	}

	public void mousePressed(int x, int y, boolean otherBlock) {
		if (!Main.CREATIVE_MODE) {
			return; // place blocks only in creative mode
		}

		int aparentX = this.x - Main.CAMERA_X;
		int aparentY = this.y - Main.PLAYER_Y;

		if (x < aparentX)
			return;
		if (y < aparentY)
			return;
		if (x >= aparentX + Main.CELL_SIZE)
			return;
		if (y >= aparentY + Main.CELL_SIZE)
			return;

		// check for other blocks (non-base blocks)
		if (otherBlock) {
			if (Main.map[idxX][idxY] < FAKE) { // if is base block
				Main.map[idxX][idxY] = FAKE; // set to fake and return
				return;
			}
			Main.map[idxX][idxY]++; // else increment
			if (Main.map[idxX][idxY] >= Main.tiles.length) { // and if passed the limit
				Main.map[idxX][idxY] = FAKE; // set to FAKE (1st non-base block)
			}
			return; // return so it doesn't check base blocks code
		}

		if (Main.map[idxX][idxY] == WHITE) {
			Main.map[idxX][idxY] = BLACK;
		} else {
			Main.map[idxX][idxY] = WHITE;
		}

	}

	public boolean colliding(int x, int y, int w, int h) {
		if (!Main.solid[Main.map[idxX][idxY]]) {
			return false;
		}
		int thisX = this.x - Main.CAMERA_X;
		int thisY = this.y - Main.PLAYER_Y;
		if (x > thisX + w) {
			return false;
		}
		if (y > thisY + h) {
			return false;
		}
		if (x + w < thisX) {
			return false;
		}
		if (y + h < thisY) {
			return false;
		}
		return true;
	}

	public boolean deadColliding(int x, int y) {

		if (!Main.deadly[Main.map[idxX][idxY]]) {
			return false;
		}

		int thisX = this.x - Main.CAMERA_X;
		int thisY = this.y - Main.PLAYER_Y;
		if (x + Main.PLAYER_HITBOX_X > thisX + Main.PLAYER_HITBOX_WIDTH) {
			return false;
		}
		if (y + Main.PLAYER_HITBOX_Y > thisY + Main.PLAYER_HITBOX_HEIGHT) {
			return false;
		}
		if (x + Main.PLAYER_HITBOX_X + Main.PLAYER_HITBOX_WIDTH < thisX) {
			return false;
		}
		// Spikes have half block hit box
		if (y + Main.PLAYER_HITBOX_Y + Main.PLAYER_HITBOX_HEIGHT < thisY + (Main.CELL_SIZE >>> 1)) {
			return false;
		}
		return true;
	}

	public boolean checkFor(int x, int y, int what) {
		if (Main.map[idxX][idxY] != what) {
			return false;
		}
		int thisX = this.x - Main.CAMERA_X;
		int thisY = this.y - Main.PLAYER_Y;
		if (x + Main.PLAYER_HITBOX_X > thisX + Main.PLAYER_HITBOX_WIDTH) {
			return false;
		}
		if (y + Main.PLAYER_HITBOX_Y > thisY + Main.PLAYER_HITBOX_HEIGHT) {
			return false;
		}
		if (x + Main.PLAYER_HITBOX_X + Main.PLAYER_HITBOX_WIDTH < thisX) {
			return false;
		}
		if (y + Main.PLAYER_HITBOX_Y + Main.PLAYER_HITBOX_HEIGHT < thisY) {
			return false;
		}
		return true;
	}

}
