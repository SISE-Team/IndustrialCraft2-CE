package ic2ce.neoforge.personal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

/** Headless verification of the actual baked door mesh and the renderer's actual transforms. */
public final class PrivateChestRenderVerification {
  private static int checks;

  private static void check(boolean ok, String message) {
    checks++;
    if (!ok) throw new AssertionError(message);
  }

  private static List<Vector3f> vertices(Direction facing, float openness) {
    var pose = new PoseStack();
    PrivateChestRenderer.poseDoor(pose, facing, openness);
    var vertices = new ArrayList<Vector3f>();
    PrivateChestRenderer.createDoor().visit(pose, (transform, name, index, cube) -> {
      check(cube.polygons.length == 6, "door has all six solid faces");
      for (var face : cube.polygons) for (var vertex : face.vertices()) {
        check(vertex.u() >= 0 && vertex.u() <= 1 && vertex.v() >= 0 && vertex.v() <= 1,
            "every door UV stays inside its atlas");
        vertices.add(transform.pose().transformPosition(
            new Vector3f(vertex.worldX(), vertex.worldY(), vertex.worldZ())));
      }
    });
    return vertices;
  }

  public static void main(String[] args) {
    for (Direction facing : List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST)) {
      var closed = vertices(facing, 0);
      check(closed.size() == 24, "closed door produces six quads");
      double minY = closed.stream().mapToDouble(v -> v.y).min().orElseThrow();
      double maxY = closed.stream().mapToDouble(v -> v.y).max().orElseThrow();
      check(Math.abs(minY - 1/16d) < 1e-6 && Math.abs(maxY - 15/16d) < 1e-6,
          "closed door covers the case opening vertically");
      // The front sheet is one pixel in from the facing edge, covering x=2..14.
      var inward = facing.getOpposite();
      double nearest = closed.stream().mapToDouble(v ->
          (v.x-.5)*inward.getStepX() + (v.z-.5)*inward.getStepZ()).min().orElseThrow();
      check(Math.abs(nearest + 7/16d) < 1e-6, "closed door follows the block facing");
      for (float fraction : new float[]{.1f,.5f,1f}) {
        var open = vertices(facing, fraction);
        boolean projectsOutward = false;
        for (int i=0;i<open.size();i++) {
          check(Math.abs(open.get(i).y-closed.get(i).y)<1e-6,
              "hinge stays vertical; the door never turns into a floor panel");
          var p = open.get(i);
          projectsOutward |= (p.x-.5)*facing.getStepX() + (p.z-.5)*facing.getStepZ() > .5;
        }
        check(projectsOutward, "open door swings outside the case");
        float before = closed.get(0).distance(closed.get(2));
        check(Math.abs(open.get(0).distance(open.get(2))-before)<1e-6,
            "door remains rigid through interpolation");
      }
      check(vertices(facing,Float.NaN).equals(closed), "non-finite progress closes safely");
    }
    System.out.println("IC2_SAFE_RENDER_VERIFY checks="+checks+" failures=0");
  }
}
