package org.sakaiproject.tool.assessment.audio;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JPanel;

public class AudioMeterPanel extends JPanel {
	
	private static final long serialVersionUID = 0L;
	
	int frame;
    int delay;
    Thread animator;
    
    static ResourceBundle res = AudioUtil.getInstance().getResourceBundle();
    
    String imageUrl = "";

    Dimension offDimension;
    Image offImage;
    Graphics offGraphics;
	private Image[] frames;
	static ColorModel colorModel= new ColorModel();
	private static final Font font12 = new Font("serif", Font.PLAIN, 12);
	private static final Color graphColor = colorModel.getColor("graphColor");
	
	int level;
	double seconds;
	
	public AudioMeterPanel() {
		super();
	}
	
	public AudioMeterPanel(String imageUrl) {
		super();
		this.imageUrl = imageUrl;
		try {
			frames = new Image[10];
			for (int i = 0 ; i < 10 ; i++) {
			    frames[i] = Toolkit.getDefaultToolkit().getImage(new URL(imageUrl + "/meter" + i + ".gif"));
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setBackground(Color.BLACK);
		
	}
	
	public void init() {
		int fps = 10;
		delay = (fps > 0) ? (1000 / fps) : 100;
	}
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		
		//System.out.println("***********level = " + level);
		if (level > 9) {
			level = 9;
			//System.out.println("***********level = " + level);
		}
		if (level > 5) {
			level = level - 2;
			//System.out.println("***********level = " + level);
		}
		if (level > 3 && level <= 5) {
			level = level - 1;
			//System.out.println("***********level = " + level);
		}
		
		this.level = level;
	}

	/**
     * Update a frame of animation.
     */
    public void update(Graphics g) {
	Dimension d = getSize();

	// Create the offscreen graphics context
	if ((offGraphics == null)
	 || (d.width != offDimension.width)
	 || (d.height != offDimension.height)) {
	    offDimension = d;
	    offImage = createImage(d.width, d.height);
	    offGraphics = offImage.getGraphics();
	}

	// Erase the previous image
	offGraphics.setColor(getBackground());
	offGraphics.fillRect(0, 0, d.width, d.height);
	offGraphics.setColor(Color.black);

	// Paint the frame into the image
	paintFrame(offGraphics);

	// Paint the image onto the screen
	g.drawImage(offImage, 0, 0, null);
	drawLengthText(getSeconds(), d.height, g);
    }
    

    /**
     * Paint the previous frame (if any).
     */
    public void paint(Graphics g) {
	update(g);
    }

    /**
     * Paint a frame of animation.
     */
    public void paintFrame(Graphics g) {
    	
    	try {
    		//System.out.println("level = " + level);
			g.drawImage(frames[level], 0, 20, null);
		} catch (ArrayIndexOutOfBoundsException e) {
			//System.out.println("ArrayIndexOutOfBoundsException: level = " + level);
			g.drawImage(frames[9], 0, 20, null);
		}
    }
    
    private void drawLengthText(double seconds, int h, Graphics g2)
    {
      g2.setColor(graphColor);
      g2.setFont(font12);
      g2.drawString(res.getString("Length_") + String.valueOf(seconds), 3,
                    h - 4);
    }

	public double getSeconds() {
		return seconds;
	}

	public void setSeconds(double seconds) {
		this.seconds = seconds;
	}

}
