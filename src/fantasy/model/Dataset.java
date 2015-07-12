package fantasy.model;

import jasonlib.Rect;
import jasonlib.util.Utils;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import com.google.common.primitives.Doubles;

public class Dataset {

  public final String name;
  public final double[] xValues, yValues;
  private Function<Double, String> xStringer = Utils::format;
  private Function<Double, String> yStringer = Utils::format;

  public Dataset(String name, List<LocalDate> dates, List<Double> values) {
    this.name = name;
    xValues = map(dates, date -> date.toEpochDay());
    yValues = Doubles.toArray(values);
    xStringer = d -> LocalDate.ofEpochDay(d.longValue()).toString();
  }
  
  public String xText(double value) {
    return xStringer.apply(value);
  }

  public String yText(double value) {
    return yStringer.apply(value);
  }

  private <T> double[] map(List<T> c, Function<T, Number> f) {
    int size = c.size();
    double[] ret = new double[size];
    for (int i = 0; i < size; i++) {
      ret[i] = f.apply(c.get(i)).doubleValue();
    }
    return ret;
  }

  public Rect getBounds() {
    double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
    double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
    for (int i = 0; i < xValues.length; i++) {
      double x = xValues[i];
      double y = yValues[i];
      if(x < minX) {
        minX = x;
      }
      if(y < minY) {
        minY = y;
      }
      if (x > maxX) {
        maxX = x;
      }
      if(y > maxY) {
        maxY = y;
      }
    }
    return new Rect(minX, minY, maxX - minX, maxY - minY);
  }

}
