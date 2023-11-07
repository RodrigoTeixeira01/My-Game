package deleteme;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, FileNotFoundException, IOException {
		int[][] map;
		ObjectInputStream in = new ObjectInputStream(new FileInputStream("save.save"));
		map = (int[][]) in.readObject();
		in.close();
		
		int[][] out = new int[map.length][map.length];
		for(int i=0; i<map.length; i++) {
			for(int j=0; j<map.length; j++) {
				int cur = map[i][j];
				if(cur > 1) {
					out[i][j] = cur +13;
				}else {
					out[i][j] = cur;
				}
			}
		}
		ObjectOutputStream outFile = new ObjectOutputStream(new FileOutputStream("save.save"));
		outFile.writeObject(out);
		outFile.close();
	
	}

}
