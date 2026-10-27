package io.github.bridge_noob.strayarmor.mixin;

import io.github.bridge_noob.strayarmor.access.TextDisplayAccess;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the TextDisplay text in 1.21, where the getter is private. */
@Mixin(Display.TextDisplay.class)
public abstract class TextDisplayMixin implements TextDisplayAccess {

    @Override
    @Invoker("getText")
    public abstract Component strayarmor$getText();
}
