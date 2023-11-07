package main;

import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Shot extends Entity {

	int dx;
	
	private static final int setup = Main.CELL_SIZE / 4;
	
	Image image;
	
	public Shot(int x, int y, int idxX, int idxY) {
		super(x, y, idxX, idxY);
		this.w = Main.CELL_SIZE / 2;
		this.h = Main.CELL_SIZE / 4;
	}

	public Shot(int x, int y, int idxX, int idxY, int dx) {
		this(x, y, idxX, idxY);
		this.dx = dx;
		try {
			image = ImageIO.read(new File("textures\\shot.png"));
		} catch (IOException e) {
			System.out.println("ERROR IMPORTING SHOT IMAGE");
			e.printStackTrace();
		}
	}

	@Override
	public void show(Graphics g) {
		g.drawImage(image, x + setup - Main.CAMERA_X, y + setup - Main.PLAYER_Y, w, h, null);
	}

	@Override
	public void tick() {
		x += dx;
		int idxX = x / Main.CELL_SIZE;
		if(idxX<0) {
			idxX += Main.MAP_SIZE;
		}
		int idxY = y / Main.CELL_SIZE;
		if(idxY<0) {
			idxY += Main.MAP_SIZE;
		}
		if(Main.solid[Main.map[idxX][idxY]]) {
			die(false);
		}

	}

	@Override
	protected void stepedByPlayer() {
		this.touchedByPlayer();
		
	}

	@Override
	protected void touchedByPlayer() {
		Main.die();
		die(false);
		
	}

}
