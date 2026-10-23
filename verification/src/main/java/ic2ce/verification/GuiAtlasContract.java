package ic2ce.verification;

import com.google.gson.GsonBuilder;
import ic2ce.neoforge.client.widget.FillGauge;
import ic2ce.neoforge.client.widget.ElectricalGauge;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/** Export native UV contracts for new-art composition; no game or screen is opened. */
public final class GuiAtlasContract {
  private static Map<String,Object> row(String name, FillGauge.GaugeProperties props) throws Exception {
    var row=new LinkedHashMap<String,Object>();row.put("name",name);
    for (var field:props.getClass().getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers()))continue;
      field.setAccessible(true);var value=field.get(props);
      row.put(field.getName(),value instanceof Number || value instanceof Boolean ? value:value.toString());
    }
    int u=props.uInner,v=props.vInner,w=props.innerWidth,h=props.innerHeight;
    if (u<0 || v<0 || w<=0 || h<=0 || u+w>256 || v+h>256)
      throw new AssertionError("Native gauge exceeds atlas: "+name);
    return row;
  }

  public static void main(String[] args) throws Exception {
    var rows=new ArrayList<Map<String,Object>>();
    for (var style:FillGauge.GaugeStyle.values())rows.add(row(style.name(),style.getProperties()));
    for (var style:ElectricalGauge.EnergyGaugeStyle.values())rows.add(row("Energy"+style.name(),style.properties));
    Path target=Path.of(args[0]);Files.createDirectories(target.getParent());
    Files.writeString(target,new GsonBuilder().setPrettyPrinting().create().toJson(rows)+"\n");
    System.out.println("IC2_GUI_ATLAS_CONTRACT gauges="+rows.size()+" failures=0 output="+target);
  }
}
