package dev.fire.client;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.fire.Mod;
import dev.fire.chat.ResponseManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
//import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
//import net.minecraft.client.item.TooltipContext;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.tooltip.*;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {
    private static KeyBinding keyBinding;
    private static int tickNum = 0;

    // The KeyBinding declaration and registration are commonly executed here statically

    @Override
    public void onInitializeClient() {
        //KeyInputHandler.register();

        //ClientTickEvents.END_CLIENT_TICK.register(ModClient::clientTick);

        //ItemTooltipCallback.EVENT.register(ModClient::onInjectTooltip);
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient minecraftClient) {
        tickNum++;
        if (tickNum % 70 == 0) {
            Mod.responseManager.sendResponseQueue();
        }
        if (tickNum % 600 == 0) {
            Mod.responseManager.saveFile();
        }
    }



    private static void onInjectTooltip(ItemStack itemStack, Item.TooltipContext context, TooltipType tooltipType, List<Text> list) {
        NbtCompound nbt = encodeStack(itemStack, Objects.requireNonNull(context.getRegistryLookup()).getOps(NbtOps.INSTANCE));

        int flagCmdColor = 0x858dd6;

        if (nbt != null){
            var bukkitvalues = nbt.getCompound("PublicBukkitValues");
            if (bukkitvalues != null) {
                Set<String> keys = bukkitvalues.getKeys();
                if (!keys.isEmpty()) {
                    list.add(Text.empty());
                    for (String key : keys) {
                        int keyColor = 0xb785d6;
                        int valueColor = 0x6fd6f2;
                        String value = bukkitvalues.get(key).toString();
                        if ((!(value.startsWith("\"") && value.endsWith("\""))) && !(value.startsWith("'") && value.endsWith("'"))) {
                            valueColor = 0xeb4b4b;
                        }
                        Text addText = Text.literal(key.substring(10) + ": ").withColor(keyColor).append(Text.literal(value).withColor(valueColor));
                        list.add(addText);
                    }
                }
            }
            var cmd = nbt.get("CustomModelData");
            var flags = nbt.get("HideFlags");
            if (cmd != null || flags != null) {
                list.add(Text.empty());
                if (cmd != null) { list.add(Text.literal("CustomModelData: ").withColor(flagCmdColor).append(Text.literal(cmd.toString()).withColor(0xeb4b4b))); }
                if (flags != null) { list.add(Text.literal("HideFlags: ").withColor(flagCmdColor).append(Text.literal(flags.toString()).withColor(0xeb4b4b))); }
            }

        }
    }

    private static NbtCompound encodeStack(ItemStack stack, DynamicOps<NbtElement> ops) {
        DataResult<NbtElement> result = ComponentChanges.CODEC.encodeStart(ops, stack.getComponentChanges());
        result.ifError(e->{

        });
        NbtElement nbtElement = result.getOrThrow();
        // cast here, as soon as this breaks, the mod will need to update anyway
        return (NbtCompound) nbtElement;
    }


}

/// mc.getToastManager().add(new SystemToast(Type.PERIODIC_NOTIFICATION, Text.translatable("nbttooltip.copy_failed"), Text.literal(e.getMessage())));
//