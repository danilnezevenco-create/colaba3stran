package tech.squadmc.squadmcor.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import tech.squadmc.squadmcor.init.ModEntities;
import tech.squadmc.squadmcor.smoke.VehicleSmokeCloudEntity;
import tech.squadmc.squadmcor.smoke.VehicleSmokeSystem;

public class TDADummyProjectile extends ThrowableItemProjectile {
    private boolean isAdditional = false;

    public TDADummyProjectile(EntityType<? extends TDADummyProjectile> type, Level level) {
        super(type, level);
    }

    public void setAdditional(boolean additional) {
        this.isAdditional = additional;
    }

    public boolean isAdditional() {
        return this.isAdditional;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        this.explode();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        this.explode();
    }

    @Override
    public void tick() {
        if (this.tickCount == 1 && !this.level().isClientSide && !this.isAdditional) {
            Entity owner = this.getOwner();
            if (owner != null) {
                Entity vehicle = owner.getVehicle();
                if (vehicle instanceof btr82Entity btr) {
                    int seat = btr.getSeatIndex(owner);
                    if (seat == 1) {
                        btr.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof bmp2Entity bmp) {
                    int seat = bmp.getSeatIndex(owner);
                    if (seat == 1) {
                        bmp.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof lav25Entity lav) {
                    int seat = lav.getSeatIndex(owner);
                    if (seat == 1) {
                        lav.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof bradleyEntity bradley) {
                    int seat = bradley.getSeatIndex(owner);
                    if (seat == 1) {
                        bradley.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof bmd4Entity bmd4) {
                    int seat = bmd4.getSeatIndex(owner);
                    if (seat == 1) {
                        bmd4.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof T72AVEntity t72av) {
                    int seat = t72av.getSeatIndex(owner);
                    if (seat == 1) {
                        t72av.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof Bmp1Entity bmp1) {
                    int seat = bmp1.getSeatIndex(owner);
                    if (seat == 1) {
                        bmp1.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof t80Entity t80) {
                    int seat = t80.getSeatIndex(owner);
                    if (seat == 1) {
                        t80.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof m1a2Entity m1a2) {
                    int seat = m1a2.getSeatIndex(owner);
                    if (seat == 1) {
                        m1a2.spawnAdditionalGrenades(this);
                    }
                } else if (vehicle instanceof sprutEntity sprut) {
                    int seat = sprut.getSeatIndex(owner);
                    if (seat == 1) {
                        sprut.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof StrykerM2Entity stryker) {
                    int seat = stryker.getSeatIndex(owner);
                    if (seat == 1) {
                        stryker.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof m1a2Entity_sand m1a2) {
                    int seat = m1a2.getSeatIndex(owner);
                    if (seat == 1) {
                        m1a2.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof bradley_sandEntity bradley) {
                    int seat = bradley.getSeatIndex(owner);
                    if (seat == 1) {
                        bradley.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof brdmEntity brdm2) {
                    int seat = brdm2.getSeatIndex(owner);
                    if (seat == 1) {
                        brdm2.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof leopardEntity leopard) {
                    int seat = leopard.getSeatIndex(owner);
                    if (seat == 1) {
                        leopard.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof PumaEntity puma) {
                    int seat = puma.getSeatIndex(owner);
                    if (seat == 1) {
                        puma.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof T64Entity t64) {
                    int seat = t64.getSeatIndex(owner);
                    if (seat == 1) {
                        t64.spawnAdditionalGrenades(this);
                    }
                }
                else if (vehicle instanceof FenekEntity fenek) {
                    int seat = fenek.getSeatIndex(owner);
                    if (seat == 1) {
                        fenek.spawnAdditionalGrenades(this);
                    }
                }
                // ==================================
            }
        }

        super.tick();

        if (this.tickCount > 30) {
            this.explode();
        }
    }

    private void explode() {
        if (!this.level().isClientSide) {
            boolean isDriver = false;
            Entity owner = this.getOwner();

            if (owner != null) {
                Entity vehicle = owner.getVehicle();
                if (vehicle instanceof btr82Entity btr) {
                    if (btr.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof bmd4Entity bmp) {
                    if (bmp.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof lav25Entity lav) {
                    if (lav.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof bradleyEntity bradley) {
                    if (bradley.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof T72AVEntity t72av) {
                    if (t72av.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof Bmp1Entity bmp1) {
                    if (bmp1.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof m1a2Entity m1a2) {
                    if (m1a2.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                } else if (vehicle instanceof sprutEntity sprut) {
                    if (sprut.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof StrykerM2Entity stryker) {
                    if (stryker.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof t80Entity t80) {
                    if (t80.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof m1a2Entity_sand m1a2) {
                    if (m1a2.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof bradley_sandEntity bradley) {
                    if (bradley.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof mtlbEntity mtlb) {
                    if (mtlb.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof leopardEntity leopard) {
                    if (leopard.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof PumaEntity puma) {
                    if (puma.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof T64Entity t64) {
                    if (t64.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
                else if (vehicle instanceof FenekEntity fenek) {
                    if (fenek.getSeatIndex(owner) == 0) {
                        isDriver = true;
                    }
                }
            }

            if (!isDriver) {
                this.level().playSound(
                        null,
                        this.getX(), this.getY(), this.getZ(),
                        tech.squadmc.squadmcor.init.ModSounds.BTR82_SMOKE_EXPLODE.get(),
                        net.minecraft.sounds.SoundSource.NEUTRAL,
                        2.0F,
                        1.0F
                );
                this.spawnSmokeCloud();
            }

            this.discard();
        }
    }

    /** Новая объёмная система дыма (VehicleSmokeCloudEntity) вместо старых ванильных частиц. Только сервер. */
    private void spawnSmokeCloud() {
        if (this.level().isClientSide) return;

        VehicleSmokeCloudEntity cloud =
                new VehicleSmokeCloudEntity(ModEntities.VEHICLE_SMOKE_CLOUD.get(), this.level());
        cloud.setPos(this.position());
        cloud.configure(
                VehicleSmokeSystem.LAUNCHER_RADIUS,
                VehicleSmokeSystem.LAUNCHER_HEIGHT,
                VehicleSmokeSystem.LAUNCHER_GROW_TICKS,
                VehicleSmokeSystem.LAUNCHER_HOLD_TICKS,
                VehicleSmokeSystem.LAUNCHER_FADE_TICKS);
        this.level().addFreshEntity(cloud);
    }
}