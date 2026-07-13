package me.noramibu.lumentooltips.tooltip;

import java.util.Optional;
import me.noramibu.lumentooltips.config.LumenConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.util.Unit;

public final class LumenTextGuard {
  private static final String WARNING_KEY = "tooltip.lumen_tooltips.unsafe_text";

  private LumenTextGuard() {}

  public static <T> FormattedText.ContentConsumer<T> guard(
      FormattedText.ContentConsumer<T> output) {
    return output instanceof GuardedConsumer || !enabled()
        ? output
        : new PlainGuard<>(output, new Budget());
  }

  public static <T> FormattedText.StyledContentConsumer<T> guard(
      FormattedText.StyledContentConsumer<T> output) {
    return output instanceof GuardedConsumer || !enabled()
        ? output
        : new StyledGuard<>(output, new Budget());
  }

  public static <T> FormattedText.ContentConsumer<T> guardComponent(
      FormattedText.ContentConsumer<T> output) {
    return output instanceof GuardedConsumer || !globalEnabled()
        ? output
        : new PlainGuard<>(output, new Budget());
  }

  public static <T> FormattedText.StyledContentConsumer<T> guardComponent(
      FormattedText.StyledContentConsumer<T> output) {
    return output instanceof GuardedConsumer || !globalEnabled()
        ? output
        : new StyledGuard<>(output, new Budget());
  }

  public static boolean shouldInspect(Object output, Component component) {
    return output instanceof GuardedConsumer guarded
        && !(output instanceof InspectionConsumer)
        && globalEnabled()
        && (!(component.getContents() instanceof PlainTextContents)
            || !component.getSiblings().isEmpty())
        && guarded.budget().beginInspection();
  }

  public static boolean inspect(Component component) {
    InspectionConsumer inspection = new InspectionConsumer();
    component.visit(inspection);
    return !inspection.budget().blocked;
  }

  public static boolean enterTranslation(Object output) {
    return !(output instanceof GuardedConsumer guarded)
        || guarded.budget().enterTranslation();
  }

  public static void exitTranslation(Object output) {
    if (output instanceof GuardedConsumer guarded) {
      guarded.budget().exitTranslation();
    }
  }

  public static <T> Optional<T> reject(FormattedText.ContentConsumer<T> output) {
    if (output instanceof PlainGuard<?> guarded) {
      return LumenTextGuard.<PlainGuard<T>>cast(guarded).warning();
    }
    return output.accept(warningText());
  }

  public static <T> Optional<T> reject(
      FormattedText.StyledContentConsumer<T> output, Style style) {
    if (output instanceof StyledGuard<?> guarded) {
      return LumenTextGuard.<StyledGuard<T>>cast(guarded).warning(style);
    }
    return output.accept(warningStyle(style), warningText());
  }

  private static boolean enabled() {
    return LumenConfigManager.current().modules.safety.translationCrashFix;
  }

  private static boolean globalEnabled() {
    var safety = LumenConfigManager.current().modules.safety;
    return safety.translationCrashFix && safety.globalComponentVisitGuard;
  }

  @SuppressWarnings("unchecked")
  private static <T> T cast(Object value) {
    return (T) value;
  }

  private static String warningText() {
    String warning = Language.getInstance().getOrDefault(WARNING_KEY);
    return warning.length() <= 256 ? warning : WARNING_KEY;
  }

  private static Style warningStyle(Style style) {
    return Style.EMPTY
        .withBold(style.isBold())
        .withItalic(style.isItalic())
        .withUnderlined(style.isUnderlined())
        .withStrikethrough(style.isStrikethrough())
        .withColor(ChatFormatting.RED);
  }

  private interface GuardedConsumer {
    Budget budget();
  }

  private record InspectionConsumer(Budget budget)
      implements FormattedText.ContentConsumer<Unit>, GuardedConsumer {
    private InspectionConsumer() {
      this(new Budget());
    }

    @Override
    public Optional<Unit> accept(String text) {
      return this.budget.accept(text.length()) ? Optional.empty() : FormattedText.STOP_ITERATION;
    }
  }

  private record PlainGuard<T>(FormattedText.ContentConsumer<T> output, Budget budget)
      implements FormattedText.ContentConsumer<T>, GuardedConsumer {
    @Override
    public Optional<T> accept(String text) {
      return this.budget.accept(text.length()) ? this.output.accept(text) : warning();
    }

    private Optional<T> warning() {
      return this.budget.takeWarning() ? this.output.accept(warningText()) : Optional.empty();
    }
  }

  private record StyledGuard<T>(FormattedText.StyledContentConsumer<T> output, Budget budget)
      implements FormattedText.StyledContentConsumer<T>, GuardedConsumer {
    @Override
    public Optional<T> accept(Style style, String text) {
      return this.budget.accept(text.length())
          ? this.output.accept(style, text)
          : this.warning(style);
    }

    private Optional<T> warning(Style style) {
      return this.budget.takeWarning()
          ? this.output.accept(warningStyle(style), warningText())
          : Optional.empty();
    }
  }

  private static final class Budget {
    private final int maxTranslationDepth;
    private final int maxTranslationVisits;
    private int remainingCharacters;
    private int translationDepth;
    private int translationVisits;
    private boolean blocked;
    private boolean inspected;
    private boolean warned;

    private Budget() {
      var safety = LumenConfigManager.current().modules.safety;
      this.remainingCharacters = safety.maxCharacters;
      this.maxTranslationDepth = safety.maxTranslationDepth;
      this.maxTranslationVisits = safety.maxTranslationVisits;
    }

    private boolean beginInspection() {
      if (this.inspected) {
        return false;
      }
      this.inspected = true;
      return true;
    }

    private boolean accept(int length) {
      if (this.blocked || length > this.remainingCharacters) {
        this.blocked = true;
        return false;
      }
      this.remainingCharacters -= length;
      return true;
    }

    private boolean enterTranslation() {
      this.translationDepth++;
      if (this.blocked
          || ++this.translationVisits > this.maxTranslationVisits
          || this.translationDepth > this.maxTranslationDepth) {
        this.blocked = true;
        return false;
      }
      return true;
    }

    private void exitTranslation() {
      if (this.translationDepth > 0) {
        this.translationDepth--;
      }
    }

    private boolean takeWarning() {
      if (this.warned) {
        return false;
      }
      this.warned = true;
      return true;
    }
  }
}
