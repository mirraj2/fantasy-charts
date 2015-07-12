package fantasy.charts;

import jasonlib.Rect;
import jasonlib.swing.Graphics3D;
import jasonlib.swing.component.GFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import com.google.common.collect.Lists;
import fantasy.model.CMath;
import fantasy.model.Dataset;

public class LineChart extends Chart {

  private static final List<Color> colors = Lists.newArrayList();
  static {
    Random rand = new Random(1337);
    colors.add(Color.green);
    for (int i = 0; i < 50; i++) {
      colors.add(Color.getHSBColor(rand.nextFloat(), .75f + rand.nextFloat() / 4, .75f + rand.nextFloat() / 4));
    }
  }
  
  final static BasicStroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_MITER, 10.0f, new float[] { 4f }, 0.0f);
  private static final Font font = new Font("Arial", Font.PLAIN, 14);

  private List<Dataset> datasets = Lists.newCopyOnWriteArrayList();
  private Rect bounds = new Rect(0, 0, 1, 1);
  private int pointSize = 3;
  private Color lineColor = new Color(255, 255, 255, 100);
  private boolean drawLines = true;

  private int mouseX = -1, mouseY = -1;
  private Dataset hoverDataset;
  private int hoverIndex;

  public LineChart() {
    listen();
  }

  @Override
  protected void render(Graphics3D g) {
    int datasetIndex = 0;
    for (Dataset set : datasets) {
      for (int i = 1; i < set.xValues.length; i++) {
        double x1 = screenX(set.xValues[i - 1]);
        double x2 = screenX(set.xValues[i]);
        double y1 = screenY(set.yValues[i - 1]);
        double y2 = screenY(set.yValues[i]);
        if (drawLines) {
          g.color(lineColor).line(x1, y1, x2, y2);
        }
        drawPoint(g, x1, y1, set == hoverDataset && i - 1 == hoverIndex, datasetIndex);
        if (i == set.xValues.length - 1) {
          drawPoint(g, x2, y2, set == hoverDataset && i == hoverIndex, datasetIndex);
        }
      }
      datasetIndex++;
    }

    drawCross(g);

    if (hoverDataset != null) {
      double infoW = 300, infoH = 67;
      Rect r = new Rect(w - infoW - 10, h - infoH - 10, infoW, infoH);
      g.color(Color.DARK_GRAY).fillRoundRect(r, 20, 20);
      g.color(Color.white).drawRoundRect(r, 20, 20);
      String xText = hoverDataset.xText(hoverDataset.xValues[hoverIndex]);
      String yText = hoverDataset.yText(hoverDataset.yValues[hoverIndex]);
      double y = r.y + 10 + 14;
      g.font(font)
          .text(hoverDataset.name, r.x + 10, y)
          .text("x = " + xText, r.x + 10, y + 14)
          .text("y = " + yText, r.x + 10, y + 28);
    }
  }

  private void drawCross(Graphics3D g) {
    g.stroke(dashed).color(Color.white);
    double crossX = mouseX, crossY = mouseY;
    if (hoverDataset != null) {
      crossX = screenX(hoverDataset.xValues[hoverIndex]);
      crossY = screenY(hoverDataset.yValues[hoverIndex]);
    }
    g.line(0, crossY, w, crossY);
    g.line(crossX, 0, crossX, h);
    g.setStroke(1);
  }

  private void drawPoint(Graphics3D g, double x, double y, boolean hover, int datasetIndex) {
    // if (hover) {
    // g.color(Color.yellow).fillOval(x - pointSize, y - pointSize, pointSize * 2, pointSize * 2);
    // }
    int pointSize = this.pointSize;
    if (hover) {
      pointSize += 2;
    }
    g.color(colors.get(datasetIndex)).fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
  }

  private double screenX(double x) {
    return ((x - bounds.x) / bounds.w) * w;
  }

  private double screenY(double y) {
    return (1 - ((y - bounds.y) / bounds.h)) * h;
  }

  // private double dataX(double x) {
  // return x / w * bounds.w + bounds.x;
  // }
  //
  // private double dataY(double y) {
  // return (1 - y / h) * bounds.h + bounds.y;
  // }

  public LineChart dataset(Dataset dataset) {
    this.datasets.add(dataset);
    adjustBounds();
    return this;
  }

  private void adjustBounds() {
    Iterator<Dataset> iter = datasets.iterator();
    bounds = iter.next().getBounds();
    while (iter.hasNext()) {
      bounds = bounds.union(iter.next().getBounds());
    }
    bounds = bounds.grow(bounds.w * .01, bounds.h * .05);
    repaint();
  }

  private void updateHover() {
    hoverDataset = null;
    double bestDist = Double.MAX_VALUE;

    for (Dataset set : datasets) {
      int index = getIndexClosestTo(set, mouseX, mouseY);
      double dist = CMath.dist(screenX(set.xValues[index]), screenY(set.yValues[index]), mouseX, mouseY);
      if (dist < bestDist) {
        hoverDataset = set;
        hoverIndex = index;
        bestDist = dist;
      }
    }
  }

  public int getIndexClosestTo(Dataset set, double x, double y) {
    int best = -1;
    double bestDist = Double.MAX_VALUE;
    for (int i = 0; i < set.xValues.length; i++) {
      double dist = CMath.dist(screenX(set.xValues[i]), screenY(set.yValues[i]), x, y);
      if (dist < bestDist) {
        bestDist = dist;
        best = i;
      }
    }

    return best;
  }

  private void listen() {
    MouseAdapter listener = new MouseAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        updateHover();
        repaint();
      }

      @Override
      public void mouseExited(MouseEvent e) {
        mouseX = mouseY = -1;
        hoverDataset = null;
        repaint();
      }
    };
    addMouseListener(listener);
    addMouseMotionListener(listener);
  }

  public static LineChart create() {
    LineChart ret = new LineChart();
    GFrame.create().content(ret).maximize().start();
    return ret;
  }

  public static LineChart create(Dataset dataset) {
    return create().dataset(dataset);
  }

}
