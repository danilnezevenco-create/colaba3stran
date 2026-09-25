package tech.squadmc.squadmcor.entity;

import com.atsuishio.superbwarfare.entity.vehicle.damage.DamageModifier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tech.squadmc.squadmcor.init.ModEntities;

public class Bmp1Entity extends SquadBaseVehicleEntity {

    private static final EntityDataAccessor<Float> STEERING_ANGLE;
    public float prevSteeringAngle = 0.0F;
    public float wheelRotation = 0.0F;
    public float prevWheelRotation = 0.0F;

    public Bmp1Entity(EntityType<? extends Bmp1Entity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(STEERING_ANGLE, 0.0F);
    }

    public float getSteeringAngle() {
        return this.entityData.get(STEERING_ANGLE);
    }

    public void setSteeringAngle(float angle) {
        this.entityData.set(STEERING_ANGLE, angle);
    }

    public float getPrevSteeringAngle() {
        return this.prevSteeringAngle;
    }

    public float getWheelRotation() {
        return this.wheelRotation;
    }

    public float getPrevWheelRotation() {
        return this.prevWheelRotation;
    }

    @Override
    public DamageModifier getDamageModifier() {
        return super.getDamageModifier().custom((source, damage) -> this.getSourceAngle(source, 0.5F) * damage);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("SteeringAngle", this.getSteeringAngle());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("SteeringAngle")) {
            this.setSteeringAngle(compound.getFloat("SteeringAngle"));
        }
    }

    @Override
    public void tick() {
        if (this.level().isClientSide) {
            net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT, () -> () ->
                    tech.squadmc.squadmcor.client.ClientAccess.handleBmp1ClientTick(this)
            );
        }

        super.tick();

        // 2. Exhaust smoke generator (Front-right deck placement on BMP-1)
        if (this.getShootAnimationTimer(0, 0) > 0) {
            Vec3 look = this.getLookAngle();
            Vec3 forward = this.position().add(look.scale(1.1));

            if (this.level().isClientSide) {
                Vec3 right = new Vec3(-look.z, 0.0, look.x).normalize();
                Vec3 exhaustPos = forward.add(right.scale(0.95)).add(0.0, 0.70, 0.0);

                for (int i = 0; i < 3; i++) {
                    double sideOffset = (this.random.nextDouble() - 0.5) * 0.2;
                    double upOffset = this.random.nextDouble() * 0.2;
                    double backOffset = (this.random.nextDouble() - 0.5) * 0.2;

                    Vec3 spawnPos = exhaustPos
                            .add(right.scale(sideOffset))
                            .add(0.0, upOffset, 0.0)
                            .add(look.scale(backOffset));

                    double sideSpeed = (this.random.nextDouble() - 0.5) * 0.04;
                    double upSpeed = 0.02 + this.random.nextDouble() * 0.03;
                    Vec3 velocity = look.scale(-0.20D)
                            .add(right.scale(sideSpeed))
                            .add(0.0, upSpeed, 0.0);

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

        // 3. Track physics and wheel rotation
        this.prevSteeringAngle = this.getSteeringAngle();
        float currentAngle = this.getSteeringAngle();
        double speed = this.getDeltaMovement().horizontalDistance();
        boolean isMoving = speed > 0.05;

        boolean turningLeft = this.leftInputDown();
        boolean turningRight = this.rightInputDown();

        if (turningLeft && !turningRight) {
            currentAngle = Math.min(45.0F, currentAngle + 2.5F);
            this.setSteeringAngle(currentAngle);
        } else if (turningRight && !turningLeft) {
            currentAngle = Math.max(-45.0F, currentAngle - 2.5F);
            this.setSteeringAngle(currentAngle);
        } else if (isMoving && Math.abs(currentAngle) > 0.5F) {
            currentAngle *= 0.88F;
            this.setSteeringAngle(currentAngle);
        }

        if (isMoving && Math.abs(currentAngle) > 1.0F) {
            float turnAmount = currentAngle * 0.011F * (float) speed;
            this.setYRot(this.getYRot() + turnAmount);
        }

        this.prevWheelRotation = this.wheelRotation;
        this.wheelRotation += (float) (speed * 22.0D);
    }

    public void spawnAdditionalGrenades(TDADummyProjectile original) {
        float baseAngle = this.getYRot() - this.getTurretAngle();
        float pitch = -15.0F;

        float f = -pitch * ((float)Math.PI / 180F);
        float f1 = (float)Math.sin(f);
        float f2 = (float)Math.cos(f);

        float[] angles = new float[]{-25.0F, -8.0F, 8.0F, 25.0F};
        float speed = 0.55F;
        float inaccuracy = 0.6F;

        float yaw0 = baseAngle + angles[0];
        float f3_0 = -yaw0 * ((float)Math.PI / 180F);
        double vx0 = Math.sin(f3_0) * f2;
        double vy0 = f1;
        double vz0 = Math.cos(f3_0) * f2;
        original.shoot(vx0, vy0, vz0, speed, inaccuracy);

        for (int i = 1; i < angles.length; i++) {
            float yaw = baseAngle + angles[i];
            float f3 = -yaw * ((float)Math.PI / 180F);
            double vx = Math.sin(f3) * f2;
            double vy = f1;
            double vz = Math.cos(f3) * f2;

            TDADummyProjectile grenade = new TDADummyProjectile(ModEntities.TDA_DUMMY.get(), this.level());
            grenade.setAdditional(true);
            grenade.setPos(original.getX(), original.getY(), original.getZ());
            grenade.setOwner(original.getOwner());
            grenade.shoot(vx, vy, vz, speed, inaccuracy);
            this.level().addFreshEntity(grenade);
        }
    }

    private float getTurretAngle() {
        try {
            java.lang.reflect.Field field = com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity.class.getDeclaredField("turretYRot");
            field.setAccessible(true);
            return field.getFloat(this);
        } catch (Exception e) {
            return this.getYRot();
        }
    }

    static {
        STEERING_ANGLE = SynchedEntityData.defineId(Bmp1Entity.class, EntityDataSerializers.FLOAT);
    }
}