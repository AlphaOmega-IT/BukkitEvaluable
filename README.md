<!-- This file is rendered by https://github.com/BlvckBytes/readme_helper -->

# BukkitEvaluable

## maven
```xml
  <dependency>
    <groupId>de.alphaomega-it.bukkitevaluable</groupId>
    <artifactId>BukkitEvaluable</artifactId>
    <version>1.0</version>
  </dependency>
```

A wrapper on [BBConfigMapper](https://github.com/BlvckBytes/BBConfigMapper), which manages configuration
instances and adds value converters, like the item-buildable as well as the bukkit-evaluable itself, which provides
many bukkit type interpretations and support for the use of `&`-colors.

## Table of Contents
- [Expression Marker Suffix](#expression-marker-suffix)
- [Permissions Section](#permissions-section)
  - [Full Example](#full-example-1)
- [ItemStack Section](#itemstack-section)
  - [Full Example](#full-example-2)
  - [Bukkit Color Value](#bukkit-color-value)
  - [Available Keys](#available-keys)
    - [amount](#amount)
    - [type](#type)
    - [name](#name)
    - [lore](#lore)
    - [flags](#flags)
    - [color](#color)
    - [enchantments](#enchantments)
      - [enchantment section](#enchantment-section)
    - [textures](#textures)
    - [baseEffect](#baseeffect)
    - [customEffects](#customeffects)
      - [custom effect section](#custom-effect-section)
    - [bannerPatterns](#bannerpatterns)
      - [banner pattern section](#banner-pattern-section)
    - [patchFlags](#patchflags)
- [Functions](#functions)
  - [base64_to_skin_url](#base64_to_skin_url)
  - [skin_url_to_base64](#skin_url_to_base64)
- [Lookup-Table (LUT) section](#lookup-table-(lut)-section)

## Expression Marker Suffix

In order to signal to the system that a key's value is an evaluable expression, add a trailing dollar sign to it's name (`$`).

## Permissions Section

All permission nodes of a plugin can be configured to a custom value, based on a unique internal key.

### Full Example

```yaml
permissions:
  # permission: The missing permission node string
  missingMessage$: 'lut["prefix"] & "&7You\sre lacking the permission &c" & permission'
  # Map of internal name to custom configurable node value
  nodes:
    open: headcatalog.open
    request: headcatalog.request
```

## ItemStack Section

The item-stack section can be used to either define a custom item from scratch, patch another item description in order
to customize/update/adapt it to a special use-case or to describe a matcher for already existing item-stacks. For a matcher,
non-specified properties act as wildcards. For a patcher, only specified values get updated, according to the patch-flags.

### Full Example

This full example shows off all available keys. A section like this wouldn't make any sense, as some properties are only
applicable to a certain material type, but it still serves as a great template.

```yaml
amount: 32
type: DIAMOND_PICKAXE
name: '&6My fancy pickaxe'
lore:
  - ' '
  - '&7This is a &dlore line&7!'
  - '&7This is another &dlore line&7!'
flags:
  - HIDE_ATTRIBUTES
  - HIDE_UNBREAKABLE
color: ORANGE
enchantments:
  -
    enchantment: ARROW_FIRE
    level: 4
  -
    enchantment: KNOCKBACK
    level: 2
textures: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTE4YTJkZDViZWYwYjA3M2IxMzI3MWE3ZWViOWNmZWE3YWZlODU5M2M1N2E5MzgyMWU0MzE3NTU3MjQ2MTgxMiJ9fX0='
baseEffect:
  type: HARM
  extended: false
  upgraded: true
customEffects:
  -
    effect: HUNGER
    duration: 200
    amplifier: 3
    ambient: false
    particles: true
    icon: true
bannerPatterns:
  -
    pattern: STRIPE_TOP
    color: RED
  -
    pattern: STRIPE_BOTTOM
    color: BLUE
patchFlags:
  - OVERRIDE_LORE
  - OVERRIDE_BANNER_PATTERNS
```

### Bukkit Color Value

Bukkit colors already come with a set of predefined [constants](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Color.html), which
you can specify on properties of type color. If a custom RGB-color is required, it can be written down as `R G B`, where each letter
can take on a value from `0` to `255`, including both ends.

### Available Keys

#### amount

The amount of items, interpreted as an integer value.

#### type

The type (material), interpreted as a [XMaterial Enum](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XMaterial.java).

#### name

The display-name, interpreted as a string value.

#### lore

The displayed lore lines, interpreted as a string-list.

#### flags

The active item-flags, interpreted as a set of [ItemFlag Enumeration Values](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/ItemFlag.html).

#### color

The color of this item, applied if the item is colorizable (leather-armor, potion, map, ...), interpreted as a [Bukkit Color](#bukkit-color-value).

#### enchantments

All enchantments which are present on this item, interpreted as a list of `enchantment sections`.

##### enchantment section

An enchantment to be applied to an item, consisting of the following properties:

| Property    | Description               | Interpretation                                                                                                                  |
|-------------|---------------------------|---------------------------------------------------------------------------------------------------------------------------------|
| enchantment | Enchantment to be applied | [XEnchantment Enum](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XEnchantment.java) |
| level       | Level of the enchantment  | Integer value                                                                                                                   |

#### textures

The base64 textures value to be applied to a skull item, interpreted as a string.

#### baseEffect

A potion's base effect, consisting of the following properties:

| Property | Description                          | Interpretation                                                                                                                                                                                                         |
|----------|--------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type     | Potion effect to be applied          | [XPotion Enum](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XPotion.java) or [PotionType Enum](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html) |
| extended | Whether the duration is extended     | Boolean value                                                                                                                                                                                                          |
| upgraded | Whether the effect has been upgraded | Boolean value                                                                                                                                                                                                          |

#### customEffects

All custom potion effects which are present on this item, interpreted as a list of `custom effect sections`.

##### custom effect section

A potion's custom effect, consisting of the following properties:

| Property  | Description                         | Interpretation                                                                                                        |
|-----------|-------------------------------------|-----------------------------------------------------------------------------------------------------------------------|
| effect    | Potion effect to be applied         | [XPotion Enum](https://github.com/CryptoMorin/XSeries/blob/master/src/main/java/com/cryptomorin/xseries/XPotion.java) |
| duration  | Duration of the effect in **ticks** | Integer value                                                                                                         |
| amplifier | Amplifier of the effect             | Integer value                                                                                                         |
| ambient   | More translucent particles          | Boolean value                                                                                                         |
| particles | Whether to show particles           | Boolean value                                                                                                         |
| icon      | Whether to show an icon             | Boolean value                                                                                                         |

#### bannerPatterns

All custom banner patterns which are present on this item, interpreted as a list of `banner pattern sections`.

##### banner pattern section

A banner's custom pattern, consisting of the following properties:

| Property | Description           | Interpretation                                                                                        |
|----------|-----------------------|-------------------------------------------------------------------------------------------------------|
| pattern  | Pattern to be applied | [PatternType Enum](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/banner/PatternType.html) |
| color    | Color of the pattern  | [DyeColor Enum](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html)                    |

#### patchFlags

Active patch flags when this item-stack section is being used to patch (customize, adapt) another item-stack section,
interpreted as a set of [Patch Flags](https://github.com/BlvckBytes/BukkitEvaluable/blob/main/src/main/java/me/blvckbytes/bukkitevaluable/EPatchFlag.java).

These are override true flags, so if they're specified, the representing property is being **overridden**. If it's not specified, it's **extended**.

## Functions

This library adds a few functions to the base evaluation environment of all retrievable values:

### base64_to_skin_url

Decodes the passed base64 encoded string, parses it as JSON and returns the extracted textures.SKIN.url string value.

`base64_to_skin_url("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTE4YTJkZDViZWYwYjA3M2IxMzI3MWE3ZWViOWNmZWE3YWZlODU5M2M1N2E5MzgyMWU0MzE3NTU3MjQ2MTgxMiJ9fX0=")` -> `"http://textures.minecraft.net/texture/118a2dd5bef0b073b13271a7eeb9cfea7afe8593c57a93821e43175572461812"`

### skin_url_to_base64

Creates the json string required to provide skin texture urls and returns the base64 encoded value of that json string.

`skin_url_to_base64("http://textures.minecraft.net/texture/118a2dd5bef0b073b13271a7eeb9cfea7afe8593c57a93821e43175572461812")` -> `"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTE4YTJkZDViZWYwYjA3M2IxMzI3MWE3ZWViOWNmZWE3YWZlODU5M2M1N2E5MzgyMWU0MzE3NTU3MjQ2MTgxMiJ9fX0="`

## Lookup-Table (LUT) section

The top level section named `"lut"` is a standard section in every configuration file and serves as a central store for
constants and duplicate data in general. It's of type **map** and is available as a static variable by the name `"lut"` within
the base (and thus every) evaluation environment. As this table is bound at the evaluation level, it can be specified at the
bottom of the file and still be used anywhere, in contrast to yaml anchors.

Here's a shortened excerpt, used as an example:

```yaml
commonItems:
  previousPage:
    type: PLAYER_HEAD
    textures$: 'lut[if current_page > 1 then "ARROW_LEFT" else "ARROW_LEFT_RED"]'

lut:
  ARROW_LEFT: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTE4YTJkZDViZWYwYjA3M2IxMzI3MWE3ZWViOWNmZWE3YWZlODU5M2M1N2E5MzgyMWU0MzE3NTU3MjQ2MTgxMiJ9fX0='
  ARROW_LEFT_RED: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWRmNWMyZjg5M2JkM2Y4OWNhNDA3MDNkZWQzZTQyZGQwZmJkYmE2ZjY3NjhjODc4OWFmZGZmMWZhNzhiZjYifX19'
```
