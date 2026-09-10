package me.noramibu.lumentooltips.tooltip;

import com.mojang.brigadier.StringReader;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Optional;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/** Budget checks; an in-game mixin test is still necessary. */
public final class TextBudgetRegression {
  public static void main(String[] args) throws Exception {
    if (args.length != 0) inspectFixture(Path.of(args[0]));
    for (int width : new int[] {48, 96, 192}) {
      long start = System.nanoTime();
      for (int run = 0; run < 100; run++) {
        var budget = budget();
        for (int cell = 0; cell < width * width / 2; cell++) {
          check(budget.enterComponent(new Object()), "glyph component rejected");
          check(budget.accept("\uD833\uDD00".length()), "glyph truncated");
          budget.exitComponent();
          check(budget.enterComponent(new Object()), "padding component rejected");
          check(budget.accept("\u200C\u200C".length()), "padding truncated");
          budget.exitComponent();
        }
      }
      System.out.println("Width " + width + ": " + (System.nanoTime() - start) / 100_000_000.0 + " ms/pass");
    }
    // A large literal argument wrapped in Minecraft's translated command response.
    var command = budget();
    check(command.enterComponent(new Object()), "command root rejected");
    check(command.enterTranslation(), "command translation rejected");
    check(command.enterComponent(new Object()), "data argument rejected");
    check(command.accept(100_000), "large command argument rejected");
    command.exitComponent();
    command.exitTranslation();
    command.exitComponent();
    // Reusing one argument repeatedly must still spend the small expansion budget.
    var repeated = budget();
    Object argument = new Object();
    check(repeated.enterTranslation(), "repeated translation rejected");
    check(repeated.enterComponent(argument), "first argument rejected");
    check(repeated.accept(16_384), "first argument truncated");
    repeated.exitComponent();
    check(repeated.enterComponent(argument), "second argument rejected");
    check(!repeated.accept(16_384), "repeated expansion escaped limit");
    // Equal strings in distinct components are not repeated graph expansion.
    var distinct = budget();
    check(distinct.enterTranslation(), "wrapper rejected");
    for (int i = 0; i < 100; i++) {
      check(distinct.enterComponent(new Object()), "distinct component rejected");
      check(distinct.accept(1000), "distinct text misclassified");
      distinct.exitComponent();
    }
    var literal = budget();
    check(literal.accept(524_288), "literal boundary rejected");
    check(!literal.accept(1), "unbounded literal text");
    var depth = budget();
    for (int i = 0; i < 128; i++) check(depth.enterComponent(new Object()), "early depth rejection");
    check(!depth.enterComponent(new Object()), "unbounded component nesting");
    var nodes = budget();
    for (int i = 0; i < 262_144; i++) {
      check(nodes.enterComponent(new Object()), "early node rejection");
      nodes.exitComponent();
    }
    check(!nodes.enterComponent(new Object()), "unbounded empty components");
    var translations = budget();
    for (int i = 0; i < 64; i++) check(translations.enterTranslation(), "early translation rejection");
    check(!translations.enterTranslation(), "unbounded translation depth");
    var visits = budget();
    for (int i = 0; i < 2048; i++) {
      check(visits.enterTranslation(), "early visit rejection");
      visits.exitTranslation();
    }
    check(!visits.enterTranslation(), "unbounded translation visits");
    var expansion = budget();
    check(expansion.enterTranslation(), "translation rejected");
    check(expansion.accept(8192), "translation boundary rejected");
    check(!expansion.accept(1), "unbounded translation expansion");
    System.out.println("Budget regressions passed");
  }

  private record Frame(Component component, int depth, boolean exit) {}

  private static void inspectFixture(Path path) throws Exception {
    String command = Files.readString(path);
    CompoundTag data;
    if (command.stripLeading().startsWith("{")) {
      var json = JsonParser.parseString(command).getAsJsonObject()
          .getAsJsonObject("components").getAsJsonObject("minecraft:entity_data");
      data = (CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    } else {
      StringReader reader = new StringReader(command);
      reader.setCursor(command.indexOf("minecraft:entity_data=") + "minecraft:entity_data=".length());
      data = TagParser.parseCompoundAsArgument(reader);
    }
    Component formatted = NbtUtils.toPrettyComponent(data.get("text"));
    var pending = new ArrayDeque<Frame>();
    pending.push(new Frame(formatted, 1, false));
    var budget = budget();
    budget.enterTranslation();
    int nodes = 0;
    int maxDepth = 0;
    int[] characters = {0};
    boolean[] accepted = {true};
    while (!pending.isEmpty()) {
      Frame frame = pending.pop();
      if (frame.exit()) {
        budget.exitComponent();
        continue;
      }
      nodes++;
      maxDepth = Math.max(maxDepth, frame.depth());
      accepted[0] &= budget.enterComponent(frame.component());
      frame.component().getContents().visit((FormattedText.ContentConsumer<Void>) text -> {
        characters[0] += text.length();
        accepted[0] &= budget.accept(text.length());
        return Optional.empty();
      });
      pending.push(new Frame(frame.component(), frame.depth(), true));
      var siblings = frame.component().getSiblings();
      for (int i = siblings.size() - 1; i >= 0; i--) {
        pending.push(new Frame(siblings.get(i), frame.depth() + 1, false));
      }
    }
    System.out.println("Exact /data fixture: nodes=" + nodes + ", depth=" + maxDepth
        + ", UTF-16=" + characters[0] + ", accepted=" + accepted[0]);
    check(accepted[0], "legitimate /data fixture rejected");
  }

  private static LumenTextGuard.Budget budget() {
    return new LumenTextGuard.Budget(8192, 64, 2048);
  }

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
}
