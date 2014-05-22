package net.communitycraft.core.modular;

import lombok.Getter;
import net.communitycraft.core.Core;
import net.communitycraft.core.config.YAMLConfigurationFile;
import net.communitycraft.core.model.ModelManager;
import net.communitycraft.core.modular.command.ModuleCommand;
import net.communitycraft.core.modular.command.ModuleCommandMap;
import net.communitycraft.core.netfiles.NetFileManager;
import net.communitycraft.core.network.NetworkManager;
import net.communitycraft.core.player.CPermissionsManager;
import net.communitycraft.core.player.CPlayerManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ModularPlugin extends JavaPlugin {

    private YAMLConfigurationFile formatsFile;
    @Getter private ModuleMeta meta;
    @Getter private ModuleCommandMap commandMap;

    @Override
    public final void onEnable() {
        try {
            if (!Core.getInstance().isEnabled()) {
                onFailureToEnable();
                return;
            }
            meta = getClass().getAnnotation(ModuleMeta.class);
            saveDefaultConfig();
            this.formatsFile = new YAMLConfigurationFile(this, "formats.yml");
            this.formatsFile.saveDefaultConfig();
            onModuleEnable();
        } catch (Exception e) {
            e.printStackTrace();
            onFailureToEnable();
            getServer().getPluginManager().disablePlugin(this);
        }
        logMessage("&cModule " + meta.name() + " &a&lEnabled");
    }

    @Override
    public final void onDisable() {
        try {
            onModuleDisable();
        } catch (Exception e) {
            onFailureToDisable();
            e.printStackTrace();
        }
        logMessage("&cModule " + meta.name() + " &4&lDisabled");
    }

    /* Delegated Methods */
    protected void onModuleEnable() {}
    protected void onModuleDisable() {}
    protected void onFailureToEnable() {}
    protected void onFailureToDisable() {}

    /* Util methods */
    public final <T extends Listener> T registerListener(T listener) {
        getServer().getPluginManager().registerEvents(listener, this);
        return listener;
    }

    public final void logMessage(String message) {
        getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    protected final void addCommand(ModuleCommand command) {
        this.commandMap.registerCommand(command);
    }

    protected final ModuleCommand getModuleCommand(String name) {
        return this.commandMap.getCommandByName(name);
    }

    /* Formatting methods */

    public final String getFormatRaw(String key, String[]... formatters) {
        FileConfiguration config = formatsFile.getConfig(); //Get the formats file
        if (!config.contains(key)) return null; //Check if it has this format key, and if not return null
        String unFormattedString = config.getString(key); //Get the un-formatted key
        for (String[] formatter : formatters) { //Iterate through the formatters
            if (formatter.length < 2) continue; //Validate the length
            unFormattedString = unFormattedString.replace(formatter[0], formatter[1]); //Replace all in the unformatted string
        }
        return unFormattedString; //Return
    }

    public final String getFormat(String key, boolean prefix, String[]... formatters) {
        return !prefix ? "" : getFormatRaw("prefix") + getFormatRaw(key, formatters);
    }

    public final String getFormat(String key, String[]... formatters) {
        return getFormat(key, true, formatters);
    }

    public final String getFormat(String key) {
        return getFormatRaw(key);
    }

    public final CPlayerManager getPlayerManager() {
        return Core.getPlayerManager();
    }

    public final NetworkManager getNetworkManager() {
        return Core.getNetworkManager();
    }

    public final CPermissionsManager getPermissionsManager() {
        return Core.getPermissionsManager();
    }

    public final ModelManager getModelManager() {
        return Core.getModelManager();
    }

    public final NetFileManager getNetFileManager() {
        return Core.getNetFileManager();
    }
}
