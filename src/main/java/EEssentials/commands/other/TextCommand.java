package EEssentials.commands.other;

import EEssentials.EEssentials;
import EEssentials.util.ColorUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                    Component message = getFileText(commandName, context.getSource());
                            context.getSource().getPlayer().sendMessage(message);
                            return Command.SINGLE_SUCCESS;
                        }
                )
        );
    }

    public Component getFileText(String commandName, ServerCommandSource source) {
        try {
            File textFile = EEssentials.INSTANCE.getOrCreateConfigurationFile("text-commands/" + commandName + ".txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(textFile));
            Component completeText = Component.empty();
            String textLine;
            while ((textLine = bufferedReader.readLine()) != null) {
                Component lineComponent;

                // Check and handle URLs first
                if (textLine.startsWith("http")) {
                    lineComponent = Component.text(textLine)
                            .clickEvent(ClickEvent.openUrl(textLine));
                } else {
                    // Create a PlaceholderContext from the source
                    PlaceholderContext context = PlaceholderContext.of(source);

                    // Parse the text with placeholders
                    Text placeholderParsedText = Placeholders.parseText(Text.literal(textLine), context);

                    // Apply color formatting if needed
                    lineComponent = ColorUtil.parseColour(placeholderParsedText.getString());
                }

                completeText = completeText.append(lineComponent);
            }
            return completeText;
        } catch (IOException e) {
            return Component.text("Failed to find the text file! Contact an administrator.");
        }
    }
}
