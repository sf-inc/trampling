package com.github.charlyb01.trampling.mixin.crops;

import com.github.charlyb01.trampling.Utils;
import com.github.charlyb01.trampling.config.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PlantBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.charlyb01.trampling.Trampling.STUNNED;

@Mixin(StemBlock.class)
public abstract class StemBlockMixin extends PlantBlock {
        public StemBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void setDefaultStunnedState(CallbackInfo ci) {
        this.setDefaultState(this.stateManager.getDefaultState().with(STUNNED, false));
    }

    @Inject(method = "appendProperties", at = @At("HEAD"))
    void addStunnedProperty(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(STUNNED);
    }

    @Inject(method = "randomTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"), cancellable = true)
    void noTickIfCropStunned(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (state.get(STUNNED)) {
            if (world.getRandom().nextInt(100) < ModConfig.get().cropDebuff.unstunSuccessChance) {
                world.setBlockState(pos, state.with(STUNNED, false));
            }
            ci.cancel();
        }
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (Utils.cantCancelTrample(entity ,world)) {
            if (world.getRandom().nextInt(100) < ModConfig.get().cropDebuff.blockBreakingChance
                    || entity instanceof RavagerEntity) {
                world.breakBlock(pos, true, entity);
            } else {
                Utils.tryStunAround(world, pos);
            }
        }

        super.onEntityCollision(state, world, pos, entity);
    }
}
