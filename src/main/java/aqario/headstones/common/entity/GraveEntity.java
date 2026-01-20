package aqario.headstones.common.entity;

import aqario.headstones.common.Headstones;
import aqario.headstones.common.config.HeadstonesConfig;
import aqario.headstones.common.integration.TrinketsIntegration;
import aqario.headstones.common.network.HeadstonesEntityDataSerializers;
import aqario.headstones.common.screen.GraveScreenHandler;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class GraveEntity extends Entity implements ContainerListener, MenuProvider {
    public static final EntityDataAccessor<Optional<EntityReference<Player>>> OWNER = SynchedEntityData.defineId(
        GraveEntity.class,
        HeadstonesEntityDataSerializers.OPTIONAL_PLAYER_REFERENCE
    );
    public final float uniqueOffset;
    public final SimpleContainer items = new SimpleContainer(54);
    public final float bobOffset = this.random.nextFloat() * (float) Math.PI * 2.0F;

    public GraveEntity(EntityType<?> type, Level world) {
        super(type, world);
        this.uniqueOffset = this.random.nextFloat() * (float) Math.PI * 2.0F;
        this.items.addListener(this);
    }

    public static GraveEntity create(Player player) {
        GraveEntity grave = new GraveEntity(HeadstonesEntityTypes.GRAVE, player.level());
        grave.setPosRaw(player.getX(), player.getY(), player.getZ());
        grave.setCustomName(player.getName());
        grave.setOwner(player);

        List<ItemStack> items = new ObjectArrayList<>();
        items.addAll(player.getInventory().items);
        items.addAll(player.equipment.items.values().stream().toList().reversed());

        grave.items.clearContent();
        for(ItemStack item : items) {
            grave.items.addItem(item);
        }
        if(Headstones.isTrinketsLoaded()) {
            TrinketsIntegration.putTrinketsInGrave(player, grave);
        }
        grave.setOldPosAndRot();
        grave.reapplyPosition();
        return grave;
    }

    @NotNull
    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @NotNull
    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if(!HeadstonesConfig.openOtherGraves
            && this.getOwnerReference() != null
            && !(player.level() instanceof ServerLevel level
            && player.equals(EntityReference.getPlayer(this.getOwnerReference(), level)))) {
            return super.interact(player, hand);
        }
        if(!player.level().isClientSide()
            && player instanceof ServerPlayer serverPlayer
        ) {
            serverPlayer.openMenu(this);
        }
        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new GraveScreenHandler(syncId, playerInventory, this.items, this);
    }

    @Override
    public void tick() {
        super.tick();
        this.xo = this.getX();
        this.yo = this.getY();
        this.zo = this.getZ();
        Vec3 vec3d = this.getDeltaMovement();

        float f = this.getEyeHeight() - 0.11111111F;
        if(this.isInWater() && this.getFluidHeight(FluidTags.WATER) > (double) f) {
            this.applyWaterBuoyancy();
        }
        else if(this.isInLava() && this.getFluidHeight(FluidTags.LAVA) > (double) f) {
            this.applyLavaBuoyancy();
        }
        else if(!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        if(this.level().isClientSide()) {
            this.noPhysics = false;
        }
        else {
            this.noPhysics = !this.level().noCollision(this, this.getBoundingBox().deflate(1.0E-7));
            if(this.noPhysics) {
                this.moveTowardsClosestSpace(this.getX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0, this.getZ());
            }
        }

        if(!this.onGround() || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5F || (this.tickCount + this.getId()) % 4 == 0) {
            this.move(MoverType.SELF, this.getDeltaMovement());
            float g = 0.98F;
            if(this.onGround()) {
                g = this.level().getBlockState(new BlockPos((int) this.getX(), (int) (this.getY() - 1.0), (int) this.getZ())).getBlock().getFriction() * 0.98F;
            }

            this.setDeltaMovement(this.getDeltaMovement().multiply(g, 0.98, g));
            if(this.onGround()) {
                Vec3 vec3d2 = this.getDeltaMovement();
                if(vec3d2.y < 0.0) {
                    this.setDeltaMovement(vec3d2.multiply(1.0, -0.5, 1.0));
                }
            }
        }

        this.needsSync |= this.updateInWaterStateAndDoFluidPushing();
        if(!this.level().isClientSide()) {
            double d = this.getDeltaMovement().subtract(vec3d).lengthSqr();
            if(d > 0.01) {
                this.needsSync = true;
            }
        }
    }

    private void applyWaterBuoyancy() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.99F, vec3d.y + (double) (vec3d.y < 0.06F ? 5.0E-4F : 0.0F), vec3d.z * 0.99F);
    }

    private void applyLavaBuoyancy() {
        Vec3 vec3d = this.getDeltaMovement();
        this.setDeltaMovement(vec3d.x * 0.95F, vec3d.y + (double) (vec3d.y < 0.06F ? 5.0E-4F : 0.0F), vec3d.z * 0.95F);
    }

    @Override
    public void playerTouch(Player player) {
        if(this.level() instanceof ServerLevel level) {
            if(!this.getBoundingBox().intersects(player.getBoundingBox())) {
                return;
            }

            if(this.getOwnerReference() != null
                && player.equals(EntityReference.getPlayer(this.getOwnerReference(), level))
            ) {
                if(this.items != null) {
                    for(int i = 0; i < this.items.getContainerSize(); ++i) {
                        ItemStack stack = this.items.getItem(i);
                        if(stack.isEmpty()) {
                            continue;
                        }
                        this.spawnAtLocation(level, stack);
                    }
                }
                this.discard();
            }
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Nullable
    @Override
    public ItemEntity spawnAtLocation(ServerLevel level, ItemStack stack) {
        if(stack.isEmpty()) {
            return null;
        }
        if(this.level().isClientSide()) {
            return null;
        }
        ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), stack);
        entity.setNoPickUpDelay();
        this.level().addFreshEntity(entity);
        return entity;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return HeadstonesConfig.highlightGraves && this.level().isClientSide() && this.getOwnerReference() != null || super.isCurrentlyGlowing();
    }

    @Nullable
    public EntityReference<Player> getOwnerReference() {
        return this.entityData.get(OWNER).orElse(null);
    }

    public void setOwner(@Nullable Player entity) {
        this.entityData.set(OWNER, Optional.ofNullable(entity).map(EntityReference::of));
    }

    public void setOwnerReference(@Nullable EntityReference<Player> reference) {
        this.entityData.set(OWNER, Optional.ofNullable(reference));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        EntityReference<Player> entityReference = this.getOwnerReference();
        EntityReference.store(entityReference, output, "owner");
        this.writeInventoryToTag(output);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        EntityReference<Player> entityReference = EntityReference.readWithOldOwnerConversion(input, "owner", this.level());
        this.setOwnerReference(entityReference);
        this.readInventoryFromTag(input);
    }

    private void readInventoryFromTag(ValueInput valueInput) {
        valueInput.list("items", ItemStack.CODEC).ifPresent(this.items::fromItemList);
    }

    private void writeInventoryToTag(ValueOutput valueOutput) {
        this.items.storeAsItemList(valueOutput.list("items", ItemStack.CODEC));
    }

    public float getRotation(float tickDelta) {
        return (this.tickCount + tickDelta) / 20.0F + this.uniqueOffset;
    }

    @NotNull
    @Override
    public SoundSource getSoundSource() {
        return SoundSource.AMBIENT;
    }

    @Override
    public float getVisualRotationYInDegrees() {
        return 180.0F - this.getRotation(0.5F) / (float) (Math.PI * 2) * 360.0F;
    }

    @Override
    public void containerChanged(Container sender) {
    }
}
