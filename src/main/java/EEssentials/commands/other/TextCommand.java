package EEssentials.commands.other;

import EEssentials.EEssentials;
import EEssentials.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.ServerCommandSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;

import static net.minecraft.server.command.CommandManager.literal;

public class TextCommand {
    public TextCommand(String commandName, CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(commandName)
                .requires(Permissions
                        .require("eessentials.textcommand." +
                                commandName.toLowerCase(Locale.ENGLISH),2))
                .executes(context -> {
                            context.getSource().sendMessage(ColorUtil.parseColour(getFileText(commandName)));
                            return Command.SINGLE_SUCCESS;
                        }
                )
        );
    }

    public String getFileText(String commandName) {
        try {
            File textFile = EEssentials.INSTANCE.getOrCreateConfigurationFile("text-commands/" + commandName + ".txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(textFile));
            StringBuilder completeText = new StringBuilder();
            String textLine;
            while((textLine = bufferedReader.readLine()) != null) {
                completeText.append(textLine).append("<newline>");
            }
            return completeText.toString();
        } catch (IOException e) {
            return "Failed to find the text file! Contact an administrator.";
        }
    }
}
