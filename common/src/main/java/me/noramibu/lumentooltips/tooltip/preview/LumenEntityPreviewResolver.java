package me.noramibu.lumentooltips.tooltip.preview;

import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfig;
import me.noramibu.lumentooltips.mixin.EntityAccessor;
import me.noramibu.lumentooltips.mixin.MobBucketItemAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;

final class LumenEntityPreviewResolver {
  private LumenEntityPreviewResolver() {}

  static boolean supports(ItemStack stack, LumenConfig.PreviewConfig config) {
    if (!sourceEnabled(stack, config)) {
      return false;
    }
    if (stack.getItem() instanceof SpawnEggItem || stack.getItem() instanceof MobBucketItem) {
      return true;
    }
    TypedEntityData<BlockEntityType<?>> blockEntityData =
        stack.get(DataComponents.BLOCK_ENTITY_DATA);
    return findSpawnerEntityTag(blockEntityData.copyTagWithoutId())
            .filter(tag -> supportsEntityTag(tag, config))
            .isPresent();
  }

  static Optional<Entity> create(ItemStack stack, LumenConfig.PreviewConfig config) {
    if (!sourceEnabled(stack, config)) {
      return Optional.empty();
    }
    Level level = Minecraft.getInstance().level;
    if (level == null) {
      return Optional.empty();
    }
    if (config.spawnEggs && stack.getItem() instanceof SpawnEggItem) {
      return createSpawnEggEntity(stack, level);
    }
    return config.mobBuckets && stack.getItem() instanceof MobBucketItem
        ? createBucketEntity(stack, level)
        : createSpawnerEntity(stack, level);
  }

  private static boolean sourceEnabled(ItemStack stack, LumenConfig.PreviewConfig config) {
    if (!config.entities) {
      return false;
    }
    if (stack.getItem() instanceof SpawnEggItem) {
      return config.spawnEggs;
    }
    if (stack.getItem() instanceof MobBucketItem) {
      return config.mobBuckets;
    }
    TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
    return config.spawners && data != null && data.type() == BlockEntityType.MOB_SPAWNER;
  }

  private static Optional<Entity> createSpawnEggEntity(ItemStack stack, Level level) {
    EntityType<?> type = SpawnEggItem.getType(stack);
    if (type == null) {
      return Optional.empty();
    }
    Entity entity = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
    if (entity == null) {
      return Optional.empty();
    }
    TypedEntityData<EntityType<?>> entityData = stack.get(DataComponents.ENTITY_DATA);
    try {
      entity.applyComponentsFromItemStack(stack);
      if (entityData != null && entityData.type() == type) {
        entityData.loadInto(entity);
      }
    } catch (RuntimeException ignored) {
      return Optional.empty();
    }
    return Optional.of(prepare(entity));
  }

  private static Optional<Entity> createBucketEntity(ItemStack stack, Level level) {
    MobBucketItem bucketItem = (MobBucketItem) stack.getItem();
    EntityType<? extends Mob> type = ((MobBucketItemAccessor) bucketItem).lumenTooltips$getType();
    Mob mob = type.create(level, EntitySpawnReason.BUCKET);
    if (mob == null) {
      return Optional.empty();
    }
    if (mob instanceof Bucketable bucketable) {
      CustomData entityData =
          stack.getOrDefault(DataComponents.BUCKET_ENTITY_DATA, CustomData.EMPTY);
      try {
        mob.applyComponentsFromItemStack(stack);
        bucketable.loadFromBucketTag(entityData.copyTag());
        bucketable.setFromBucket(true);
        ((EntityAccessor) mob).lumenTooltips$setInWater(true);
      } catch (RuntimeException ignored) {
        return Optional.empty();
      }
    }
    return Optional.of(prepare(mob));
  }

  private static Optional<Entity> createSpawnerEntity(ItemStack stack, Level level) {
    TypedEntityData<BlockEntityType<?>> blockEntityData =
        stack.get(DataComponents.BLOCK_ENTITY_DATA);
    return findSpawnerEntityTag(blockEntityData.copyTagWithoutId())
        .map(CompoundTag::copy)
        .map(
            tag ->
                EntityType.loadEntityRecursive(
                    tag, level, EntitySpawnReason.SPAWNER, EntityProcessor.NOP))
        .map(LumenEntityPreviewResolver::prepare);
  }

  private static Optional<CompoundTag> findSpawnerEntityTag(CompoundTag blockEntityTag) {
    Optional<CompoundTag> direct =
        blockEntityTag
            .getCompound("SpawnData")
            .flatMap(LumenEntityPreviewResolver::spawnDataEntity);
    if (direct.isPresent()) {
      return direct;
    }

    ListTag spawnPotentials = blockEntityTag.getListOrEmpty("SpawnPotentials");
    for (int index = 0; index < spawnPotentials.size(); index++) {
      Optional<CompoundTag> entity =
          spawnPotentials
              .getCompound(index)
              .flatMap(LumenEntityPreviewResolver::potentialSpawnDataEntity);
      if (entity.isPresent()) {
        return entity;
      }
    }
    return Optional.empty();
  }

  private static boolean supportsEntityTag(CompoundTag tag, LumenConfig.PreviewConfig config) {
    return tag.getString("id")
        .map(
            id ->
                switch (id) {
                  case "minecraft:area_effect_cloud" -> config.areaEffectClouds;
                  case "minecraft:block_display",
                      "minecraft:item_display",
                      "minecraft:text_display" -> config.displayEntities;
                  case "minecraft:item_frame", "minecraft:glow_item_frame" -> config.itemFrames;
                  default -> true;
                })
        .orElse(false);
  }

  private static Optional<CompoundTag> potentialSpawnDataEntity(CompoundTag potential) {
    return potential
        .getCompound("data")
        .flatMap(LumenEntityPreviewResolver::spawnDataEntity)
        .or(() -> spawnDataEntity(potential));
  }

  private static Optional<CompoundTag> spawnDataEntity(CompoundTag spawnData) {
    return spawnData
        .getCompound("entity")
        .filter(entityTag -> entityTag.getString("id").isPresent())
        .or(
            () ->
                spawnData.getString("id").isPresent()
                    ? Optional.of(spawnData)
                    : Optional.empty());
  }

  private static Entity prepare(Entity entity) {
    entity.snapTo(0.0, 0.0, 0.0, 180.0F, 0.0F);
    entity.setYHeadRot(180.0F);
    entity.setYBodyRot(180.0F);
    if (entity instanceof Display display) {
      display.tick();
    }
    return entity;
  }
}
