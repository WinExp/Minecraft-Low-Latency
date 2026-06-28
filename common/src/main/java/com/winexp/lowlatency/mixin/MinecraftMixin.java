package com.winexp.lowlatency.mixin;

import com.winexp.lowlatency.LowLatencyMod;
import com.winexp.lowlatency.LowLatencyScheduler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(Lcom/mojang/blaze3d/systems/GpuDevice;)V", shift = At.Shift.AFTER))
    private void afterRendererInit(GameConfig gameConfig, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER = new LowLatencyScheduler();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V"))
    private void beforePoll(CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.beforePoll();
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V", shift = At.Shift.AFTER))
    private void afterPoll(CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.afterPoll();
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/Profiler;get()Lnet/minecraft/util/profiling/ProfilerFiller;"))
    private void beforeRender(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.beforeRender();
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/CommandEncoder;submit()V"))
    private void beforeSubmit(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.beforeSubmit();
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;endFrame()V", shift = At.Shift.AFTER))
    private void afterSubmit(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.afterSubmit();
    }
}
