package tech.squadmc.squadmcor.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import tech.squadmc.squadmcor.init.ModEntities;

public class mtlbEntity extends SquadBaseVehicleEntity {

    public int atgmShakeTicks = 0;

    public mtlbEntity(EntityType<? extends mtlbEntity> type, Level world) {
        super(type, world);
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((source, damage) -> this.getSourceAngle(source, 0.4F) * damage);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getShootAnimationTimer(0, 0) > 0) {
            Vec3 look = this.getLookAngle();
            Vec3 rear = this.position().subtract(look.scale(3.5));

            if (this.level().isClientSide) {
                Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();

                for (int i = 0; i < 3; i++) {
                    double sideOffset = (this.random.nextDouble() - 0.5) * 5.0;
                    double upOffset = this.random.nextDouble() * 1.5;
                    double backOffset = (this.random.nextDouble() - 0.5) * 1.0;

                    Vec3 spawnPos = rear
                            .add(right.scale(sideOffset))
                            .add(0.0, upOffset + 0.6, 0.0)
                            .add(look.scale(backOffset));

                    double sideSpeed = (this.random.nextDouble() - 0.5) * 0.12;
                    double upSpeed = 0.01 + this.random.nextDouble() * 0.02;

                    Vec3 velocity = right.scale(sideSpeed)
                            .add(0.0, upSpeed, 0.0)
                            .add(look.scale((this.random.nextDouble() - 0.5) * 0.02));

                    net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
                            tech.squadmc.squadmcor.client.ClientAccess.spawnSmoke(
                                    this.level(),
                                    spawnPos.x, spawnPos.y, spawnPos.z,
                                    velocity.x, velocity.y, velocity.z
                            )
                    );
                }
            }
        }

    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 101) {
            this.atgmShakeTicks = 4;
        } else {
            super.handleEntityEvent(id);
        }
    }


    @Override
    public boolean hasDecoy() {
        return false;
    }

    private PlayState cannonFirePredicate(AnimationState<mtlbEntity> event) {
        return this.getShootAnimationTimer(1, 0) > 0 ?
                event.setAndContinue(RawAnimation.begin().thenPlay("animation.lav_150.fire")) :
                event.setAndContinue(RawAnimation.begin().thenLoop("animation.lav_150.idle"));
    }

    private PlayState machineGunFirePredicate(AnimationState<mtlbEntity> event) {
        return this.getShootAnimationTimer(1, 1) > 0 ?
                event.setAndContinue(RawAnimation.begin().thenPlay("animation.lav_150.fire2")) :
                event.setAndContinue(RawAnimation.begin().thenLoop("animation.lav_150.idle2"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar data) {
        data.add(new AnimationController<>(this, "cannon", 0, this::cannonFirePredicate));
        data.add(new AnimationController<>(this, "machineGun", 0, this::machineGunFirePredicate));
    }
}
