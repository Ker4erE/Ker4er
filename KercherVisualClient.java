package com.kercher;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class KercherVisualClient implements ClientModInitializer {
    public static boolean enabled = true;
    public static Theme theme = Theme.KERCHER;

    private static KeyBinding toggleKey;
    private static KeyBinding themeKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kercher.toggle", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.category.kercher"));
        themeKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kercher.theme", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_ALT, "key.category.kercher"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.wasPressed()) enabled = !enabled;
            while (themeKey.wasPressed()) theme = theme.next();
        });

        // Хит-кольцо: кольцо от прицела при ударе по живому существу
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient && enabled && entity instanceof LivingEntity) {
                KercherHud.addHit();
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register(KercherHud::render);
    }
}
