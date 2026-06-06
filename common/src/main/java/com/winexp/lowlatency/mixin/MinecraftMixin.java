package com.winexp.lowlatency.mixin;

import com.winexp.lowlatency.LowLatencyMod;
import com.winexp.lowlatency.LowLatencyScheduler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V"))
    private void beforePoll(CallbackInfo ci) {
        LowLatencyScheduler.wait(LowLatencyMod.SCHEDULER);
    }

    @Inject(method = "run", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pollEvents()V", shift = At.Shift.AFTER))
    private void afterPoll(CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.recordCpuBegin();
    }

    @Inject(method = "renderFrame", at = @At("HEAD"))
    private void beforeRender(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.recordGpuBegin();
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;flipFrame(Lcom/mojang/blaze3d/TracyFrameCapture;)V"))
    private void beforeFlip(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.recordCpuEnd();
        LowLatencyMod.SCHEDULER.statistics.updateFrameQueueBacklog();
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;flipFrame(Lcom/mojang/blaze3d/TracyFrameCapture;)V", shift = At.Shift.AFTER))
    private void afterFlip(boolean advanceGameTime, CallbackInfo ci) {
        LowLatencyMod.SCHEDULER.recordGpuEnd();
    }
}
