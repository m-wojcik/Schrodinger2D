package schrodinger2D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

@SuppressWarnings("serial")
public class SchPanel extends JPanel {
	
	int dimX, dimY;
	float xSize, ySize;
	LatticeV V;
	LatticeW wave, wavePrev, waveNow;
	boolean init = false;
	static int framecount = 1;
	static int framesTotal = 0;
	static int animationTime;
	static double maxProb = 0;
	Boolean draw, drawPotential, drawWave;
	String animationTitle;
	double potentialBorder = 0;
	
	// DRAWING
	int squareX, squareY, squareW, squareH;
	int waveX, waveY;
	int waveVelX, waveVelY;
	BufferedImage image;

	public SchPanel() {
		dimX = 400;
		dimY = 400;
		xSize = 2;
		ySize = 2;
		V = new LatticeV(dimX, dimY, xSize, ySize);
		wave = new LatticeW(dimX, dimY, xSize, ySize);
		waveNow = new LatticeW(dimX, dimY, xSize, ySize);
		wavePrev = new LatticeW(dimX, dimY, xSize, ySize);
		
		waveX = 200;
		waveY = 200;
		
		init = true;
		
		draw = false;
		drawPotential = true;
		drawWave = false;
		
		animationTitle = "Kwanty";
		
		MyListener myListener = new MyListener();
		this.addMouseListener(myListener);
		this.addMouseMotionListener(myListener);
	}

	public void paintComponent(Graphics g){
		if(!init) super.paintComponent(g);
		if(init) {
			super.paintComponent(g);
			//double probMax = computeProbMax();
			for(int i=0; i<dimX; i++) {
				for(int j=0; j<dimY; j++) {					
					g.setColor(new Color((float)V.data[i][j] > 0 ? 1 : 0, (float) 0, (float) 0));
					g.fillRect(i, j, 1, 1);
	
				}
			}
			
			g.setColor(Color.white);
			g.fillOval(waveX-10, waveY-10, 20, 20);
		}
	}
	
	public double computeRealLaplacian(int i, int j) {
		double dh = 2 * xSize / dimX;
		double laplacian = 0;
		
		laplacian += i > 0 ? wave.data[i-1][j].Re : potentialBorder;
		laplacian += i+1 < dimX ? wave.data[i+1][j].Re : potentialBorder;
		laplacian += j > 0 ? wave.data[i][j-1].Re : potentialBorder;
		laplacian += j+1 < dimY ? wave.data[i][j+1].Re : potentialBorder;
		
		laplacian -= 4 * wave.data[i][j].Re;
		
		laplacian /= dh*dh * 4;
		return laplacian;
	}
	
	public double computeImagLaplacian(int i, int j) {
		double dh = 2 * ySize / dimY;
		double laplacian = 0;
		
		laplacian += i > 0 ? waveNow.data[i-1][j].Im : potentialBorder;
		laplacian += i+1 < dimX ? waveNow.data[i+1][j].Im : potentialBorder;
		laplacian += j > 0 ? waveNow.data[i][j-1].Im : potentialBorder;
		laplacian += j+1 < dimY ? waveNow.data[i][j+1].Im : potentialBorder;
		
		laplacian -= 4* waveNow.data[i][j].Im;
	
		laplacian /= dh*dh * 4;
		return laplacian;
	}
	
