package main;

import java.awt.Graphics;

public class Boss extends Entity {

	private static final int FRAMES_UNTIL_NEXT = Main.FPS / 4;
	private static final int SHOT_FRAME_COUNT = Main.FPS;
	private int animationFrame = 1;
	private int shotFrame = 1;
	
	private static final int SHOT_SPEED = 600 / Main.FPS;
	private static final int X_SPEED = SHOT_SPEED / 2;
	private int dx = X_SPEED;
	private int dy = 0;
	
	private int invulnerable = 0;
	
	public Boss(int x, int y, int idxX, int idxY) {
		super(x, y, idxX, idxY, 3);
		w=Main.CELL_SIZE;
		h=Main.CELL_SIZE;
		spawner = Cell.ENEMY;
	}

	@Override
	public void show(Graphics g) {
		frame++;
		if(frame > FRAMES_UNTIL_NEXT) {
			frame = 0;
			animationFrame = 3 - animationFrame;
		}
		g.drawImage(Main.tiles[Cell.ENEMY + animationFrame], x-Main.CAMERA_X, y-Main.PLAYER_Y, Main.CELL_SIZE, Main.CELL_SIZE, null);
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
		if(dx > 0 && x - Main.CAMERA_X < Main.WINDOW_SIZE * 0.8d) {
			dx = 0;
		}
		if(dx == 0 && x - Main.CAMERA_X < Main.WINDOW_SIZE * 0.7d) {
			dx = X_SPEED;
		}
		y += dy;
		if(colliding()) {
			y -= dy;
			dy = -1;
			if(dx < 0)dx = 0;
			if(Main.rand.nextInt(Main.FPS) <= 1 && invulnerable < 1 && x - Main.CAMERA_X > Main.WINDOW_SIZE * 0.5f) {
				dy = -14;
				dx = -X_SPEED;
			}
		}
		dy++;
		if(colliding()) {
			y -= Main.CELL_SIZE;
		}
		
		shotFrame++;
		if(shotFrame >= SHOT_FRAME_COUNT) {
			shotFrame = 1;
			Main.entities.add(new Shot(x, y, -1, -1, (Main.PLAYER_X + Main.PLAYER_POS > x ? SHOT_SPEED: -SHOT_SPEED)));
		}
		
		invulnerable--;
		if(invulnerable < 1) {
			invulnerable = 0;
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
		if(invulnerable > 0) {
			return;
		}
		invulnerable = Main.FPS;
		shotFrame -= invulnerable;
		hp--;
		if(hp <= 0) {
			die(false);
		}
	}

	@Override
	protected void touchedByPlayer() {
		if(invulnerable > 0) {
			return;
		}
		//die(true);
		Main.die();
		
	}

}
