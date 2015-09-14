package center.rodrigo.model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

public class Particula {

    private int x;
    private int y;
    private int size;
    private boolean life = false;
    private int velocidade;
    private Color color;
    private int heightTela = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

    public Particula(Color color) {
        this.color = color;
    }

    public void reset(int x, int y) {
        this.life = true;
        this.x = x;
        this.y = y;
        for (int i = 0; i < 20; i++) {
            this.color = color.brighter();
        }
        this.velocidade = (int) (Math.random() * 30 + 15);
        this.size = 2;
    }

    public void update() {
        if (life) {
            if (y < heightTela) {
                y += velocidade;
            } else
                life = false;
        }
    }

    public void render(Graphics g) {
        if (life) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(color);
            g2d.fillRect(x, y, size, size);
            g2d.dispose();
        }
    }

    /* GETs AND SETs */
    public boolean isLife() {
        return life;
    }
}
