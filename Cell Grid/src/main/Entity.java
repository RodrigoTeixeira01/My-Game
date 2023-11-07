package main;

import java.awt.Graphics;

public abstract class Entity {

	int x;
	int y;
	int w;
	int h;
	
	int idxX;
	int idxY;
	int spawner;
	
	int frame = 0;
	
	int DEFAULT_HP;
	int hp;
	
	public abstract void show(Graphics g);
	public abstract void tick();
	
	public Entity(int x, int y, int idxX, int idxY) {
		this(x, y, idxX, idxY, 1);
	}
	
	public Entity(int x, int y, int idxX, int idxY, int DEFAULT_HP) {
		this.x = x;
		this.y = y;
		this.idxX = idxX;
		this.idxY = idxY;
		this.DEFAULT_HP = DEFAULT_HP;
		this.hp = DEFAULT_HP;
	}
	protected void die() {
		die(true);
	}
	
	protected void die(boolean respawn) {
		Main.entities.remove(this);
		if(respawn)Main.map[idxX][idxY]=spawner;
	}
	protected abstract void stepedByPlayer();
	protected abstract void touchedByPlayer();
	
}
