package main;

import java.awt.Graphics;

public class Mushroom extends Entity {
	private int dx = 300 / Main.FPS;
	private int dy = 0;
	
	public Mushroom(int x, int y, int idxX, int idxY) {
		super(x, y, idxX, idxY);
		w=Main.CELL_SIZE;
		h=Main.CELL_SIZE;
		spawner = Cell.MUSHROOM;
	}

	@Override
	public void show(Graphics g) {
		
		g.drawImage(Main.tiles[Cell.MUSHROOM + (dx > 0?2:1)], x-Main.CAMERA_X, y-Main.PLAYER_Y, Main.CELL_SIZE, Main.CELL_SIZE, null);
	}

	@Override
	public void tick() {
		if(x - Main.CAMERA_X + Main.CELL_SIZE < 0) {
			die(true);
			return;
		}

		if(y - Main.PLAYER_Y + Main.CELL_SIZE < 0) {
			die(true);
			return;
		}
		if(x - Main.CAMERA_X > Main.WINDOW_SIZE) {
			die(true);
			return;
		}
		if(y - Main.PLAYER_Y > Main.WINDOW_SIZE) {
			die(true);
			return;
		}
		x += dx;
		if(colliding()) {
			x -= dx;
			dx = -dx;
		}
		y += dy;
		if(colliding()) {
			y -= dy;
			dy = -1;
			
			//test
			int oldX = x;
			int oldY = y;
			x+=dx>0?Main.CELL_SIZE:-Main.CELL_SIZE;
			y+=Main.CELL_SIZE;
			if(!colliding()) {
				dx=-dx;
			}
			x = oldX; y=oldY;
		}
		dy++;
		if(colliding()) {
			y -= Main.CELL_SIZE;
		}
		
	}

	private boolean colliding() {
		for(Cell cell: Main.cells) {
			boolean temp = cell.colliding(x - Main.CAMERA_X, y - Main.PLAYER_Y, w-1, h-1);
			if(temp) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void stepedByPlayer() {
		die(false);
	}

	@Override
	protected void touchedByPlayer() {
		//Main.die();
	}

}
