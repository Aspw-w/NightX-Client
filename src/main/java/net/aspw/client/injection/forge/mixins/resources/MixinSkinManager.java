package net.aspw.client.injection.forge.mixins.resources;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

/**
 * The type Mixin skin manager.
 */
@Mixin(SkinManager.class)
public class MixinSkinManager {

    @Inject(method = "loadSkinFromCache", at = @At("HEAD"))
    private void injectSkinProtect(GameProfile gameProfile, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> cir) {
    }
}