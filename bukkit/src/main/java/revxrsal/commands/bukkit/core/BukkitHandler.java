package revxrsal.commands.bukkit.core;

import static revxrsal.commands.util.Preconditions.notNull;

import dev.demeng.pluginbase.lib.adventure.platform.bukkit.BukkitAudiences;
import dev.demeng.pluginbase.lib.adventure.text.ComponentLike;
import dev.demeng.pluginbase.plugin.BaseManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import revxrsal.commands.CommandHandler;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitBrigadier;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.adventure.AudienceSenderResolver;
import revxrsal.commands.bukkit.adventure.ComponentResponseHandler;
import revxrsal.commands.bukkit.brigadier.CommodoreBukkitBrigadier;
import revxrsal.commands.bukkit.core.EntitySelectorResolver.SelectorSuggestionFactory;
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.bukkit.exception.InvalidWorldException;
import revxrsal.commands.bukkit.exception.MalformedEntitySelectorException;
import revxrsal.commands.bukkit.exception.MoreThanOnePlayerException;
import revxrsal.commands.bukkit.exception.NonPlayerEntitiesException;
import revxrsal.commands.command.CommandCategory;
import revxrsal.commands.command.ExecutableCommand;
import revxrsal.commands.core.BaseCommandHandler;
import revxrsal.commands.core.CommandPath;
import revxrsal.commands.exception.EnumNotFoundException;
import revxrsal.commands.util.Primitives;

@Internal
public final class BukkitHandler extends BaseCommandHandler implements BukkitCommandHandler {

  public static final SuggestionProvider playerSuggestionProvider = (args, sender, command) -> Bukkit.getOnlinePlayers()
      .stream()
      .filter(player -> !((BukkitCommandActor) sender).isPlayer()
          || ((BukkitCommandActor) sender).requirePlayer().canSee(player))
      .map(HumanEntity::getName)
      .collect(Collectors.toList());

  private final Plugin plugin;
  private Optional<BukkitBrigadier> brigadier;
  BukkitAudiences bukkitAudiences;

