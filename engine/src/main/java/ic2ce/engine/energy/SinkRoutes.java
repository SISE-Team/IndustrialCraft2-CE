package ic2ce.engine.energy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Per-source route selection over a solved topology: reachable sinks ordered by loss, then by node
 * index, keeping only the cheapest route into each sink owner. Several sink nodes may share one
 * owner (a multi-block or multi-face sink).
 */
public final class SinkRoutes {
  private SinkRoutes() {}

  /** A selected route; {@code sink} is the topology node index. */
  public record Route(int sink, double loss) {}

  /**
   * @param owners owner identity per node index, compared with {@code equals}; only sink entries are
   *     read
   * @return routes in delivery order, at most one per owner
   */
  public static List<Route> select(
      WeightedTopology.Role[] roles, WeightedTopology.Workspace solved, Object[] owners) {
    var sinks = new ArrayList<Integer>();
    for (int i = 0; i < roles.length; i++)
      if (roles[i] == WeightedTopology.Role.SINK && Double.isFinite(solved.lossTo(i))) sinks.add(i);
    sinks.sort(Comparator.comparingDouble(solved::lossTo).thenComparingInt(Integer::intValue));
    var chosen = new HashMap<Object, Integer>();
    var routes = new ArrayList<Route>();
    for (int sink : sinks) {
      double loss = solved.lossTo(sink);
      var slot = chosen.get(owners[sink]);
      if (slot != null) {
        // Sinks are visited in ascending loss, so an earlier route for the owner is never worse.
        if (routes.get(slot).loss() <= loss) continue;
        routes.set(slot, new Route(sink, loss));
      } else {
        chosen.put(owners[sink], routes.size());
        routes.add(new Route(sink, loss));
      }
    }
    return routes;
  }
}
