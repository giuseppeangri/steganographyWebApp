package stego_psnr;

import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.IOException;

import javax.swing.ImageIcon;

public class PSNR {
	
	private Image image01;
	private Image image02;
	
	private int width;
	private int height;
	
	private float psnr_255, psnr_peak;
    private float mse;
    private double peak;
	
	public PSNR(Image image01, Image image02) throws IOException {
		
		this.image01 = image01;
		this.image02 = image02;
		
		ImageIcon icon = new ImageIcon(image02);
		
		this.width = icon.getIconWidth();
        this.height = icon.getIconHeight();
        
        calculate();
        
	}
	
	private void calculate() throws IOException {
		
		int[][] image01_pixels = this.conver2Pixels(image01);
		int[][] image02_pixels = this.conver2Pixels(image02);
		
        mse = 0;
        peak = 0;
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
            	
                mse += Math.pow(image01_pixels[i][j] - image02_pixels[i][j], 2);
                
                if(peak < image01_pixels[i][j]) {
                	peak = image01_pixels[i][j];
                }
            }
        }
        
        mse = mse / (width * height);
        
        psnr_255 = (float) (10 * (Math.log10(Math.pow(255, 2) / mse)));
        
        psnr_peak = (float) (10 * (Math.log10(Math.pow(peak, 2) / mse)));
                
    }
	
	private int[][] conver2Pixels(Image image) throws IOException {
		
        int[] pixel = new int[width * height];
        int[][] pixelMap = new int[width][height];
        int count = 0;
                
        PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixel, 0, width);
        try { pg.grabPixels();
        } catch (Exception e) { System.err.println(e);}
        
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int grayLevel = (int) ((0.299 * ((pixel[count] >> 16) & 0xff)) + (0.587 * ((pixel[count] >> 8) & 0xff)) + (0.114 * (pixel[count] & 0xff)));
                pixelMap[i][j] = grayLevel;
                count++;
            }
        }
        
        return  pixelMap;
    }

	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public float getPsnr_255() {
		return psnr_255;
	}

	public float getPsnr_peak() {
		return psnr_peak;
	}

	public float getMse() {
		return mse;
	}

	public double getPeak() {
		return peak;
	}
	
}
