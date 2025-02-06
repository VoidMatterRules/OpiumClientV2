package we.devs.opium.asm.mixins;

import net.minecraft.util.math.Vec3i;
import org.joml.Vector3d;

public interface IVec3d {
    void opium$set(double x, double y, double z);

    default void opium$set(Vec3i vec) {
        opium$set(vec.getX(), vec.getY(), vec.getZ());
    }

    default void opium$set(Vector3d vec) {
        opium$set(vec.x, vec.y, vec.z);
    }

    void opium$setXZ(double x, double z);

    void opium$setY(double y);
}
