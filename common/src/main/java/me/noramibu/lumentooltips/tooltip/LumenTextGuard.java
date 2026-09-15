package me.noramibu.lumentooltips.tooltip;

import java.util.IdentityHashMap;
import java.util.List;
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
    private static final String TRANSLATION_WARNING_KEY = "tooltip.lumen_tooltips.translation_crash_prevented";

    private LumenTextGuard() {}

    public static <T> FormattedText.ContentConsumer<T> guard(FormattedText.ContentConsumer<T> output) {
        return output instanceof GuardedConsumer || !enabled() ? output : new PlainGuard<>(output, new Budget());
    }

    public static <T> FormattedText.StyledContentConsumer<T> guard(FormattedText.StyledContentConsumer<T> output) {
        return output instanceof GuardedConsumer || !enabled() ? output : new StyledGuard<>(output, new Budget());
    }

    public static <T> FormattedText.ContentConsumer<T> guardComponent(FormattedText.ContentConsumer<T> output) {
        return output instanceof GuardedConsumer || !globalEnabled() ? output : new PlainGuard<>(output, new Budget());
    }

    public static <T> FormattedText.StyledContentConsumer<T> guardComponent(
            FormattedText.StyledContentConsumer<T> output) {
        return output instanceof GuardedConsumer || !globalEnabled() ? output : new StyledGuard<>(output, new Budget());
    }

    public static boolean shouldInspect(Object output, Component component) {
        return output instanceof GuardedConsumer guarded
                && !(output instanceof InspectionConsumer)
                && globalEnabled()
                && (!(component.getContents() instanceof PlainTextContents)
                        || !component.getSiblings().isEmpty())
                && guarded.budget().beginInspection();
    }

    public static boolean inspect(Component component, Object output) {
        InspectionConsumer inspection = new InspectionConsumer();
        component.visit(inspection);
        if (inspection.budget().blocked && output instanceof GuardedConsumer guarded) {
            guarded.budget().translationFailure = inspection.budget().translationFailure;
        }
        return !inspection.budget().blocked;
    }

    public static Component protect(Component component) {
        if (!enabled()) return component;
        InspectionConsumer inspection = new InspectionConsumer();
        component.visit(inspection);
        return inspection.budget().blocked
                ? Component.literal(warningText(inspection.budget())).withStyle(warningStyle(component.getStyle()))
                : component;
    }

    public static List<Component> protectTooltip(List<Component> tooltip) {
        return !enabled() || globalEnabled()
                ? tooltip
                : tooltip.stream().map(LumenTextGuard::protect).toList();
    }

    public static boolean enterTranslation(Object output) {
        return !(output instanceof GuardedConsumer guarded) || guarded.budget().enterTranslation();
    }

    public static boolean enterComponent(Object output, Component component) {
        return !(output instanceof GuardedConsumer guarded) || guarded.budget().enterComponent(component);
    }

    public static void exitComponent(Object output) {
        if (output instanceof GuardedConsumer guarded) {
            guarded.budget().exitComponent();
        }
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

    public static <T> Optional<T> reject(FormattedText.StyledContentConsumer<T> output, Style style) {
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
        return warningText(WARNING_KEY);
    }

    private static String warningText(Budget budget) {
        return warningText(budget.translationFailure ? TRANSLATION_WARNING_KEY : WARNING_KEY);
    }

    private static String warningText(String key) {
        String warning = Language.getInstance().getOrDefault(key);
        return warning.length() <= 256 ? warning : key;
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

    private record InspectionConsumer(Budget budget) implements FormattedText.ContentConsumer<Unit>, GuardedConsumer {
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
            return this.budget.takeWarning() ? this.output.accept(warningText(this.budget)) : Optional.empty();
        }
    }

    private record StyledGuard<T>(FormattedText.StyledContentConsumer<T> output, Budget budget)
            implements FormattedText.StyledContentConsumer<T>, GuardedConsumer {
        @Override
        public Optional<T> accept(Style style, String text) {
            return this.budget.accept(text.length()) ? this.output.accept(style, text) : this.warning(style);
        }

        private Optional<T> warning(Style style) {
            return this.budget.takeWarning()
                    ? this.output.accept(warningStyle(style), warningText(this.budget))
                    : Optional.empty();
        }
    }

    static final class Budget {
        private final int maxTranslationDepth;
        private final int maxTranslationVisits;
        private final boolean limitCharacters;
        private int remainingCharacters;
        private int remainingLiteralCharacters = 524_288;
        private final IdentityHashMap<Object, Boolean> visitedComponents = new IdentityHashMap<>();
        private final boolean[] repeatedComponents = new boolean[128];
        private int componentDepth;
        private int componentVisits;
        private int translationDepth;
        private int translationVisits;
        private boolean blocked;
        private boolean translationFailure;
        private boolean inspected;
        private boolean warned;

        private Budget() {
            this(
                    LumenConfigManager.current().modules.safety.textLengthLimit,
                    LumenConfigManager.current().modules.safety.maxCharacters,
                    LumenConfigManager.current().modules.safety.maxTranslationDepth,
                    LumenConfigManager.current().modules.safety.maxTranslationVisits);
        }

        Budget(int characters, int depth, int visits) {
            this(true, characters, depth, visits);
        }

        private Budget(boolean limitCharacters, int characters, int depth, int visits) {
            this.limitCharacters = limitCharacters;
            this.remainingCharacters = characters;
            this.maxTranslationDepth = depth;
            this.maxTranslationVisits = visits;
        }

        private boolean beginInspection() {
            if (this.inspected) {
                return false;
            }
            this.inspected = true;
            return true;
        }

        boolean accept(int length) {
            if (this.blocked) return false;
            if (!this.limitCharacters) return true;
            boolean expanded = this.translationDepth > 0
                    && (this.componentDepth == 0 || this.repeatedComponents[this.componentDepth - 1]);
            if (length > this.remainingLiteralCharacters) {
                this.blocked = true;
                return false;
            }
            if (expanded && length > this.remainingCharacters) {
                this.blocked = this.translationFailure = true;
                return false;
            }
            if (expanded) {
                this.remainingCharacters -= length;
            }
            this.remainingLiteralCharacters -= length;
            return true;
        }

        boolean enterComponent(Object component) {
            this.componentDepth++;
            if (++this.componentVisits > 262_144 || this.componentDepth > 128) {
                this.blocked = true;
            }
            if (!this.blocked) {
                this.repeatedComponents[this.componentDepth - 1] =
                        this.visitedComponents.put(component, Boolean.TRUE) != null;
            }
            return !this.blocked;
        }

        void exitComponent() {
            this.componentDepth--;
        }

        boolean enterTranslation() {
            this.translationDepth++;
            if (this.blocked) return false;
            if (++this.translationVisits > this.maxTranslationVisits
                    || this.translationDepth > this.maxTranslationDepth) {
                this.blocked = this.translationFailure = true;
                return false;
            }
            return true;
        }

        void exitTranslation() {
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
