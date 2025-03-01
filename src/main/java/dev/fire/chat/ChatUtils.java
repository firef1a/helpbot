package dev.fire.chat;

import dev.fire.Mod;
import net.minecraft.text.Text;

import java.util.Objects;
//a

public class ChatUtils {
    public static void sendMessageAsPlayer(String content) {
        if (content.charAt(0) == '/') {
            Objects.requireNonNull(Mod.MC.getNetworkHandler()).sendChatCommand(content.substring(1));
        } else {
            Objects.requireNonNull(Mod.MC.getNetworkHandler()).sendChatMessage(content);
        }
    }
    private static void sendMessageToPlayerDisplay(Text content) {
        assert Mod.MC.player != null;
        Mod.MC.player.sendMessage(content, false);
    }

    public static void displayChatMessageToPlayer(Text content) {
        if (Mod.MC.player != null) {
            Mod.MC.player.sendMessage(Text.literal("[SITEMOD]").withColor(0xed743b).append(Text.literal(" ").append(content)), false);
        }
    }
}
