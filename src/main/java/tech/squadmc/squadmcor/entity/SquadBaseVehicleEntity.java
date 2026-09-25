package tech.squadmc.squadmcor.entity;

import com.atsuishio.superbwarfare.entity.vehicle.base.GeoVehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import tech.squadmc.squadmcor.smoke.VehicleSmokeCloudEntity;
import tech.squadmc.squadmcor.smoke.VehicleSmokeSystem;

public abstract class SquadBaseVehicleEntity extends GeoVehicleEntity {

    public static final EntityDataAccessor<Boolean> ENGINE_STARTED =
            SynchedEntityData.defineId(SquadBaseVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SMOKE_GENERATOR =
            SynchedEntityData.defineId(SquadBaseVehicleEntity.class, EntityDataSerializers.BOOLEAN);

    // --- Дымовая система: ДГ + дымогенератор (порт системы aasgranate) ---
    private int smokeVolleyCooldown = 0;
    private int smokeCharges = VehicleSmokeSystem.DEFAULT_CHARGES;
    @javax.annotation.Nullable
    private VehicleSmokeCloudEntity exhaustSmoke; // сервер; null, пока генератор не включали

    public SquadBaseVehicleEntity(EntityType<? extends SquadBaseVehicleEntity> type, Level world) {
        super(type, world);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ENGINE_STARTED, false);
        this.entityData.define(SMOKE_GENERATOR, false);
    }

    public boolean isEngineStarted() {
        return this.entityData.get(ENGINE_STARTED);
    }

    public void setEngineStarted(boolean started) {
        this.entityData.set(ENGINE_STARTED, started);
    }

    // --- Дымовая система: accessors ---
    public boolean isSmokeGeneratorOn() { return this.entityData.get(SMOKE_GENERATOR); }
    public void setSmokeGeneratorOn(boolean on) { this.entityData.set(SMOKE_GENERATOR, on); }
    public int getSmokeVolleyCooldown() { return this.smokeVolleyCooldown; }
    public void setSmokeVolleyCooldown(int v) { this.smokeVolleyCooldown = v; }
    public int getSmokeCharges() { return this.smokeCharges; }
    public void setSmokeCharges(int v) { this.smokeCharges = v; }
    @javax.annotation.Nullable public VehicleSmokeCloudEntity getExhaustSmoke() { return this.exhaustSmoke; }
    public void setExhaustSmoke(@javax.annotation.Nullable VehicleSmokeCloudEntity e) { this.exhaustSmoke = e; }

    /** Есть ли у машины дымовые гранатомёты. */
    public boolean hasSmokeLauncher() { return false; }
    /** Локальные offset'ы точек крепления ДГ (учитывается yaw корпуса). */
    public Vec3[] getSmokeLauncherPoints() { return new Vec3[0]; }
    /** Есть ли дымогенератор (постоянный дым из выхлопа, пока включён). */
    public boolean hasSmokeGenerator() { return false; }
    /** Локальный offset точки выпуска дыма генератора. */
    public Vec3 getSmokeGeneratorOffset() { return new Vec3(0.0, 1.0, -2.0); }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("EngineStarted", this.isEngineStarted());
        compound.putInt("SmokeCharges", this.smokeCharges);
        compound.putBoolean("SmokeGenerator", this.isSmokeGeneratorOn());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("EngineStarted")) {
            this.setEngineStarted(compound.getBoolean("EngineStarted"));
        }
        if (compound.contains("SmokeCharges")) this.smokeCharges = compound.getInt("SmokeCharges");
        if (compound.contains("SmokeGenerator")) this.setSmokeGeneratorOn(compound.getBoolean("SmokeGenerator"));
        // Облако генератора не сохраняем: после загрузки создастся заново при первом keepAlive-тике.
    }

    // =========================================================================
    // 1. РџРћР’РћР РћРў Р‘РђРЁРќР РЎ РљР›РђР’РРђРўРЈР Р« (A / D)
    // =========================================================================
    public boolean hasKeyboardTurretTraverse() {
        return false;
    }

    public float getTurretTraverseSpeed() {
        return 2.2F;
    }

    // =========================================================================
    // 2. Р”Р«РњРћР’Р«Р• Р“Р РђРќРђРўР« (TDA)
    // =========================================================================
    public void spawnAdditionalGrenades(TDADummyProjectile original) {
    }

    // =========================================================================
    // 3. Р‘Р›РћРљРР РћР’РљРђ Р”Р’РР–Р•РќРРЇ РџР Р Р’Р«РљР›Р®Р§Р•РќРќРћРњ Р”Р’РР“РђРўР•Р›Р•
    // =========================================================================
    @Override
    public void move(MoverType type, Vec3 movement) {
        if (!this.isEngineStarted()) {
            super.move(type, new Vec3(0.0, Math.min(movement.y, 0.0), 0.0));
            return;
        }
        super.move(type, movement);
    }

    @Override
    public void setDeltaMovement(Vec3 delta) {
        if (!this.isEngineStarted()) {
            super.setDeltaMovement(new Vec3(0.0, Math.min(delta.y, 0.0), 0.0));
            return;
        }
        super.setDeltaMovement(delta);
    }

    @Override
    public void setDeltaMovement(double x, double y, double z) {
        if (!this.isEngineStarted()) {
            super.setDeltaMovement(0.0, Math.min(y, 0.0), 0.0);
            return;
        }
        super.setDeltaMovement(x, y, z);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (this.smokeVolleyCooldown > 0) this.smokeVolleyCooldown--;
            if (hasSmokeGenerator() && isSmokeGeneratorOn()) VehicleSmokeSystem.tickGenerator(this);
        }

        double prevX = this.getX();
        double prevZ = this.getZ();

        super.tick();

        if (!this.isEngineStarted()) {
            this.setPos(prevX, this.getY(), prevZ);
            Vec3 delta = this.getDeltaMovement();
            super.setDeltaMovement(0.0, Math.min(delta.y, 0.0), 0.0);
        }
    }
}
