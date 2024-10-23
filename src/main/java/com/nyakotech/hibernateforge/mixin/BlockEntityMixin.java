package com.nyakotech.hibernateforge.mixin;

import com.nyakotech.hibernateforge.HibernateForge;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockEntity.class)
public class BlockEntityMixin {
    @Redirect(method = "tick", at = @At(value = "HEAD"), require = 1)
    private void redirectTick(BlockEntity blockEntity) {
        if (HibernateForge.isHibernating()) {
            return; // Skip tick method
        }
    }
}
