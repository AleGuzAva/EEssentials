package EEssentials.commands.suggestionproviders;

import EEssentials.EEssentials;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PlayerSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
    private boolean permissionToInclude = false;
    private String exemptPermission = null;

    public PlayerSuggestionProvider() {}

    public PlayerSuggestionProvider(boolean permissionToInclude) {
        this.permissionToInclude = permissionToInclude;
    }

    public PlayerSuggestionProvider(String exemptPermission) {
        this.exemptPermission = exemptPermission;
    }

    public PlayerSuggestionProvider(boolean permissionToInclude, String exemptPermission) {
        this.permissionToInclude = permissionToInclude;
        this.exemptPermission = exemptPermission;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        List<String> completions = new ArrayList<>();
        List<ServerPlayerEntity> players = EEssentials.server.getPlayerManager().getPlayerList()
                .stream().filter(player -> exemptPermission == null || 
                        Permissions.check(player, exemptPermission, 4)).toList();
        players.forEach(player -> completions.add(player.getName().getString()));
        if(permissionToInclude) completions.add("all");
        try {
            String arg = context.getArgument("player", String.class);
            if (arg != null) {
                for (String completion : completions) {
                    if (startsWith(arg, completion)) {
                        builder.suggest(completion);
                    }
                }
            }
        } catch(IllegalArgumentException e) {
            completions.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private static boolean startsWith(String arg, String completion) {
        if(arg.length() > completion.length()) return false;
        String argEquiv = completion.substring(0, arg.length());
        return arg.equalsIgnoreCase(argEquiv);
    }
}
