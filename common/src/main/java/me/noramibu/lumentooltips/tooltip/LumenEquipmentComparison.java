package me.noramibu.lumentooltips.tooltip;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

final class LumenEquipmentComparison {
  private static final double EPSILON = 1.0E-7;

  private LumenEquipmentComparison() {}

  static void append(ItemStack candidate, Player player, List<Component> tooltip) {
    if (player == null) {
      return;
    }
    EquipmentSlot slot = player.getEquipmentSlotForItem(candidate);
    ItemAttributeModifiers candidateModifiers =
        candidate.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    if (candidateModifiers.modifiers().stream()
        .noneMatch(entry -> entry.slot().test(slot) && isNumericModifier(entry))) {
      return;
    }
    ItemStack equipped = player.getItemBySlot(slot);
    if (equipped.isEmpty() || ItemStack.isSameItemSameComponents(candidate, equipped)) {
      return;
    }
    List<Difference> differences =
        differences(
            candidateModifiers,
            equipped.getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY),
            slot);
    if (differences.isEmpty()) {
      return;
    }
    tooltip.add(
        Component.translatable(
                "tooltip.lumen_tooltips.compared_with", equipped.getHoverName())
            .withStyle(ChatFormatting.DARK_GRAY));
    differences.forEach(difference -> tooltip.add(displayLine(difference)));
  }

  private static List<Difference> differences(
      ItemAttributeModifiers candidate,
      ItemAttributeModifiers equipped,
      EquipmentSlot slot) {
    Map<Key, Double> amounts = new LinkedHashMap<>();
    add(amounts, candidate, slot, 1.0);
    add(amounts, equipped, slot, -1.0);
    return amounts.entrySet().stream()
        .filter(entry -> Math.abs(entry.getValue()) > EPSILON)
        .map(
            entry ->
                new Difference(
                    entry.getKey().attribute(), entry.getKey().operation(), entry.getValue()))
        .toList();
  }

  private static void add(
      Map<Key, Double> amounts,
      ItemAttributeModifiers modifiers,
      EquipmentSlot slot,
      double multiplier) {
    for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
      if (entry.slot().test(slot) && isNumericModifier(entry)) {
        Key key = new Key(entry.attribute(), entry.modifier().operation());
        amounts.merge(key, entry.modifier().amount() * multiplier, Double::sum);
      }
    }
  }

  private static boolean isNumericModifier(ItemAttributeModifiers.Entry entry) {
    return entry.display() instanceof ItemAttributeModifiers.Display.Default;
  }

  private static Component displayLine(Difference difference) {
    boolean increase = difference.amount() > 0.0;
    double amount = Math.abs(difference.amount());
    if (difference.operation() != AttributeModifier.Operation.ADD_VALUE) {
      amount *= 100.0;
    } else if (difference.attribute().equals(Attributes.KNOCKBACK_RESISTANCE)) {
      amount *= 10.0;
    }
    Component attributeName =
        Component.translatable(difference.attribute().value().getDescriptionId());
    Component value =
        Component.translatable(
                "attribute.modifier."
                    + (increase ? "plus." : "take.")
                    + difference.operation().id(),
                ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(amount),
                attributeName)
            .withStyle(difference.attribute().value().getStyle(increase));
    return CommonComponents.space().append(value);
  }

  private record Difference(
      Holder<Attribute> attribute, AttributeModifier.Operation operation, double amount) {}

  private record Key(Holder<Attribute> attribute, AttributeModifier.Operation operation) {}
}
