package dev.fire.mixin.hud;

import dev.fire.Mod;
import dev.fire.chat.ResponseManager;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;


@Mixin(ChatHud.class)
public class MChatHud {
    @ModifyVariable(method = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    public Text inject(Text message) {
        return Mod.responseManager.process(message);
    }
}