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

import com.cryptomorin.xseries.XMaterial;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.blvckbytes.bbconfigmapper.ScalarType;
import me.blvckbytes.bukkitevaluable.section.*;
import me.blvckbytes.gpeee.GPEEE;
import me.blvckbytes.utilitytypes.Tuple;
import me.blvckbytes.gpeee.interpreter.IEvaluationEnvironment;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.maven.model.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class ItemBuilder implements IItemBuildable {

  private final ItemStack baseItem;
  private final ItemMeta baseMeta;

  private boolean loreOverride;
  private boolean flagsOverride;
  private boolean enchantmentsOverride;
  private boolean customEffectsOverride;
  private boolean patternOverride;

  private @Nullable BukkitEvaluable name;
  private final List<BukkitEvaluable> loreBlocks;

  private @Nullable BukkitEvaluable amount;
  private @Nullable BukkitEvaluable type;
  private @Nullable BukkitEvaluable color;
  private @Nullable BukkitEvaluable textures;

  private @Nullable ItemStackBaseEffectSection baseEffect;
  private final List<ItemStackCustomEffectSection> customEffects;

  private final List<ItemStackEnchantmentSection> enchantments;
  private final List<ItemStackBannerPatternSection> bannerPatterns;
  private final List<BukkitEvaluable> flags;

  private ItemBuilder(
    ItemStack baseItem,
    ItemMeta baseMeta,
    boolean loreOverride,
    boolean flagsOverride,
    boolean enchantmentsOverride,
    boolean customEffectsOverride,
    boolean patternOverride,
    @Nullable BukkitEvaluable name,
    List<BukkitEvaluable> loreBlocks,
    @Nullable BukkitEvaluable amount,
    @Nullable BukkitEvaluable type,
    @Nullable BukkitEvaluable color,
    @Nullable BukkitEvaluable textures,
    @Nullable ItemStackBaseEffectSection baseEffect,
    List<ItemStackCustomEffectSection> customEffects,
    List<ItemStackEnchantmentSection> enchantments,
    List<ItemStackBannerPatternSection> bannerPatterns,
    List<BukkitEvaluable> flags
  ) {
    this.baseItem = new ItemStack(baseItem);
    this.baseMeta = baseMeta.clone();
    this.loreOverride = loreOverride;
    this.flagsOverride = flagsOverride;
    this.enchantmentsOverride = enchantmentsOverride;
    this.customEffectsOverride = customEffectsOverride;
    this.patternOverride = patternOverride;
    this.name = name;
    this.loreBlocks = new ArrayList<>(loreBlocks);
    this.amount = amount;
    this.type = type;
    this.color = color;
    this.textures = textures;
    this.baseEffect = baseEffect;
    this.customEffects = new ArrayList<>(customEffects);
    this.enchantments = new ArrayList<>(enchantments);
    this.bannerPatterns = new ArrayList<>(bannerPatterns);
    this.flags = new ArrayList<>(flags);
  }

  public ItemBuilder(Material material, int amount) {
    this(new ItemStack(material), amount);
  }

  public ItemBuilder(ItemStack item, int amount) {
    if (item == null)
      throw new IllegalStateException("Item cannot be null");

    this.baseItem = new ItemStack(item);
    this.baseItem.setAmount(amount);

    this.baseMeta = item.getItemMeta();

    if (this.baseMeta == null)
      throw new IllegalStateException("Could not get meta of item");

    this.loreBlocks = new ArrayList<>();
    this.enchantments = new ArrayList<>();
    this.flags = new ArrayList<>();
    this.customEffects = new ArrayList<>();
    this.bannerPatterns = new ArrayList<>();
  }

  //=========================================================================//
  //                                 Builder                                 //
  //=========================================================================//

  public ItemBuilder setType(@Nullable BukkitEvaluable type) {
    if (type == null)
      return this;

    this.type = type;
    return this;
  }

  public ItemBuilder setAmount(@Nullable BukkitEvaluable amount) {
    if (amount == null)
      return this;

    this.amount = amount;
    return this;
  }

  public ItemBuilder setName(@Nullable BukkitEvaluable name) {
    this.name = name;
    return this;
  }

  public ItemBuilder extendLore(@Nullable BukkitEvaluable lore) {
    if (lore == null)
      return this;

    this.loreOverride = false;
    this.loreBlocks.add(lore);
    return this;
  }

  public ItemBuilder overrideLore(@Nullable BukkitEvaluable lore) {
    if (lore == null)
      return this;

    this.loreOverride = true;
    this.loreBlocks.clear();
    this.loreBlocks.add(lore);
    return this;
  }

  public ItemBuilder extendEnchantments(@Nullable ItemStackEnchantmentSection[] enchantments) {
    if (enchantments == null)
      return this;

    this.enchantmentsOverride = false;
    Collections.addAll(this.enchantments, enchantments);
    return this;
  }

  public ItemBuilder overrideEnchantments(@Nullable ItemStackEnchantmentSection[] enchantments) {
    this.enchantmentsOverride = true;
    this.enchantments.clear();

    if (enchantments != null)
      Collections.addAll(this.enchantments, enchantments);

    return this;
  }

  public ItemBuilder extendFlags(@Nullable BukkitEvaluable flags) {
    if (flags == null)
      return this;

    this.flagsOverride = false;
    this.flags.add(flags);
    return this;
  }

  public ItemBuilder overrideFlags(@Nullable BukkitEvaluable flag) {
    if (flag == null)
      return this;

    this.flagsOverride = true;
    this.flags.clear();
    this.flags.add(flag);
    return this;
  }

  public ItemBuilder setColor(@Nullable BukkitEvaluable color) {
    if (color == null)
      return this;

    this.color = color;
    return this;
  }

  public ItemBuilder setBaseEffect(@Nullable ItemStackBaseEffectSection effect) {
    if (effect == null)
      return this;

    this.baseEffect = effect;
    return this;
  }

  public ItemBuilder extendCustomEffects(@Nullable ItemStackCustomEffectSection[] effects) {
    if (effects == null)
      return this;

    this.customEffectsOverride = false;
    Collections.addAll(this.customEffects, effects);
    return this;
  }

  public ItemBuilder overrideCustomEffects(@Nullable ItemStackCustomEffectSection[] effects) {
    this.customEffectsOverride = true;
    this.customEffects.clear();

    if (effects != null)
      Collections.addAll(this.customEffects, effects);

    return this;
  }

  public ItemBuilder setTextures(@Nullable BukkitEvaluable textures) {
    if (textures == null)
      return this;

    this.textures = textures;
    return this;
  }

  public ItemBuilder extendBannerPatterns(@Nullable ItemStackBannerPatternSection[] patterns) {
    if (patterns == null)
      return this;

    this.patternOverride = false;
    Collections.addAll(this.bannerPatterns, patterns);
    return this;
  }

  public ItemBuilder overrideBannerPatterns(@Nullable ItemStackBannerPatternSection[] patterns) {
    this.patternOverride = true;
    this.bannerPatterns.clear();

    if (patterns != null)
      Collections.addAll(this.bannerPatterns, patterns);

    return this;
  }

  //=========================================================================//
  //                              IItemBuildable                             //
  //=========================================================================//

  @Override
  public ItemStack build() {
    return build(GPEEE.EMPTY_ENVIRONMENT);
  }

  @Override
  public ItemStack build(IEvaluationEnvironment environment) {
    ItemStack res = baseItem.clone();
    ItemMeta resMeta = baseMeta.clone();

    //////////////////////////////////// Type ///////////////////////////////////

    if (type != null) {
      XMaterial material = this.type.asXMaterial(environment);

      if (material != null) {
        material.setType(res);
        resMeta = res.getItemMeta();
      }
    }

    ////////////////////////////////// Amount ///////////////////////////////////

    if (amount != null)
      res.setAmount(this.amount.<Long>asScalar(ScalarType.LONG, environment).intValue());

    //////////////////////////////// Display name ////////////////////////////////

    if (this.name != null)
			resMeta.displayName(MiniMessage.miniMessage().deserialize(this.name.asScalar(ScalarType.STRING, environment)));

    /////////////////////////////////// Lore ///////////////////////////////////

    if (loreOverride)
      resMeta.lore(null);

    if (! loreBlocks.isEmpty()) {
      // Get old lore lines to extend, if applicable
      final List<Component> lines = resMeta.lore() == null ? new ArrayList<>() :
																		resMeta.lore();


      // Extend by new lore lines
      for (BukkitEvaluable line : loreBlocks) {
				line.asList(ScalarType.STRING, environment).forEach(lineline -> lines.add(MiniMessage.miniMessage().deserialize(lineline.toString())));
			}

      resMeta.lore(lines);
    }

    /////////////////////////////////// Color //////////////////////////////////

    if (color != null) {
      Color bukkitColor = color.asBukkitColor(environment);

      if (bukkitColor != null)
        applyColor(resMeta, bukkitColor);
    }

    ////////////////////////////////// Textures /////////////////////////////////

    if (textures != null)
      applyTextures(resMeta, textures.asScalar(ScalarType.STRING, environment));

    //////////////////////////////// Base Effect ////////////////////////////////

    if (baseEffect != null) {
      PotionData data = baseEffect.asData(environment);
      if (data != null)
        applyBaseEffect(resMeta, data);
    }

    ////////////////////////////// Custom Effects ///////////////////////////////

    if (resMeta instanceof PotionMeta) {
      if (customEffectsOverride)
        ((PotionMeta) resMeta).clearCustomEffects();

      for (ItemStackCustomEffectSection customEffect : customEffects) {
        PotionEffect effect = customEffect.asEffect(environment);
        if (effect != null)
          applyCustomEffect(resMeta, effect);
      }
    }

    //////////////////////////////// Enchantments ///////////////////////////////

    if (enchantmentsOverride)
      resMeta.getEnchants().keySet().forEach(resMeta::removeEnchant);

    for (ItemStackEnchantmentSection enchantment : enchantments) {
      Tuple<Enchantment, Integer> enchantmentData = enchantment.asEnchantment(environment);
      if (enchantmentData != null)
        resMeta.addEnchant(enchantmentData.a, enchantmentData.b, true);
    }

    ///////////////////////////////// Item Flags ////////////////////////////////

    if (flagsOverride) {
      for (ItemFlag flag : ItemFlag.values())
        resMeta.removeItemFlags(flag);
    }

    for (BukkitEvaluable flag : flags) {
      for (ItemFlag itemFlag : flag.asEnumerationConstantSet(ItemFlag.class, environment))
        resMeta.addItemFlags(itemFlag);
    }

    /////////////////////////////// Banner Patterns /////////////////////////////

    if (resMeta instanceof BannerMeta) {
      BannerMeta bannerMeta = (BannerMeta) resMeta;

      if (patternOverride) {
        while (bannerMeta.getPatterns().size() > 0)
          bannerMeta.removePattern(0);
      }

      for (ItemStackBannerPatternSection bannerPattern : bannerPatterns) {
        Pattern pattern = bannerPattern.asPattern(environment);
        if (pattern != null)
          applyPattern(resMeta, pattern);
      }
    }

    res.setItemMeta(resMeta);
    return res;
  }

  @Override
  public ItemBuilder copy() {
    return new ItemBuilder(
      baseItem,
      baseMeta,
      loreOverride,
      flagsOverride,
      enchantmentsOverride,
      customEffectsOverride,
      patternOverride,
      name,
      loreBlocks,
      amount,
      type,
      color,
      textures,
      baseEffect,
      customEffects,
      enchantments,
      bannerPatterns,
      flags
    );
  }

  @Override
  public ItemBuilder patch(ItemStackSection data) {
    ItemBuilder res = this.copy();

    if (data.getAmount() != null)
      res.setAmount(data.getAmount());

    if (data.getType() != null)
      res.setType(data.getType());

    if (data.getName() != null)
      res.setName(data.getName());

    if (data.getPatchFlags().contains(EPatchFlag.OVERRIDE_LORE))
      res.overrideLore(data.getLore());
    else
      res.extendLore(data.getLore());

    if (data.getPatchFlags().contains(EPatchFlag.OVERRIDE_FLAGS))
      res.overrideFlags(data.getFlags());
    else
      res.extendFlags(data.getFlags());

    if (data.getColor() != null)
      res.setColor(data.getColor());

    if (data.getPatchFlags().contains(EPatchFlag.OVERRIDE_ENCHANTMENTS))
      res.overrideEnchantments(data.getEnchantments());
    else
      res.extendEnchantments(data.getEnchantments());

    if (data.getTextures() != null)
      res.setTextures(data.getTextures());

    if (data.getBaseEffect() != null)
      res.setBaseEffect(data.getBaseEffect());

    if (data.getPatchFlags().contains(EPatchFlag.OVERRIDE_CUSTOM_EFFECTS))
      res.overrideCustomEffects(data.getCustomEffects());
    else
      res.extendCustomEffects(data.getCustomEffects());

    if (data.getPatchFlags().contains(EPatchFlag.OVERRIDE_BANNER_PATTERNS))
      res.overrideBannerPatterns(data.getBannerPatterns());
    else
      res.extendBannerPatterns(data.getBannerPatterns());

    return res;
  }

  //=========================================================================//
  //                                Utilities                                //
  //=========================================================================//

  /**
   * Applies a color value to the item-meta, based on it's type
   * @param color Color value
   */
  private void applyColor(ItemMeta meta, Color color) {
    // FIXME: In order to support future added meta types with colors, this should be accessed using reflect, as all methods have the same signature
    if (meta instanceof LeatherArmorMeta)
      ((LeatherArmorMeta) meta).setColor(color);

    else if (meta instanceof PotionMeta)
      ((PotionMeta) meta).setColor(color);

    else if (meta instanceof MapMeta)
      ((MapMeta) meta).setColor(color);
  }

  /**
   * Applies a potion base effect
   * @param effect Base effect
   */
  private void applyBaseEffect(ItemMeta meta, PotionData effect) {
    if (meta instanceof PotionMeta)
      ((PotionMeta) meta).setBasePotionData(effect);
  }

  /**
   * Applies a custom potion effect
   * @param effect Custom effect
   */
  private void applyCustomEffect(ItemMeta meta, @Nullable PotionEffect effect) {
    if (effect == null)
      return;

    if (meta instanceof PotionMeta)
      ((PotionMeta) meta).addCustomEffect(effect, true);
  }

  /**
   * Applies a base64 texture value to a skull
   * @param textures Texture value
   */
  private void applyTextures(ItemMeta meta, String textures) {
    PlayerProfile prof = Bukkit.createProfile(UUID.randomUUID());
		prof.setProperty(
			new ProfileProperty(
				"textures",
				textures
			)
		);
    applyHeadProfile(meta, prof);
  }

  /**
   * Applies a banner pattern to a banner
   * @param pattern Banner pattern
   */
  private void applyPattern(ItemMeta meta, Pattern pattern) {
    if (meta instanceof BannerMeta)
      ((BannerMeta) meta).addPattern(pattern);
  }

  /**
   * Sets the head game profile of the item-meta
   * @param profile Game profile to set
   */
  private void applyHeadProfile(ItemMeta meta, PlayerProfile profile) {
    if (!(meta instanceof SkullMeta skullMeta))
      return;

		skullMeta.setPlayerProfile(profile);
  }
}