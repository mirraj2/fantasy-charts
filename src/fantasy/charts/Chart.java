package fantasy.charts;

import jasonlib.swing.Graphics3D;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

public abstract class Chart extends JComponent {

  private Color backgroundColor = new Color(60, 60, 80);

  protected int w, h;

  @Override
  protected void paintComponent(Graphics gg) {
    w = getWidth();
    h = getHeight();
    Graphics3D g = Graphics3D.create(gg);
    g.color(backgroundColor).fillRect(0, 0, w, h);
    render(g);
  }
  
  protected abstract void render(Graphics3D g);

}