	public void evolve(double T) {
		double dh = 2 * xSize / dimX;
		double VMax = computeVMax();
		double dt = 2 * dh*dh / (VMax * dh*dh + 1) * 0.95;
		int frame = 0;
		int pass = 100;
		animationTime = (int) ( 1000 * T);
		framecount = 1;
		framesTotal = (int) T;
		
		deleteDir(new File("./data"));
		Path path = Paths.get("./data");
		
		try {
		    Files.createDirectories(path);
		} catch (IOException e) {
		    System.err.println("Cannot create directories - " + e);
		}
		
		// plik konfiguracji
		FileWriter fwrt = null;
		
		try {
			
			fwrt = new FileWriter("./data/config.txt", true);
			BufferedWriter bw = new BufferedWriter(fwrt);
			bw.write("[config-file]");
			bw.newLine();
			
			bw.write(String.format("n_x = %d", dimX));
			bw.newLine();
			bw.write(String.format("n_y = %d", dimY));
			bw.newLine();
			bw.write(String.format("frames = %d", framesTotal));
			bw.newLine();
			bw.write("title = " + animationTitle);
			bw.newLine();
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			}
		
		// plik potencjalu
		
		FileWriter fwr = null;
			
		try {
			
			fwr = new FileWriter("./data/" + animationTitle + "_potential.txt", true);
			BufferedWriter bw = new BufferedWriter(fwr);
			for(int i=1; i<dimX; i++) {
				for(int j=1; j<dimY; j++) {
					float potentialTmp = (float) V.data[i][j] > 0 ? 1 : 0;					
					bw.write(String.format(Locale.US, "%.4f\t", potentialTmp));
				}
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
			}
		
		while(framecount <= framesTotal) {

			for(int i=0; i<dimX; i++) {
				for(int j=0; j<dimY; j++) {
					wave.data[i][j].Re = wavePrev.data[i][j].Re + dt * (-computeImagLaplacian(i,j)/2 + V.data[i][j] * waveNow.data[i][j].Im);
				}
			}
			
			for(int i=0; i<dimX; i++) {
				for(int j=0; j<dimY; j++) {
					wave.data[i][j].Im = waveNow.data[i][j].Im - dt * (-computeRealLaplacian(i,j)/2 + V.data[i][j] * wave.data[i][j].Re);
				}
			}
			wavePrev = waveNow;
			waveNow = wave;
			
			frame++;
			if(frame % pass == 0) {
				FileWriter fw = null;
				
				try {
					String filename = "./data/" + animationTitle + "_frame" + String.format("%05d", framecount) + ".txt";
					fw = new FileWriter(filename, true);
					BufferedWriter bw = new BufferedWriter(fw);
					for(int i=1; i<dimX; i++) {
						for(int j=1; j<dimY; j++) {
							float probTmp = (float) staggeredProb(i,j);
							
							maxProb = probTmp > maxProb ? probTmp : maxProb;
							
							bw.write(String.format(Locale.US, "%.4f\t", probTmp));
							
						}
						bw.newLine();
					}
					System.out.println(String.format("%d / %d", framecount, framesTotal));
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
					}
				framecount++;
				}
			}
		}
		
	public double computeVMax() {
		double vmax = 0;
		for(int i=0; i<dimX; i++) {
			for(int j=0; j<dimY; j++) {
				if(V.data[i][j] > vmax) vmax = V.data[i][j];
			}
		}
		return vmax;
	}
		
	public double computeProbMax() {
		double probMax = 0;
		for(int i=0; i<dimX; i++) {
			for(int j=0; j<dimY; j++) {
				if(wave.probData[i][j] > probMax) probMax = wave.probData[i][j];
			}
		}
		return probMax;
	}
	
	double staggeredProb(int i, int j) {
		return wavePrev.data[i][j].Re * wave.data[i][j].Re + waveNow.data[i][j].Im * waveNow.data[i][j].Im;
	}
	
	void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            if (! Files.isSymbolicLink(f.toPath())) {
	                deleteDir(f);
	            }
	        }
	    }
	    file.delete();
	}
	
	public class MyListener extends MouseInputAdapter {
		
		
		// Graphics2D g2;
		
		public void mousePressed(MouseEvent e) {
			if(draw && drawPotential) {
					squareX = e.getX();
					squareY = e.getY();
			}
			
			if(draw && drawWave) {
				waveX = e.getX();
				waveY = e.getY();
				
				wave = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
				waveNow = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
				wavePrev = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
				//repaint();
			}
		} //End of mousePressed()
	
		public void mouseReleased(MouseEvent e) {
			if(draw && drawPotential) {
			
				if (e.getX() > squareX) {
					squareW = e.getX() < 400 ? e.getX() - squareX : 400 - squareX; 
				}
				else {
					squareW = e.getX() > 0 ? squareX - e.getX() : squareX; 
					squareX = e.getX() > 0 ? e.getX() : 0;
				}
				
				if (e.getY() > squareY) {
					squareH = e.getY() < 400 ? e.getY() - squareY : 400 - squareY; 
				}
				else {
					squareH = e.getY() > 0 ? squareY - e.getY() : squareY; 
					squareY = e.getY() > 0 ? e.getY() : 0;
				}

				for(int i=0; i<squareW; i++ ) {
					for(int j=0; j<squareH; j++ ) {
						V.data[i + squareX][j + squareY] = 1000;
					}
				}
			}
			repaint();
		} //End of mouseReleased()
	}
	
	public void repositionWaves() {
		wave = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
		waveNow = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
		wavePrev = new LatticeW(dimX, dimY, xSize, ySize, waveX, waveY, waveVelX, waveVelY);
	}

	public Boolean getDraw() {
		return draw;
	}

	public void setDraw(Boolean draw) {
		this.draw = draw;
	}

	public Boolean getDrawPotential() {
		return drawPotential;
	}

	public void setDrawPotential(Boolean drawPotential) {
		this.drawPotential = drawPotential;
	}

	public Boolean getDrawWave() {
		return drawWave;
	}

	public void setDrawWave(Boolean drawWave) {
		this.drawWave = drawWave;
	}

	public double getWaveVelX() {
		return waveVelX;
	}

	public void setWaveVelX(int waveVelX) {
		this.waveVelX = waveVelX;
	}

	public double getWaveVelY() {
		return waveVelY;
	}

	public void setWaveVelY(int waveVelY) {
		this.waveVelY = waveVelY;
	}
	
	public void setAnimationTitle(String title) {
		this.animationTitle = title;
	}

	}

class LatticeV {
	int dimX, dimY;
	float xSize, ySize;
	double[][] data; 
			