  @SuppressWarnings("rawtypes")
  public BukkitHandler(@NotNull Plugin plugin) {
    super();
    this.plugin = notNull(plugin, "plugin");
    try {
      brigadier = Optional.of(new CommodoreBukkitBrigadier(this));
    } catch (NoClassDefFoundError e) {
      brigadier = Optional.empty();
    }
    registerSenderResolver(BukkitSenderResolver.INSTANCE);
    registerValueResolver(Player.class, context -> {
      String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return ((BukkitCommandActor) context.actor()).requirePlayer();
      }
      if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors()) {
        try {
          List<Entity> entityList = Bukkit.selectEntities(
              ((BukkitActor) context.actor()).getSender(), value);
          if (entityList.stream().anyMatch(c -> !(c instanceof Player))) {
            throw new NonPlayerEntitiesException(value);
          }
          if (entityList.isEmpty()) {
            throw new InvalidPlayerException(context.parameter(), value);
          }
          if (entityList.size() > 1) {
            throw new MoreThanOnePlayerException(value);
          }
          return (Player) entityList.get(0);
        } catch (IllegalArgumentException e) {
          throw new MalformedEntitySelectorException(context.actor(), value,
              e.getCause().getMessage());
        }
      }
      Player player = Bukkit.getPlayerExact(value);
      if (player == null) {
        throw new InvalidPlayerException(context.parameter(), value);
      }
      return player;
    });
    registerValueResolver(OfflinePlayer.class, context -> {
      String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return ((BukkitCommandActor) context.actor()).requirePlayer();
      }
      OfflinePlayer player = Bukkit.getOfflinePlayer(value);
      if (!player.hasPlayedBefore()) {
        throw new InvalidPlayerException(context.parameter(), value);
      }
      return player;
    });
    registerValueResolver(World.class, context -> {
      String value = context.pop();
      if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) {
        return ((BukkitCommandActor) context.actor()).requirePlayer().getWorld();
      }
      World world = Bukkit.getWorld(value);
      if (world == null) {
        throw new InvalidWorldException(context.parameter(), value);
      }
      return world;
    });
    registerValueResolver(EntityType.class, context -> {
      String value = context.pop().toLowerCase();
      if (value.startsWith("minecraft:")) {
        value = value.substring("minecraft:".length());
      }
      EntityType type = EntityType.fromName(value);
      if (type == null) {
        throw new EnumNotFoundException(context.parameter(), value);
      }
      return type;
    });
    if (EntitySelectorResolver.INSTANCE.supportsComplexSelectors() && isBrigadierSupported()) {
      getAutoCompleter().registerParameterSuggestions(EntityType.class,
          SuggestionProvider.EMPTY);
    }
    registerValueResolverFactory(EntitySelectorResolver.INSTANCE);
    if (!isBrigadierSupported()) {
      getAutoCompleter().registerParameterSuggestions(Player.class, playerSuggestionProvider);
    }
    getAutoCompleter().registerSuggestion("players", playerSuggestionProvider);
    getAutoCompleter()
        .registerSuggestion("worlds", SuggestionProvider.map(Bukkit::getWorlds, World::getName))
        .registerParameterSuggestions(Player.class, "players")
        .registerParameterSuggestions(World.class, "worlds")
        .registerSuggestionFactory(SelectorSuggestionFactory.INSTANCE);
    registerContextValue((Class) plugin.getClass(), plugin);
    registerDependency((Class) plugin.getClass(), plugin);
    registerDependency(FileConfiguration.class, (Supplier<FileConfiguration>) plugin::getConfig);
    registerDependency(Logger.class, (Supplier<Logger>) plugin::getLogger);
    registerPermissionReader(BukkitPermissionReader.INSTANCE);
    setExceptionHandler(BukkitExceptionAdapter.INSTANCE);
    Bukkit.getServer().getPluginManager().registerEvents(new BukkitCommandListeners(this), plugin);
    enableAdventure(BaseManager.getAdventure());
  }

  @Override
  public @NotNull CommandHandler register(@NotNull Object... commands) {
    super.register(commands);
    for (ExecutableCommand command : executables.values()) {
      if (command.getParent() != null) {
        continue;
      }
      createPluginCommand(command.getName(), command.getDescription(), command.getUsage());
    }
    for (CommandCategory category : categories.values()) {
      if (category.getParent() != null) {
        continue;
      }
      createPluginCommand(category.getName(), null, null);
    }
    return this;
  }

  @Override
  public @NotNull Optional<BukkitBrigadier> getBrigadier() {
    return brigadier;
  }

  @Override
  public boolean isBrigadierSupported() {
    return brigadier.isPresent();
  }

  @Override
  public BukkitCommandHandler registerBrigadier() {
    brigadier.ifPresent(BukkitBrigadier::register);
    return this;
  }

  @Override
  public @NotNull Plugin getPlugin() {
    return plugin;
  }

  private void enableAdventure(@NotNull BukkitAudiences audiences) {
    notNull(audiences, "audiences");
    bukkitAudiences = audiences;
    registerSenderResolver(new AudienceSenderResolver(audiences::sender));
    registerResponseHandler(ComponentLike.class, new ComponentResponseHandler(audiences::sender));
  }

  private @SneakyThrows void createPluginCommand(String name, @Nullable String description,
      @Nullable String usage) {
    PluginCommand cmd = COMMAND_CONSTRUCTOR.newInstance(name, plugin);
    COMMAND_MAP.register(plugin.getName(), cmd);
    BukkitCommandExecutor executor = new BukkitCommandExecutor(this);
    cmd.setExecutor(executor);
    cmd.setTabCompleter(executor);
    cmd.setDescription(description == null ? "" : description);
    if (usage != null) {
      cmd.setUsage(usage);
    }
  }

  @Override
  public boolean unregister(@NotNull CommandPath path) {
    if (path.isRoot()) {
      PluginCommand command = ((JavaPlugin) plugin).getCommand(path.getFirst());
      unregisterCommand(command);
    }
    return super.unregister(path);
  }

  private void unregisterCommand(PluginCommand command) {
    if (command != null) {
      command.unregister(COMMAND_MAP);
      Map<String, Command> knownCommands = getKnownCommands();
      if (knownCommands != null) {
        Command rawAlias = knownCommands.get(command.getName());
        if (rawAlias instanceof PluginCommand
            && ((PluginCommand) rawAlias).getPlugin() == plugin) {
          knownCommands.remove(command.getName());
        }
        knownCommands.remove(plugin.getDescription().getName() + ":" + command.getName());
      }
    }
  }

  private static final Constructor<PluginCommand> COMMAND_CONSTRUCTOR;
  private static final @Nullable Field KNOWN_COMMANDS;
  private static final CommandMap COMMAND_MAP;

  static {
    Constructor<PluginCommand> ctr;
    Field knownCommands = null;
    CommandMap commandMap;
    try {
      ctr = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
      ctr.setAccessible(true);
      Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
      commandMapField.setAccessible(true);
      commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
      if (commandMap instanceof SimpleCommandMap) {
        knownCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
        knownCommands.setAccessible(true);
      }
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Unable to access PluginCommand(String, Plugin) construtor!");
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      throw new IllegalStateException("Unable to access Bukkit.getServer()#commandMap!");
    }
    COMMAND_CONSTRUCTOR = ctr;
    COMMAND_MAP = commandMap;
    KNOWN_COMMANDS = knownCommands;
  }

  public static Class<? extends Entity> getSelectedEntity(@NotNull Type selectorType) {
    return (Class<? extends Entity>) Primitives.getInsideGeneric(selectorType, Entity.class);
  }

  @SneakyThrows
  private static @Nullable Map<String, Command> getKnownCommands() {
    if (KNOWN_COMMANDS != null) {
      return (Map<String, Command>) KNOWN_COMMANDS.get(COMMAND_MAP);
    }
    return null;

  }

}
