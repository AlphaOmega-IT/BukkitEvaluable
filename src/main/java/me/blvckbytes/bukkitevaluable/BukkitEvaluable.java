/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitevaluable;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import me.blvckbytes.bbconfigmapper.ConfigValue;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.gpeee.IExpressionEvaluator;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import me.blvckbytes.utilitytypes.FUnsafeTriFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class BukkitEvaluable extends ConfigValue {

  private static final Map<Class<?>, Map<String, Object>> enumCache;

  static {
    enumCache = new HashMap<>();
  }

  public BukkitEvaluable(@Nullable Object value, @Nullable IExpressionEvaluator evaluator) {
    super(value, evaluator);
  }

  public static BukkitEvaluable of(@Nullable Object value) {
    return new BukkitEvaluable(value, null);
  }

  public Component asTextComponent(IEvaluationEnvironment environment) {
    return MiniMessage.miniMessage().deserialize(asString(environment));
  }

  public @Nullable XMaterial asXMaterial(IEvaluationEnvironment environment) {
    return XMaterial.matchXMaterial(asString(environment)).orElse(null);
  }

  public @Nullable Enchantment asEnchantment(IEvaluationEnvironment environment) {
    XEnchantment xEnchantment = XEnchantment.matchXEnchantment(asString(environment)).orElse(null);
    return xEnchantment == null ? null : xEnchantment.getEnchant();
  }

  @SuppressWarnings("deprecation")
  public @Nullable PotionType asPotionType(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);

    // Try to get the abstracted type
    Optional<XPotion> xPotion = XPotion.matchXPotion(stringValue);
    if (xPotion.isPresent())
      return xPotion.get().getPotionType();

    // Otherwise, try to parse directly
    // Useful for types like AWKWARD, MUNDANE, ...
    try {
      return PotionType.valueOf(stringValue.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ignored) {}

    return null;
  }

  public @Nullable PotionEffectType asPotionEffectType(IEvaluationEnvironment environment) {
    XPotion potion = XPotion.matchXPotion(asString(environment)).orElse(null);
    return potion == null ? null : potion.getPotionEffectType();
  }

  public @Nullable java.awt.Color asJavaColor(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);
    return parseRGBNotation(stringValue, java.awt.Color::new);
  }

  public @Nullable Color asBukkitColor(IEvaluationEnvironment environment) {
    String stringValue = asString(environment);

    // Try to parse an enum name
    Color enumValue = parseEnum(Color.class, stringValue);
    if (enumValue != null)
      return enumValue;

    return parseRGBNotation(stringValue, Color::fromRGB);
  }

  private <T> @Nullable T parseRGBNotation(String value, FUnsafeTriFunction<Integer, Integer, Integer, T, Exception> parser) {
    // Assume it's an RGB color
    String[] parts = value.split(" ");

    // Malformed
    if (parts.length != 3)
      return null;

    try {
      // Parse RGB parts
      return parser.apply(
        Integer.parseInt(parts[0]),
        Integer.parseInt(parts[1]),
        Integer.parseInt(parts[2])
      );
    } catch (Exception e) {
      return null;
    }
  }

  public <T> @Nullable T asEnumerationConstant(Class<T> enumerationClass, IEvaluationEnvironment environment) {
    return parseEnum(enumerationClass, asString(environment));
  }

  public <T> Set<T> asEnumerationConstantSet(Class<T> enumerationClass, IEvaluationEnvironment environment) {
    Set<String> values = asSet(ScalarType.STRING, environment);
    Set<T> constants = new HashSet<>();

    for (String value : values) {
      T constant = parseEnum(enumerationClass, value);

      if (constant == null)
        continue;

      constants.add(constant);
    }

    return constants;
  }

  /**
   * Stringifies this component and possibly appends other, also stringified, components to it's tail
   * @param environment Optional environment to evaluate in
   * @param others Other evaluables to also stringify and append
   */
  public String stringify(@Nullable IEvaluationEnvironment environment, BukkitEvaluable... others) {
    String self = asScalar(ScalarType.STRING, environment);

    if (others.length == 0)
      return self;

    StringJoiner joiner = new StringJoiner("");

    joiner.add(self);

    for (BukkitEvaluable evaluable : others)
      joiner.add(evaluable.asScalar(ScalarType.STRING, environment));

    return joiner.toString();
  }

  public String stringify(BukkitEvaluable... others) {
    return stringify(null, others);
  }

  //=========================================================================//
  //                                Overrides                                //
  //=========================================================================//

  @Override
  @SuppressWarnings("unchecked")
  protected <T> T interpretScalar(@Nullable Object input, ScalarType type, IEvaluationEnvironment env) {
    T scalar = super.interpretScalar(input, type, env);

    if (scalar instanceof String)
      scalar = (T) ChatColor.translateAlternateColorCodes('&', (String) scalar);

    return scalar;
  }


  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  private String asString(IEvaluationEnvironment environment) {
    return asScalar(ScalarType.STRING, environment);
  }

  //=========================================================================//
  //                               Enum Lookup                               //
  //=========================================================================//

  /**
   * Parses an "enum" from either true enum constants or static self-typed
   * constant declarations within the specified class
   * @param type Target type class
   * @param value String value to parse
   * @return Optional constant, empty if there was no such constant
   */
  @SuppressWarnings("unchecked")
  private static <T> @Nullable T parseEnum(Class<T> type, String value) {
    value = value.trim().toLowerCase(Locale.ROOT);

    Optional<Object> cacheLookup = performEnumCacheLookup(type, value);
    Object result = null;

    if (cacheLookup.isPresent())
      return (T) cacheLookup.get();

    // Parse enums
    if (type.isEnum()) {
      for (T enumConstant : type.getEnumConstants()) {
        if (!((Enum<?>) enumConstant).name().toLowerCase().equals(value))
          continue;

        result = enumConstant;
        break;
      }
    }

    // Parse classes with static constants
    else {
      try {
        List<Field> constants = Arrays.stream(type.getDeclaredFields())
          .filter(field -> field.getType().equals(type) && Modifier.isStatic(field.getModifiers()))
          .toList();

        for (Field constant : constants) {
          if (!constant.getName().toLowerCase().equals(value))
            continue;

          result = constant.get(null);
          break;
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    if (result != null)
      cacheEnumLookup(type, value, result);

    return (T) result;
  }

  private static Optional<Object> performEnumCacheLookup(Class<?> type, String value) {
    Map<String, Object> constantCache = enumCache.get(type);

    if (constantCache != null && constantCache.containsKey(value))
      return Optional.of(constantCache.get(value));

    return Optional.empty();
  }

  private static void cacheEnumLookup(Class<?> type, String value, Object constant) {
    enumCache.computeIfAbsent(type, key -> new HashMap<>()).put(value, constant);
  }
}