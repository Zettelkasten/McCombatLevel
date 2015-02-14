package com.gmail.mrphpfan;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    protected final McCombatLevel pluginInstance;

    public PlayerListener(McCombatLevel plugin) {
        this.pluginInstance = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(pluginInstance, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    //profiles are loaded async. We need to wait for it
                    pluginInstance.updateLevel(player);
                }
            }
        }, 3 * 20L);

        //send them the scoreboard
        if (pluginInstance.isTagEnabled()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        //remove the player from the hashmap
        pluginInstance.removeCachedLevels(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerLevelUp(McMMOPlayerLevelUpEvent event) {
        SkillType skill = event.getSkill();

        //only level up combat if one of the following was leveled
        if (skill.equals(SkillType.SWORDS) || skill.equals(SkillType.ARCHERY)
                || skill.equals(SkillType.AXES) || skill.equals(SkillType.UNARMED)
                || skill.equals(SkillType.TAMING) || skill.equals(SkillType.ACROBATICS)) {
            pluginInstance.updateLevel(event.getPlayer());
        }
    }

    //todo make thread-safe
    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        if (!pluginInstance.isPrefixEnabled()) {
            //check if prefix is enabled
            return;
        }

        //append a level prefix to their name
        Integer combatLevel = pluginInstance.getCombatLevel(event.getPlayer());
        if (combatLevel != null) {
            ChatColor prefixColor = pluginInstance.getPrefixColor();
            ChatColor prefixBracketColor = pluginInstance.getPrefixBracketColor();
            event.setFormat(prefixBracketColor + "[" + prefixColor + combatLevel + prefixBracketColor + "]" + ChatColor.RESET + event.getFormat());
        }
    }
}