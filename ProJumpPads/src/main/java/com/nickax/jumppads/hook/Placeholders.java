package com.nickax.jumppads.hook;

import com.nickax.jumppads.pad.JumpPad;
import com.nickax.jumppads.util.Util;
import com.nickax.jumppads.util.Variable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.nickax.jumppads.api.JumpPadsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "jumppads";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Nickax";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer offline, @NotNull String identifier) {
        if (!offline.isOnline() || identifier.isEmpty()) return null;

        Player player = (Player) offline;
        JumpPad pad = JumpPadsAPI.getByName(identifier.replace("canuse_", ""));
        if (pad == null) return null;

        String permission = null;
        String perm1 = pad.getPermission1();
        String perm2 = pad.getPermission2();

        if (perm1 != null && !player.hasPermission(perm1)) {
            permission = perm1;
        } else if (perm2 != null && !player.hasPermission(perm2)) {
            permission = perm2;
        }

        String message = permission == null ? Variable.messages.getString("can-use") : Variable.messages.getString("cannot-use");
        if (message == null || message.isEmpty()) return null;

        return Util.color(
                message
                        .replace("%permission%", permission != null && !permission.isEmpty() ? permission : "")
                        .replace("%name%", pad.getName())
        );
    }
}
