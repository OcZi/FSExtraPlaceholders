package me.oczi;

import com.songoda.skyblock.api.SkyBlockAPI;
import com.songoda.skyblock.placeholder.PlaceholderManager;
import com.songoda.skyblock.placeholder.PlaceholderProcessor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * FabledSkyBlock expansion for extra placeholders.
 * Their jar is imported manually.
 */
public class FSExtraExpansion extends PlaceholderExpansion {
  private final String FABLED_SKYBLOCK = "fabledskyblock_";

  private List<String> originalPlaceholders;
  private List<String> placeholders;
  private PlaceholderProcessor processor;

  @Override
  public String getRequiredPlugin() {
    return "FabledSkyBlock";
  }

  @Override
  public boolean register() {
    checkArgument(canRegister(),
        "Cannot initialize this expansion. " +
            "FabledSkyBlock is not enabled.");
    this.processor = new PlaceholderProcessor();
    ConfigurationSection root = SkyBlockAPI
        .getImplementation()
        .getPlaceholders()
        .getConfigurationSection("Placeholders");
    checkNotNull(root,
        "Root node of FabledSkyblock's " +
            "Placeholders.yml is null.");
    this.originalPlaceholders = root
        .getKeys(false)
        .stream()
        .map(p -> p.replace(FABLED_SKYBLOCK, ""))
        .collect(Collectors.toList());
    this.placeholders = originalPlaceholders
        .stream()
        .map(p -> p + "_'player'")
        .collect(Collectors.toList());
    return super.register();
  }

  @Override
  public String onRequest(OfflinePlayer p, String params) {
    return onPlaceholderRequest(p.getPlayer(), params);
  }

  @Override
  public String onPlaceholderRequest(Player p, String params) {
    return parseFabledSkyBlock(p, params);
  }

  /**
   * Convert parameter into a {@link Player} and placeholder
   * to pass them to {@link PlaceholderManager}.
   *
   * @param param Placeholder with player.
   * @return Result.
   */
  public String parseFabledSkyBlock(Player player, String param) {
    if (originalPlaceholders.contains(param))
      return processPlaceholder(player, param);

    int playerIndex = param.indexOf("'");
    if (playerIndex == -1) {
      return processPlaceholder(player, param);
    }
    int endIndex = param.lastIndexOf("'");
    if (endIndex == playerIndex) {
      param = param.substring(0, playerIndex);
      return processPlaceholder(player, param);
    }
    String playerName = param.substring(
        playerIndex + 1, endIndex);
    player = Bukkit.getPlayerExact(playerName);
    if (player == null) return "N/A";
    String placeholder = param.substring(0, playerIndex);
    return processPlaceholder(player, placeholder);
  }

  public String removeLastUnderscore(String placeholder) {
    return placeholder.endsWith("_")
        ? placeholder.substring(0,
        placeholder.length() - 1)
        : placeholder;
  }

  public String processPlaceholder(Player player, String param) {
    return processor.processPlaceholder(
        player,
        FABLED_SKYBLOCK + removeLastUnderscore(param));
  }

  @Override
  public String getIdentifier() {
    return "fabledskyblockextra";
  }

  @Override
  public String getAuthor() {
    return "_OcZi";
  }

  @Override
  public String getVersion() {
    return "1.0.0";
  }

  @Override
  public List<String> getPlaceholders() {
    return placeholders
        .stream()
        .map(p -> getIdentifier() + "_" + p)
        .collect(Collectors.toList());
  }
}
