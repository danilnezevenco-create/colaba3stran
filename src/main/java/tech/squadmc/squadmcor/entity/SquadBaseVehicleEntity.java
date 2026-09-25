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
    private VehicleSmokeCloudEntity exhaustSmoke; // не используется дымогенератором с версии на шлейфах, оставлено для совместимости

    /**
     * "Запас" тиков, на протяжении которых генератор считается включённым
     * после последнего замеченного импульса стрельбы. Нужен, потому что
     * getShootAnimationTimer(...) при авто-огне САМ мигает 0/>0 между
     * выстрелами (например, RPM 600 + ShootAnimationTime 1 даёт >0 только
     * 1 тик из 2) — без запаса генератор гас бы каждый второй тик.
     */
    private int smokeGraceTicks = 0;
    private static final int SMOKE_GRACE_TICKS = 6;

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

    /**
     * Обновить состояние дымогенератора и, если он сейчас должен дымить,
     * тут же (в этом же тике) попытаться оставить очередной клубок шлейфа.
     * Вызывать из tick() конкретной техники ПОСЛЕ super.tick(), передавая
     * "живой" импульс стрельбы за этот тик (например,
     * getShootAnimationTimer(...) > 0).
     *
     * ВАЖНО: firingPulse — это именно импульс, он сам мигает 0/>0 между
     * выстрелами при авто-огне (так уже работает в других машинах). Чтобы
     * генератор не гас в паузах между импульсами, используется "запас"
     * SMOKE_GRACE_TICKS: пока он не иссяк, генератор считается включённым
     * даже если в этот конкретный тик импульса не было.
     */
    protected void updateSmokeGenerator(boolean firingPulse) {
        if (firingPulse) {
            this.smokeGraceTicks = SMOKE_GRACE_TICKS;
        } else if (this.smokeGraceTicks > 0) {
            this.smokeGraceTicks--;
        }

        boolean on = this.smokeGraceTicks > 0;
        this.setSmokeGeneratorOn(on);
        if (!this.level().isClientSide && hasSmokeGenerator() && on) {
            VehicleSmokeSystem.tickGenerator(this);
        }
    }

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
        // Облако генератора не сохраняем: клубки шлейфа сами по себе временные сущности.
    }

    // =========================================================================
    // 1. РџРћР’РћР РћРў Р‘РђРЁРќР РЎ РљР›РђР’РРђРўРЈР Р« (A / D)
    // =========================================================================
    public boolean hasKeyboardTurretTraverse() {
        return false;
    }

    public float getTurretTraverseSpeed() {
        return 2.2F;
    }

    // =========================================================================
    // 2. Р”Р«РњРћР’Р«Р• Р“Р РђРќРђРўР« (TDA)
    // =========================================================================
    public void spawnAdditionalGrenades(TDADummyProjectile original) {
    }

    // =========================================================================
    // 3. Р‘Р›РћРљРР РћР’РљРђ Р”Р’РР–Р•РќРРЇ РџР Р Р’Р«РљР›Р®Р§Р•РќРќРћРњ Р”Р’РР“РђРўР•Р›Р•
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
            // Обновление и запуск дымогенератора теперь делает updateSmokeGenerator(),
            // вызываемый из tick() конкретной техники (см. T64Entity) — на свежем,
            // а не на прошлом тике.
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