	public LatticeV(int dimX, int dimY, float xSize, float ySize) {
		this.dimX = dimX;
		this.dimY = dimY;
		this.xSize = xSize;
		this.ySize = ySize;
		
		data = new double[dimX][dimY];
		
		// Zamiast rysowac potencjal mozna tutaj wpisac wlasna funkcje. Trzeba wtedy ni¿ej zmieniæ na:
		// Boolean potentialFunction = true;
		// Potencjal trzeba zapisac w postaci dwuwymiarowej funkcji.
		// Zeby nie bylo problemow z wyswietlaniem potencjalu trzeba wtedy zmienic w pliku plot.ipynb linijkê:
		// im2 = plt.imshow(b, cmap='hot', alpha=alphas)
		// na:
		// im2 = plt.imshow(b, cmap='hot', alpha=0.0)
		
		Boolean potentialFunction = false;
		if(potentialFunction) {
			double omega = 10;
			
			for(int i=0; i<dimX; i++) {
				for(int j=0; j<dimY; j++) {
					double x_tmp = -xSize + 2*xSize/dimX*i;
					double y_tmp = -ySize + 2*ySize/dimY*j;
					
					//oscylator
					data[i][j] = ( x_tmp*x_tmp + y_tmp*y_tmp ) * omega*omega / 2;
				}
			}
		}
		
		if(!potentialFunction) {
			for(int i=0; i<dimX; i++) {
				for(int j=0; j<dimY; j++) {
					//scianki
					if(i<10 || i > dimX-10 || j < 10 || j > dimY-10)
					data[i][j] = 1000;
				}
			}
		}
	}
}

class LatticeW {
	
	class ReIm {
		double Re, Im;
		
		public ReIm(double r, double i) {
			Re = r;
			Im = i;
		}
		
		public double Prob() {
			double probabilityDensity = Re*Re+Im*Im;
			return probabilityDensity;
		}
	}
	
	int dimX, dimY;
	float xSize, ySize;
	
	float xPos, yPos;
	double sigma;
	
	ReIm[][] data; 
	double[][] probData;
 			
	public LatticeW(int dimX, int dimY, float xSize, float ySize) {
		this.dimX = dimX;
		this.dimY = dimY;
		this.xSize = xSize;
		this.ySize = ySize;
		xPos = 0;
		yPos = 0;
		sigma = 0.01;
		double omega_x = 0;
		double omega_y = 0;
		
		data = new ReIm[dimX][dimY];
		probData = new double[dimX][dimY];
				
		for(int i=0; i<dimX; i++) {
			for(int j=0; j<dimY; j++) {
				double x_tmp = -xSize + 2*xSize/dimX*i;
				double y_tmp = -ySize + 2*ySize/dimY*j;
				data[i][j] = new ReIm(
						1 / (2*Math.PI*sigma) * Math.exp(-1/(2*sigma) * ( (x_tmp-xPos)*(x_tmp-xPos) + (y_tmp-yPos)*(y_tmp-yPos) )) * Math.cos(omega_y * y_tmp + omega_x * x_tmp),
						1 / (2*Math.PI*sigma) * Math.exp(-1/(2*sigma) * ( (x_tmp-xPos)*(x_tmp-xPos) + (y_tmp-yPos)*(y_tmp-yPos) )) * Math.sin(omega_y * y_tmp + omega_x * x_tmp)
						);
				probData[i][j] = data[i][j].Prob();
			}
		}
	}
	
	public LatticeW(int dimX, int dimY, float xSize, float ySize, int posX, int posY, double velX, double velY) {
		this.dimX = dimX;
		this.dimY = dimY;
		this.xSize = xSize;
		this.ySize = ySize;
		xPos = (float) -xSize + 2*xSize/dimX*posX;
		yPos = (float) -ySize + 2*ySize/dimY*posY;
		sigma = 0.01;
		double omega_x = velX;
		double omega_y = velY;
		
		data = new ReIm[dimX][dimY];
		probData = new double[dimX][dimY];
				
		for(int i=0; i<dimX; i++) {
			for(int j=0; j<dimY; j++) {
				double x_tmp = -xSize + 2*xSize/dimX*i;
				double y_tmp = -ySize + 2*ySize/dimY*j;
				data[i][j] = new ReIm(
						1 / (2*Math.PI*sigma) * Math.exp(-1/(2*sigma) * ( (x_tmp-xPos)*(x_tmp-xPos) + (y_tmp-yPos)*(y_tmp-yPos) )) * Math.cos(omega_y * y_tmp + omega_x * x_tmp),
						1 / (2*Math.PI*sigma) * Math.exp(-1/(2*sigma) * ( (x_tmp-xPos)*(x_tmp-xPos) + (y_tmp-yPos)*(y_tmp-yPos) )) * Math.sin(omega_y * y_tmp + omega_x * x_tmp)
						);
				probData[i][j] = data[i][j].Prob();
			}
		}
	}
}
